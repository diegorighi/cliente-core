package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.ContatoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa a Port de persistência de Contato.
 * Conecta a camada de aplicação com o Spring Data JPA.
 */
@Component
public class ContatoRepositoryAdapter implements ContatoRepositoryPort {

    private final ContatoJpaRepository jpaRepository;

    public ContatoRepositoryAdapter(ContatoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Contato> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Contato save(Contato contato) {
        return jpaRepository.save(contato);
    }

    @Override
    public boolean existsByClienteIdAndContatoPrincipalAndIdNot(
            Long clienteId,
            Boolean contatoPrincipal,
            Long idExcluir
    ) {
        return jpaRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                clienteId,
                contatoPrincipal,
                idExcluir
        );
    }

    @Override
    public List<Contato> findByClienteId(Long clienteId) {
        return jpaRepository.findByClienteId(clienteId);
    }
}
