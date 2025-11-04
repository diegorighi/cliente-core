package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateContatoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateDocumentoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateEnderecoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePFMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.UpdateClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.DocumentoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para atualização de Cliente Pessoa Física.
 * Implementa o Use Case de atualização com update seletivo de agregados.
 *
 * PADRÃO: Aggregate Update
 * - Atualiza cliente + documentos + endereços + contatos em uma única transação
 * - Updates são seletivos (apenas itens presentes no request são atualizados)
 *
 * Cache Eviction Strategy:
 * - Evict: clientes:findById (specific cliente)
 * - Evict: clientes:list (all pages - cliente pode mudar de posição)
 * - Nota: Não esvazia clientes:findByCpf pois CPF não pode ser alterado
 */
@Service
public class UpdateClientePFService implements UpdateClientePFUseCase {

    private final ClientePFRepositoryPort clientePFRepository;
    private final DocumentoRepositoryPort documentoRepository;
    private final EnderecoRepositoryPort enderecoRepository;
    private final ContatoRepositoryPort contatoRepository;
    private final ValidarDataValidadeStrategy validadorDataValidade;
    private final ValidarEnderecoPrincipalUnicoStrategy validadorEnderecoPrincipal;
    private final ValidarContatoPrincipalUnicoStrategy validadorContatoPrincipal;

    public UpdateClientePFService(
            ClientePFRepositoryPort clientePFRepository,
            DocumentoRepositoryPort documentoRepository,
            EnderecoRepositoryPort enderecoRepository,
            ContatoRepositoryPort contatoRepository,
            ValidarDataValidadeStrategy validadorDataValidade,
            ValidarEnderecoPrincipalUnicoStrategy validadorEnderecoPrincipal,
            ValidarContatoPrincipalUnicoStrategy validadorContatoPrincipal
    ) {
        this.clientePFRepository = clientePFRepository;
        this.documentoRepository = documentoRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
        this.validadorDataValidade = validadorDataValidade;
        this.validadorEnderecoPrincipal = validadorEnderecoPrincipal;
        this.validadorContatoPrincipal = validadorContatoPrincipal;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "clientes:findById", key = "#request.publicId().toString()"),
        @CacheEvict(value = "clientes:list", allEntries = true)
    })
    @Transactional
    public ClientePFResponse atualizar(UpdateClientePFRequest request) {
        // 1. Buscar cliente existente
        ClientePF cliente = buscarCliente(request.publicId());

        // 2. Atualizar dados básicos do cliente (se presentes)
        if (request.temDadosBasicosParaAtualizar()) {
            atualizarDadosCliente(cliente, request);
        }

        // 3. Atualizar documentos (se presentes)
        if (request.temDocumentosParaAtualizar()) {
            atualizarDocumentos(cliente, request);
        }

        // 4. Atualizar endereços (se presentes)
        if (request.temEnderecosParaAtualizar()) {
            atualizarEnderecos(cliente, request);
        }

        // 5. Atualizar contatos (se presentes)
        if (request.temContatosParaAtualizar()) {
            atualizarContatos(cliente, request);
        }

        // 6. Salvar cliente (cascade salvará as entidades relacionadas)
        ClientePF clienteAtualizado = clientePFRepository.save(cliente);

        // 7. Converter para Response
        return ClientePFMapper.toResponse(clienteAtualizado);
    }

    private ClientePF buscarCliente(java.util.UUID publicId) {
        return clientePFRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));
    }

    private void atualizarDadosCliente(ClientePF cliente, UpdateClientePFRequest request) {
        // Atualizar dados básicos usando método comportamental
        cliente.atualizarDadosBasicos(
                request.primeiroNome(),
                request.nomeDoMeio(),
                request.sobrenome(),
                request.rg(),
                request.sexo(),
                request.email()
        );

        // Atualizar dados complementares
        cliente.atualizarDadosComplementares(
                request.nomeMae(),
                request.nomePai(),
                request.estadoCivil(),
                request.profissao(),
                request.nacionalidade(),
                request.naturalidade()
        );

        // Atualizar tipo de cliente e observações (campos do Cliente base)
        if (request.tipoCliente() != null) {
            cliente.setTipoCliente(request.tipoCliente());
        }
        if (request.observacoes() != null) {
            cliente.setObservacoes(request.observacoes());
        }
    }

    private void atualizarDocumentos(ClientePF cliente, UpdateClientePFRequest request) {
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

    private void atualizarEnderecos(ClientePF cliente, UpdateClientePFRequest request) {
        for (UpdateEnderecoDTO dto : request.enderecos()) {
            // Buscar endereço existente
            Endereco endereco = enderecoRepository.findById(dto.id())
                    .orElseThrow(() -> new EnderecoNaoEncontradoException(dto.id()));

            // Validar que o endereço pertence ao cliente
            validarPropriedade(endereco.getCliente().getId(), cliente.getId(), "Endereço");

            // Validar unicidade de endereço principal (se estiver marcando como principal)
            if (dto.enderecoPrincipal() != null) {
                // Usar tipo do DTO se presente, senão usar tipo da entidade existente
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

    private void atualizarContatos(ClientePF cliente, UpdateClientePFRequest request) {
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
     * Previne que um cliente atualize entidades de outro cliente.
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
