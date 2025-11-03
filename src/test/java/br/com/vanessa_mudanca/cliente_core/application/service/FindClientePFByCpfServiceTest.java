package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para FindClientePFByCpfService.
 * Testa busca por CPF com e sem formatação.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FindClientePFByCpfService - Busca por CPF")
class FindClientePFByCpfServiceTest {

    @Mock
    private ClientePFRepositoryPort clientePFRepository;

    @InjectMocks
    private FindClientePFByCpfService service;

    private ClientePF clientePFMock;
    private static final String CPF_FORMATADO = "123.456.789-10";
    private static final String CPF_SEM_FORMATACAO = "12345678910";

    @BeforeEach
    void setUp() {
        clientePFMock = ClientePF.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .primeiroNome("João")
                .sobrenome("Silva")
                .cpf(CPF_FORMATADO)
                .dataNascimento(LocalDate.of(1990, 5, 15))
                .sexo(SexoEnum.MASCULINO)
                .email("joao.silva@email.com")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve encontrar cliente por CPF COM formatação")
    void deveEncontrarClientePorCpfComFormatacao() {
        // Arrange
        when(clientePFRepository.findByCpf(CPF_FORMATADO))
                .thenReturn(Optional.of(clientePFMock));

        // Act
        ClientePFResponse response = service.findByCpf(CPF_FORMATADO);

        // Assert
        assertNotNull(response);
        assertEquals("João", response.primeiroNome());
        assertEquals("Silva", response.sobrenome());
        assertEquals(CPF_FORMATADO, response.cpf());
        verify(clientePFRepository, times(1)).findByCpf(CPF_FORMATADO);
    }

    @Test
    @DisplayName("Deve encontrar cliente por CPF SEM formatação (apenas números)")
    void deveEncontrarClientePorCpfSemFormatacao() {
        // Arrange
        when(clientePFRepository.findByCpf(CPF_FORMATADO))
                .thenReturn(Optional.of(clientePFMock));

        // Act
        ClientePFResponse response = service.findByCpf(CPF_SEM_FORMATACAO);

        // Assert
        assertNotNull(response);
        assertEquals("João", response.primeiroNome());
        assertEquals("Silva", response.sobrenome());
        assertEquals(CPF_FORMATADO, response.cpf());

        // Verifica que o service formatou o CPF antes de buscar
        verify(clientePFRepository, times(1)).findByCpf(CPF_FORMATADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF não for encontrado (com formatação)")
    void deveLancarExcecaoQuandoCpfNaoEncontradoComFormatacao() {
        // Arrange
        when(clientePFRepository.findByCpf(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> service.findByCpf(CPF_FORMATADO)
        );

        assertTrue(exception.getMessage().contains(CPF_FORMATADO));
        verify(clientePFRepository, times(1)).findByCpf(CPF_FORMATADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF não for encontrado (sem formatação)")
    void deveLancarExcecaoQuandoCpfNaoEncontradoSemFormatacao() {
        // Arrange
        when(clientePFRepository.findByCpf(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> service.findByCpf(CPF_SEM_FORMATACAO)
        );

        assertTrue(exception.getMessage().contains(CPF_SEM_FORMATACAO));
        verify(clientePFRepository, times(1)).findByCpf(CPF_FORMATADO);
    }

    @Test
    @DisplayName("Deve formatar CPF de 11 dígitos corretamente")
    void deveFormatarCpfDe11DigitosCorretamente() {
        // Arrange
        String cpfNaoFormatado = "98765432100";
        String cpfEsperadoFormatado = "987.654.321-00";

        ClientePF clienteMock = ClientePF.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .cpf(cpfEsperadoFormatado)
                .dataNascimento(LocalDate.of(1985, 3, 20))
                .sexo(SexoEnum.FEMININO)
                .email("maria.santos@email.com")
                .ativo(true)
                .build();

        when(clientePFRepository.findByCpf(cpfEsperadoFormatado))
                .thenReturn(Optional.of(clienteMock));

        // Act
        ClientePFResponse response = service.findByCpf(cpfNaoFormatado);

        // Assert
        assertNotNull(response);
        assertEquals("Maria", response.primeiroNome());
        assertEquals(cpfEsperadoFormatado, response.cpf());
        verify(clientePFRepository, times(1)).findByCpf(cpfEsperadoFormatado);
    }

    @Test
    @DisplayName("Não deve alterar CPF que já está formatado")
    void naoDeveAlterarCpfJaFormatado() {
        // Arrange
        String cpfJaFormatado = "111.222.333-44";

        when(clientePFRepository.findByCpf(cpfJaFormatado))
                .thenReturn(Optional.of(clientePFMock));

        // Act
        service.findByCpf(cpfJaFormatado);

        // Assert
        // Verifica que buscou exatamente com o CPF fornecido
        verify(clientePFRepository, times(1)).findByCpf(cpfJaFormatado);
    }
}
