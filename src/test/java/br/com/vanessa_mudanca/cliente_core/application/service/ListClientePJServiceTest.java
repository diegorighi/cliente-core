package br.com.vanessa_mudanca.cliente_core.application.service;

import java.util.UUID;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ListClientePJService.
 * Valida paginação e uso de programação funcional com Streams.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListClientePJService - Testes")
class ListClientePJServiceTest {

    @Mock
    private ClientePJRepositoryPort clientePJRepository;

    @InjectMocks
    private ListClientePJService service;

    private ClientePJ clientePJ1;
    private ClientePJ clientePJ2;
    private ClientePJ clientePJ3;

    @BeforeEach
    void setUp() {
        clientePJ1 = ClientePJ.builder()
                .id(UUID.randomUUID())
                .razaoSocial("Empresa XYZ Ltda")
                .nomeFantasia("XYZ Transportes")
                .cnpj("11111111000111")
                .inscricaoEstadual("111111111")
                .capitalSocial(new BigDecimal("100000.00"))
                .dataAbertura(LocalDate.of(2020, 1, 15))
                .build();

        clientePJ2 = ClientePJ.builder()
                .id(2L)
                .razaoSocial("Empresa ABC Ltda")
                .nomeFantasia("ABC Logística")
                .cnpj("22222222000122")
                .inscricaoEstadual("222222222")
                .capitalSocial(new BigDecimal("200000.00"))
                .dataAbertura(LocalDate.of(2019, 5, 20))
                .build();

        clientePJ3 = ClientePJ.builder()
                .id(3L)
                .razaoSocial("Empresa DEF Ltda")
                .nomeFantasia(null) // Sem nome fantasia
                .cnpj("33333333000133")
                .inscricaoEstadual("333333333")
                .capitalSocial(new BigDecimal("50000.00"))
                .dataAbertura(LocalDate.of(2021, 3, 10))
                .build();
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há clientes")
    void deveRetornarPaginaVazia_QuandoNaoHaClientes() {
        // Given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

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

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar primeira página com 3 clientes")
    void deveRetornarPrimeiraPagina_Com3Clientes() {
        // Given
        List<ClientePJ> clientes = Arrays.asList(clientePJ1, clientePJ2, clientePJ3);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 3);
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

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
        assertThat(response.content().get(0).razaoSocial()).isEqualTo("Empresa XYZ Ltda");
        assertThat(response.content().get(1).razaoSocial()).isEqualTo("Empresa ABC Ltda");
        assertThat(response.content().get(2).razaoSocial()).isEqualTo("Empresa DEF Ltda");

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página com tamanho customizado (2 elementos)")
    void deveRetornarPagina_ComTamanhoCustomizado() {
        // Given
        List<ClientePJ> clientes = Arrays.asList(clientePJ1, clientePJ2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 3); // Total: 3, mas página retorna 2
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

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

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar segunda página corretamente")
    void deveRetornarSegundaPagina() {
        // Given
        List<ClientePJ> clientes = Collections.singletonList(clientePJ3);
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 3); // Página 2 (index 1)
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

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
        assertThat(response.content().get(0).razaoSocial()).isEqualTo("Empresa DEF Ltda");

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve ordenar por ID em ordem ascendente")
    void deveOrdenarPorId_EmOrdemAscendente() {
        // Given
        List<ClientePJ> clientes = Arrays.asList(clientePJ1, clientePJ2, clientePJ3);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 3);
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(3);
        assertThat(response.content().get(0).publicId()).isEqualTo(UUID.randomUUID());
        assertThat(response.content().get(1).publicId()).isEqualTo(2L);
        assertThat(response.content().get(2).publicId()).isEqualTo(3L);

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve ordenar por ID em ordem descendente")
    void deveOrdenarPorId_EmOrdemDescendente() {
        // Given
        List<ClientePJ> clientes = Arrays.asList(clientePJ3, clientePJ2, clientePJ1);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 3);
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(3);
        assertThat(response.content().get(0).publicId()).isEqualTo(3L);
        assertThat(response.content().get(1).publicId()).isEqualTo(2L);
        assertThat(response.content().get(2).publicId()).isEqualTo(UUID.randomUUID());

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve mapear corretamente entidade para response usando programação funcional")
    void devemapearCorretamenteEntidadeParaResponse() {
        // Given
        List<ClientePJ> clientes = Collections.singletonList(clientePJ1);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 1);
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(1);
        ClientePJResponse clienteResponse = response.content().get(0);

        assertThat(clienteResponse.publicId()).isEqualTo(UUID.randomUUID());
        assertThat(clienteResponse.razaoSocial()).isEqualTo("Empresa XYZ Ltda");
        assertThat(clienteResponse.nomeFantasia()).isEqualTo("XYZ Transportes");
        assertThat(clienteResponse.nomeExibicao()).isEqualTo("XYZ Transportes");
        assertThat(clienteResponse.cnpj()).isEqualTo("11111111000111");
        assertThat(clienteResponse.inscricaoEstadual()).isEqualTo("111111111");
        assertThat(clienteResponse.capitalSocial()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(clienteResponse.dataAbertura()).isEqualTo(LocalDate.of(2020, 1, 15));

        verify(clientePJRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve calcular nomeExibicao corretamente quando nomeFantasia é nulo")
    void deveCalcularNomeExibicao_QuandoNomeFantasiaNulo() {
        // Given
        List<ClientePJ> clientes = Collections.singletonList(clientePJ3);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientePJ> page = new PageImpl<>(clientes, pageable, 1);
        when(clientePJRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PageResponse<ClientePJResponse> response = service.findAll(pageable);

        // Then
        assertThat(response.content()).hasSize(1);
        ClientePJResponse clienteResponse = response.content().get(0);

        assertThat(clienteResponse.nomeFantasia()).isNull();
        assertThat(clienteResponse.nomeExibicao()).isEqualTo("Empresa DEF Ltda");
        assertThat(clienteResponse.nomeExibicao()).isEqualTo(clienteResponse.razaoSocial());

        verify(clientePJRepository, times(1)).findAll(pageable);
    }
}
