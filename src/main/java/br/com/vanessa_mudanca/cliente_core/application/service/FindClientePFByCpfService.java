package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePFMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByCpfUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para buscar Cliente Pessoa Física por CPF.
 * Permite descobrir o UUID público através do CPF.
 */
@Service
public class FindClientePFByCpfService implements FindClientePFByCpfUseCase {

    private final ClientePFRepositoryPort clientePFRepository;

    public FindClientePFByCpfService(ClientePFRepositoryPort clientePFRepository) {
        this.clientePFRepository = clientePFRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePFResponse findByCpf(String cpf) {
        // Formata o CPF se vier sem formatação (11 dígitos)
        String cpfFormatado = cpf;
        if (cpf.matches("\\d{11}")) {
            cpfFormatado = cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }

        return clientePFRepository.findByCpf(cpfFormatado)
                .map(ClientePFMapper::toResponse)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                    String.format("Cliente PF com CPF '%s' não encontrado", cpf)));
    }
}
