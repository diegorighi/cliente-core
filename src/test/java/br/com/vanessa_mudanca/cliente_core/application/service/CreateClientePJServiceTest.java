package br.com.vanessa_mudanca.cliente_core.application.service;

import java.util.UUID;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteIndicadorNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjJaCadastradoException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CreateClientePJService.
 * Valida todos os cenários de criação de cliente PJ.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateClientePJService - Testes de criação de cliente PJ")
class CreateClientePJServiceTest {

    @Mock
    private ClientePJRepositoryPort clientePJRepository;

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private CreateClientePJService service;

    private CreateClientePJRequest requestValido;
    private ClientePJ clientePJSalvo;

    @BeforeEach
    void setUp() {
        requestValido = new CreateClientePJRequest(
                "Empresa XYZ Ltda",
                "XYZ Comércio",
                "11.222.333/0001-81",
                "123456789",
                "987654321",
                LocalDate.of(2010, 5, 20),
                "Pequeno Porte",
                "Sociedade Limitada",
                "Comércio Varejista",
                new BigDecimal("100000.00"),
                "João Silva",
                "123.456.789-09",
                "Diretor",
                "www.empresaxyz.com.br",
                "contato@empresaxyz.com.br",
                TipoClienteEnum.CONSIGNANTE,
                null,
                null,
                null,
                null,
                null,
                "Cliente corporativo"
        );

        clientePJSalvo = ClientePJ.builder()
                .id(UUID.randomUUID())
                .razaoSocial("Empresa XYZ Ltda")
                .nomeFantasia("XYZ Comércio")
                .cnpj("11222333000181")
                .email("contato@empresaxyz.com.br")
                .tipoCliente(TipoClienteEnum.CONSIGNANTE)
                .ativo(true)
                .bloqueado(false)
                .build();
    }

    // ========== CENÁRIOS DE SUCESSO ==========

    @Test
    @DisplayName("Deve criar cliente PJ com sucesso quando todos os dados são válidos")
    void deveCriarClientePJComSucesso() {
        // Arrange
        when(clientePJRepository.existsByCnpj(anyString())).thenReturn(false);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clientePJSalvo);

        // Act
        ClientePJResponse response = service.criar(requestValido);

        // Assert
        assertNotNull(response);
        assertEquals(UUID.randomUUID(), response.publicId());
        assertEquals("Empresa XYZ Ltda", response.razaoSocial());
        assertEquals("XYZ Comércio", response.nomeFantasia());
        assertEquals("XYZ Comércio", response.nomeExibicao());
        assertEquals("11222333000181", response.cnpj());
        assertEquals("contato@empresaxyz.com.br", response.email());
        assertTrue(response.ativo());
        assertFalse(response.bloqueado());

