package br.com.vanessa_mudanca.cliente_core.application.mapper;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ContatoResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.DocumentoResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.EnderecoResponse;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
                entity.getDataAtualizacao(),
                mapDocumentos(entity.getListaDocumentos()),
                mapContatos(entity.getListaContatos()),
                mapEnderecos(entity.getListaEnderecos())
        );
    }

    private static List<DocumentoResponse> mapDocumentos(List<Documento> documentos) {
        return documentos.stream()
                .map(doc -> new DocumentoResponse(
                        doc.getId(),
                        doc.getTipoDocumento(),
                        doc.getNumero(),
                        doc.getOrgaoEmissor(),
                        doc.getDataEmissao(),
                        doc.getDataValidade(),
                        doc.getObservacoes(),
                        doc.getStatusDocumento(),
                        doc.getDocumentoPrincipal(),
                        doc.getAtivo(),
                        doc.getDataCriacao(),
                        doc.getDataAtualizacao()
                ))
                .collect(Collectors.toList());
    }

    private static List<ContatoResponse> mapContatos(List<Contato> contatos) {
        return contatos.stream()
                .map(contato -> new ContatoResponse(
                        contato.getId(),
                        contato.getTipoContato(),
                        contato.getValor(),
                        contato.getObservacoes(),
                        contato.getContatoPrincipal(),
                        contato.getVerificado(),
                        contato.getAtivo(),
                        contato.getDataCriacao(),
                        contato.getDataAtualizacao()
                ))
                .collect(Collectors.toList());
    }

    private static List<EnderecoResponse> mapEnderecos(List<Endereco> enderecos) {
        return enderecos.stream()
                .map(endereco -> new EnderecoResponse(
                        endereco.getId(),
                        endereco.getCep(),
                        endereco.getLogradouro(),
                        endereco.getNumero(),
                        endereco.getComplemento(),
                        endereco.getBairro(),
                        endereco.getCidade(),
                        endereco.getEstado(),
                        endereco.getPais(),
                        endereco.getTipoEndereco(),
                        endereco.getEnderecoPrincipal(),
                        endereco.getAtivo(),
                        endereco.getDataCriacao(),
                        endereco.getDataAtualizacao()
                ))
                .collect(Collectors.toList());
    }
}
