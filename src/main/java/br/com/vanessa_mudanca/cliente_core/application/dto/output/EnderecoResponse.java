package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import br.com.vanessa_mudanca.cliente_core.domain.enums.EstadoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de resposta para Endereco.
 *
 * @since 1.0.0
 */
@Schema(description = "Dados de um endereço do cliente")
public record EnderecoResponse(

        @Schema(description = "ID interno do endereço", example = "789")
        Long id,

        @Schema(description = "CEP", example = "01310-100")
        String cep,

        @Schema(description = "Logradouro", example = "Avenida Paulista")
        String logradouro,

        @Schema(description = "Número", example = "1578")
        String numero,

        @Schema(description = "Complemento", example = "Apto 42")
        String complemento,

        @Schema(description = "Bairro", example = "Bela Vista")
        String bairro,

        @Schema(description = "Cidade", example = "São Paulo")
        String cidade,

        @Schema(description = "Estado", example = "SP")
        EstadoEnum estado,

        @Schema(description = "País", example = "Brasil")
        String pais,

        @Schema(description = "Tipo de endereço", example = "RESIDENCIAL")
        TipoEnderecoEnum tipoEndereco,

        @Schema(description = "Indica se é o endereço principal", example = "true")
        Boolean enderecoPrincipal,

        @Schema(description = "Indica se o endereço está ativo", example = "true")
        Boolean ativo,

        @Schema(description = "Data de criação do registro")
        LocalDateTime dataCriacao,

        @Schema(description = "Data da última atualização")
        LocalDateTime dataAtualizacao
) {
}
