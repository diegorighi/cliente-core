package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Configuração de métricas para Virtual Threads (Java 21).
 *
 * <p>Esta classe registra métricas customizadas no Micrometer para monitorar
 * o comportamento das Virtual Threads habilitadas no Spring Boot 3.5.7+.
 *
 * <p><b>Métricas disponíveis:</b>
 * <ul>
 *   <li>{@code jvm.threads.platform} - Total de Platform Threads (carrier threads)</li>
 *   <li>{@code jvm.threads.virtual} - Total de Virtual Threads ativas</li>
 *   <li>{@code jvm.threads.daemon} - Total de daemon threads</li>
 *   <li>{@code jvm.threads.peak} - Pico histórico de threads</li>
 * </ul>
 *
 * <p><b>Como consultar (Actuator):</b>
 * <pre>
 * curl http://localhost:8081/api/clientes/actuator/metrics/jvm.threads.virtual
 * </pre>
 *
 * <p><b>Como consultar (Prometheus):</b>
 * <pre>
 * # Grafana query
 * jvm_threads_virtual{application="cliente-core"}
 * </pre>
 *
 * @see <a href="https://openjdk.org/jeps/444">JEP 444: Virtual Threads</a>
 * @see <a href="https://docs.spring.io/spring-boot/3.2.0/reference/features/io.html#features.io.virtual-threads">Spring Boot Virtual Threads</a>
 * @since 0.1.0
 */
@Slf4j
@Configuration
public class VirtualThreadsConfig {

    /**
     * Registra métricas customizadas de Virtual Threads no Micrometer.
     *
     * <p>Usa {@link ThreadMXBean} para coletar estatísticas de threads em tempo real.
     * As métricas são expostas automaticamente nos endpoints {@code /actuator/metrics}
     * e {@code /actuator/prometheus}.
     *
     * @return MeterBinder que registra as métricas de Virtual Threads
     */
    @Bean
    public MeterBinder virtualThreadMetrics() {
        log.info("Registrando métricas de Virtual Threads (Java 21)");

        return (MeterRegistry registry) -> {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            // Total de Platform Threads (carrier threads para Virtual Threads)
            Gauge.builder("jvm.threads.platform", threadBean, ThreadMXBean::getThreadCount)
                    .description("Total de platform threads (incluindo carrier threads)")
                    .tag("type", "platform")
                    .register(registry);

            // Total de Virtual Threads (estimativa via thread count total)
            // Nota: Java 21 não expõe diretamente VThread count via MXBean
            // Em produção, considere usar JFR (Java Flight Recorder) para métricas precisas
            Gauge.builder("jvm.threads.virtual", threadBean, bean -> {
                        // Heurística: threads além do pool padrão são provavelmente virtual threads
                        long totalThreads = bean.getThreadCount();
                        long platformThreads = 50; // Estimativa: carrier threads + system threads
                        return Math.max(0, totalThreads - platformThreads);
                    })
                    .description("Estimativa de virtual threads ativas (threads totais - platform threads)")
                    .tag("type", "virtual")
                    .register(registry);

            // Total de daemon threads
            Gauge.builder("jvm.threads.daemon", threadBean, ThreadMXBean::getDaemonThreadCount)
                    .description("Total de daemon threads")
                    .tag("type", "daemon")
                    .register(registry);

            // Pico histórico de threads
            Gauge.builder("jvm.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount)
                    .description("Pico histórico de threads desde início da JVM")
                    .register(registry);

            log.info("Métricas de Virtual Threads registradas com sucesso");
        };
    }
}
