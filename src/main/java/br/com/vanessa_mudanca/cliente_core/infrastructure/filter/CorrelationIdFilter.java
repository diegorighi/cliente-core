package br.com.vanessa_mudanca.cliente_core.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter que adiciona um Correlation ID a cada requisição HTTP.
 *
 * O Correlation ID permite rastrear uma requisição através de múltiplos microserviços
 * e logs distribuídos (CloudWatch, Datadog, etc.).
 *
 * Funcionamento:
 * 1. Verifica se o header "X-Correlation-ID" já existe (vindo de outro MS ou API Gateway)
 * 2. Se não existir, gera um novo UUID
 * 3. Adiciona ao MDC (Mapped Diagnostic Context) do Logback
 * 4. Adiciona ao response header para propagação
 * 5. Remove do MDC após o processamento (evita memory leaks em thread pools)
 *
 * @see org.slf4j.MDC
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    /**
     * Nome do header HTTP para o Correlation ID.
     * Padrão: X-Correlation-ID (usado pela maioria dos API Gateways)
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * Nome da chave no MDC do Logback.
     * Este valor será incluído automaticamente em todos os logs estruturados.
     */
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 1. Busca o Correlation ID do header (se existir)
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

            // 2. Se não existir, gera um novo UUID
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // 3. Adiciona ao MDC (disponível para todos os logs)
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // 4. Adiciona ao response header (propagação para outros serviços)
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            // 5. Continua o processamento da requisição
            chain.doFilter(request, response);

        } finally {
            // 6. Remove do MDC após o processamento (CRÍTICO para evitar memory leaks)
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Nenhuma inicialização necessária
    }

    @Override
    public void destroy() {
        // Nenhuma destruição necessária
    }
}
