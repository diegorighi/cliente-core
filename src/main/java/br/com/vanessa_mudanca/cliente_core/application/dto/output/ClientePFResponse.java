package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import br.com.vanessa_mudanca.cliente_core.domain.enums.OrigemLeadEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record para resposta de Cliente Pessoa Física.
 * Utilizado como output na API REST.
 *
 * IMPORTANTE: Expõe apenas o publicId (UUID) por segurança.
 * O ID interno (Long sequencial) não é exposto para evitar enumeração.
 */
public record ClientePFResponse(
        UUID publicId,
        String primeiroNome,
        String nomeDoMeio,
        String sobrenome,
        String nomeCompleto,
        String cpf,
        String rg,
        LocalDate dataNascimento,
        Integer idade,
        SexoEnum sexo,
        String email,
        String nomeMae,
        String nomePai,
        String estadoCivil,
        String profissao,
        String nacionalidade,
        String naturalidade,
        TipoClienteEnum tipoCliente,
        OrigemLeadEnum origemLead,
        String utmSource,
        String utmCampaign,
        String utmMedium,
        UUID clienteIndicadorPublicId,
        Integer totalComprasRealizadas,
        Integer totalVendasRealizadas,
        BigDecimal valorTotalComprado,
        BigDecimal valorTotalVendido,
        LocalDateTime dataPrimeiraTransacao,
        LocalDateTime dataUltimaTransacao,
        Boolean bloqueado,
        String motivoBloqueio,
        String observacoes,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
