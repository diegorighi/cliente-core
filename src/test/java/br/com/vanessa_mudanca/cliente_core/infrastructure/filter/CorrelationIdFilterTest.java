package br.com.vanessa_mudanca.cliente_core.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes para o CorrelationIdFilter.
 *
 * Verifica que:
 * 1. Correlation ID é gerado quando não existe no header
 * 2. Correlation ID existente no header é reutilizado
 * 3. Correlation ID é adicionado ao MDC
 * 4. Correlation ID é adicionado ao response header
 * 5. MDC é limpo após o processamento (evita memory leaks)
 */
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private FilterChain filterChain;

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Limpa MDC antes de cada teste
        MDC.clear();
    }

    @Test
    void deveGerarNovoCorrelationIdQuandoNaoExistirNoHeader() throws IOException, ServletException {
        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        String correlationIdHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(correlationIdHeader).isNotNull();
        assertThat(correlationIdHeader).hasSize(36); // UUID tem 36 caracteres com hífens

        // Verifica que o filter chain foi chamado
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void deveReutilizarCorrelationIdExistenteNoHeader() throws IOException, ServletException {
        // Arrange
        String correlationIdExistente = "12345678-1234-1234-1234-123456789012";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationIdExistente);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        String correlationIdHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(correlationIdHeader).isEqualTo(correlationIdExistente);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void deveAdicionarCorrelationIdAoMDCDuranteProcessamento() throws IOException, ServletException {
        // Arrange
        final String[] mdcValue = new String[1]; // Array para capturar valor dentro do lambda

        doAnswer(invocation -> {
            // Captura o valor do MDC durante a execução do filter chain
            mdcValue[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
            return null;
        }).when(filterChain).doFilter(request, response);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertThat(mdcValue[0]).isNotNull();
        assertThat(mdcValue[0]).hasSize(36);
    }

    @Test
    void deveLimparMDCDepoisDoProcessamento() throws IOException, ServletException {
        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - MDC deve estar vazio após o processamento
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void deveLimparMDCMesmoQuandoOcorrerExcecao() throws IOException, ServletException {
        // Arrange
        try {
            doThrow(new RuntimeException("Erro simulado"))
                    .when(filterChain).doFilter(request, response);

            // Act & Assert
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                try {
                    filter.doFilter(request, response, filterChain);
                } catch (IOException | ServletException e) {
                    throw new RuntimeException(e);
                }
            });

        } finally {
            // Assert - MDC deve estar vazio mesmo com exceção
            assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
        }
    }

    @Test
    void deveAdicionarCorrelationIdAoResponseHeader() throws IOException, ServletException {
        // Arrange
        String correlationIdEsperado = "test-correlation-id-12345";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationIdEsperado);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        String correlationIdHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(correlationIdHeader).isEqualTo(correlationIdEsperado);
    }

    @Test
    void deveIgnorarCorrelationIdVazioNoHeader() throws IOException, ServletException {
        // Arrange
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   "); // Header vazio (apenas espaços)

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - Deve gerar novo UUID, não usar o valor vazio
        String correlationIdHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(correlationIdHeader).isNotNull();
        assertThat(correlationIdHeader).hasSize(36);
        assertThat(correlationIdHeader).doesNotContain(" "); // Não deve conter espaços
    }
}
