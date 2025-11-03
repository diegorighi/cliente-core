package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;

/**
 * Port de entrada (Use Case) para buscar Cliente Pessoa Jurídica por CNPJ.
 */
public interface FindClientePJByCnpjUseCase {

    /**
     * Busca um cliente pessoa jurídica por CNPJ.
     * Esse método permite descobrir o UUID público de um cliente através do CNPJ.
     *
     * @param cnpj CNPJ do cliente (pode estar formatado ou não)
     * @return cliente PJ encontrado com o publicId
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     */
    ClientePJResponse findByCnpj(String cnpj);
}
