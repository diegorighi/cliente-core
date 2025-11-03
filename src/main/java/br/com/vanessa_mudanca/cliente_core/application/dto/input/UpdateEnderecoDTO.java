package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import br.com.vanessa_mudanca.cliente_core.domain.enums.EstadoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/**
 * DTO para atualização de Endereço.
 * TODOS os campos são mutáveis.
 */
@Builder
public record UpdateEnderecoDTO(

        @NotNull(message = "ID do endereço é obrigatório para atualização")
        Long id,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve estar no formato 12345-678")
        String cep,

        @NotBlank(message = "Logradouro é obrigatório")
        String logradouro,

        String numero,

        String complemento,

        @NotBlank(message = "Bairro é obrigatório")
        String bairro,

        @NotBlank(message = "Cidade é obrigatória")
        String cidade,

        @NotNull(message = "Estado é obrigatório")
        EstadoEnum estado,

        String pais,

        TipoEnderecoEnum tipoEndereco,

        Boolean enderecoPrincipal
) {
}
