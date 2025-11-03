package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.EnderecoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa a Port de persistência de Endereco.
 * Conecta a camada de aplicação com o Spring Data JPA.
 */
@Component
public class EnderecoRepositoryAdapter implements EnderecoRepositoryPort {

    private final EnderecoJpaRepository jpaRepository;

    public EnderecoRepositoryAdapter(EnderecoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Endereco> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Endereco save(Endereco endereco) {
        return jpaRepository.save(endereco);
    }

    @Override
    public boolean existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
            Long clienteId,
            TipoEnderecoEnum tipoEndereco,
            Boolean enderecoPrincipal,
            Long idExcluir
    ) {
        return jpaRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                clienteId,
                tipoEndereco,
                enderecoPrincipal,
                idExcluir
        );
    }

    @Override
    public List<Endereco> findByClienteId(Long clienteId) {
        return jpaRepository.findByClienteId(clienteId);
    }
}
