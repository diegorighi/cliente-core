package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO para atualização de campos MUTÁVEIS de Documento.
 *
 * CAMPOS IMUTÁVEIS (não presentes):
 * - numero (CPF/RG/CNPJ não pode mudar)
 * - tipoDocumento (não pode converter CPF em RG)
 *
 * CAMPOS MUTÁVEIS:
 * - dataEmissao, dataValidade (correções, renovações)
 * - orgaoEmissor (correções de formatação)
 * - observacoes (notas administrativas)
 */
@Builder
public record UpdateDocumentoDTO(

        @NotNull(message = "ID do documento é obrigatório para atualização")
        Long id,

        LocalDate dataEmissao,

        LocalDate dataValidade,

        String orgaoEmissor,

        String observacoes
) {
}
