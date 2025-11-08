# Logging de Execução com Correlation ID

## Arquivos

- `LogExecutionTime.java` - Anotação para marcar métodos
- `ExecutionTimeLoggingAspect.java` - Aspect que intercepta e loga
- `CorrelationIdFilter.java` - Filter que adiciona correlation ID

## Como Usar

### 1. Em Controllers

```java
@RestController
@RequestMapping("/v1/clientes/pf")
public class ClientePFController {

    @PostMapping
    @LogExecutionTime(layer = LogExecutionTime.Layer.CONTROLLER)
    public ResponseEntity<ClientePFResponse> criar(@Valid @RequestBody CreateClientePFRequest request) {
        // implementação
    }
}
```

### 2. Em Services

```java
@Service
public class CreateClientePFService implements CreateClientePFUseCase {

    @Override
    @Transactional
    @LogExecutionTime(layer = LogExecutionTime.Layer.SERVICE)
    public ClientePFResponse criar(CreateClientePFRequest request) {
        // implementação
    }
}
```

### 3. Em Repositories (se necessário)

```java
@Repository
public class ClientePFRepositoryAdapter implements ClientePFRepositoryPort {

    @Override
    @LogExecutionTime(layer = LogExecutionTime.Layer.REPOSITORY)
    public Optional<ClientePF> findByCpf(String cpf) {
        // implementação
    }
}
```

## Exemplo de Logs Gerados

```
# Request chega
INFO  [correlationId=abc-123] Request iniciado - GET /v1/clientes/pf/12345678910

# Controller inicia
INFO  [abc-123] Iniciando ClientePFController.buscarPorCpf - Camada: CONTROLLER - Args: [CPF:***.***.789-10]

# Service inicia
INFO  [abc-123] Iniciando FindClientePFByCpfService.findByCpf - Camada: SERVICE - Args: [CPF:***.***.789-10]

# Repository inicia (se anotado)
INFO  [abc-123] Iniciando ClientePFRepositoryAdapter.findByCpf - Camada: REPOSITORY - Args: [CPF:***.***.789-10]

# Repository termina
INFO  [abc-123] Finalizando ClientePFRepositoryAdapter.findByCpf - Duração: 45ms - Status: SUCCESS

# Service termina
INFO  [abc-123] Finalizando FindClientePFByCpfService.findByCpf - Duração: 67ms - Status: SUCCESS

# Controller termina
INFO  [abc-123] Finalizando ClientePFController.buscarPorCpf - Duração: 89ms - Status: SUCCESS

# Request termina (CorrelationIdFilter)
INFO  [correlationId=abc-123] Request finalizado - GET /v1/clientes/pf/12345678910 - Status: 200 - Duração total: 95ms
```

## CloudWatch Insights Query

```sql
fields @timestamp, message, correlationId, @duration
| filter correlationId = "abc-123"
| sort @timestamp asc
```

## Métricas Prometheus

O Aspect NÃO substitui Prometheus. Use ambos:

- **Logs**: Debug de requisições específicas ("por que ESTA request demorou?")
- **Prometheus**: Métricas agregadas (p50, p95, p99 de todos requests)

## Performance

- **Overhead**: ~0.1ms por método anotado
- **Recomendação**: Anotar apenas métodos de entrada (controllers e services principais)
- **Produção**: Logs em INFO level (pode mudar para WARN se necessário reduzir volume)

## LGPD Compliance

Todos os argumentos são automaticamente mascarados:

- CPF: `***.***.789-10`
- CNPJ: `**.***.***/**01-81`
- Email: `jo***@example.com`
- UUIDs: `12345678...` (primeiros 8 chars)
- DTOs: Nome da classe apenas

**NUNCA** loga dados sensíveis sem mascaramento!
