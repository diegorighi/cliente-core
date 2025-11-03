package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import br.com.vanessa_mudanca.cliente_core.domain.enums.OrigemLeadEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record para resposta de Cliente Pessoa Jurídica.
 * Utilizado como output na API REST.
 *
 * IMPORTANTE: Expõe apenas o publicId (UUID) por segurança.
 * O ID interno (Long sequencial) não é exposto para evitar enumeração.
 */
public record ClientePJResponse(
        UUID publicId,
        String razaoSocial,
        String nomeFantasia,
        String nomeExibicao,
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
        String email,
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
