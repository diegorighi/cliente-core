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
import br.com.vanessa_mudanca.cliente_core.infrastructure.util.MaskingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para criação de Cliente Pessoa Física.
 * Implementa o Use Case de criação com todas as validações necessárias.
 */
@Service
public class CreateClientePFService implements CreateClientePFUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateClientePFService.class);

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
        // Adiciona contexto ao MDC para rastreamento
        MDC.put("operationType", "CREATE_CLIENTE_PF");

        try {
            log.info("Iniciando criação de cliente PF - CPF: {}, Email: {}",
                    MaskingUtil.maskCpf(request.cpf()),
                    MaskingUtil.maskEmail(request.email()));

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

            // Adiciona clientId ao MDC para logs subsequentes
            MDC.put("clientId", clienteSalvo.getPublicId().toString());

            log.info("Cliente PF criado com sucesso - PublicId: {}, CPF: {}",
                    clienteSalvo.getPublicId(),
                    MaskingUtil.maskCpf(clienteSalvo.getCpf()));

            // 6. Converter Entity para Response
            return ClientePFMapper.toResponse(clienteSalvo);

        } catch (CpfInvalidoException | CpfJaCadastradoException e) {
            log.warn("Falha na validação do CPF - CPF: {}, Erro: {}",
                    MaskingUtil.maskCpf(request.cpf()),
                    e.getMessage());
            throw e;
        } catch (ClienteIndicadorNaoEncontradoException e) {
            log.warn("Cliente indicador não encontrado - IndicadorId: {}, Erro: {}",
                    request.clienteIndicadorId(),
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao criar cliente PF - CPF: {}, Erro: {}",
                    MaskingUtil.maskCpf(request.cpf()),
                    e.getMessage(),
                    e);
            throw e;
        } finally {
            // Remove contexto do MDC (cleanup)
            MDC.remove("operationType");
            MDC.remove("clientId");
        }
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
