package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePFMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para listar Clientes Pessoa Física com paginação.
 * Utiliza programação funcional com Streams.
 *
 * Cache Strategy:
 * - Cache: clientes:list
 * - TTL: 1 minuto (cold cache - muito volátil)
 * - Key: página + tamanho + sort
 * - Evict: CreateClientePFService, UpdateClientePFService, DeleteClienteService
 */
@Service
public class ListClientePFService implements ListClientePFUseCase {

    private final ClientePFRepositoryPort clientePFRepository;

    public ListClientePFService(ClientePFRepositoryPort clientePFRepository) {
        this.clientePFRepository = clientePFRepository;
    }

    @Override
    @Cacheable(
        value = "clientes:list",
        key = "'pf-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()"
    )
    @Transactional(readOnly = true)
    public PageResponse<ClientePFResponse> findAll(Pageable pageable) {
        return PageResponse.of(
                clientePFRepository.findAll(pageable)
                        .map(ClientePFMapper::toResponse)
        );
    }
}
