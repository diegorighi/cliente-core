package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;

/**
 * Port (interface) para atualização de Cliente Pessoa Jurídica.
 *
 * PADRÃO: Hexagonal Architecture (Port)
 * - Interface de entrada (Driving Port)
 * - Implementada pelo Service (Application Layer)
 * - Invocada pelo Controller (Infrastructure Layer)
 *
 * OPERAÇÃO: Aggregate Update
 * - Atualiza cliente + documentos + endereços + contatos em uma transação
 */
public interface UpdateClientePJUseCase {

    /**
     * Atualiza Cliente Pessoa Jurídica e suas entidades relacionadas.
     *
     * @param request DTO com dados para atualizar (selective update)
     * @return ClientePJResponse com dados atualizados
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException
     *         se publicId não encontrado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.DocumentoNaoEncontradoException
     *         se documento ID não encontrado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoNaoEncontradoException
     *         se endereço ID não encontrado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoNaoEncontradoException
     *         se contato ID não encontrado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoPrincipalDuplicadoException
     *         se tentar marcar 2º endereço como principal do mesmo tipo
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoPrincipalDuplicadoException
     *         se tentar marcar 2º contato como principal
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.DataValidadeInvalidaException
     *         se data de validade > 50 anos ou antes de emissão
     * @throws IllegalArgumentException
     *         se entidade não pertencer ao cliente (cross-client attack)
     */
    ClientePJResponse atualizar(UpdateClientePJRequest request);
}
