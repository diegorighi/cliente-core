package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import br.com.vanessa_mudanca.cliente_core.infrastructure.cache.DynamoDbCacheManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do DynamoDB como backend de cache (Free Tier MVP).
 * <p>
 * DynamoDB oferece Free Tier permanente com 25 GB storage + 25 WCU/RCU = $0/month.
 * Ideal para MVP até atingir escala que justifique ElastiCache Redis (~$12-25/month).
 * </p>
 *
 * <h3>Arquitetura do Cache:</h3>
 * <pre>
 * Tabela DynamoDB: cliente-core-cache
 * Partition Key: cacheKey (String) - formato: "cacheName::key" (ex: "clientes:findById::uuid")
 * Sort Key: N/A (tabela simples, não composite)
 * TTL Attribute: expirationTime (Number) - Unix timestamp em segundos
 * Attributes: value (String - JSON serializado), createdAt (Number)
 * </pre>
 *
 * <h3>Free Tier Limits:</h3>
 * <ul>
 *   <li>25 GB storage (suficiente para ~250k clientes cached)</li>
 *   <li>25 WCU = 25 escritas/sec (cache misses)</li>
 *   <li>25 RCU = 100 leituras/sec de 4 KB (cache hits)</li>
 *   <li>TTL deletion: gratuito (cleanup automático)</li>
 * </ul>
 *
 * <h3>Performance Esperada:</h3>
 * <ul>
 *   <li>Cache hit: ~10-20ms (DynamoDB query)</li>
 *   <li>Cache miss: ~150-200ms (PostgreSQL query + DynamoDB write)</li>
 *   <li>vs Redis: ~5-10ms (cache hit) - 2x mais rápido, mas custa $12/month</li>
 * </ul>
 *
 * <h3>Ambientes:</h3>
 * <ul>
 *   <li><b>Dev:</b> DynamoDB Local via Docker Compose (localhost:8000)</li>
 *   <li><b>Prod:</b> AWS DynamoDB (região configurada via application.yml)</li>
 * </ul>
 *
 * @see DynamoDbCacheManager
 * @see org.springframework.cache.annotation.Cacheable
 */
@Configuration
@ConditionalOnProperty(name = "cache.backend", havingValue = "dynamodb", matchIfMissing = true)
public class DynamoDbCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbCacheConfig.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoDbEndpoint;

    @Value("${cache.dynamodb.table-name:cliente-core-cache}")
    private String tableName;

    /**
     * TTL padrão para cache (5 minutos).
     * <p>
     * Adequado para dados de clientes que não mudam com frequência,
     * mas precisam estar relativamente atualizados.
     * </p>
     */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    /**
     * TTL para cache de listagens (1 minuto).
     * <p>
     * Listagens são mais voláteis e podem conter dados de múltiplos clientes.
     * </p>
     */
    private static final Duration LIST_TTL = Duration.ofMinutes(1);

    /**
     * Cria cliente DynamoDB configurado para ambiente (local ou AWS).
     *
     * @return DynamoDbClient configurado
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Dev/Test: usar DynamoDB Local (Docker Compose)
        if (dynamoDbEndpoint != null && !dynamoDbEndpoint.isEmpty()) {
            log.info("Using DynamoDB Local endpoint: {}", dynamoDbEndpoint);
            builder.endpointOverride(URI.create(dynamoDbEndpoint));
        } else {
            log.info("Using AWS DynamoDB in region: {}", awsRegion);
        }

        return builder.build();
    }

    /**
     * Configura ObjectMapper para serialização JSON do cache.
     * <p>
     * Suporta LocalDate, LocalDateTime e tipos polimórficos (Cliente → ClientePF/ClientePJ).
     * </p>
     *
     * @return ObjectMapper configurado
     */
    @Bean
    public ObjectMapper cacheObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    /**
     * Configura CacheManager usando DynamoDB como backend.
     * <p>
     * Define TTLs específicos por tipo de cache:
     * <ul>
     *   <li>clientes:findById - 5 minutos (hot cache)</li>
     *   <li>clientes:findByCpf - 5 minutos</li>
     *   <li>clientes:findByCnpj - 5 minutos</li>
     *   <li>clientes:findByEmail - 3 minutos (mais volátil)</li>
     *   <li>clientes:list - 1 minuto (muito volátil)</li>
     *   <li>clientes:count - 30 segundos</li>
     * </ul>
     * </p>
     *
     * @param dynamoDbClient Cliente DynamoDB
     * @param objectMapper ObjectMapper para serialização
     * @return CacheManager configurado
     */
    @Bean
    public CacheManager cacheManager(DynamoDbClient dynamoDbClient, ObjectMapper objectMapper) {
        Map<String, Duration> ttlConfig = new HashMap<>();

        // Cache de consultas por UUID (5 minutos - hot cache)
        ttlConfig.put("clientes:findById", DEFAULT_TTL);

        // Cache de consultas por CPF (5 minutos)
        ttlConfig.put("clientes:findByCpf", DEFAULT_TTL);

        // Cache de consultas por CNPJ (5 minutos)
        ttlConfig.put("clientes:findByCnpj", DEFAULT_TTL);

        // Cache de consultas por email (3 minutos - mais volátil)
        ttlConfig.put("clientes:findByEmail", Duration.ofMinutes(3));

        // Cache de listagens paginadas (1 minuto - muito volátil)
        ttlConfig.put("clientes:list", LIST_TTL);

        // Cache de contagem total (30 segundos - muito volátil)
        ttlConfig.put("clientes:count", Duration.ofSeconds(30));

        log.info("Initializing DynamoDB cache backend - Table: {}, Default TTL: {} minutes",
                tableName, DEFAULT_TTL.toMinutes());

        return new DynamoDbCacheManager(
                dynamoDbClient,
                objectMapper,
                tableName,
                DEFAULT_TTL,
                ttlConfig
        );
    }
}
