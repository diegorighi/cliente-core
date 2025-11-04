package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import java.util.UUID;

/**
 * Use Case para deletar cliente (soft delete).
 *
 * Implementa soft delete pattern:
 * - Define ativo = false
 * - Registra data_delecao
 * - Registra motivo e usuário
 * - NÃO remove fisicamente do banco
 */
public interface DeleteClienteUseCase {

    /**
     * Deleta cliente pelo UUID público.
     *
     * @param publicId UUID público do cliente
     * @param motivo Motivo da deleção
     * @param usuario Usuário que solicitou a deleção
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteJaDeletadoException se cliente já foi deletado
     */
    void deletar(UUID publicId, String motivo, String usuario);

    /**
     * Restaura cliente deletado (undelete).
     *
     * @param publicId UUID público do cliente
     * @param usuario Usuário que solicitou a restauração
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     */
    void restaurar(UUID publicId, String usuario);
}
