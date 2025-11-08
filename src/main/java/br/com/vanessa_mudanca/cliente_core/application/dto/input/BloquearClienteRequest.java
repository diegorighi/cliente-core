package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Record para bloqueio de Cliente (PF ou PJ).
 * Utilizado como input na API REST para inativar/bloquear clientes.
 */
public record BloquearClienteRequest(

        @NotBlank(message = "Motivo do bloqueio é obrigatório")
        @Size(min = 10, max = 500, message = "Motivo deve ter entre 10 e 500 caracteres")
        String motivoBloqueio,

        @NotBlank(message = "Usuário que está bloqueando é obrigatório")
        @Size(max = 100, message = "Nome do usuário não pode exceder 100 caracteres")
        String usuarioBloqueou
) {
}
