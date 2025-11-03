package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import br.com.vanessa_mudanca.cliente_core.domain.enums.OrigemLeadEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Record para criação de Cliente Pessoa Jurídica.
 * Utilizado como input na API REST.
 */
public record CreateClientePJRequest(

        @NotBlank(message = "Razão social é obrigatória")
        String razaoSocial,

        String nomeFantasia,

        @NotBlank(message = "CNPJ é obrigatório")
        String cnpj,

        String inscricaoEstadual,

        String inscricaoMunicipal,

        LocalDate dataAbertura,

        String porteEmpresa,

        String naturezaJuridica,

        String atividadePrincipal,

        BigDecimal capitalSocial,

        String nomeResponsavel,

        String cpfResponsavel,

        String cargoResponsavel,

        String site,

        @Email(message = "Email deve ser válido")
        String email,

        @NotNull(message = "Tipo de cliente é obrigatório")
        TipoClienteEnum tipoCliente,

        OrigemLeadEnum origemLead,

        String utmSource,

        String utmCampaign,

        String utmMedium,

        Long clienteIndicadorId,

        String observacoes
) {
}
