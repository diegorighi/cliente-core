package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
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
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.vanessa_mudanca.cliente_core.infrastructure.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para ClientePFController.
 * Foca em testar regras de negócio e tratamento de exceções.
 */
@WebMvcTest(ClientePFController.class)
@Import({GlobalExceptionHandler.class, br.com.vanessa_mudanca.cliente_core.infrastructure.security.TestSecurityConfig.class})
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

    @MockBean
    private br.com.vanessa_mudanca.cliente_core.application.ports.input.BloquearClienteUseCase bloquearClienteUseCase;

    @MockBean
    private br.com.vanessa_mudanca.cliente_core.infrastructure.security.CustomerAccessValidator customerAccessValidator;

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
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of()
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

    @Test
    @DisplayName("GET /v1/clientes/pf/{publicId} - Deve buscar cliente PF por publicId e retornar 200")
    void deveBuscarClientePorPublicIdComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        when(findClientePFByIdUseCase.findByPublicId(publicId))
                .thenReturn(responseEsperado);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pf/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.primeiroNome").value("João"))
                .andExpect(jsonPath("$.sobrenome").value("Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678909"));
    }

    @Test
    @DisplayName("GET /v1/clientes/pf/{publicId} - Deve retornar 404 quando cliente não existir")
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        when(findClientePFByIdUseCase.findByPublicId(publicId))
                .thenThrow(new ClienteNaoEncontradoException(publicId));

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pf/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /v1/clientes/pf/cpf/{cpf} - Deve buscar cliente PF por CPF e retornar dados reduzidos")
    void deveBuscarClientePorCpfComSucesso() throws Exception {
        // Arrange
        String cpf = "12345678909";
        when(findClientePFByCpfUseCase.findByCpf(cpf))
                .thenReturn(responseEsperado);

        // Act & Assert - Verifica que retorna apenas primeiroNome, sobrenome e publicId
        mockMvc.perform(get("/v1/clientes/pf/cpf/{cpf}", cpf)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.primeiroNome").value("João"))
                .andExpect(jsonPath("$.sobrenome").value("Silva"))
                .andExpect(jsonPath("$.publicId").value(responseEsperado.publicId().toString()))
                .andExpect(jsonPath("$.cpf").doesNotExist())  // CPF não retorna em lookup
                .andExpect(jsonPath("$.email").doesNotExist());  // Email não retorna em lookup
    }

    @Test
    @DisplayName("GET /v1/clientes/pf/cpf/{cpf} - Deve aceitar CPF formatado e retornar dados reduzidos")
    void deveBuscarClientePorCpfFormatado() throws Exception {
        // Arrange
        String cpfFormatado = "123.456.789-09";
        when(findClientePFByCpfUseCase.findByCpf(cpfFormatado))
                .thenReturn(responseEsperado);

        // Act & Assert - Verifica que retorna apenas dados reduzidos
        mockMvc.perform(get("/v1/clientes/pf/cpf/{cpf}", cpfFormatado)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primeiroNome").value("João"))
                .andExpect(jsonPath("$.sobrenome").value("Silva"))
                .andExpect(jsonPath("$.publicId").value(responseEsperado.publicId().toString()));
    }

    @Test
    @DisplayName("GET /v1/clientes/pf - Deve listar clientes PF com paginação padrão")
    void deveListarClientesPFComPaginacaoPadrao() throws Exception {
        // Arrange
        PageResponse<ClientePFResponse> page = new PageResponse<>(
                List.of(responseEsperado),
                0,
                20,
                1,
                1,
                true,
                true,
                false
        );

        when(listClientePFUseCase.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pf")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].primeiroNome").value("João"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    @DisplayName("GET /v1/clientes/pf - Deve listar clientes PF com paginação customizada")
    void deveListarClientesPFComPaginacaoCustomizada() throws Exception {
        // Arrange
        PageResponse<ClientePFResponse> page = new PageResponse<>(
                List.of(responseEsperado),
                1,
                10,
                15,
                2,
                false,
                true,
                false
        );

        when(listClientePFUseCase.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pf")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "nomeCompleto")
                        .param("direction", "DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("PUT /v1/clientes/pf/{publicId} - Deve atualizar cliente PF e retornar 200")
    void deveAtualizarClientePFComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .primeiroNome("João Carlos")
                .email("joao.carlos@email.com")
                .build();

        ClientePFResponse responseAtualizado = new ClientePFResponse(
                publicId,
                "João Carlos",
                "da",
                "Silva",
                "João Carlos da Silva",
                "12345678909",
                "MG-12.345.678",
                LocalDate.of(1990, 1, 15),
                35,
                SexoEnum.MASCULINO,
                "joao.carlos@email.com",
                "Maria da Silva",
                "José da Silva",
                "Casado",
                "Engenheiro",
                "Brasileira",
                "Belo Horizonte",
                TipoClienteEnum.COMPRADOR,
                null, null, null, null, null,
                0, 0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null, null,
                false,
                null,
                "Cliente preferencial",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of()
        );

        when(updateClientePFUseCase.atualizar(any(UpdateClientePFRequest.class)))
                .thenReturn(responseAtualizado);

        // Act & Assert
        mockMvc.perform(put("/v1/clientes/pf/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.primeiroNome").value("João Carlos"))
                .andExpect(jsonPath("$.email").value("joao.carlos@email.com"));
    }

    @Test
    @DisplayName("PUT /v1/clientes/pf/{publicId} - Deve retornar 404 quando cliente não existir")
    void deveRetornar404AoAtualizarClienteInexistente() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .primeiroNome("João")
                .build();

        when(updateClientePFUseCase.atualizar(any(UpdateClientePFRequest.class)))
                .thenThrow(new ClienteNaoEncontradoException(publicId));

        // Act & Assert
        mockMvc.perform(put("/v1/clientes/pf/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /v1/clientes/pf/{publicId} - Deve deletar cliente PF (soft delete) e retornar 204")
    void deveDeletarClientePFComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doNothing().when(deleteClienteUseCase).deletar(eq(publicId), any(String.class), any(String.class));

        // Act & Assert
        mockMvc.perform(delete("/v1/clientes/pf/{publicId}", publicId)
                        .param("motivo", "Cliente solicitou exclusão")
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/clientes/pf/{publicId} - Deve retornar 404 quando cliente não existir")
    void deveRetornar404AoDeletarClienteInexistente() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doThrow(new ClienteNaoEncontradoException(publicId))
                .when(deleteClienteUseCase).deletar(eq(publicId), any(String.class), any(String.class));

        // Act & Assert
        mockMvc.perform(delete("/v1/clientes/pf/{publicId}", publicId)
                        .param("motivo", "Teste")
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /v1/clientes/pf/{publicId}/restaurar - Deve restaurar cliente deletado e retornar 204")
    void deveRestaurarClienteDeletadoComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doNothing().when(deleteClienteUseCase).restaurar(eq(publicId), any(String.class));

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pf/{publicId}/restaurar", publicId)
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /v1/clientes/pf/{publicId}/restaurar - Deve retornar 404 quando cliente não existir")
    void deveRetornar404AoRestaurarClienteInexistente() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doThrow(new ClienteNaoEncontradoException(publicId))
                .when(deleteClienteUseCase).restaurar(eq(publicId), any(String.class));

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pf/{publicId}/restaurar", publicId)
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNotFound());
    }
}
