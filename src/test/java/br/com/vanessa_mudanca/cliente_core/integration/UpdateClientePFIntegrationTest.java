package br.com.vanessa_mudanca.cliente_core.integration;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração end-to-end para UPDATE de Cliente PF.
 *
 * Cobertura:
 * - Criar cliente via POST
 * - Atualizar cliente via PUT (dados básicos, contatos, endereços)
 * - Validar persistência no banco (via GET)
 * - Validar responses HTTP corretos
 * - Validar tratamento de erros (404, 400)
 */
@DisplayName("Integration Tests - UPDATE Cliente PF")
class UpdateClientePFIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID clientePublicId;

    @BeforeEach
    void setUp() {
        // Gerar email E CPF únicos para cada teste (evita conflitos de unicidade)
        long timestamp = System.currentTimeMillis();
        String emailUnico = "joao.silva." + timestamp + "@email.com";
        String cpfValido = gerarCpfValido(timestamp);

        // Criar cliente PF para ser atualizado nos testes
        CreateClientePFRequest createRequest = new CreateClientePFRequest(
                "João",
                "da",
                "Silva",
                cpfValido,
                "MG-12.345.678",
                LocalDate.of(1990, 1, 15),
                SexoEnum.MASCULINO,
                emailUnico,
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
                "Cliente teste"
        );

        String url = getBaseUrl() + "/clientes/pf";

        ResponseEntity<ClientePFResponse> createResponse;
        try {
            createResponse = restTemplate.postForEntity(
                    url,
                    createRequest,
                    ClientePFResponse.class
            );
        } catch (Exception e) {
            System.err.println("❌ ERROR during POST to: " + url);
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            throw e;
        }

        // Debug: Print response details if not CREATED
        if (createResponse.getStatusCode() != HttpStatus.CREATED) {
            System.err.println("❌ ERROR: Expected 201, got " + createResponse.getStatusCode());
            System.err.println("URL: " + url);
            System.err.println("Response body: " + createResponse.getBody());
        }

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        ClientePFResponse created = createResponse.getBody();
        clientePublicId = created.publicId();
    }

    @Test
    @DisplayName("Deve atualizar dados básicos do cliente com sucesso")
    void deveAtualizarDadosBasicosComSucesso() {
        // Arrange
        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(clientePublicId)
                .primeiroNome("Carlos")
                .sobrenome("Santos")
                .email("carlos.santos@newemail.com")
                .profissao("Arquiteto")
                .tipoCliente(TipoClienteEnum.CONSIGNANTE)
                .build();

        String url = getBaseUrl() + "/clientes/pf/" + clientePublicId;

        // Act
        HttpEntity<UpdateClientePFRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<ClientePFResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                ClientePFResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ClientePFResponse updated = response.getBody();
        assertEquals("Carlos", updated.primeiroNome());
        assertEquals("Santos", updated.sobrenome());
        assertEquals("carlos.santos@newemail.com", updated.email());
        assertEquals("Arquiteto", updated.profissao());
        assertEquals(TipoClienteEnum.CONSIGNANTE, updated.tipoCliente());

        // Validar que dados NÃO atualizados permaneceram inalterados
        assertEquals("da", updated.nomeDoMeio()); // Não foi atualizado
        assertEquals(SexoEnum.MASCULINO, updated.sexo()); // Não foi atualizado
    }

    @Test
    @DisplayName("Deve persistir atualização no banco de dados")
    void devePersistirAtualizacaoNoBanco() {
        // Arrange
        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(clientePublicId)
                .primeiroNome("Pedro")
                .estadoCivil("Divorciado")
                .build();

        String urlUpdate = getBaseUrl() + "/clientes/pf/" + clientePublicId;
        String urlGet = getBaseUrl() + "/clientes/pf/" + clientePublicId;

        // Act - Atualizar
        HttpEntity<UpdateClientePFRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<ClientePFResponse> updateResponse = restTemplate.exchange(
                urlUpdate,
                HttpMethod.PUT,
                requestEntity,
                ClientePFResponse.class
        );

        // Act - Buscar novamente
        ResponseEntity<ClientePFResponse> getResponse = restTemplate.getForEntity(
                urlGet,
                ClientePFResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        ClientePFResponse retrieved = getResponse.getBody();
        assertNotNull(retrieved);
        assertEquals("Pedro", retrieved.primeiroNome());
        assertEquals("Divorciado", retrieved.estadoCivil());
    }

    @Test
    @DisplayName("Deve retornar 404 quando cliente não existe")
    void deveRetornar404QuandoClienteNaoExiste() {
        // Arrange
        UUID publicIdInexistente = UUID.randomUUID();
        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(publicIdInexistente)
                .primeiroNome("Teste")
                .build();

        String url = getBaseUrl() + "/clientes/pf/" + publicIdInexistente;

        // Act
        HttpEntity<UpdateClientePFRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve prevenir path traversal (publicId do path vs body)")
    void devePrevenirPathTraversal() {
        // Arrange - Tentar passar publicId diferente no body vs path
        UUID publicIdDoPath = clientePublicId;
        UUID publicIdDoBody = UUID.randomUUID(); // ID diferente no body

        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(publicIdDoBody) // Tentando enganar com ID diferente
                .primeiroNome("Hacker")
                .build();

        String url = getBaseUrl() + "/clientes/pf/" + publicIdDoPath;

        // Act
        HttpEntity<UpdateClientePFRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<ClientePFResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                ClientePFResponse.class
        );

        // Assert - Deve atualizar o cliente do PATH, não do body
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(publicIdDoPath, response.getBody().publicId());
        assertEquals("Hacker", response.getBody().primeiroNome());

        // Validar que cliente com publicIdDoBody NÃO foi criado/atualizado
        String urlInvalido = getBaseUrl() + "/clientes/pf/" + publicIdDoBody;
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                urlInvalido,
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Deve fazer selective update (apenas campos presentes são atualizados)")
    void deveFazerSelectiveUpdate() {
        // Arrange - Primeiro buscar estado atual
        String urlGet = getBaseUrl() + "/clientes/pf/" + clientePublicId;
        ResponseEntity<ClientePFResponse> getResponse = restTemplate.getForEntity(
                urlGet,
                ClientePFResponse.class
        );
        ClientePFResponse estadoAnterior = getResponse.getBody();
        assertNotNull(estadoAnterior);

        // Atualizar APENAS profissão (outros campos não devem mudar)
        UpdateClientePFRequest updateRequest = UpdateClientePFRequest.builder()
                .publicId(clientePublicId)
                .profissao("Médico") // Apenas este campo
                .build();

        String urlUpdate = getBaseUrl() + "/clientes/pf/" + clientePublicId;

        // Act
        HttpEntity<UpdateClientePFRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<ClientePFResponse> updateResponse = restTemplate.exchange(
                urlUpdate,
                HttpMethod.PUT,
                requestEntity,
                ClientePFResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        ClientePFResponse atualizado = updateResponse.getBody();
        assertNotNull(atualizado);

        // Profissão foi atualizada
        assertEquals("Médico", atualizado.profissao());

        // Outros campos permaneceram inalterados
        assertEquals(estadoAnterior.primeiroNome(), atualizado.primeiroNome());
        assertEquals(estadoAnterior.sobrenome(), atualizado.sobrenome());
        assertEquals(estadoAnterior.email(), atualizado.email());
        assertEquals(estadoAnterior.sexo(), atualizado.sexo());
        assertEquals(estadoAnterior.estadoCivil(), atualizado.estadoCivil());
    }

    /**
     * Gera um CPF válido (com dígitos verificadores corretos) baseado em um seed.
     * Útil para testes onde precisamos de CPFs únicos mas válidos.
     */
    private String gerarCpfValido(long seed) {
        // Gera 9 dígitos base a partir do seed
        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) {
            cpf[i] = (int) ((seed / Math.pow(10, i)) % 10);
        }

        // Calcula primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += cpf[i] * (10 - i);
        }
        cpf[9] = 11 - (soma % 11);
        if (cpf[9] >= 10) cpf[9] = 0;

        // Calcula segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += cpf[i] * (11 - i);
        }
        cpf[10] = 11 - (soma % 11);
        if (cpf[10] >= 10) cpf[10] = 0;

        // Formata CPF
        return String.format("%d%d%d.%d%d%d.%d%d%d-%d%d",
                cpf[0], cpf[1], cpf[2], cpf[3], cpf[4], cpf[5],
                cpf[6], cpf[7], cpf[8], cpf[9], cpf[10]);
    }
}
