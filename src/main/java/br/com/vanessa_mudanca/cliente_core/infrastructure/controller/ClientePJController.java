package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePJUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByCnpjUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePJByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePJUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST para Cliente Pessoa Jurídica.
 * Adapter de entrada (driving adapter) na arquitetura hexagonal.
 */
@RestController
@RequestMapping("/v1/clientes/pj")
@Tag(name = "Cliente Pessoa Jurídica", description = "Endpoints para gerenciamento de clientes PJ")
public class ClientePJController {

    private final CreateClientePJUseCase createClientePJUseCase;
    private final FindClientePJByIdUseCase findClientePJByIdUseCase;
    private final FindClientePJByCnpjUseCase findClientePJByCnpjUseCase;
    private final ListClientePJUseCase listClientePJUseCase;

    public ClientePJController(
            CreateClientePJUseCase createClientePJUseCase,
            FindClientePJByIdUseCase findClientePJByIdUseCase,
            FindClientePJByCnpjUseCase findClientePJByCnpjUseCase,
            ListClientePJUseCase listClientePJUseCase) {
        this.createClientePJUseCase = createClientePJUseCase;
        this.findClientePJByIdUseCase = findClientePJByIdUseCase;
        this.findClientePJByCnpjUseCase = findClientePJByCnpjUseCase;
        this.listClientePJUseCase = listClientePJUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar cliente PJ", description = "Cria um novo cliente pessoa jurídica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<ClientePJResponse> criar(@Valid @RequestBody CreateClientePJRequest request) {
        ClientePJResponse response = createClientePJUseCase.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar cliente PJ por Public ID", description = "Retorna um cliente pessoa jurídica pelo UUID público")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientePJResponse> buscarPorId(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId) {
        ClientePJResponse response = findClientePJByIdUseCase.findByPublicId(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cnpj/{cnpj}")
    @Operation(summary = "Buscar cliente PJ por CNPJ",
               description = "Retorna um cliente pessoa jurídica pelo CNPJ. Permite descobrir o UUID público através do CNPJ. Aceita CNPJ com formatação (12.345.678/0001-90) ou apenas números (12345678000190). RECOMENDADO: enviar apenas números para evitar problemas com caracteres especiais na URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientePJResponse> buscarPorCnpj(
            @Parameter(description = "CNPJ do cliente - aceita formato '12.345.678/0001-90' ou '12345678000190' (recomendado)",
                      example = "12345678000190") @PathVariable String cnpj) {
        ClientePJResponse response = findClientePJByCnpjUseCase.findByCnpj(cnpj);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes PJ", description = "Lista todos os clientes pessoa jurídica com paginação")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<ClientePJResponse>> listar(
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<ClientePJResponse> response = listClientePJUseCase.findAll(pageable);
        return ResponseEntity.ok(response);
    }
}
