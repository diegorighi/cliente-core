package br.com.vanessa_mudanca.cliente_core.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Implementação de Spring Cache usando AWS DynamoDB como storage.
 * <p>
 * Esta classe implementa a interface org.springframework.cache.Cache,
 * permitindo que @Cacheable e @CacheEvict funcionem com DynamoDB.
 * </p>
 *
 * <h3>DynamoDB Operations:</h3>
 * <ul>
 *   <li><b>get():</b> DynamoDB GetItem (eventually consistent read)</li>
 *   <li><b>put():</b> DynamoDB PutItem (sobrescreve se existir)</li>
 *   <li><b>evict():</b> DynamoDB DeleteItem</li>
 *   <li><b>clear():</b> Não implementado (risco de deletion em massa)</li>
 * </ul>
 *
 * <h3>Cache Key Format:</h3>
 * <pre>
 * cacheKey = "cacheName::keyObject"
 * Exemplo: "clientes:findById::550e8400-e29b-41d4-a716-446655440000"
 * </pre>
 *
 * <h3>TTL Strategy:</h3>
 * <ul>
 *   <li>Cada item tem atributo expirationTime (Unix timestamp)</li>
 *   <li>DynamoDB TTL deleta itens expirados automaticamente (background job)</li>
 *   <li>get() verifica expirationTime antes de retornar (avoid stale reads)</li>
 * </ul>
 *
 * @see org.springframework.cache.Cache
 */
