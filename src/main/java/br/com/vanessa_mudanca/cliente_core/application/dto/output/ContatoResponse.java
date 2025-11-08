package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoContatoEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de resposta para Contato.
 *
 * @since 1.0.0
 */
@Schema(description = "Dados de um contato do cliente")
public record ContatoResponse(

        @Schema(description = "ID interno do contato", example = "456")
        Long id,

        @Schema(description = "Tipo do contato", example = "CELULAR")
        TipoContatoEnum tipoContato,

        @Schema(description = "Valor do contato", example = "(11) 98765-4321")
        String valor,

        @Schema(description = "Observações sobre o contato")
        String observacoes,

        @Schema(description = "Indica se é o contato principal", example = "true")
        Boolean contatoPrincipal,

        @Schema(description = "Indica se o contato foi verificado", example = "false")
        Boolean verificado,

        @Schema(description = "Indica se o contato está ativo", example = "true")
        Boolean ativo,

        @Schema(description = "Data de criação do registro")
        LocalDateTime dataCriacao,

        @Schema(description = "Data da última atualização")
        LocalDateTime dataAtualizacao
) {
}
