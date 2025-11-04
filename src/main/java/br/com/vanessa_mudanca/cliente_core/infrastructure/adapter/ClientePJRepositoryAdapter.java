package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.ClientePJJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa a Port de persistência de Cliente PJ.
 * Conecta a camada de aplicação com o Spring Data JPA.
 */
@Component
public class ClientePJRepositoryAdapter implements ClientePJRepositoryPort {

    private final ClientePJJpaRepository jpaRepository;

    public ClientePJRepositoryAdapter(ClientePJJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ClientePJ save(ClientePJ clientePJ) {
        return jpaRepository.save(clientePJ);
    }

    @Override
    public Optional<ClientePJ> findByCnpj(String cnpj) {
        return jpaRepository.findByCnpj(cnpj);
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return jpaRepository.existsByCnpj(cnpj);
    }

    @Override
    public Optional<ClientePJ> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ClientePJ> findByPublicId(UUID publicId) {
        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public Page<ClientePJ> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Optional<ClientePJ> findActiveByCnpj(String cnpj) {
        return jpaRepository.findByCnpjAndAtivoTrueAndDataDelecaoIsNull(cnpj);
    }

    @Override
    public Optional<ClientePJ> findActiveByPublicId(UUID publicId) {
        return jpaRepository.findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(publicId);
    }

    @Override
    public boolean existsActiveByCnpj(String cnpj) {
        return jpaRepository.existsByCnpjAndAtivoTrueAndDataDelecaoIsNull(cnpj);
    }
}
