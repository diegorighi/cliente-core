package br.com.vanessa_mudanca.cliente_core.application.ports.output;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;

import java.util.List;
import java.util.Optional;

/**
 * Port de saída para operações de persistência de Documento.
 * Implementado pela camada de infraestrutura.
 */
public interface DocumentoRepositoryPort {

    Optional<Documento> findById(Long id);

    Documento save(Documento documento);

    /**
     * Verifica se existe outro documento principal para o cliente.
     *
     * @param clienteId ID do cliente
     * @param documentoPrincipal Flag de principal (true)
     * @param idExcluir ID do documento a excluir da busca (o que está sendo atualizado)
     * @return true se já existe outro documento principal
     */
    boolean existsByClienteIdAndDocumentoPrincipalAndIdNot(
            Long clienteId,
            Boolean documentoPrincipal,
            Long idExcluir
    );

    List<Documento> findByClienteId(Long clienteId);
}
