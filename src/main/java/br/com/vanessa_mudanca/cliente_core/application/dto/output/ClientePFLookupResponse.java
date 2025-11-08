package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO de resposta reduzido para busca de Cliente Pessoa Física por CPF.
 *
 * Retorna apenas dados mínimos para validação e identificação,
 * seguindo princípio de minimização de dados (LGPD).
 *
 * Para dados completos, use GET /v1/clientes/pf/{publicId}
 *
 * @since 1.0.0
 */
@Schema(description = "Dados reduzidos de Cliente Pessoa Física (apenas identificação)")
public record ClientePFLookupResponse(

        @Schema(description = "Primeiro nome do cliente", example = "João")
        String primeiroNome,

        @Schema(description = "Sobrenome do cliente", example = "Silva")
        String sobrenome,

        @Schema(description = "Identificador único do cliente (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID publicId
) {
}
