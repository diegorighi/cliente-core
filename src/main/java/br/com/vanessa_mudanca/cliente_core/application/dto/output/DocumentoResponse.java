package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import br.com.vanessa_mudanca.cliente_core.domain.enums.StatusDocumentoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoDocumentoEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de resposta para Documento.
 *
 * @since 1.0.0
 */
@Schema(description = "Dados de um documento do cliente")
public record DocumentoResponse(

        @Schema(description = "ID interno do documento", example = "123")
        Long id,

        @Schema(description = "Tipo do documento", example = "RG")
        TipoDocumentoEnum tipoDocumento,

        @Schema(description = "Número do documento", example = "12.345.678-9")
        String numero,

        @Schema(description = "Órgão emissor", example = "SSP/SP")
        String orgaoEmissor,

        @Schema(description = "Data de emissão")
        LocalDate dataEmissao,

        @Schema(description = "Data de validade")
        LocalDate dataValidade,

        @Schema(description = "Observações sobre o documento")
        String observacoes,

        @Schema(description = "Status do documento", example = "APROVADO")
        StatusDocumentoEnum statusDocumento,

        @Schema(description = "Indica se é o documento principal", example = "true")
        Boolean documentoPrincipal,

        @Schema(description = "Indica se o documento está ativo", example = "true")
        Boolean ativo,

        @Schema(description = "Data de criação do registro")
        LocalDateTime dataCriacao,

        @Schema(description = "Data da última atualização")
        LocalDateTime dataAtualizacao
) {
}
