package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateContatoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateDocumentoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateEnderecoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePJMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.UpdateClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.DocumentoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.DocumentoNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.validator.ValidarContatoPrincipalUnicoStrategy;
import br.com.vanessa_mudanca.cliente_core.domain.validator.ValidarDataValidadeStrategy;
import br.com.vanessa_mudanca.cliente_core.domain.validator.ValidarEnderecoPrincipalUnicoStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para atualização de Cliente Pessoa Jurídica.
 * Implementa o Use Case de atualização com update seletivo de agregados.
 *
 * PADRÃO: Aggregate Update
 * - Atualiza cliente + documentos + endereços + contatos em uma única transação
 * - Updates são seletivos (apenas itens presentes no request são atualizados)
 *
 * LIÇÕES DO CODE REVIEW (aplicadas):
 * 1. ✅ Null safety em todos os métodos
 * 2. ✅ Fallback para valores da entidade quando DTO é parcial
 * 3. ✅ Validação de ownership (cross-client protection)
 * 4. ✅ Transaction rollback automático em caso de falha
 */
@Service
public class UpdateClientePJService implements UpdateClientePJUseCase {

    private final ClientePJRepositoryPort clientePJRepository;
    private final DocumentoRepositoryPort documentoRepository;
    private final EnderecoRepositoryPort enderecoRepository;
    private final ContatoRepositoryPort contatoRepository;
    private final ValidarDataValidadeStrategy validadorDataValidade;
    private final ValidarEnderecoPrincipalUnicoStrategy validadorEnderecoPrincipal;
    private final ValidarContatoPrincipalUnicoStrategy validadorContatoPrincipal;

    public UpdateClientePJService(
            ClientePJRepositoryPort clientePJRepository,
            DocumentoRepositoryPort documentoRepository,
            EnderecoRepositoryPort enderecoRepository,
            ContatoRepositoryPort contatoRepository,
            ValidarDataValidadeStrategy validadorDataValidade,
            ValidarEnderecoPrincipalUnicoStrategy validadorEnderecoPrincipal,
            ValidarContatoPrincipalUnicoStrategy validadorContatoPrincipal
    ) {
        this.clientePJRepository = clientePJRepository;
        this.documentoRepository = documentoRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
        this.validadorDataValidade = validadorDataValidade;
        this.validadorEnderecoPrincipal = validadorEnderecoPrincipal;
        this.validadorContatoPrincipal = validadorContatoPrincipal;
    }

    @Override
    @Transactional
    public ClientePJResponse atualizar(UpdateClientePJRequest request) {
        // 1. Buscar cliente existente
        ClientePJ cliente = buscarCliente(request.publicId());

        // 2. Atualizar dados básicos da empresa (se presentes)
        if (request.temDadosBasicosParaAtualizar()) {
            atualizarDadosCliente(cliente, request);
        }

        // 3. Atualizar dados de classificação (se presentes)
        if (request.temDadosClassificacaoParaAtualizar()) {
            cliente.atualizarDadosClassificacao(
                    request.porteEmpresa(),
                    request.naturezaJuridica(),
                    request.atividadePrincipal(),
                    request.capitalSocial()
            );
        }

        // 4. Atualizar dados do responsável (se presentes)
        if (request.temDadosResponsavelParaAtualizar()) {
            cliente.atualizarDadosResponsavel(
                    request.nomeResponsavel(),
                    request.cpfResponsavel(),
                    request.cargoResponsavel()
            );
        }

        // 5. Atualizar informações adicionais (se presentes)
        if (request.site() != null) {
            cliente.setSite(request.site());
        }
        if (request.tipoCliente() != null) {
            cliente.setTipoCliente(request.tipoCliente());
        }
        if (request.observacoes() != null) {
            cliente.setObservacoes(request.observacoes());
        }

        // 6. Atualizar documentos (se presentes)
        if (request.temDocumentosParaAtualizar()) {
            atualizarDocumentos(cliente, request);
        }

        // 7. Atualizar endereços (se presentes)
        if (request.temEnderecosParaAtualizar()) {
            atualizarEnderecos(cliente, request);
        }

        // 8. Atualizar contatos (se presentes)
        if (request.temContatosParaAtualizar()) {
            atualizarContatos(cliente, request);
        }

        // 9. Salvar cliente (cascade salvará as entidades relacionadas)
        ClientePJ clienteAtualizado = clientePJRepository.save(cliente);

        // 10. Converter para Response
        return ClientePJMapper.toResponse(clienteAtualizado);
    }

