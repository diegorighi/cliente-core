package br.com.vanessa_mudanca.cliente_core.infrastructure.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para logar tempo de execução de métodos.
 *
 * Quando aplicada a um método, registra automaticamente:
 * - Início da execução com parâmetros (mascarados se sensíveis)
 * - Fim da execução com tempo total
 * - Correlation ID para rastreamento distribuído
 *
 * Exemplo de uso:
 * <pre>
 * {@literal @}LogExecutionTime
 * public ClientePFResponse criar(CreateClientePFRequest request) {
 *     // implementação
 * }
 * </pre>
 *
 * Log gerado:
 * <pre>
 * INFO  [correlationId=abc-123] Iniciando CreateClientePFService.criar - CPF: ***.***.789-10
 * INFO  [correlationId=abc-123] Finalizando CreateClientePFService.criar - Duração: 145ms
 * </pre>
 *
 * @see ExecutionTimeLoggingAspect
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {

    /**
     * Camada da aplicação (CONTROLLER, SERVICE, REPOSITORY).
     * Usado para agrupar métricas e logs.
     */
    Layer layer() default Layer.SERVICE;

    /**
     * Camadas da arquitetura hexagonal.
     */
    enum Layer {
        CONTROLLER,
        SERVICE,
        REPOSITORY
    }
}
