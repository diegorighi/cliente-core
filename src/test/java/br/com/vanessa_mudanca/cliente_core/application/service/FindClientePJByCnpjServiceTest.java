package br.com.vanessa_mudanca.cliente_core.application.service;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para FindClientePJByCnpjService.
 * Testa busca por CNPJ com e sem formatação.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FindClientePJByCnpjService - Busca por CNPJ")
class FindClientePJByCnpjServiceTest {

    @Mock
    private ClientePJRepositoryPort clientePJRepository;

    @InjectMocks
    private FindClientePJByCnpjService service;

    private ClientePJ clientePJMock;
    private static final String CNPJ_FORMATADO = "12.345.678/0001-90";
    private static final String CNPJ_SEM_FORMATACAO = "12345678000190";

    @BeforeEach
    void setUp() {
        clientePJMock = ClientePJ.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .razaoSocial("EMPRESA TESTE LTDA")
                .nomeFantasia("Empresa Teste")
                .cnpj(CNPJ_FORMATADO)
                .inscricaoEstadual("123456789")
                .dataAbertura(LocalDate.of(2020, 1, 15))
                .porteEmpresa("EPP")
                .capitalSocial(new BigDecimal("100000.00"))
                .email("contato@empresateste.com.br")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve encontrar cliente por CNPJ COM formatação")
    void deveEncontrarClientePorCnpjComFormatacao() {
        // Arrange
        when(clientePJRepository.findByCnpj(CNPJ_FORMATADO))
                .thenReturn(Optional.of(clientePJMock));

        // Act
        ClientePJResponse response = service.findByCnpj(CNPJ_FORMATADO);

        // Assert
        assertNotNull(response);
        assertEquals("EMPRESA TESTE LTDA", response.razaoSocial());
        assertEquals("Empresa Teste", response.nomeFantasia());
        assertEquals(CNPJ_FORMATADO, response.cnpj());
        verify(clientePJRepository, times(1)).findByCnpj(CNPJ_FORMATADO);
    }

    @Test
    @DisplayName("Deve encontrar cliente por CNPJ SEM formatação (apenas números)")
    void deveEncontrarClientePorCnpjSemFormatacao() {
        // Arrange
        when(clientePJRepository.findByCnpj(CNPJ_FORMATADO))
                .thenReturn(Optional.of(clientePJMock));

        // Act
        ClientePJResponse response = service.findByCnpj(CNPJ_SEM_FORMATACAO);

        // Assert
        assertNotNull(response);
        assertEquals("EMPRESA TESTE LTDA", response.razaoSocial());
        assertEquals("Empresa Teste", response.nomeFantasia());
        assertEquals(CNPJ_FORMATADO, response.cnpj());

        // Verifica que o service formatou o CNPJ antes de buscar
        verify(clientePJRepository, times(1)).findByCnpj(CNPJ_FORMATADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ não for encontrado (com formatação)")
    void deveLancarExcecaoQuandoCnpjNaoEncontradoComFormatacao() {
        // Arrange
        when(clientePJRepository.findByCnpj(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> service.findByCnpj(CNPJ_FORMATADO)
        );

        assertTrue(exception.getMessage().contains(CNPJ_FORMATADO));
        verify(clientePJRepository, times(1)).findByCnpj(CNPJ_FORMATADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ não for encontrado (sem formatação)")
    void deveLancarExcecaoQuandoCnpjNaoEncontradoSemFormatacao() {
        // Arrange
        when(clientePJRepository.findByCnpj(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> service.findByCnpj(CNPJ_SEM_FORMATACAO)
        );

        assertTrue(exception.getMessage().contains(CNPJ_SEM_FORMATACAO));
        verify(clientePJRepository, times(1)).findByCnpj(CNPJ_FORMATADO);
    }

    @Test
    @DisplayName("Deve formatar CNPJ de 14 dígitos corretamente")
    void deveFormatarCnpjDe14DigitosCorretamente() {
        // Arrange
        String cnpjNaoFormatado = "98765432000199";
        String cnpjEsperadoFormatado = "98.765.432/0001-99";

        ClientePJ clienteMock = ClientePJ.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .razaoSocial("OUTRA EMPRESA LTDA")
                .nomeFantasia("Outra Empresa")
                .cnpj(cnpjEsperadoFormatado)
                .inscricaoEstadual("987654321")
                .dataAbertura(LocalDate.of(2019, 5, 10))
                .porteEmpresa("ME")
                .capitalSocial(new BigDecimal("50000.00"))
                .email("contato@outraempresa.com.br")
                .ativo(true)
                .build();

        when(clientePJRepository.findByCnpj(cnpjEsperadoFormatado))
                .thenReturn(Optional.of(clienteMock));

        // Act
        ClientePJResponse response = service.findByCnpj(cnpjNaoFormatado);

        // Assert
        assertNotNull(response);
        assertEquals("OUTRA EMPRESA LTDA", response.razaoSocial());
        assertEquals(cnpjEsperadoFormatado, response.cnpj());
        verify(clientePJRepository, times(1)).findByCnpj(cnpjEsperadoFormatado);
    }

    @Test
    @DisplayName("Não deve alterar CNPJ que já está formatado")
    void naoDeveAlterarCnpjJaFormatado() {
        // Arrange
        String cnpjJaFormatado = "11.222.333/0001-44";

        when(clientePJRepository.findByCnpj(cnpjJaFormatado))
                .thenReturn(Optional.of(clientePJMock));

        // Act
        service.findByCnpj(cnpjJaFormatado);

        // Assert
        // Verifica que buscou exatamente com o CNPJ fornecido
        verify(clientePJRepository, times(1)).findByCnpj(cnpjJaFormatado);
    }

    @Test
    @DisplayName("Deve formatar CNPJ com zeros à esquerda corretamente")
    void deveFormatarCnpjComZerosAEsquerdaCorretamente() {
        // Arrange
        String cnpjNaoFormatado = "00123456000190";
        String cnpjEsperadoFormatado = "00.123.456/0001-90";

        ClientePJ clienteMock = ClientePJ.builder()
                .id(3L)
                .publicId(UUID.randomUUID())
                .razaoSocial("EMPRESA COM ZEROS LTDA")
                .nomeFantasia("Empresa Zeros")
                .cnpj(cnpjEsperadoFormatado)
                .inscricaoEstadual("111222333")
                .dataAbertura(LocalDate.of(2021, 3, 25))
                .porteEmpresa("EPP")
                .capitalSocial(new BigDecimal("75000.00"))
                .email("contato@empresazeros.com.br")
                .ativo(true)
                .build();

        when(clientePJRepository.findByCnpj(cnpjEsperadoFormatado))
                .thenReturn(Optional.of(clienteMock));

        // Act
        ClientePJResponse response = service.findByCnpj(cnpjNaoFormatado);

        // Assert
        assertNotNull(response);
        assertEquals("EMPRESA COM ZEROS LTDA", response.razaoSocial());
        assertEquals(cnpjEsperadoFormatado, response.cnpj());
        verify(clientePJRepository, times(1)).findByCnpj(cnpjEsperadoFormatado);
    }
}
