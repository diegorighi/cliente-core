package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoContatoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO para atualização de Contato.
 * TODOS os campos são mutáveis.
 *
 * REGRA ESPECIAL:
 * - Alterar 'valor' invalida flag 'verificado' (volta para false automaticamente)
 */
@Builder
public record UpdateContatoDTO(

        @NotNull(message = "ID do contato é obrigatório para atualização")
        Long id,

        @NotNull(message = "Tipo de contato é obrigatório")
        TipoContatoEnum tipoContato,

        @NotBlank(message = "Valor do contato é obrigatório")
        String valor,

        String observacoes,

        Boolean contatoPrincipal
) {
}
