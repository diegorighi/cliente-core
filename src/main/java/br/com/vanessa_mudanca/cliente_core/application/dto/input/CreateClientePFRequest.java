package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import br.com.vanessa_mudanca.cliente_core.domain.enums.OrigemLeadEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.infrastructure.validation.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Record para criação de Cliente Pessoa Física.
 * Utilizado como input na API REST.
 */
public record CreateClientePFRequest(

        @NotBlank(message = "Primeiro nome é obrigatório")
        String primeiroNome,

        String nomeDoMeio,

        @NotBlank(message = "Sobrenome é obrigatório")
        String sobrenome,

        @NotBlank(message = "CPF é obrigatório")
        @ValidCpf(message = "CPF inválido")
        String cpf,

        String rg,

        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        SexoEnum sexo,

        @Email(message = "Email deve ser válido")
        String email,

        String nomeMae,

        String nomePai,

        String estadoCivil,

        String profissao,

        String nacionalidade,

        String naturalidade,

        @NotNull(message = "Tipo de cliente é obrigatório")
        TipoClienteEnum tipoCliente,

        OrigemLeadEnum origemLead,

        String utmSource,

        String utmCampaign,

        String utmMedium,

        UUID clienteIndicadorId,

        String observacoes
) {
}
