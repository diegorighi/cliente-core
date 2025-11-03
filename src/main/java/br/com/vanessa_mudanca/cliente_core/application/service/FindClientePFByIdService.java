package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePFMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para buscar Cliente Pessoa Física por Public ID (UUID).
 * Utiliza Optional e programação funcional.
 */
@Service
public class FindClientePFByIdService implements FindClientePFByIdUseCase {

    private final ClientePFRepositoryPort clientePFRepository;

    public FindClientePFByIdService(ClientePFRepositoryPort clientePFRepository) {
        this.clientePFRepository = clientePFRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePFResponse findByPublicId(UUID publicId) {
        return clientePFRepository.findByPublicId(publicId)
                .map(ClientePFMapper::toResponse)
                .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));
    }
}
