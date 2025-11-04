package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePFMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.infrastructure.util.MaskingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para buscar Cliente Pessoa Física por Public ID (UUID).
 * Utiliza Optional e programação funcional.
 */
@Service
public class FindClientePFByIdService implements FindClientePFByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindClientePFByIdService.class);

    private final ClientePFRepositoryPort clientePFRepository;

    public FindClientePFByIdService(ClientePFRepositoryPort clientePFRepository) {
        this.clientePFRepository = clientePFRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientePFResponse findByPublicId(UUID publicId) {
        MDC.put("operationType", "FIND_CLIENTE_PF_BY_ID");
        MDC.put("clientId", publicId.toString());

        try {
            log.debug("Buscando cliente PF por PublicId: {}", publicId);

            return clientePFRepository.findByPublicId(publicId)
                    .map(clientePF -> {
                        log.info("Cliente PF encontrado - PublicId: {}, CPF: {}",
                                publicId,
                                MaskingUtil.maskCpf(clientePF.getCpf()));
                        return ClientePFMapper.toResponse(clientePF);
                    })
                    .orElseThrow(() -> {
                        log.warn("Cliente PF não encontrado - PublicId: {}", publicId);
                        return new ClienteNaoEncontradoException(publicId);
                    });

        } finally {
            MDC.remove("operationType");
            MDC.remove("clientId");
        }
    }
}
