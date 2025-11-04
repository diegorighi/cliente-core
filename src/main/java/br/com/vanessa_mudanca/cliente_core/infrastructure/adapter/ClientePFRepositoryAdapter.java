package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.ClientePFJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa a Port de persistência de Cliente PF.
 * Conecta a camada de aplicação com o Spring Data JPA.
 */
@Component
public class ClientePFRepositoryAdapter implements ClientePFRepositoryPort {

    private final ClientePFJpaRepository jpaRepository;

    public ClientePFRepositoryAdapter(ClientePFJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ClientePF save(ClientePF clientePF) {
        return jpaRepository.save(clientePF);
    }

    @Override
    public Optional<ClientePF> findByCpf(String cpf) {
        return jpaRepository.findByCpf(cpf);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }

    @Override
    public Optional<ClientePF> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ClientePF> findByPublicId(UUID publicId) {
        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public Page<ClientePF> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Optional<ClientePF> findActiveByCpf(String cpf) {
        return jpaRepository.findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpf);
    }

    @Override
    public Optional<ClientePF> findActiveByPublicId(UUID publicId) {
        return jpaRepository.findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(publicId);
    }

    @Override
    public boolean existsActiveByCpf(String cpf) {
        return jpaRepository.existsByCpfAndAtivoTrueAndDataDelecaoIsNull(cpf);
    }
}
