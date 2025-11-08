package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.ports.input.DeleteClienteUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteJaDeletadoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.infrastructure.util.MaskingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para deletar clientes (soft delete).
 *
 * Implementa soft delete pattern:
 * - Define ativo = false
 * - Registra data_delecao, motivo_delecao, usuario_deletou
 * - NÃO remove fisicamente do banco
 * - Preserva integridade referencial
 * - Permite restauração posterior
 */
@Service
public class DeleteClienteService implements DeleteClienteUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteClienteService.class);

    private final ClienteRepositoryPort clienteRepository;

    public DeleteClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
    public void deletar(UUID publicId, String motivo, String usuario) {
        MDC.put("operationType", "DELETE_CLIENTE");
        MDC.put("clientId", publicId.toString());

        try {
            // Sanitize user-controlled input to prevent log injection
            String sanitizedMotivo = motivo != null ? motivo.replaceAll("[\n\r]", "_") : null;
            String sanitizedUsuario = usuario != null ? usuario.replaceAll("[\n\r]", "_") : null;

            log.info("Iniciando deleção de cliente - PublicId: {}, Motivo: {}, Usuario: {}",
                    publicId, sanitizedMotivo, sanitizedUsuario);

            // Busca cliente
            Cliente cliente = clienteRepository.findByPublicId(publicId)
                    .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

            // Verifica se já foi deletado
            if (cliente.isDeletado()) {
                log.warn("Tentativa de deletar cliente já deletado - PublicId: {}, DataDelecao: {}",
                        publicId, cliente.getDataDelecao());
                throw new ClienteJaDeletadoException(publicId);
            }

            // Soft delete
            cliente.deletar(motivo, usuario);
            clienteRepository.save(cliente);

            log.info("Cliente deletado com sucesso - PublicId: {}, DataDelecao: {}",
                    publicId, cliente.getDataDelecao());

        } catch (ClienteNaoEncontradoException | ClienteJaDeletadoException e) {
            log.warn("Falha ao deletar cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar cliente - PublicId: {}, Erro: {}",
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
    public void restaurar(UUID publicId, String usuario) {
        MDC.put("operationType", "RESTAURAR_CLIENTE");
        MDC.put("clientId", publicId.toString());

        try {
            // Sanitize user-controlled input to prevent log injection
            String sanitizedUsuario = usuario != null ? usuario.replaceAll("[\n\r]", "_") : null;

            log.info("Iniciando restauração de cliente - PublicId: {}, Usuario: {}",
                    publicId, sanitizedUsuario);

            // Busca cliente (inclusive deletados)
            Cliente cliente = clienteRepository.findByPublicId(publicId)
                    .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

            // Restaura
            cliente.restaurar(usuario);
            clienteRepository.save(cliente);

            log.info("Cliente restaurado com sucesso - PublicId: {}",
                    publicId);

        } catch (ClienteNaoEncontradoException e) {
            log.warn("Falha ao restaurar cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao restaurar cliente - PublicId: {}, Erro: {}",
                    publicId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("operationType");
            MDC.remove("clientId");
        }
    }
}
