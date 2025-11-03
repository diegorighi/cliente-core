package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByCnpjUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjJaCadastradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.vanessa_mudanca.cliente_core.infrastructure.exception.GlobalExceptionHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para ClientePJController.
 * Foca em testar regras de negócio e tratamento de exceções.
 */
@WebMvcTest(ClientePJController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ClientePJController - Testes de endpoints REST")
class ClientePJControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateClientePJUseCase createClientePJUseCase;

    @MockBean
    private FindClientePJByIdUseCase findClientePJByIdUseCase;

    @MockBean
    private FindClientePJByCnpjUseCase findClientePJByCnpjUseCase;

    @MockBean
    private ListClientePJUseCase listClientePJUseCase;

    private ObjectMapper objectMapper;
    private CreateClientePJRequest requestValido;
    private ClientePJResponse responseEsperado;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

        responseEsperado = new ClientePJResponse(
                java.util.UUID.randomUUID(),
                "Empresa XYZ Ltda",
                "XYZ Comércio",
                "XYZ Comércio",
                "11222333000181",
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
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                false,
                null,
                "Cliente corporativo",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /v1/clientes/pj - Deve criar cliente PJ e retornar 201 CREATED")
    void deveCriarClientePJComSucesso() throws Exception {
        // Arrange
        when(createClientePJUseCase.criar(any(CreateClientePJRequest.class)))
                .thenReturn(responseEsperado);

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pj")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.publicId").exists())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa XYZ Ltda"))
                .andExpect(jsonPath("$.nomeFantasia").value("XYZ Comércio"))
                .andExpect(jsonPath("$.nomeExibicao").value("XYZ Comércio"))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"))
                .andExpect(jsonPath("$.email").value("contato@empresaxyz.com.br"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false));
    }

    @Test
    @DisplayName("POST /v1/clientes/pj - Deve retornar 400 quando CNPJ for inválido")
    void deveRetornar400QuandoCnpjInvalido() throws Exception {
        // Arrange
        when(createClientePJUseCase.criar(any(CreateClientePJRequest.class)))
                .thenThrow(new CnpjInvalidoException("11.222.333/0001-00"));

        CreateClientePJRequest requestCnpjInvalido = new CreateClientePJRequest(
                "Empresa XYZ Ltda",
                "XYZ Comércio",
                "11.222.333/0001-00",
                null, null, null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.CONSIGNANTE,
                null, null, null, null, null, null
        );

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pj")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCnpjInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("CNPJ '11.222.333/0001-00' é inválido"));
    }

    @Test
    @DisplayName("POST /v1/clientes/pj - Deve retornar 409 quando CNPJ já estiver cadastrado")
    void deveRetornar409QuandoCnpjJaCadastrado() throws Exception {
        // Arrange
        when(createClientePJUseCase.criar(any(CreateClientePJRequest.class)))
                .thenThrow(new CnpjJaCadastradoException("11.222.333/0001-81"));

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pj")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("CNPJ '11.222.333/0001-81' já está cadastrado no sistema"));
    }
}
