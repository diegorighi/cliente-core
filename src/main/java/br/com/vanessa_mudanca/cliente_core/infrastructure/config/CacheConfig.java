package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache in-memory usando Caffeine.
 * <p>
 * Caffeine oferece:
 * <ul>
 *   <li><b>Performance:</b> <1ms de latência (vs 10-20ms DynamoDB)</li>
 *   <li><b>Simplicidade:</b> zero dependências de infra</li>
 *   <li><b>Adequado para MVP:</b> até 10.000 clientes (~100 MB RAM)</li>
 * </ul>
 * </p>
 *
 * <h3>Limitações:</h3>
 * <ul>
 *   <li>Cache perdido em restart (aceitável para dados de referência)</li>
 *   <li>Limitado à memória JVM (max 512 MB recomendado)</li>
 *   <li>Não compartilhado entre instâncias (usar Redis quando escalar)</li>
 * </ul>
 *
 * <h3>Quando migrar para Redis:</h3>
 * <ul>
 *   <li>Múltiplas instâncias da aplicação (horizontal scaling)</li>
 *   <li>>50.000 clientes ativos</li>
 *   <li>Necessidade de cache persistente (sobreviver a restarts)</li>
 * </ul>
 *
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.cache.annotation.CachePut
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura Caffeine como backend de cache do Spring.
     * <p>
     * <b>Configurações:</b>
     * <ul>
     *   <li>TTL: 5 minutos (dados de cliente mudam raramente)</li>
     *   <li>Max Size: 10.000 entradas (~100 MB de RAM)</li>
     *   <li>Stats: habilitado para monitoramento via Actuator</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Métricas disponíveis via Actuator:</b>
     * <pre>
     * GET /actuator/caches
     * GET /actuator/metrics/cache.gets
     * GET /actuator/metrics/cache.puts
     * GET /actuator/metrics/cache.evictions
     * </pre>
     * </p>
     *
     * @return CacheManager configurado com Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)  // TTL 5 minutos
            .maximumSize(10_000)                     // Max 10k entradas (~100 MB)
            .recordStats());                         // Métricas via Actuator

        return cacheManager;
    }
}
