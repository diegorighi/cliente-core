package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import br.com.vanessa_mudanca.cliente_core.infrastructure.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuração do RestTemplate com propagação automática do Correlation ID.
 *
 * Este interceptor adiciona automaticamente o header X-Correlation-ID
 * em TODAS as requisições HTTP para outros microserviços.
 *
 * Uso:
 * <pre>
 * @Autowired
 * private RestTemplate restTemplate;
 *
 * // O header X-Correlation-ID será adicionado automaticamente!
 * VendaResponse venda = restTemplate.getForObject(
 *     "http://venda-core/v1/vendas/{id}",
 *     VendaResponse.class,
 *     vendaId
 * );
 * </pre>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate configurado para propagar Correlation ID automaticamente.
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Adiciona interceptor para incluir Correlation ID em todas as chamadas
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new CorrelationIdPropagationInterceptor());
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

    /**
     * Interceptor que adiciona X-Correlation-ID em todas as requisições HTTP.
     *
     * Busca o Correlation ID atual do MDC (que foi adicionado pelo CorrelationIdFilter)
     * e propaga para o próximo microserviço.
     */
    private static class CorrelationIdPropagationInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            // Busca o Correlation ID do MDC (adicionado pelo CorrelationIdFilter)
            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

            // Adiciona ao header da requisição para o próximo MS
            if (correlationId != null) {
                request.getHeaders().add(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
            }

            // Continua com a requisição
            return execution.execute(request, body);
        }
    }
}
