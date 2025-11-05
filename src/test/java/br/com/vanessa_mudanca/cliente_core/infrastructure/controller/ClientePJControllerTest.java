package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.DeleteClienteUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByCnpjUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.UpdateClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjInvalidoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjJaCadastradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.vanessa_mudanca.cliente_core.infrastructure.exception.GlobalExceptionHandler;

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
 * Testes unitários para ClientePJController.
 * Foca em testar regras de negócio e tratamento de exceções.
 */
@WebMvcTest(ClientePJController.class)
@Import({GlobalExceptionHandler.class, br.com.vanessa_mudanca.cliente_core.infrastructure.security.TestSecurityConfig.class})
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

    @MockBean
    private UpdateClientePJUseCase updateClientePJUseCase;

    @MockBean
    private DeleteClienteUseCase deleteClienteUseCase;

    @MockBean
    private br.com.vanessa_mudanca.cliente_core.infrastructure.security.CustomerAccessValidator customerAccessValidator;

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

    @Test
    @DisplayName("GET /v1/clientes/pj/{publicId} - Deve buscar cliente PJ por publicId e retornar 200")
    void deveBuscarClientePorPublicIdComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        when(findClientePJByIdUseCase.findByPublicId(publicId))
                .thenReturn(responseEsperado);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pj/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.razaoSocial").value("Empresa XYZ Ltda"))
                .andExpect(jsonPath("$.nomeFantasia").value("XYZ Comércio"))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"));
    }

    @Test
    @DisplayName("GET /v1/clientes/pj/{publicId} - Deve retornar 404 quando cliente não existir")
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        when(findClientePJByIdUseCase.findByPublicId(publicId))
                .thenThrow(new ClienteNaoEncontradoException(publicId));

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pj/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /v1/clientes/pj/cnpj/{cnpj} - Deve buscar cliente PJ por CNPJ e retornar 200")
    void deveBuscarClientePorCnpjComSucesso() throws Exception {
        // Arrange
        String cnpj = "11222333000181";
        when(findClientePJByCnpjUseCase.findByCnpj(cnpj))
                .thenReturn(responseEsperado);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pj/cnpj/{cnpj}", cnpj)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"));
    }

    @Test
    @DisplayName("GET /v1/clientes/pj - Deve listar clientes PJ com paginação padrão")
    void deveListarClientesPJComPaginacaoPadrao() throws Exception {
        // Arrange
        PageResponse<ClientePJResponse> page = new PageResponse<>(
                List.of(responseEsperado),
                0,
                20,
                1,
                1,
                true,
                true,
                false
        );

        when(listClientePJUseCase.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pj")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].razaoSocial").value("Empresa XYZ Ltda"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    @DisplayName("GET /v1/clientes/pj - Deve listar clientes PJ com paginação customizada")
    void deveListarClientesPJComPaginacaoCustomizada() throws Exception {
        // Arrange
        PageResponse<ClientePJResponse> page = new PageResponse<>(
                List.of(responseEsperado),
                1,
                10,
                25,
                3,
                false,
                false,
                false
        );

        when(listClientePJUseCase.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes/pj")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "razaoSocial")
                        .param("direction", "DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("PUT /v1/clientes/pj/{publicId} - Deve atualizar cliente PJ e retornar 200")
    void deveAtualizarClientePJComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        UpdateClientePJRequest updateRequest = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .razaoSocial("Empresa XYZ Atualizada Ltda")
                .email("novoemail@empresaxyz.com.br")
                .build();

        ClientePJResponse responseAtualizado = new ClientePJResponse(
                publicId,
                "Empresa XYZ Atualizada Ltda",
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
                "novoemail@empresaxyz.com.br",
                TipoClienteEnum.CONSIGNANTE,
                null, null, null, null, null,
                0, 0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null, null,
                false,
                null,
                "Cliente corporativo",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(updateClientePJUseCase.atualizar(any(UpdateClientePJRequest.class)))
                .thenReturn(responseAtualizado);

        // Act & Assert
        mockMvc.perform(put("/v1/clientes/pj/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.razaoSocial").value("Empresa XYZ Atualizada Ltda"))
                .andExpect(jsonPath("$.email").value("novoemail@empresaxyz.com.br"));
    }

    @Test
    @DisplayName("PUT /v1/clientes/pj/{publicId} - Deve retornar 404 quando cliente não existir")
    void deveRetornar404AoAtualizarClienteInexistente() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        UpdateClientePJRequest updateRequest = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .razaoSocial("Empresa ABC")
                .build();

        when(updateClientePJUseCase.atualizar(any(UpdateClientePJRequest.class)))
                .thenThrow(new ClienteNaoEncontradoException(publicId));

        // Act & Assert
        mockMvc.perform(put("/v1/clientes/pj/{publicId}", publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /v1/clientes/pj/{publicId} - Deve deletar cliente PJ (soft delete) e retornar 204")
    void deveDeletarClientePJComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doNothing().when(deleteClienteUseCase).deletar(eq(publicId), any(String.class), any(String.class));

        // Act & Assert
        mockMvc.perform(delete("/v1/clientes/pj/{publicId}", publicId)
                        .param("motivo", "Empresa encerrada")
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/clientes/pj/{publicId} - Deve retornar 404 quando cliente não existir")
    void deveRetornar404AoDeletarClienteInexistente() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doThrow(new ClienteNaoEncontradoException(publicId))
                .when(deleteClienteUseCase).deletar(eq(publicId), any(String.class), any(String.class));

        // Act & Assert
        mockMvc.perform(delete("/v1/clientes/pj/{publicId}", publicId)
                        .param("motivo", "Teste")
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /v1/clientes/pj/{publicId}/restaurar - Deve restaurar cliente deletado e retornar 204")
    void deveRestaurarClienteDeletadoComSucesso() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doNothing().when(deleteClienteUseCase).restaurar(eq(publicId), any(String.class));

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pj/{publicId}/restaurar", publicId)
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /v1/clientes/pj/{publicId}/restaurar - Deve retornar 404 quando cliente não existir")
    void deveRetornar404AoRestaurarClienteInexistente() throws Exception {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doThrow(new ClienteNaoEncontradoException(publicId))
                .when(deleteClienteUseCase).restaurar(eq(publicId), any(String.class));

        // Act & Assert
        mockMvc.perform(post("/v1/clientes/pj/{publicId}/restaurar", publicId)
                        .param("usuario", "admin@example.com"))
                .andExpect(status().isNotFound());
    }
}