    private ClientePJ buscarCliente(java.util.UUID publicId) {
        return clientePJRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));
    }

    private void atualizarDadosCliente(ClientePJ cliente, UpdateClientePJRequest request) {
        // Atualizar dados básicos da empresa
        cliente.atualizarDadosBasicos(
                request.razaoSocial(),
                request.nomeFantasia(),
                request.inscricaoEstadual(),
                request.inscricaoMunicipal(),
                request.dataAbertura(),
                request.email()
        );
    }

    private void atualizarDocumentos(ClientePJ cliente, UpdateClientePJRequest request) {
        for (UpdateDocumentoDTO dto : request.documentos()) {
            // Buscar documento existente
            Documento documento = documentoRepository.findById(dto.id())
                    .orElseThrow(() -> new DocumentoNaoEncontradoException(dto.id()));

            // Validar que o documento pertence ao cliente
            validarPropriedade(documento.getCliente().getId(), cliente.getId(), "Documento");

            // Validar data de validade
            validadorDataValidade.validar(dto);

            // Atualizar usando métodos comportamentais
            documento.atualizarDatasEEmissor(
                    dto.dataEmissao(),
                    dto.dataValidade(),
                    dto.orgaoEmissor()
            );

            if (dto.observacoes() != null) {
                documento.atualizarObservacoes(dto.observacoes());
            }

            // Salvar documento
            documentoRepository.save(documento);
        }
    }

    private void atualizarEnderecos(ClientePJ cliente, UpdateClientePJRequest request) {
        for (UpdateEnderecoDTO dto : request.enderecos()) {
            // Buscar endereço existente
            Endereco endereco = enderecoRepository.findById(dto.id())
                    .orElseThrow(() -> new EnderecoNaoEncontradoException(dto.id()));

            // Validar que o endereço pertence ao cliente
            validarPropriedade(endereco.getCliente().getId(), cliente.getId(), "Endereço");

            // Validar unicidade de endereço principal (se estiver marcando como principal)
            if (dto.enderecoPrincipal() != null) {
                // ✅ LIÇÃO APLICADA: Usar tipo do DTO se presente, senão usar tipo da entidade existente
                TipoEnderecoEnum tipoParaValidar = dto.tipoEndereco() != null
                    ? dto.tipoEndereco()
                    : endereco.getTipoEndereco();

                validadorEnderecoPrincipal.validar(
                        cliente.getId(),
                        dto.id(),
                        tipoParaValidar,
                        dto.enderecoPrincipal()
                );

                if (dto.enderecoPrincipal()) {
                    endereco.marcarComoPrincipal();
                } else {
                    endereco.removerFlagPrincipal();
                }
            }

            // Atualizar usando método comportamental
            endereco.atualizarDadosEndereco(
                    dto.cep(),
                    dto.logradouro(),
                    dto.numero(),
                    dto.complemento(),
                    dto.bairro(),
                    dto.cidade(),
                    dto.estado(),
                    dto.pais()
            );

            // Salvar endereço
            enderecoRepository.save(endereco);
        }
    }

    private void atualizarContatos(ClientePJ cliente, UpdateClientePJRequest request) {
        for (UpdateContatoDTO dto : request.contatos()) {
            // Buscar contato existente
            Contato contato = contatoRepository.findById(dto.id())
                    .orElseThrow(() -> new ContatoNaoEncontradoException(dto.id()));

            // Validar que o contato pertence ao cliente
            validarPropriedade(contato.getCliente().getId(), cliente.getId(), "Contato");

            // Validar unicidade de contato principal (se estiver marcando como principal)
            if (dto.contatoPrincipal() != null) {
                validadorContatoPrincipal.validar(
                        cliente.getId(),
                        dto.id(),
                        dto.contatoPrincipal()
                );

                if (dto.contatoPrincipal()) {
                    contato.marcarComoPrincipal();
                } else {
                    contato.removerFlagPrincipal();
                }
            }

            // Atualizar tipo e valor (invalida verificado se mudou)
            // ✅ LIÇÃO APLICADA: Null safety já implementada nos métodos comportamentais
            contato.atualizarTipo(dto.tipoContato());
            contato.atualizarValor(dto.valor());

            if (dto.observacoes() != null) {
                contato.atualizarObservacoes(dto.observacoes());
            }

            // Salvar contato
            contatoRepository.save(contato);
        }
    }

    /**
     * Valida que a entidade pertence ao cliente que está sendo atualizado.
     * Previne que um cliente atualize entidades de outro cliente (cross-client attack).
     */
    private void validarPropriedade(Long entidadeClienteId, Long clienteId, String entidadeTipo) {
        if (!entidadeClienteId.equals(clienteId)) {
            throw new IllegalArgumentException(
                    String.format(
                            "%s com ID não pertence ao cliente sendo atualizado",
                            entidadeTipo
                    )
            );
        }
    }
}
