package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO de resposta reduzido para busca de Cliente Pessoa Jurídica por CNPJ.
 *
 * Retorna apenas dados mínimos para validação e identificação,
 * seguindo princípio de minimização de dados (LGPD).
 *
 * Para dados completos, use GET /v1/clientes/pj/{publicId}
 *
 * @since 1.0.0
 */
@Schema(description = "Dados reduzidos de Cliente Pessoa Jurídica (apenas identificação)")
public record ClientePJLookupResponse(

        @Schema(description = "Nome fantasia da empresa", example = "Mudanças Express")
        String nomeFantasia,

        @Schema(description = "Identificador único do cliente (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID publicId
) {
}