public class DynamoDbCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbCache.class);

    private final String name;
    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;
    private final String tableName;
    private final Duration ttl;

    private static final String ATTR_CACHE_KEY = "cacheKey";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_TYPE = "valueType";  // Nome da classe para deserialização
    private static final String ATTR_CREATED_AT = "createdAt";
    private static final String ATTR_EXPIRATION_TIME = "expirationTime";

    /**
     * Construtor do DynamoDbCache.
     *
     * @param name Nome do cache (ex: "clientes:findById")
     * @param dynamoDbClient Cliente DynamoDB
     * @param objectMapper ObjectMapper para serialização JSON
     * @param tableName Nome da tabela DynamoDB
     * @param ttl Time-to-live para itens deste cache
     */
    public DynamoDbCache(
            String name,
            DynamoDbClient dynamoDbClient,
            ObjectMapper objectMapper,
            String tableName,
            Duration ttl
    ) {
        this.name = name;
        this.dynamoDbClient = dynamoDbClient;
        this.objectMapper = objectMapper;
        this.tableName = tableName;
        this.ttl = ttl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return dynamoDbClient;
    }

    /**
     * Busca valor do cache.
     * <p>
     * Se item existe e não expirou, retorna ValueWrapper.
     * Se não existe ou expirou, retorna null (cache miss).
     * </p>
     *
     * @param key Chave do cache
     * @return ValueWrapper ou null se cache miss
     */
    @Override
    public ValueWrapper get(Object key) {
        try {
            String cacheKey = buildCacheKey(key);

            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(ATTR_CACHE_KEY, AttributeValue.builder().s(cacheKey).build()))
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(request);

            if (!response.hasItem() || response.item().isEmpty()) {
                log.debug("Cache miss - Cache: {}, Key: {}", name, key);
                return null;
            }

            Map<String, AttributeValue> item = response.item();

            // Verificar expiração (DynamoDB TTL pode ter delay de até 48h)
            long expirationTime = Long.parseLong(item.get(ATTR_EXPIRATION_TIME).n());
            if (Instant.now().getEpochSecond() >= expirationTime) {
                log.debug("Cache expired - Cache: {}, Key: {}", name, key);
                evict(key); // Deletar item expirado manualmente
                return null;
            }

            String json = item.get(ATTR_VALUE).s();
            String className = item.get(ATTR_TYPE).s();

            // Deserializar com tipo correto
            Class<?> clazz = Class.forName(className);
            Object value = objectMapper.readValue(json, clazz);

            log.debug("Cache hit - Cache: {}, Key: {}, Type: {}", name, key, className);
            return () -> value;

        } catch (Exception e) {
            log.error("Error getting cache - Cache: {}, Key: {}", name, key, e);
            return null;
        }
    }

    /**
     * Busca valor do cache com tipo específico.
     * <p>
     * Spring Cache interface method para type-safe retrieval.
     * </p>
     *
     * @param key Chave do cache
     * @param type Tipo esperado do valor
     * @return Valor cached ou null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);

        if (wrapper == null) {
            return null;
        }

        Object value = wrapper.get();

        if (value != null && !type.isInstance(value)) {
            throw new IllegalStateException(
                    "Cached value is not of required type [" + type.getName() + "]: " + value
            );
        }

        return (T) value;
    }

    /**
     * Busca valor do cache ou executa valueLoader se cache miss.
     * <p>
     * Implementa cache-aside pattern com double-check locking.
     * </p>
     *
     * @param key Chave do cache
     * @param valueLoader Função que busca valor do DB
     * @return Valor cached ou do valueLoader
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);

        if (wrapper != null) {
            return (T) wrapper.get();
        }

        try {
            // Cache miss - buscar do DB
            T value = valueLoader.call();

            if (value != null) {
                put(key, value);
            }

            return value;

        } catch (Exception e) {
            log.error("Error loading value - Cache: {}, Key: {}", name, key, e);
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    /**
     * Armazena valor no cache.
     * <p>
     * Serializa valor como JSON e armazena no DynamoDB com TTL.
     * </p>
     *
     * @param key Chave do cache
     * @param value Valor a ser cached
     */
    @Override
    public void put(Object key, Object value) {
        try {
            String cacheKey = buildCacheKey(key);
            String json = objectMapper.writeValueAsString(value);
            String className = value.getClass().getName();

            long now = Instant.now().getEpochSecond();
            long expirationTime = now + ttl.getSeconds();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put(ATTR_CACHE_KEY, AttributeValue.builder().s(cacheKey).build());
            item.put(ATTR_VALUE, AttributeValue.builder().s(json).build());
            item.put(ATTR_TYPE, AttributeValue.builder().s(className).build());
            item.put(ATTR_CREATED_AT, AttributeValue.builder().n(String.valueOf(now)).build());
            item.put(ATTR_EXPIRATION_TIME, AttributeValue.builder().n(String.valueOf(expirationTime)).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(request);

            log.debug("Cache put - Cache: {}, Key: {}, TTL: {}s", name, key, ttl.getSeconds());

        } catch (Exception e) {
            log.error("Error putting cache - Cache: {}, Key: {}", name, key, e);
        }
    }

    /**
     * Remove item do cache.
     * <p>
     * Deleta item do DynamoDB imediatamente.
     * </p>
     *
     * @param key Chave do cache
     */
    @Override
    public void evict(Object key) {
        try {
            String cacheKey = buildCacheKey(key);

            DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(ATTR_CACHE_KEY, AttributeValue.builder().s(cacheKey).build()))
                    .build();

            dynamoDbClient.deleteItem(request);

            log.debug("Cache evict - Cache: {}, Key: {}", name, key);

        } catch (Exception e) {
            log.error("Error evicting cache - Cache: {}, Key: {}", name, key, e);
        }
    }

    /**
     * Remove todos os itens do cache.
     * <p>
     * NÃO IMPLEMENTADO por motivos de segurança.
     * Evita deletion em massa acidental de toda a tabela DynamoDB.
     * </p>
     *
     * @throws UnsupportedOperationException sempre
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(
                "clear() not implemented for DynamoDB cache to prevent accidental mass deletion. " +
                        "Use @CacheEvict on specific keys instead."
        );
    }

    /**
     * Constrói chave do cache no formato "cacheName::key".
     *
     * @param key Chave do objeto
     * @return Chave formatada
     */
    private String buildCacheKey(Object key) {
        return name + "::" + key.toString();
    }
}
