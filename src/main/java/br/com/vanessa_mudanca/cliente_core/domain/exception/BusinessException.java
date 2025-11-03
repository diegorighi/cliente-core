package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção base para erros de negócio.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
