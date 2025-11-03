package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA para Documento.
 * Interface Spring Data JPA com queries derivadas.
 */
@Repository
public interface DocumentoJpaRepository extends JpaRepository<Documento, Long> {

    /**
     * Busca todos os documentos de um cliente.
     *
     * @param clienteId ID do cliente
     * @return Lista de documentos do cliente
     */
    List<Documento> findByClienteId(Long clienteId);

    /**
     * Verifica se existe outro documento principal para o cliente (excluindo o ID especificado).
     *
     * @param clienteId ID do cliente
     * @param documentoPrincipal Flag de principal (true)
     * @param id ID do documento a excluir da busca
     * @return true se já existe outro documento principal
     */
    boolean existsByClienteIdAndDocumentoPrincipalAndIdNot(
            Long clienteId,
            Boolean documentoPrincipal,
            Long id
    );
}
