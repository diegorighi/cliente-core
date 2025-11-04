package br.com.vanessa_mudanca.cliente_core.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação de CacheManager usando AWS DynamoDB como backend.
 * <p>
 * Esta implementação permite usar Spring Cache abstraction (@Cacheable, @CacheEvict)
 * com DynamoDB, facilitando migração futura para Redis sem alterar código de negócio.
 * </p>
 *
 * <h3>Design Pattern:</h3>
 * <ul>
 *   <li><b>Strategy Pattern:</b> Implementa CacheManager (interface do Spring)</li>
 *   <li><b>Factory Pattern:</b> Cria DynamoDbCache instances sob demanda</li>
 *   <li><b>Cache-Aside:</b> Lazy loading com TTL-based expiration</li>
 * </ul>
 *
 * <h3>DynamoDB Table Schema:</h3>
 * <pre>
 * Table: cliente-core-cache
 * Partition Key: cacheKey (String) - formato: "cacheName::key"
 * TTL Attribute: expirationTime (Number) - Unix timestamp
 * Attributes: value (String - JSON), createdAt (Number)
 * </pre>
 *
 * <h3>Performance:</h3>
 * <ul>
 *   <li>Cache hit: ~10-20ms (DynamoDB GetItem)</li>
 *   <li>Cache miss: ~150-200ms (DB query + DynamoDB PutItem)</li>
 *   <li>Eviction: ~5ms (DynamoDB DeleteItem)</li>
 * </ul>
 *
 * @see org.springframework.cache.CacheManager
 * @see DynamoDbCache
 */
public class DynamoDbCacheManager implements CacheManager {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;
    private final String tableName;
    private final Duration defaultTtl;
    private final Map<String, Duration> ttlConfig;
    private final Map<String, DynamoDbCache> cacheMap = new ConcurrentHashMap<>();

    /**
     * Construtor do DynamoDbCacheManager.
     *
     * @param dynamoDbClient Cliente DynamoDB configurado
     * @param objectMapper ObjectMapper para serialização JSON
     * @param tableName Nome da tabela DynamoDB
     * @param defaultTtl TTL padrão para caches não configurados
     * @param ttlConfig Mapa de TTLs específicos por cache name
     */
    public DynamoDbCacheManager(
            DynamoDbClient dynamoDbClient,
            ObjectMapper objectMapper,
            String tableName,
            Duration defaultTtl,
            Map<String, Duration> ttlConfig
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.objectMapper = objectMapper;
        this.tableName = tableName;
        this.defaultTtl = defaultTtl;
        this.ttlConfig = ttlConfig != null ? ttlConfig : Map.of();
    }

    /**
     * Obtém ou cria um cache pelo nome.
     * <p>
     * Caches são criados sob demanda (lazy initialization).
     * Cada cache usa o TTL configurado em ttlConfig ou defaultTtl.
     * </p>
     *
     * @param name Nome do cache (ex: "clientes:findById")
     * @return Cache instance ou null se não existir
     */
    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, cacheName -> {
            Duration ttl = ttlConfig.getOrDefault(cacheName, defaultTtl);
            return new DynamoDbCache(
                    cacheName,
                    dynamoDbClient,
                    objectMapper,
                    tableName,
                    ttl
            );
        });
    }

    /**
     * Retorna nomes de todos os caches disponíveis.
     * <p>
     * Como DynamoDB cria caches dinamicamente, retornamos apenas
     * os caches que já foram solicitados via getCache().
     * </p>
     *
     * @return Collection de nomes de caches
     */
    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }
}
