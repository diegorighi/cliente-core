package br.com.vanessa_mudanca.cliente_core.infrastructure.exception;

import br.com.vanessa_mudanca.cliente_core.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Testes de tratamento de exceções")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/clientes/test");
    }

    @Test
    @DisplayName("Deve tratar CpfJaCadastradoException com status 409 CONFLICT")
    void deveTratarCpfJaCadastrado() {
        // Arrange
        CpfJaCadastradoException exception = new CpfJaCadastradoException("12345678910");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCpfJaCadastrado(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).contains("CPF");
        assertThat(response.getBody().path()).isEqualTo("/api/clientes/test");
    }

    @Test
    @DisplayName("Deve tratar CpfInvalidoException com status 400 BAD_REQUEST")
    void deveTratarCpfInvalido() {
        // Arrange
        CpfInvalidoException exception = new CpfInvalidoException("123");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCpfInvalido(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("Deve tratar CnpjJaCadastradoException com status 409 CONFLICT")
    void deveTratarCnpjJaCadastrado() {
        // Arrange
        CnpjJaCadastradoException exception = new CnpjJaCadastradoException("12345678000190");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCnpjJaCadastrado(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).contains("CNPJ");
    }

    @Test
    @DisplayName("Deve tratar CnpjInvalidoException com status 400 BAD_REQUEST")
    void deveTratarCnpjInvalido() {
        // Arrange
        CnpjInvalidoException exception = new CnpjInvalidoException("123");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCnpjInvalido(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Deve tratar ClienteIndicadorNaoEncontradoException com status 404 NOT_FOUND")
    void deveTratarClienteIndicadorNaoEncontrado() {
        // Arrange
        java.util.UUID uuid = java.util.UUID.randomUUID();
        ClienteIndicadorNaoEncontradoException exception = new ClienteIndicadorNaoEncontradoException(uuid);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleClienteIndicadorNaoEncontrado(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
    }

    @Test
    @DisplayName("Deve tratar ClienteNaoEncontradoException com status 404 NOT_FOUND")
    void deveTratarClienteNaoEncontrado() {
        // Arrange
        java.util.UUID uuid = java.util.UUID.randomUUID();
        ClienteNaoEncontradoException exception = new ClienteNaoEncontradoException(uuid);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleClienteNaoEncontrado(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com lista de erros de validação")
    void deveTratarMethodArgumentNotValid() {
        // Arrange
        FieldError fieldError1 = new FieldError("clientePF", "cpf", "CPF é obrigatório");
        FieldError fieldError2 = new FieldError("clientePF", "nomeCompleto", "Nome completo é obrigatório");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Failed");
        assertThat(response.getBody().details()).hasSize(2);
        assertThat(response.getBody().details()).contains("cpf: CPF é obrigatório");
        assertThat(response.getBody().details()).contains("nomeCompleto: Nome completo é obrigatório");
    }

    @Test
    @DisplayName("Deve tratar BusinessException genérica com status 400 BAD_REQUEST")
    void deveTratarBusinessException() {
        // Arrange
        BusinessException exception = new BusinessException("Erro de negócio genérico");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Business Error");
        assertThat(response.getBody().message()).isEqualTo("Erro de negócio genérico");
    }

    @Test
    @DisplayName("Deve tratar Exception genérica com status 500 INTERNAL_SERVER_ERROR")
    void deveTratarExceptionGenerica() {
        // Arrange
        Exception exception = new RuntimeException("Erro inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).contains("erro inesperado");
    }
}
