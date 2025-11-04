package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.mapper.ClientePJMapper;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para listar Clientes Pessoa Jurídica com paginação.
 * Utiliza programação funcional com Streams.
 *
 * Cache Strategy:
 * - Cache: clientes:list
 * - TTL: 1 minuto (cold cache - muito volátil)
 * - Key: página + tamanho + sort
 * - Evict: CreateClientePJService, UpdateClientePJService, DeleteClienteService
 */
@Service
public class ListClientePJService implements ListClientePJUseCase {

    private final ClientePJRepositoryPort clientePJRepository;

    public ListClientePJService(ClientePJRepositoryPort clientePJRepository) {
        this.clientePJRepository = clientePJRepository;
    }

    @Override
    @Cacheable(
        value = "clientes:list",
        key = "'pj-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()"
    )
    @Transactional(readOnly = true)
    public PageResponse<ClientePJResponse> findAll(Pageable pageable) {
        return PageResponse.of(
                clientePJRepository.findAll(pageable)
                        .map(ClientePJMapper::toResponse)
        );
    }
}
