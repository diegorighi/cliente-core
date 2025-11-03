package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePJMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByCnpjUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para buscar Cliente Pessoa Jurídica por CNPJ.
 * Permite descobrir o UUID público através do CNPJ.
 */
@Service
public class FindClientePJByCnpjService implements FindClientePJByCnpjUseCase {

    private final ClientePJRepositoryPort clientePJRepository;

    public FindClientePJByCnpjService(ClientePJRepositoryPort clientePJRepository) {
        this.clientePJRepository = clientePJRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePJResponse findByCnpj(String cnpj) {
        // Formata o CNPJ se vier sem formatação (14 dígitos)
        String cnpjFormatado = cnpj;
        if (cnpj.matches("\\d{14}")) {
            cnpjFormatado = cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }

        return clientePJRepository.findByCnpj(cnpjFormatado)
                .map(ClientePJMapper::toResponse)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                    String.format("Cliente PJ com CNPJ '%s' não encontrado", cnpj)));
    }
}
