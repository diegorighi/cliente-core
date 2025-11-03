package br.com.vanessa_mudanca.cliente_core.application.mapper;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;

import java.time.LocalDateTime;

/**
 * Mapper para conversão entre DTOs e Entity de Cliente Pessoa Jurídica.
 * Não utiliza bibliotecas externas (ModelMapper, MapStruct, etc).
 */
public class ClientePJMapper {

    private ClientePJMapper() {
        // Utility class
    }

    /**
     * Converte CreateClientePJRequest para ClientePJ entity.
     *
     * @param request DTO de entrada
     * @param clienteIndicador cliente indicador (opcional)
     * @return entidade ClientePJ
     */
    public static ClientePJ toEntity(CreateClientePJRequest request, Cliente clienteIndicador) {
        return ClientePJ.builder()
                .razaoSocial(request.razaoSocial())
                .nomeFantasia(request.nomeFantasia())
                .cnpj(request.cnpj())
                .inscricaoEstadual(request.inscricaoEstadual())
                .inscricaoMunicipal(request.inscricaoMunicipal())
                .dataAbertura(request.dataAbertura())
                .porteEmpresa(request.porteEmpresa())
                .naturezaJuridica(request.naturezaJuridica())
                .atividadePrincipal(request.atividadePrincipal())
                .capitalSocial(request.capitalSocial())
                .nomeResponsavel(request.nomeResponsavel())
                .cpfResponsavel(request.cpfResponsavel())
                .cargoResponsavel(request.cargoResponsavel())
                .site(request.site())
                .email(request.email())
                .tipoCliente(request.tipoCliente())
                .origemLead(request.origemLead())
                .utmSource(request.utmSource())
                .utmCampaign(request.utmCampaign())
                .utmMedium(request.utmMedium())
                .clienteIndicador(clienteIndicador)
                .dataIndicacao(clienteIndicador != null ? LocalDateTime.now() : null)
                .indicacaoRecompensada(false)
                .observacoes(request.observacoes())
                .ativo(true)
                .bloqueado(false)
                .totalComprasRealizadas(0)
                .totalVendasRealizadas(0)
                .build();
    }

    /**
     * Converte ClientePJ entity para ClientePJResponse.
     *
     * @param entity entidade ClientePJ
     * @return DTO de saída
     */
    public static ClientePJResponse toResponse(ClientePJ entity) {
        return new ClientePJResponse(
                entity.getPublicId(),
                entity.getRazaoSocial(),
                entity.getNomeFantasia(),
                entity.getNomeExibicao(),
                entity.getCnpj(),
                entity.getInscricaoEstadual(),
                entity.getInscricaoMunicipal(),
                entity.getDataAbertura(),
                entity.getPorteEmpresa(),
                entity.getNaturezaJuridica(),
                entity.getAtividadePrincipal(),
                entity.getCapitalSocial(),
                entity.getNomeResponsavel(),
                entity.getCpfResponsavel(),
                entity.getCargoResponsavel(),
                entity.getSite(),
                entity.getEmail(),
                entity.getTipoCliente(),
                entity.getOrigemLead(),
                entity.getUtmSource(),
                entity.getUtmCampaign(),
                entity.getUtmMedium(),
                entity.getClienteIndicador() != null ? entity.getClienteIndicador().getPublicId() : null,
                entity.getTotalComprasRealizadas(),
                entity.getTotalVendasRealizadas(),
                entity.getValorTotalComprado(),
                entity.getValorTotalVendido(),
                entity.getDataPrimeiraTransacao(),
                entity.getDataUltimaTransacao(),
                entity.getBloqueado(),
                entity.getMotivoBloqueio(),
                entity.getObservacoes(),
                entity.getAtivo(),
                entity.getDataCriacao(),
                entity.getDataAtualizacao()
        );
    }
}
