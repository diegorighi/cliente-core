package br.com.vanessa_mudanca.cliente_core.application.service;

import java.util.UUID;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteIndicadorNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CpfInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CpfJaCadastradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CreateClientePFService.
 * Valida todos os cenários de criação de cliente PF.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateClientePFService - Testes de criação de cliente PF")
class CreateClientePFServiceTest {

    @Mock
    private ClientePFRepositoryPort clientePFRepository;

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private CreateClientePFService service;

    private CreateClientePFRequest requestValido;
    private ClientePF clientePFSalvo;

    @BeforeEach
    void setUp() {
        requestValido = new CreateClientePFRequest(
                "João",
                "da",
                "Silva",
                "123.456.789-09",
                "MG-12.345.678",
                LocalDate.of(1990, 1, 15),
                SexoEnum.MASCULINO,
                "joao.silva@email.com",
                "Maria da Silva",
                "José da Silva",
                "Casado",
                "Engenheiro",
                "Brasileira",
                "Belo Horizonte",
                TipoClienteEnum.COMPRADOR,
                null,
                null,
                null,
                null,
                null,
                "Cliente preferencial"
        );

        clientePFSalvo = ClientePF.builder()
                .id(UUID.randomUUID())
                .primeiroNome("João")
                .nomeDoMeio("da")
                .sobrenome("Silva")
                .cpf("12345678909")
                .rg("MG-12.345.678")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .sexo(SexoEnum.MASCULINO)
                .email("joao.silva@email.com")
                .tipoCliente(TipoClienteEnum.COMPRADOR)
                .ativo(true)
                .bloqueado(false)
                .build();
    }

    // ========== CENÁRIOS DE SUCESSO ==========

    @Test
    @DisplayName("Deve criar cliente PF com sucesso quando todos os dados são válidos")
    void deveCriarClientePFComSucesso() {
        // Arrange
        when(clientePFRepository.existsByCpf(anyString())).thenReturn(false);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clientePFSalvo);

        // Act
        ClientePFResponse response = service.criar(requestValido);

        // Assert
        assertNotNull(response);
        assertEquals(UUID.randomUUID(), response.publicId());
        assertEquals("João", response.primeiroNome());
        assertEquals("Silva", response.sobrenome());
        assertEquals("João da Silva", response.nomeCompleto());
        assertEquals("12345678909", response.cpf());
        assertEquals("joao.silva@email.com", response.email());
        assertTrue(response.ativo());
        assertFalse(response.bloqueado());

