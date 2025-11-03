package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByCpfUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePFUseCase;
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
 * Controller REST para Cliente Pessoa Física.
 * Adapter de entrada (driving adapter) na arquitetura hexagonal.
 */
@RestController
@RequestMapping("/v1/clientes/pf")
@Tag(name = "Cliente Pessoa Física", description = "Endpoints para gerenciamento de clientes PF")
public class ClientePFController {

    private final CreateClientePFUseCase createClientePFUseCase;
    private final FindClientePFByIdUseCase findClientePFByIdUseCase;
    private final FindClientePFByCpfUseCase findClientePFByCpfUseCase;
    private final ListClientePFUseCase listClientePFUseCase;

    public ClientePFController(
            CreateClientePFUseCase createClientePFUseCase,
            FindClientePFByIdUseCase findClientePFByIdUseCase,
            FindClientePFByCpfUseCase findClientePFByCpfUseCase,
            ListClientePFUseCase listClientePFUseCase) {
        this.createClientePFUseCase = createClientePFUseCase;
        this.findClientePFByIdUseCase = findClientePFByIdUseCase;
        this.findClientePFByCpfUseCase = findClientePFByCpfUseCase;
        this.listClientePFUseCase = listClientePFUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar cliente PF", description = "Cria um novo cliente pessoa física")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    public ResponseEntity<ClientePFResponse> criar(@Valid @RequestBody CreateClientePFRequest request) {
        ClientePFResponse response = createClientePFUseCase.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Buscar cliente PF por Public ID", description = "Retorna um cliente pessoa física pelo UUID público")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientePFResponse> buscarPorId(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId) {
        ClientePFResponse response = findClientePFByIdUseCase.findByPublicId(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar cliente PF por CPF",
               description = "Retorna um cliente pessoa física pelo CPF. Permite descobrir o UUID público através do CPF. Aceita CPF formatado (123.456.789-10) ou apenas números (12345678910)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientePFResponse> buscarPorCpf(
            @Parameter(description = "CPF do cliente - aceita formato '123.456.789-10' ou '12345678910'",
                      example = "12345678910") @PathVariable String cpf) {
        ClientePFResponse response = findClientePFByCpfUseCase.findByCpf(cpf);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes PF", description = "Lista todos os clientes pessoa física com paginação")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<ClientePFResponse>> listar(
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<ClientePFResponse> response = listClientePFUseCase.findAll(pageable);
        return ResponseEntity.ok(response);
    }
}
