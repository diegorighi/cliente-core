package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.ClienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa a Port de persistência de Cliente (base).
 * Conecta a camada de aplicação com o Spring Data JPA.
 */
@Component
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Cliente> findByPublicId(UUID publicId) {
        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Cliente save(Cliente cliente) {
        return jpaRepository.save(cliente);
    }
}
