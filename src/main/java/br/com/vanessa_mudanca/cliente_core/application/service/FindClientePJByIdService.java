package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePJMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para buscar Cliente Pessoa Jurídica por Public ID (UUID).
 * Utiliza Optional e programação funcional.
 */
@Service
public class FindClientePJByIdService implements FindClientePJByIdUseCase {

    private final ClientePJRepositoryPort clientePJRepository;

    public FindClientePJByIdService(ClientePJRepositoryPort clientePJRepository) {
        this.clientePJRepository = clientePJRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePJResponse findByPublicId(UUID publicId) {
        return clientePJRepository.findByPublicId(publicId)
                .map(ClientePJMapper::toResponse)
                .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));
    }
}
