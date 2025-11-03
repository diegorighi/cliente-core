package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Port de entrada (Use Case) para listar Clientes Pessoa Jurídica com paginação.
 */
public interface ListClientePJUseCase {

    /**
     * Lista todos os clientes pessoa jurídica com paginação.
     *
     * @param pageable configuração de paginação e ordenação
     * @return página com clientes PJ
     */
    PageResponse<ClientePJResponse> findAll(Pageable pageable);
}
