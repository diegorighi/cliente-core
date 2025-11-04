package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração de cache backend-agnostic para cliente-core.
 * <p>
 * Esta configuração utiliza Spring Cache abstraction (@Cacheable, @CacheEvict)
 * permitindo trocar o backend de cache (DynamoDB → Redis) sem alterar código de negócio.
 * </p>
 *
 * <h3>Backends Suportados:</h3>
 * <ul>
 *   <li><b>DynamoDB (MVP):</b> AWS Free Tier (25 GB + 25 WCU/RCU = $0/month)</li>
 *   <li><b>Redis (Produção):</b> ElastiCache (post Month 3-6, ~$12-25/month)</li>
 * </ul>
 *
 * <h3>Migração DynamoDB → Redis:</h3>
 * <ol>
 *   <li>Alterar application.yml: cache.backend=redis</li>
 *   <li>Adicionar spring-boot-starter-data-redis ao pom.xml</li>
 *   <li>Deploy sem downtime (cache warming automático)</li>
 * </ol>
 *
 * <h3>Cache Strategy:</h3>
 * <ul>
 *   <li><b>clientes:findById:</b> Cache de consultas por UUID (5 minutos)</li>
 *   <li><b>clientes:findByCpf:</b> Cache de consultas ClientePF por CPF (5 minutos)</li>
 *   <li><b>clientes:findByCnpj:</b> Cache de consultas ClientePJ por CNPJ (5 minutos)</li>
 *   <li><b>clientes:findByEmail:</b> Cache de consultas por email (3 minutos)</li>
 *   <li><b>clientes:list:</b> Cache de listagens paginadas (1 minuto)</li>
 * </ul>
 *
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CacheEvict
 * @see DynamoDbCacheConfig
 * @see RedisCacheConfig
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Documentação para desenvolvedores:
     *
     * Para trocar de DynamoDB para Redis no futuro:
     *
     * 1. Adicionar ao pom.xml:
     *    <dependency>
     *        <groupId>org.springframework.boot</groupId>
     *        <artifactId>spring-boot-starter-data-redis</artifactId>
     *    </dependency>
     *
     * 2. Criar RedisCacheConfig com @ConditionalOnProperty("cache.backend=redis")
     *
     * 3. Alterar application.yml:
     *    cache:
     *      backend: redis  # era: dynamodb
     *    spring:
     *      data:
     *        redis:
     *          host: elasticache-endpoint.amazonaws.com
     *          port: 6379
     *
     * 4. Deploy: aplicação inicia com Redis, cache warming automático
     *
     * 5. Validar: logs devem mostrar "Using Redis cache backend"
     *
     * ZERO alteração no código de negócio (services com @Cacheable)!
     */
}
