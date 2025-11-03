package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePJMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteIndicadorNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjJaCadastradoException;
import br.com.vanessa_mudanca.cliente_core.domain.validator.DocumentoValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para criação de Cliente Pessoa Jurídica.
 * Implementa o Use Case de criação com todas as validações necessárias.
 */
@Service
public class CreateClientePJService implements CreateClientePJUseCase {

    private final ClientePJRepositoryPort clientePJRepository;
    private final ClienteRepositoryPort clienteRepository;

    public CreateClientePJService(
            ClientePJRepositoryPort clientePJRepository,
            ClienteRepositoryPort clienteRepository) {
        this.clientePJRepository = clientePJRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public ClientePJResponse criar(CreateClientePJRequest request) {
        // 1. Validar CNPJ
        validarCnpj(request.cnpj());

        // 2. Verificar se CNPJ já existe
        verificarCnpjDuplicado(request.cnpj());

        // 3. Buscar cliente indicador (se informado)
        Cliente clienteIndicador = buscarClienteIndicador(request.clienteIndicadorId());

        // 4. Converter DTO para Entity
        ClientePJ clientePJ = ClientePJMapper.toEntity(request, clienteIndicador);

        // 5. Salvar no banco
        ClientePJ clienteSalvo = clientePJRepository.save(clientePJ);

        // 6. Converter Entity para Response
        return ClientePJMapper.toResponse(clienteSalvo);
    }

    private void validarCnpj(String cnpj) {
        String cnpjLimpo = DocumentoValidator.limparDocumento(cnpj);
        if (!DocumentoValidator.isValidCnpj(cnpjLimpo)) {
            throw new CnpjInvalidoException(cnpj);
        }
    }

    private void verificarCnpjDuplicado(String cnpj) {
        String cnpjLimpo = DocumentoValidator.limparDocumento(cnpj);
        if (clientePJRepository.existsByCnpj(cnpjLimpo)) {
            throw new CnpjJaCadastradoException(cnpj);
        }
    }

    private Cliente buscarClienteIndicador(Long clienteIndicadorId) {
        if (clienteIndicadorId == null) {
            return null;
        }

        return clienteRepository.findById(clienteIndicadorId)
                .orElseThrow(() -> new ClienteIndicadorNaoEncontradoException(clienteIndicadorId));
    }
}
