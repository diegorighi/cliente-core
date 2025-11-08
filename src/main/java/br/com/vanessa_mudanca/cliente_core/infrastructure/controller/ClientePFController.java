package br.com.vanessa_mudanca.cliente_core.infrastructure.controller;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.BloquearClienteRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFLookupResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.PageResponse;
import br.com.vanessa_mudanca.cliente_core.infrastructure.logging.LogExecutionTime;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.BloquearClienteUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.CreateClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.DeleteClienteUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByCpfUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.FindClientePFByIdUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.ListClientePFUseCase;
import br.com.vanessa_mudanca.cliente_core.application.ports.input.UpdateClientePFUseCase;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.com.vanessa_mudanca.cliente_core.infrastructure.security.CustomerAccessValidator;

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
    private final UpdateClientePFUseCase updateClientePFUseCase;
    private final FindClientePFByIdUseCase findClientePFByIdUseCase;
    private final FindClientePFByCpfUseCase findClientePFByCpfUseCase;
    private final ListClientePFUseCase listClientePFUseCase;
    private final DeleteClienteUseCase deleteClienteUseCase;
    private final BloquearClienteUseCase bloquearClienteUseCase;
    private final CustomerAccessValidator customerAccessValidator;

    public ClientePFController(
            CreateClientePFUseCase createClientePFUseCase,
            UpdateClientePFUseCase updateClientePFUseCase,
            FindClientePFByIdUseCase findClientePFByIdUseCase,
            FindClientePFByCpfUseCase findClientePFByCpfUseCase,
            ListClientePFUseCase listClientePFUseCase,
            DeleteClienteUseCase deleteClienteUseCase,
            BloquearClienteUseCase bloquearClienteUseCase,
            CustomerAccessValidator customerAccessValidator) {
        this.createClientePFUseCase = createClientePFUseCase;
        this.updateClientePFUseCase = updateClientePFUseCase;
        this.findClientePFByIdUseCase = findClientePFByIdUseCase;
        this.findClientePFByCpfUseCase = findClientePFByCpfUseCase;
        this.listClientePFUseCase = listClientePFUseCase;
        this.deleteClienteUseCase = deleteClienteUseCase;
        this.bloquearClienteUseCase = bloquearClienteUseCase;
        this.customerAccessValidator = customerAccessValidator;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
    @LogExecutionTime(layer = LogExecutionTime.Layer.CONTROLLER)
    @Operation(summary = "Criar cliente PF", description = "Cria um novo cliente pessoa física. Requer role ADMIN ou EMPLOYEE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role ADMIN ou EMPLOYEE"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    public ResponseEntity<ClientePFResponse> criar(@Valid @RequestBody CreateClientePFRequest request) {
        ClientePFResponse response = createClientePFUseCase.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'CUSTOMER', 'SERVICE')")
    @Operation(summary = "Buscar cliente PF por Public ID", description = "Retorna um cliente pessoa física pelo UUID público. Requer autenticação. CUSTOMER vê apenas próprio cadastro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - CUSTOMER tentou acessar dados de outro cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientePFResponse> buscarPorId(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId,
            Authentication authentication) {

        // Valida se CUSTOMER está tentando acessar apenas próprio cadastro
        customerAccessValidator.validateAccess(publicId, authentication);

        ClientePFResponse response = findClientePFByIdUseCase.findByPublicId(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cpf/{cpf}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'CUSTOMER', 'SERVICE')")
    @Operation(summary = "Buscar cliente PF por CPF (dados reduzidos)",
               description = "Retorna dados reduzidos de um cliente pessoa física pelo CPF (apenas primeiroNome, sobrenome e publicId). " +
                           "Permite descobrir o UUID público através do CPF para posterior busca completa via GET /v1/clientes/pf/{publicId}. " +
                           "Aceita CPF formatado (123.456.789-10) ou apenas números (12345678910). " +
                           "Requer autenticação. CUSTOMER vê apenas próprio cadastro. " +
                           "Segue princípio de minimização de dados (LGPD).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                        content = @Content(schema = @Schema(implementation = ClientePFLookupResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - CUSTOMER tentou acessar dados de outro cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientePFLookupResponse> buscarPorCpf(
            @Parameter(description = "CPF do cliente - aceita formato '123.456.789-10' ou '12345678910'",
                      example = "12345678910") @PathVariable String cpf,
            Authentication authentication) {

        // Busca cliente completo
        ClientePFResponse response = findClientePFByCpfUseCase.findByCpf(cpf);

        // Valida se CUSTOMER está tentando acessar apenas próprio cadastro
        customerAccessValidator.validateAccess(response.publicId(), authentication);

        // Retorna apenas dados reduzidos (LGPD - minimização de dados)
        ClientePFLookupResponse lookupResponse = new ClientePFLookupResponse(
                response.primeiroNome(),
                response.sobrenome(),
                response.publicId()
        );

        return ResponseEntity.ok(lookupResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'SERVICE')")
    @Operation(summary = "Listar clientes PF", description = "Lista todos os clientes pessoa física com paginação. Requer role ADMIN, EMPLOYEE ou SERVICE. CUSTOMER não pode listar todos.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
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

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Atualizar cliente PF",
            description = "Atualiza dados do cliente PF e suas entidades relacionadas (documentos, endereços, contatos). " +
                    "Permite atualização seletiva: apenas os campos presentes no request serão atualizados. Requer role ADMIN ou EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role ADMIN ou EMPLOYEE"),
            @ApiResponse(responseCode = "404", description = "Cliente, documento, endereço ou contato não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito ao tentar marcar mais de um item como principal")
    })
    public ResponseEntity<ClientePFResponse> atualizar(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId,
            @Valid @RequestBody UpdateClientePFRequest request) {

        // Garantir que o publicId do path seja usado (segurança)
        UpdateClientePFRequest requestComId = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .primeiroNome(request.primeiroNome())
                .nomeDoMeio(request.nomeDoMeio())
                .sobrenome(request.sobrenome())
                .rg(request.rg())
                .sexo(request.sexo())
                .email(request.email())
                .nomeMae(request.nomeMae())
                .nomePai(request.nomePai())
                .estadoCivil(request.estadoCivil())
                .profissao(request.profissao())
                .nacionalidade(request.nacionalidade())
                .naturalidade(request.naturalidade())
                .tipoCliente(request.tipoCliente())
                .observacoes(request.observacoes())
                .documentos(request.documentos())
                .enderecos(request.enderecos())
                .contatos(request.contatos())
                .build();

        ClientePFResponse response = updateClientePFUseCase.atualizar(requestComId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Deletar cliente PF (soft delete)",
            description = "Realiza soft delete do cliente PF. O cliente é marcado como inativo (ativo=false) e preserva todos os dados para auditoria. " +
                    "Requer motivo e identificação do usuário responsável pela deleção. APENAS ADMIN pode deletar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode deletar"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Cliente já foi deletado anteriormente")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId,
            @Parameter(description = "Motivo da deleção", required = true) @RequestParam String motivo,
            @Parameter(description = "Usuário responsável pela deleção", required = true) @RequestParam String usuario) {

        deleteClienteUseCase.deletar(publicId, motivo, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{publicId}/restaurar")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Restaurar cliente PF deletado",
            description = "Restaura um cliente PF que foi deletado (soft delete). " +
                    "O cliente volta ao estado ativo (ativo=true) e limpa os campos de deleção. APENAS ADMIN pode restaurar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente restaurado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode restaurar"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> restaurar(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId,
            @Parameter(description = "Usuário responsável pela restauração", required = true) @RequestParam String usuario) {

        deleteClienteUseCase.restaurar(publicId, usuario);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}/bloquear")
    @PreAuthorize("hasAuthority('ADMIN')")
    @LogExecutionTime(layer = LogExecutionTime.Layer.CONTROLLER)
    @Operation(
            summary = "Bloquear cliente PF",
            description = "Bloqueia um cliente PF. Cliente bloqueado não pode realizar novas transações. " +
                    "Registra motivo, data e usuário responsável. APENAS ADMIN pode bloquear."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente bloqueado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode bloquear"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Cliente já está bloqueado")
    })
    public ResponseEntity<Void> bloquear(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId,
            @Valid @RequestBody BloquearClienteRequest request) {

        bloquearClienteUseCase.bloquear(publicId, request.motivoBloqueio(), request.usuarioBloqueou());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}/desbloquear")
    @PreAuthorize("hasAuthority('ADMIN')")
    @LogExecutionTime(layer = LogExecutionTime.Layer.CONTROLLER)
    @Operation(
            summary = "Desbloquear cliente PF",
            description = "Desbloqueia um cliente PF bloqueado. Remove o bloqueio e limpa os campos relacionados. APENAS ADMIN pode desbloquear."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente desbloqueado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode desbloquear"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> desbloquear(
            @Parameter(description = "UUID público do cliente") @PathVariable UUID publicId) {

        bloquearClienteUseCase.desbloquear(publicId);
        return ResponseEntity.noContent().build();
    }
}
