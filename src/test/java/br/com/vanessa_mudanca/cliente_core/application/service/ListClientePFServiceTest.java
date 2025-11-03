package br.com.vanessa_mudanca.cliente_core.application.service;

import java.util.UUID;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ListClientePFService.
 * Valida paginação e uso de programação funcional com Streams.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListClientePFService - Testes")
class ListClientePFServiceTest {

    @Mock
    private ClientePFRepositoryPort clientePFRepository;

    @InjectMocks
    private ListClientePFService service;

    private ClientePF clientePF1;
    private ClientePF clientePF2;
    private ClientePF clientePF3;

    @BeforeEach
    void setUp() {
        clientePF1 = ClientePF.builder()
                .id(UUID.randomUUID())
                .primeiroNome("João")
                .nomeDoMeio("da")
                .sobrenome("Silva")
                .cpf("11111111111")
                .dataNascimento(LocalDate.of(1990, 5, 15))
                .sexo(SexoEnum.MASCULINO)
                .build();

        clientePF2 = ClientePF.builder()
                .id(2L)
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .cpf("22222222222")
                .dataNascimento(LocalDate.of(1985, 3, 20))
                .sexo(SexoEnum.FEMININO)
                .build();

        clientePF3 = ClientePF.builder()
                .id(3L)
                .primeiroNome("José")
                .sobrenome("Oliveira")
                .cpf("33333333333")
                .dataNascimento(LocalDate.of(1995, 8, 10))
                .sexo(SexoEnum.MASCULINO)
                .build();
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há clientes")
    void deveRetornarPaginaVazia_QuandoNaoHaClientes() {
        // Given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePF> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.pageNumber()).isZero();
        assertThat(response.pageSize()).isEqualTo(20);
        assertThat(response.empty()).isTrue();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();

        verify(clientePFRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar primeira página com 3 clientes")
    void deveRetornarPrimeiraPagina_Com3Clientes() {
        // Given
        List<ClientePF> clientes = Arrays.asList(clientePF1, clientePF2, clientePF3);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePF> page = new PageImpl<>(clientes, pageable, 3);
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(3);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.pageNumber()).isZero();
        assertThat(response.pageSize()).isEqualTo(20);
        assertThat(response.empty()).isFalse();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();

        // Valida conteúdo
        assertThat(response.content().get(0).primeiroNome()).isEqualTo("João");
        assertThat(response.content().get(1).primeiroNome()).isEqualTo("Maria");
        assertThat(response.content().get(2).primeiroNome()).isEqualTo("José");

        verify(clientePFRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página com tamanho customizado (2 elementos)")
    void deveRetornarPagina_ComTamanhoCustomizado() {
        // Given
        List<ClientePF> clientes = Arrays.asList(clientePF1, clientePF2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePF> page = new PageImpl<>(clientes, pageable, 3); // Total: 3, mas página retorna 2
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2); // 3 elementos / 2 por página = 2 páginas
        assertThat(response.pageNumber()).isZero();
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.empty()).isFalse();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isFalse(); // Tem próxima página

        verify(clientePFRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar segunda página corretamente")
    void deveRetornarSegundaPagina() {
        // Given
        List<ClientePF> clientes = Collections.singletonList(clientePF3);
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePF> page = new PageImpl<>(clientes, pageable, 3); // Página 2 (index 1)
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.pageNumber()).isEqualTo(1); // Segunda página (zero-indexed)
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.empty()).isFalse();
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isTrue();

        // Valida que é o terceiro cliente
        assertThat(response.content().get(0).primeiroNome()).isEqualTo("José");

        verify(clientePFRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve ordenar por ID em ordem ascendente")
    void deveOrdenarPorId_EmOrdemAscendente() {
        // Given
        List<ClientePF> clientes = Arrays.asList(clientePF1, clientePF2, clientePF3);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePF> page = new PageImpl<>(clientes, pageable, 3);
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(3);
        assertThat(response.content().get(0).publicId()).isEqualTo(UUID.randomUUID());
        assertThat(response.content().get(1).publicId()).isEqualTo(2L);
        assertThat(response.content().get(2).publicId()).isEqualTo(3L);

        verify(clientePFRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve ordenar por ID em ordem descendente")
    void deveOrdenarPorId_EmOrdemDescendente() {
        // Given
        List<ClientePF> clientes = Arrays.asList(clientePF3, clientePF2, clientePF1);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        Page<ClientePF> page = new PageImpl<>(clientes, pageable, 3);
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(3);
        assertThat(response.content().get(0).publicId()).isEqualTo(3L);
        assertThat(response.content().get(1).publicId()).isEqualTo(2L);
        assertThat(response.content().get(2).publicId()).isEqualTo(UUID.randomUUID());

        verify(clientePFRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve mapear corretamente entidade para response usando programação funcional")
    void devemapearCorretamenteEntidadeParaResponse() {
        // Given
        List<ClientePF> clientes = Collections.singletonList(clientePF1);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePF> page = new PageImpl<>(clientes, pageable, 1);
        when(clientePFRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePFResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(1);
        ClientePFResponse clienteResponse = response.content().get(0);

        assertThat(clienteResponse.publicId()).isEqualTo(UUID.randomUUID());
        assertThat(clienteResponse.primeiroNome()).isEqualTo("João");
        assertThat(clienteResponse.sobrenome()).isEqualTo("Silva");
        assertThat(clienteResponse.nomeCompleto()).isEqualTo("João da Silva");
        assertThat(clienteResponse.cpf()).isEqualTo("11111111111");
        assertThat(clienteResponse.dataNascimento()).isEqualTo(LocalDate.of(1990, 5, 15));

        verify(clientePFRepository, times(1)).findAll(pageable);
    }
}