        verify(clientePFRepository, times(1)).existsByCpf("12345678909");
        verify(clientePFRepository, times(1)).save(any(ClientePF.class));
        verify(clienteRepository, never()).findByPublicId(any());
    }

    @Test
    @DisplayName("Deve criar cliente PF com CPF formatado (removendo pontos e traços)")
    void deveCriarClienteComCpfFormatado() {
        // Arrange
        when(clientePFRepository.existsByCpf("12345678909")).thenReturn(false);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clientePFSalvo);

        // Act
        service.criar(requestValido);

        // Assert
        verify(clientePFRepository).existsByCpf("12345678909"); // CPF limpo
    }

    @Test
    @DisplayName("Deve criar cliente PF com cliente indicador quando ID é fornecido")
    void deveCriarClienteComIndicador() {
        // Arrange
        Long clienteIndicadorId = 99L;
        Cliente clienteIndicador = ClientePF.builder()
                .id(clienteIndicadorId)
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .build();

        CreateClientePFRequest requestComIndicador = new CreateClientePFRequest(
                requestValido.primeiroNome(),
                requestValido.nomeDoMeio(),
                requestValido.sobrenome(),
                requestValido.cpf(),
                requestValido.rg(),
                requestValido.dataNascimento(),
                requestValido.sexo(),
                requestValido.email(),
                requestValido.nomeMae(),
                requestValido.nomePai(),
                requestValido.estadoCivil(),
                requestValido.profissao(),
                requestValido.nacionalidade(),
                requestValido.naturalidade(),
                requestValido.tipoCliente(),
                requestValido.origemLead(),
                requestValido.utmSource(),
                requestValido.utmCampaign(),
                requestValido.utmMedium(),
                clienteIndicadorId,
                requestValido.observacoes()
        );

        when(clientePFRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByPublicId(clienteIndicadorId)).thenReturn(Optional.of(clienteIndicador));
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clientePFSalvo);

        // Act
        ClientePFResponse response = service.criar(requestComIndicador);

        // Assert
        assertNotNull(response);
        verify(clienteRepository, times(1)).findByPublicId(clienteIndicadorId);
    }

    @Test
    @DisplayName("Deve criar cliente PF com dados mínimos obrigatórios")
    void deveCriarClienteComDadosMinimos() {
        // Arrange
        CreateClientePFRequest requestMinimo = new CreateClientePFRequest(
                "João",
                null,
                "Silva",
                "123.456.789-09",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TipoClienteEnum.PROSPECTO,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(clientePFRepository.existsByCpf(anyString())).thenReturn(false);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clientePFSalvo);

        // Act
        ClientePFResponse response = service.criar(requestMinimo);

        // Assert
        assertNotNull(response);
        verify(clientePFRepository, times(1)).save(any(ClientePF.class));
    }

    // ========== CENÁRIOS DE ERRO - CPF INVÁLIDO ==========

    @Test
    @DisplayName("Deve lançar exceção quando CPF é inválido (dígito verificador errado)")
    void deveLancarExcecaoQuandoCpfInvalido() {
        // Arrange
        CreateClientePFRequest requestCpfInvalido = new CreateClientePFRequest(
                "João", null, "Silva",
                "123.456.789-00", // CPF inválido
                null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.COMPRADOR,
                null, null, null, null, null, null
        );

        // Act & Assert
        CpfInvalidoException exception = assertThrows(
                CpfInvalidoException.class,
                () -> service.criar(requestCpfInvalido)
        );

        assertTrue(exception.getMessage().contains("123.456.789-00"));
        assertTrue(exception.getMessage().contains("inválido"));
        verify(clientePFRepository, never()).existsByCpf(any());
        verify(clientePFRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF tem todos os dígitos iguais")
    void deveLancarExcecaoQuandoCpfTemTodosDigitosIguais() {
        // Arrange
        CreateClientePFRequest requestCpfInvalido = new CreateClientePFRequest(
                "João", null, "Silva",
                "111.111.111-11",
                null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.COMPRADOR,
                null, null, null, null, null, null
        );

        // Act & Assert
        assertThrows(CpfInvalidoException.class, () -> service.criar(requestCpfInvalido));
        verify(clientePFRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE ERRO - CPF DUPLICADO ==========

    @Test
    @DisplayName("Deve lançar exceção quando CPF já está cadastrado")
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        // Arrange
        when(clientePFRepository.existsByCpf("12345678909")).thenReturn(true);

        // Act & Assert
        CpfJaCadastradoException exception = assertThrows(
                CpfJaCadastradoException.class,
                () -> service.criar(requestValido)
        );

        assertTrue(exception.getMessage().contains("123.456.789-09"));
        assertTrue(exception.getMessage().contains("já está cadastrado"));
        verify(clientePFRepository, times(1)).existsByCpf("12345678909");
        verify(clientePFRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE ERRO - CLIENTE INDICADOR ==========

    @Test
    @DisplayName("Deve lançar exceção quando cliente indicador não existe")
    void deveLancarExcecaoQuandoClienteIndicadorNaoExiste() {
        // Arrange
        Long clienteIndicadorIdInexistente = 999L;
        CreateClientePFRequest requestComIndicadorInexistente = new CreateClientePFRequest(
                requestValido.primeiroNome(),
                requestValido.nomeDoMeio(),
                requestValido.sobrenome(),
                requestValido.cpf(),
                requestValido.rg(),
                requestValido.dataNascimento(),
                requestValido.sexo(),
                requestValido.email(),
                requestValido.nomeMae(),
                requestValido.nomePai(),
                requestValido.estadoCivil(),
                requestValido.profissao(),
                requestValido.nacionalidade(),
                requestValido.naturalidade(),
                requestValido.tipoCliente(),
                requestValido.origemLead(),
                requestValido.utmSource(),
                requestValido.utmCampaign(),
                requestValido.utmMedium(),
                clienteIndicadorIdInexistente,
                requestValido.observacoes()
        );

        when(clientePFRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.findByPublicId(clienteIndicadorIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ClienteIndicadorNaoEncontradoException exception = assertThrows(
                ClienteIndicadorNaoEncontradoException.class,
                () -> service.criar(requestComIndicadorInexistente)
        );

        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("não encontrado"));
        verify(clienteRepository, times(1)).findByPublicId(clienteIndicadorIdInexistente);
        verify(clientePFRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE VALIDAÇÃO DE FLUXO ==========

    @Test
    @DisplayName("Deve validar CPF antes de verificar duplicação")
    void deveValidarCpfAntesDeVerificarDuplicacao() {
        // Arrange
        CreateClientePFRequest requestCpfInvalido = new CreateClientePFRequest(
                "João", null, "Silva",
                "111.111.111-11", // CPF inválido
                null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.COMPRADOR,
                null, null, null, null, null, null
        );

        // Act & Assert
        assertThrows(CpfInvalidoException.class, () -> service.criar(requestCpfInvalido));

        // Verifica que não chegou a consultar o repositório
        verify(clientePFRepository, never()).existsByCpf(any());
    }

    @Test
    @DisplayName("Deve verificar duplicação antes de buscar cliente indicador")
    void deveVerificarDuplicacaoAntesIndicador() {
        // Arrange
        when(clientePFRepository.existsByCpf("12345678909")).thenReturn(true);

        CreateClientePFRequest requestComIndicador = new CreateClientePFRequest(
                requestValido.primeiroNome(),
                requestValido.nomeDoMeio(),
                requestValido.sobrenome(),
                requestValido.cpf(),
                requestValido.rg(),
                requestValido.dataNascimento(),
                requestValido.sexo(),
                requestValido.email(),
                requestValido.nomeMae(),
                requestValido.nomePai(),
                requestValido.estadoCivil(),
                requestValido.profissao(),
                requestValido.nacionalidade(),
                requestValido.naturalidade(),
                requestValido.tipoCliente(),
                requestValido.origemLead(),
                requestValido.utmSource(),
                requestValido.utmCampaign(),
                requestValido.utmMedium(),
                99L,
                requestValido.observacoes()
        );

        // Act & Assert
        assertThrows(CpfJaCadastradoException.class, () -> service.criar(requestComIndicador));

        // Verifica que não chegou a buscar indicador
        verify(clienteRepository, never()).findByPublicId(any());
    }
}
