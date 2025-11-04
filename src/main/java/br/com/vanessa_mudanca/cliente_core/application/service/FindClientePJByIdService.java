package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePJMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para buscar Cliente Pessoa Jurídica por Public ID (UUID).
 * Utiliza Optional e programação funcional.
 *
 * Cache Strategy:
 * - Cache: clientes:findById
 * - TTL: 5 minutos (hot cache)
 * - Key: UUID do cliente
 * - Evict: UpdateClientePJService, DeleteClienteService
 */
@Service
public class FindClientePJByIdService implements FindClientePJByIdUseCase {

    private final ClientePJRepositoryPort clientePJRepository;

    public FindClientePJByIdService(ClientePJRepositoryPort clientePJRepository) {
        this.clientePJRepository = clientePJRepository;
    }

    @Override
    @Cacheable(
        value = "clientes:findById",
        key = "#publicId.toString()",
        unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public ClientePJResponse findByPublicId(UUID publicId) {
        return clientePJRepository.findByPublicId(publicId)
                .map(ClientePJMapper::toResponse)
                .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));
    }
}
