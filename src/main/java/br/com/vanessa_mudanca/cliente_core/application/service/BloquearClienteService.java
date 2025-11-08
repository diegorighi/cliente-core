package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.ports.input.BloquearClienteUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteJaBloqueadoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para bloquear/desbloquear clientes.
 *
 * Implementa bloqueio de clientes:
 * - Define bloqueado = true
 * - Registra data_bloqueio, motivo_bloqueio, usuario_bloqueou
 * - Cliente bloqueado não pode realizar novas transações
 * - Permite desbloqueio posterior
 */
@Service
public class BloquearClienteService implements BloquearClienteUseCase {

    private static final Logger log = LoggerFactory.getLogger(BloquearClienteService.class);

    private final ClienteRepositoryPort clienteRepository;

    public BloquearClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
    public void bloquear(UUID publicId, String motivo, String usuario) {
        MDC.put("operationType", "BLOQUEAR_CLIENTE");
        MDC.put("clientId", publicId.toString());

        try {
            // Sanitize user-controlled input to prevent log injection
            String sanitizedMotivo = motivo != null ? motivo.replaceAll("[\n\r]", "_") : null;
            String sanitizedUsuario = usuario != null ? usuario.replaceAll("[\n\r]", "_") : null;

            log.info("Iniciando bloqueio de cliente - PublicId: {}, Motivo: {}, Usuario: {}",
                    publicId, sanitizedMotivo, sanitizedUsuario);

            // Busca cliente
            Cliente cliente = clienteRepository.findByPublicId(publicId)
                    .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

            // Verifica se já está bloqueado
            if (cliente.isBloqueado()) {
                log.warn("Tentativa de bloquear cliente já bloqueado - PublicId: {}, DataBloqueio: {}",
                        publicId, cliente.getDataBloqueio());
                throw new ClienteJaBloqueadoException(publicId);
            }

            // Bloqueia
            cliente.bloquear(motivo, usuario);
            clienteRepository.save(cliente);

            log.info("Cliente bloqueado com sucesso - PublicId: {}, DataBloqueio: {}",
                    publicId, cliente.getDataBloqueio());

        } catch (ClienteNaoEncontradoException | ClienteJaBloqueadoException e) {
            log.warn("Falha ao bloquear cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao bloquear cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("operationType");
            MDC.remove("clientId");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
    public void desbloquear(UUID publicId) {
        MDC.put("operationType", "DESBLOQUEAR_CLIENTE");
        MDC.put("clientId", publicId.toString());

        try {
            log.info("Iniciando desbloqueio de cliente - PublicId: {}", publicId);

            // Busca cliente
            Cliente cliente = clienteRepository.findByPublicId(publicId)
                    .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

            // Desbloqueia
            cliente.desbloquear();
            clienteRepository.save(cliente);

            log.info("Cliente desbloqueado com sucesso - PublicId: {}", publicId);

        } catch (ClienteNaoEncontradoException e) {
            log.warn("Falha ao desbloquear cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao desbloquear cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("operationType");
            MDC.remove("clientId");
        }
    }
}
