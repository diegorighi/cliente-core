package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Port de entrada (Use Case) para listar Clientes Pessoa Física com paginação.
 */
public interface ListClientePFUseCase {

    /**
     * Lista todos os clientes pessoa física com paginação.
     *
     * @param pageable configuração de paginação e ordenação
     * @return página com clientes PF
     */
    PageResponse<ClientePFResponse> findAll(Pageable pageable);
}
