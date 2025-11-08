package br.com.vanessa_mudanca.cliente_core.application.mapper;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ContatoResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.DocumentoResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.EnderecoResponse;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre DTOs e Entity de Cliente Pessoa Física.
 * Não utiliza bibliotecas externas (ModelMapper, MapStruct, etc).
 */
public class ClientePFMapper {

    private ClientePFMapper() {
        // Utility class
    }

    /**
     * Converte CreateClientePFRequest para ClientePF entity.
     *
     * @param request DTO de entrada
     * @param clienteIndicador cliente indicador (opcional)
     * @return entidade ClientePF
     */
    public static ClientePF toEntity(CreateClientePFRequest request, Cliente clienteIndicador) {
        return ClientePF.builder()
                .primeiroNome(request.primeiroNome())
                .nomeDoMeio(request.nomeDoMeio())
                .sobrenome(request.sobrenome())
                .cpf(request.cpf())
                .rg(request.rg())
                .dataNascimento(request.dataNascimento())
                .sexo(request.sexo())
                .email(request.email())
                .nomeMae(request.nomeMae())
                .nomePai(request.nomePai())
                .estadoCivil(request.estadoCivil())
                .profissao(request.profissao())
                .nacionalidade(request.nacionalidade() != null ? request.nacionalidade() : "Brasileira")
                .naturalidade(request.naturalidade())
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
     * Converte ClientePF entity para ClientePFResponse.
     *
     * @param entity entidade ClientePF
     * @return DTO de saída
     */
    public static ClientePFResponse toResponse(ClientePF entity) {
        return new ClientePFResponse(
                entity.getPublicId(),
                entity.getPrimeiroNome(),
                entity.getNomeDoMeio(),
                entity.getSobrenome(),
                entity.getNomeCompleto(),
                entity.getCpf(),
                entity.getRg(),
                entity.getDataNascimento(),
                entity.getIdade(),
                entity.getSexo(),
                entity.getEmail(),
                entity.getNomeMae(),
                entity.getNomePai(),
                entity.getEstadoCivil(),
                entity.getProfissao(),
                entity.getNacionalidade(),
                entity.getNaturalidade(),
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
