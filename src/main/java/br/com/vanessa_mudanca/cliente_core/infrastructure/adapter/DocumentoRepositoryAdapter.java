package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.DocumentoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.DocumentoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa a Port de persistência de Documento.
 * Conecta a camada de aplicação com o Spring Data JPA.
 */
@Component
public class DocumentoRepositoryAdapter implements DocumentoRepositoryPort {

    private final DocumentoJpaRepository jpaRepository;

    public DocumentoRepositoryAdapter(DocumentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Documento> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Documento save(Documento documento) {
        return jpaRepository.save(documento);
    }

    @Override
    public boolean existsByClienteIdAndDocumentoPrincipalAndIdNot(
            Long clienteId,
            Boolean documentoPrincipal,
            Long idExcluir
    ) {
        return jpaRepository.existsByClienteIdAndDocumentoPrincipalAndIdNot(
                clienteId,
                documentoPrincipal,
                idExcluir
        );
    }

    @Override
    public List<Documento> findByClienteId(Long clienteId) {
        return jpaRepository.findByClienteId(clienteId);
    }
}
