package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import java.util.UUID;

/**
 * Use Case para bloquear/desbloquear cliente.
 *
 * Implementa bloqueio de cliente:
 * - Define bloqueado = true
 * - Registra data_bloqueio, motivo_bloqueio, usuario_bloqueou
 * - Cliente bloqueado não pode realizar novas transações
 * - Permite desbloqueio posterior
 */
public interface BloquearClienteUseCase {

    /**
     * Bloqueia cliente pelo UUID público.
     *
     * @param publicId UUID público do cliente
     * @param motivo Motivo do bloqueio
     * @param usuario Usuário que solicitou o bloqueio
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteJaBloqueadoException se cliente já está bloqueado
     */
    void bloquear(UUID publicId, String motivo, String usuario);

    /**
     * Desbloqueia cliente bloqueado.
     *
     * @param publicId UUID público do cliente
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     */
    void desbloquear(UUID publicId);
}
