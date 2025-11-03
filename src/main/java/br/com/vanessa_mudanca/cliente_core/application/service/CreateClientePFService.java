package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePFMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteIndicadorNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CpfInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CpfJaCadastradoException;
import br.com.vanessa_mudanca.cliente_core.domain.validator.DocumentoValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para criação de Cliente Pessoa Física.
 * Implementa o Use Case de criação com todas as validações necessárias.
 */
@Service
public class CreateClientePFService implements CreateClientePFUseCase {

    private final ClientePFRepositoryPort clientePFRepository;
    private final ClienteRepositoryPort clienteRepository;

    public CreateClientePFService(
            ClientePFRepositoryPort clientePFRepository,
            ClienteRepositoryPort clienteRepository) {
        this.clientePFRepository = clientePFRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public ClientePFResponse criar(CreateClientePFRequest request) {
        // 1. Validar CPF
        validarCpf(request.cpf());

        // 2. Verificar se CPF já existe
        verificarCpfDuplicado(request.cpf());

        // 3. Buscar cliente indicador (se informado)
        Cliente clienteIndicador = buscarClienteIndicador(request.clienteIndicadorId());

        // 4. Converter DTO para Entity
        ClientePF clientePF = ClientePFMapper.toEntity(request, clienteIndicador);

        // 5. Salvar no banco
        ClientePF clienteSalvo = clientePFRepository.save(clientePF);

        // 6. Converter Entity para Response
        return ClientePFMapper.toResponse(clienteSalvo);
    }

    private void validarCpf(String cpf) {
        String cpfLimpo = DocumentoValidator.limparDocumento(cpf);
        if (!DocumentoValidator.isValidCpf(cpfLimpo)) {
            throw new CpfInvalidoException(cpf);
        }
    }

    private void verificarCpfDuplicado(String cpf) {
        String cpfLimpo = DocumentoValidator.limparDocumento(cpf);
        if (clientePFRepository.existsByCpf(cpfLimpo)) {
            throw new CpfJaCadastradoException(cpf);
        }
    }

    private Cliente buscarClienteIndicador(UUID clienteIndicadorId) {
        if (clienteIndicadorId == null) {
            return null;
        }

        return clienteRepository.findByPublicId(clienteIndicadorId)
                .orElseThrow(() -> new ClienteIndicadorNaoEncontradoException(clienteIndicadorId));
    }
}
