package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.DeleteClienteUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByCpfUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.UpdateClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CpfInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CpfJaCadastradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.vanessa_mudanca.cliente_core.infrastructure.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para ClientePFController.
 * Foca em testar regras de negócio e tratamento de exceções.
 */
@WebMvcTest(ClientePFController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ClientePFController - Testes de endpoints REST")
class ClientePFControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateClientePFUseCase createClientePFUseCase;

    @MockBean
    private FindClientePFByIdUseCase findClientePFByIdUseCase;

    @MockBean
    private FindClientePFByCpfUseCase findClientePFByCpfUseCase;

    @MockBean
    private ListClientePFUseCase listClientePFUseCase;

    @MockBean
    private UpdateClientePFUseCase updateClientePFUseCase;

    @MockBean
    private DeleteClienteUseCase deleteClienteUseCase;

    private ObjectMapper objectMapper;
    private CreateClientePFRequest requestValido;
    private ClientePFResponse responseEsperado;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

        responseEsperado = new ClientePFResponse(
                java.util.UUID.randomUUID(),
                "João",
                "da",
                "Silva",
                "João da Silva",
                "12345678909",
                "MG-12.345.678",
                LocalDate.of(1990, 1, 15),
                35,
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
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                false,
                null,
                "Cliente preferencial",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /v1/clientes/pf - Deve criar cliente PF e retornar 201 CREATED")
    void deveCriarClientePFComSucesso() throws Exception {
        // Arrange
        when(createClientePFUseCase.criar(any(CreateClientePFRequest.class)))
                .thenReturn(responseEsperado);

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.publicId").exists())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.primeiroNome").value("João"))
                .andExpect(jsonPath("$.sobrenome").value("Silva"))
                .andExpect(jsonPath("$.nomeCompleto").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andExpect(jsonPath("$.email").value("joao.silva@email.com"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false));
    }

    @Test
    @DisplayName("POST /v1/clientes/pf - Deve retornar 400 quando CPF for inválido")
    void deveRetornar400QuandoCpfInvalido() throws Exception {
        // Arrange
        when(createClientePFUseCase.criar(any(CreateClientePFRequest.class)))
                .thenThrow(new CpfInvalidoException("123.456.789-00"));

        CreateClientePFRequest requestCpfInvalido = new CreateClientePFRequest(
                "João", "da", "Silva",
                "123.456.789-00",
                null, null, null, null, null, null, null, null, null, null,
                TipoClienteEnum.COMPRADOR,
                null, null, null, null, null, null
        );

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCpfInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("CPF '123.456.789-00' é inválido"));
    }

    @Test
    @DisplayName("POST /v1/clientes/pf - Deve retornar 409 quando CPF já estiver cadastrado")
    void deveRetornar409QuandoCpfJaCadastrado() throws Exception {
        // Arrange
        when(createClientePFUseCase.criar(any(CreateClientePFRequest.class)))
                .thenThrow(new CpfJaCadastradoException("123.456.789-09"));

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("CPF '123.456.789-09' já está cadastrado no sistema"));
    }
}
