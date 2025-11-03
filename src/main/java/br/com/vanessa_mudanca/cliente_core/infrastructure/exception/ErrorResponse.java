package br.com.vanessa_mudanca.cliente_core.infrastructure.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Record para padronização de respostas de erro da API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, List.of());
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
