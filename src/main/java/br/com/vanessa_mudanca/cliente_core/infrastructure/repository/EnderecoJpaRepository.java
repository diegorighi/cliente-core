package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA para Endereco.
 * Interface Spring Data JPA com queries derivadas.
 */
@Repository
public interface EnderecoJpaRepository extends JpaRepository<Endereco, Long> {

    /**
     * Busca todos os endereços de um cliente.
     *
     * @param clienteId ID do cliente
     * @return Lista de endereços do cliente
     */
    List<Endereco> findByClienteId(Long clienteId);

    /**
     * Verifica se existe outro endereço principal do mesmo tipo para o cliente (excluindo o ID especificado).
     *
     * @param clienteId ID do cliente
     * @param tipoEndereco Tipo do endereço
     * @param enderecoPrincipal Flag de principal (true)
     * @param id ID do endereço a excluir da busca
     * @return true se já existe outro endereço principal do mesmo tipo
     */
    boolean existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
            Long clienteId,
            TipoEnderecoEnum tipoEndereco,
            Boolean enderecoPrincipal,
            Long id
    );
}