        verify(clientePJRepository, times(1)).existsByCnpj("11222333000181");
        verify(clientePJRepository, times(1)).save(any(ClientePJ.class));
        verify(clienteRepository, never()).findByPublicId(any());
    }

    @Test
    @DisplayName("Deve criar cliente PJ com CNPJ formatado (removendo pontos, barras e traços)")
    void deveCriarClienteComCnpjFormatado() {
        // Arrange
        when(clientePJRepository.existsByCnpj("11222333000181")).thenReturn(false);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clientePJSalvo);

        // Act
        service.criar(requestValido);

        // Assert
        verify(clientePJRepository).existsByCnpj("11222333000181"); // CNPJ limpo
    }

    @Test
    @DisplayName("Deve criar cliente PJ com cliente indicador quando ID é fornecido")
    void deveCriarClienteComIndicador() {
        // Arrange
        Long clienteIndicadorId = 99L;
        Cliente clienteIndicador = ClientePF.builder()
                .id(clienteIndicadorId)
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .build();

        CreateClientePJRequest requestComIndicador = new CreateClientePJRequest(
                requestValido.razaoSocial(),
                requestValido.nomeFantasia(),
                requestValido.cnpj(),
                requestValido.inscricaoEstadual(),
                requestValido.inscricaoMunicipal(),
                requestValido.dataAbertura(),
                requestValido.porteEmpresa(),
                requestValido.naturezaJuridica(),
                requestValido.atividadePrincipal(),
                requestValido.capitalSocial(),
                requestValido.nomeResponsavel(),
                requestValido.cpfResponsavel(),
                requestValido.cargoResponsavel(),
                requestValido.site(),
                requestValido.email(),
                requestValido.tipoCliente(),
                requestValido.origemLead(),
                requestValido.utmSource(),
                requestValido.utmCampaign(),
                requestValido.utmMedium(),
                clienteIndicadorId,
                requestValido.observacoes()
        );

        when(clientePJRepository.existsByCnpj(anyString())).thenReturn(false);
        when(clienteRepository.findByPublicId(clienteIndicadorId)).thenReturn(Optional.of(clienteIndicador));
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clientePJSalvo);

        // Act
        ClientePJResponse response = service.criar(requestComIndicador);

        // Assert
        assertNotNull(response);
        verify(clienteRepository, times(1)).findByPublicId(clienteIndicadorId);
    }

    @Test
    @DisplayName("Deve criar cliente PJ com dados mínimos obrigatórios")
    void deveCriarClienteComDadosMinimos() {
        // Arrange
        CreateClientePJRequest requestMinimo = new CreateClientePJRequest(
                "Empresa ABC Ltda",
                null,
                "11.222.333/0001-81",
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

        when(clientePJRepository.existsByCnpj(anyString())).thenReturn(false);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clientePJSalvo);

        // Act
        ClientePJResponse response = service.criar(requestMinimo);

        // Assert
        assertNotNull(response);
        verify(clientePJRepository, times(1)).save(any(ClientePJ.class));
    }

    // ========== CENÁRIOS DE ERRO - CNPJ INVÁLIDO ==========

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ é inválido (dígito verificador errado)")
    void deveLancarExcecaoQuandoCnpjInvalido() {
        // Arrange
        CreateClientePJRequest requestCnpjInvalido = new CreateClientePJRequest(
                "Empresa XYZ Ltda", null,
                "11.222.333/0001-00", // CNPJ inválido
                null, null, null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.CONSIGNANTE,
                null, null, null, null, null, null
        );

        // Act & Assert
        CnpjInvalidoException exception = assertThrows(
                CnpjInvalidoException.class,
                () -> service.criar(requestCnpjInvalido)
        );

        assertTrue(exception.getMessage().contains("11.222.333/0001-00"));
        assertTrue(exception.getMessage().contains("inválido"));
        verify(clientePJRepository, never()).existsByCnpj(any());
        verify(clientePJRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ tem todos os dígitos iguais")
    void deveLancarExcecaoQuandoCnpjTemTodosDigitosIguais() {
        // Arrange
        CreateClientePJRequest requestCnpjInvalido = new CreateClientePJRequest(
                "Empresa XYZ Ltda", null,
                "11.111.111/1111-11",
                null, null, null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.CONSIGNANTE,
                null, null, null, null, null, null
        );

        // Act & Assert
        assertThrows(CnpjInvalidoException.class, () -> service.criar(requestCnpjInvalido));
        verify(clientePJRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE ERRO - CNPJ DUPLICADO ==========

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ já está cadastrado")
    void deveLancarExcecaoQuandoCnpjJaCadastrado() {
        // Arrange
        when(clientePJRepository.existsByCnpj("11222333000181")).thenReturn(true);

        // Act & Assert
        CnpjJaCadastradoException exception = assertThrows(
                CnpjJaCadastradoException.class,
                () -> service.criar(requestValido)
        );

        assertTrue(exception.getMessage().contains("11.222.333/0001-81"));
        assertTrue(exception.getMessage().contains("já está cadastrado"));
        verify(clientePJRepository, times(1)).existsByCnpj("11222333000181");
        verify(clientePJRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE ERRO - CLIENTE INDICADOR ==========

    @Test
    @DisplayName("Deve lançar exceção quando cliente indicador não existe")
    void deveLancarExcecaoQuandoClienteIndicadorNaoExiste() {
        // Arrange
        Long clienteIndicadorIdInexistente = 999L;
        CreateClientePJRequest requestComIndicadorInexistente = new CreateClientePJRequest(
                requestValido.razaoSocial(),
                requestValido.nomeFantasia(),
                requestValido.cnpj(),
                requestValido.inscricaoEstadual(),
                requestValido.inscricaoMunicipal(),
                requestValido.dataAbertura(),
                requestValido.porteEmpresa(),
                requestValido.naturezaJuridica(),
                requestValido.atividadePrincipal(),
                requestValido.capitalSocial(),
                requestValido.nomeResponsavel(),
                requestValido.cpfResponsavel(),
                requestValido.cargoResponsavel(),
                requestValido.site(),
                requestValido.email(),
                requestValido.tipoCliente(),
                requestValido.origemLead(),
                requestValido.utmSource(),
                requestValido.utmCampaign(),
                requestValido.utmMedium(),
                clienteIndicadorIdInexistente,
                requestValido.observacoes()
        );

        when(clientePJRepository.existsByCnpj(anyString())).thenReturn(false);
        when(clienteRepository.findByPublicId(clienteIndicadorIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ClienteIndicadorNaoEncontradoException exception = assertThrows(
                ClienteIndicadorNaoEncontradoException.class,
                () -> service.criar(requestComIndicadorInexistente)
        );

        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("não encontrado"));
        verify(clienteRepository, times(1)).findByPublicId(clienteIndicadorIdInexistente);
        verify(clientePJRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE VALIDAÇÃO DE FLUXO ==========

    @Test
    @DisplayName("Deve validar CNPJ antes de verificar duplicação")
    void deveValidarCnpjAntesDeVerificarDuplicacao() {
        // Arrange
        CreateClientePJRequest requestCnpjInvalido = new CreateClientePJRequest(
                "Empresa XYZ Ltda", null,
                "11.111.111/1111-11", // CNPJ inválido
                null, null, null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.CONSIGNANTE,
                null, null, null, null, null, null
        );

        // Act & Assert
        assertThrows(CnpjInvalidoException.class, () -> service.criar(requestCnpjInvalido));

        // Verifica que não chegou a consultar o repositório
        verify(clientePJRepository, never()).existsByCnpj(any());
    }

    @Test
    @DisplayName("Deve verificar duplicação antes de buscar cliente indicador")
    void deveVerificarDuplicacaoAntesIndicador() {
        // Arrange
        when(clientePJRepository.existsByCnpj("11222333000181")).thenReturn(true);

        CreateClientePJRequest requestComIndicador = new CreateClientePJRequest(
                requestValido.razaoSocial(),
                requestValido.nomeFantasia(),
                requestValido.cnpj(),
                requestValido.inscricaoEstadual(),
                requestValido.inscricaoMunicipal(),
                requestValido.dataAbertura(),
                requestValido.porteEmpresa(),
                requestValido.naturezaJuridica(),
                requestValido.atividadePrincipal(),
                requestValido.capitalSocial(),
                requestValido.nomeResponsavel(),
                requestValido.cpfResponsavel(),
                requestValido.cargoResponsavel(),
                requestValido.site(),
                requestValido.email(),
                requestValido.tipoCliente(),
                requestValido.origemLead(),
                requestValido.utmSource(),
                requestValido.utmCampaign(),
                requestValido.utmMedium(),
                99L,
                requestValido.observacoes()
        );

        // Act & Assert
        assertThrows(CnpjJaCadastradoException.class, () -> service.criar(requestComIndicador));

        // Verifica que não chegou a buscar indicador
        verify(clienteRepository, never()).findByPublicId(any());
    }
}
