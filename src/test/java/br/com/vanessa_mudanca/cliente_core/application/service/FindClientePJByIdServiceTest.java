package br.com.vanessa_mudanca.cliente_core.application.service;

import java.util.UUID;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para FindClientePJByIdService.
 * Valida o uso de Optional e programação funcional.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FindClientePJByIdService - Testes")
class FindClientePJByIdServiceTest {

    @Mock
    private ClientePJRepositoryPort clientePJRepository;

    @InjectMocks
    private FindClientePJByIdService service;

    private ClientePJ clientePJ;

    @BeforeEach
    void setUp() {
        clientePJ = ClientePJ.builder()
                .id(UUID.randomUUID())
                .razaoSocial("Empresa XYZ Ltda")
                .nomeFantasia("XYZ Transportes")
                .cnpj("12345678000199")
                .inscricaoEstadual("123456789")
                .inscricaoMunicipal("987654321")
                .capitalSocial(new BigDecimal("100000.00"))
                .dataAbertura(LocalDate.of(2020, 1, 15))
                .atividadePrincipal("Transporte de cargas")
                .porteEmpresa("Média")
                .build();
    }

    @Test
    @DisplayName("Deve retornar ClientePJResponse quando cliente existe")
    void deveRetornarClientePJResponse_QuandoClienteExiste() {
        // Given
        when(clientePJRepository.findByPublicId(UUID.randomUUID())).thenReturn(Optional.of(clientePJ));

        // When
        ClientePJResponse response = service.findByPublicId(UUID.randomUUID());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.publicId()).isEqualTo(UUID.randomUUID());
        assertThat(response.razaoSocial()).isEqualTo("Empresa XYZ Ltda");
        assertThat(response.nomeFantasia()).isEqualTo("XYZ Transportes");
        assertThat(response.nomeExibicao()).isEqualTo("XYZ Transportes");
        assertThat(response.cnpj()).isEqualTo("12345678000199");
        assertThat(response.inscricaoEstadual()).isEqualTo("123456789");
        assertThat(response.inscricaoMunicipal()).isEqualTo("987654321");
        assertThat(response.capitalSocial()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(response.dataAbertura()).isEqualTo(LocalDate.of(2020, 1, 15));

        verify(clientePJRepository, times(1)).findByPublicId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Deve lançar ClienteNaoEncontradoException quando cliente não existe")
    void deveLancarClienteNaoEncontradoException_QuandoClienteNaoExiste() {
        // Given
        when(clientePJRepository.findByPublicId(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.findByPublicId(999L))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessageContaining("Cliente com ID '999' não encontrado");

        verify(clientePJRepository, times(1)).findByPublicId(999L);
    }

    @Test
    @DisplayName("Deve retornar nomeExibicao igual a nomeFantasia quando disponível")
    void deveRetornarNomeExibicaoIgualNomeFantasia_QuandoDisponivel() {
        // Given
        when(clientePJRepository.findByPublicId(UUID.randomUUID())).thenReturn(Optional.of(clientePJ));

        // When
        ClientePJResponse response = service.findByPublicId(UUID.randomUUID());

        // Then
        assertThat(response.nomeExibicao()).isEqualTo("XYZ Transportes");
        assertThat(response.nomeExibicao()).isEqualTo(response.nomeFantasia());

        verify(clientePJRepository, times(1)).findByPublicId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Deve retornar nomeExibicao igual a razaoSocial quando nomeFantasia é nulo")
    void deveRetornarNomeExibicaoIgualRazaoSocial_QuandoNomeFantasiaNulo() {
        // Given
        ClientePJ clienteSemNomeFantasia = ClientePJ.builder()
                .id(2L)
                .razaoSocial("Empresa ABC Ltda")
                .nomeFantasia(null)
                .cnpj("98765432000188")
                .inscricaoEstadual("987654321")
                .capitalSocial(new BigDecimal("50000.00"))
                .dataAbertura(LocalDate.of(2019, 5, 20))
                .build();

        when(clientePJRepository.findByPublicId(2L)).thenReturn(Optional.of(clienteSemNomeFantasia));

        // When
        ClientePJResponse response = service.findByPublicId(2L);

        // Then
        assertThat(response.nomeExibicao()).isEqualTo("Empresa ABC Ltda");
        assertThat(response.nomeExibicao()).isEqualTo(response.razaoSocial());

        verify(clientePJRepository, times(1)).findByPublicId(2L);
    }

    @Test
    @DisplayName("Deve retornar capital social com precisão decimal")
    void deveRetornarCapitalSocialComPrecisaoDecimal() {
        // Given
        ClientePJ clienteComCapitalEspecifico = ClientePJ.builder()
                .id(3L)
                .razaoSocial("Empresa DEF Ltda")
                .nomeFantasia("DEF Soluções")
                .cnpj("11122233000144")
                .inscricaoEstadual("111222333")
                .capitalSocial(new BigDecimal("250000.50"))
                .dataAbertura(LocalDate.of(2021, 3, 10))
                .build();

        when(clientePJRepository.findByPublicId(3L)).thenReturn(Optional.of(clienteComCapitalEspecifico));

        // When
        ClientePJResponse response = service.findByPublicId(3L);

        // Then
        assertThat(response.capitalSocial())
                .isEqualByComparingTo(new BigDecimal("250000.50"));

        verify(clientePJRepository, times(1)).findByPublicId(3L);
    }
}
