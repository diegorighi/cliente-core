# Arquitetura de Integração - Cliente Core

**Versão:** 1.0
**Data:** 2025-11-03
**Padrão:** Híbrido (Step Functions + Kafka)

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Decisão Arquitetural](#decisão-arquitetural)
3. [Integrações Síncronas (Step Functions)](#integrações-síncronas-step-functions)
4. [Integrações Assíncronas (Kafka)](#integrações-assíncronas-kafka)
5. [Eventos Publicados](#eventos-publicados)
6. [Eventos Consumidos](#eventos-consumidos)
7. [Idempotência](#idempotência)
8. [Correlation ID](#correlation-id)
9. [Diagramas](#diagramas)

---

## Visão Geral

O **cliente-core** utiliza uma arquitetura **híbrida** para integração com outros microserviços:

- **Step Functions (AWS)**: Orquestração de fluxos transacionais síncronos com SAGA pattern
- **Kafka (MSK)**: Propagação de eventos assíncronos para analytics e notificações

### Princípios de Design

1. **Transações críticas** = Step Functions (rollback garantido, latência baixa)
2. **Propagação de estado** = Kafka (eventual consistency, event replay)
3. **Idempotência obrigatória** em ambos os padrões
4. **Correlation ID** propagado em todas as integrações

---

## Decisão Arquitetural

### Por que Híbrido?

| Critério | Step Functions | Kafka | Híbrido |
|----------|---------------|-------|---------|
| Latência | ✅ Baixa (50-100ms) | ❌ Alta (assíncrono) | ✅ Melhor de ambos |
| Rollback | ✅ Built-in SAGA | ❌ Manual | ✅ Onde necessário |
| Event Replay | ❌ Não suporta | ✅ Suporta | ✅ Para analytics |
| Debugging | ✅ Console visual | ❌ Logs distribuídos | ✅ Transações principais |
| Custo | ✅ $2.50/10k | ❌ $146/mês fixo | ✅ Otimizado |

**Decisão:** Usar Step Functions para **transações que afetam múltiplos domínios** e Kafka para **propagação de eventos não-críticos**.

---

## Integrações Síncronas (Step Functions)

### Quando Usar Step Functions

✅ **Use quando:**
- Operação envolve **múltiplos microserviços** em sequência
- **Rollback é crítico** (ex: pagamento → reserva → confirmação)
- Usuário **está esperando resposta** (requisição HTTP)
- Timeout máximo: **30 segundos**

❌ **NÃO use quando:**
- Operação é apenas **informativa** (analytics, métricas)
- Pode ser **eventual consistent** (não precisa ser imediato)
- Envolve **notificações** (email, push, SMS)

### Fluxos do cliente-core que USAM Step Functions

#### ❌ NENHUM (cliente-core é somente CRUD)

O cliente-core **NÃO inicia Step Functions**. Ele apenas:
1. Expõe endpoints REST (CRUD de clientes)
2. Publica eventos Kafka quando dados mudam
3. **É chamado POR Step Functions** de outros microserviços

**Exemplo: Step Function de outro MS chama cliente-core:**

```json
{
  "Comment": "Fluxo de Venda - Validar Cliente (venda-core)",
  "StartAt": "ValidarCompradorExiste",
  "States": {
    "ValidarCompradorExiste": {
      "Type": "Task",
      "Resource": "arn:aws:states:::http:invoke",
      "Parameters": {
        "ApiEndpoint": "https://cliente-core/v1/clientes/pf/${compradorId}",
        "Method": "GET",
        "Headers": {
          "X-Correlation-ID.$": "$.correlationId",
          "Authorization": "Bearer ${token}"
        }
      },
      "Retry": [{
        "ErrorEquals": ["States.TaskFailed"],
        "IntervalSeconds": 1,
        "MaxAttempts": 3,
        "BackoffRate": 2.0
      }],
      "Catch": [{
        "ErrorEquals": ["ClienteNaoEncontrado"],
        "Next": "Falha_ClienteNaoEncontrado"
      }],
      "ResultPath": "$.comprador",
      "Next": "ValidarVendedorExiste"
    },

    "ValidarVendedorExiste": {
      "Type": "Task",
      "Resource": "arn:aws:states:::http:invoke",
      "Parameters": {
        "ApiEndpoint": "https://cliente-core/v1/clientes/pf/${vendedorId}",
        "Method": "GET",
        "Headers": {
          "X-Correlation-ID.$": "$.correlationId"
        }
      },
      "ResultPath": "$.vendedor",
      "Next": "CriarVenda"
    },

    "CriarVenda": {
      "Type": "Task",
      "Resource": "arn:aws:states:::http:invoke",
      "Parameters": {
        "ApiEndpoint": "https://venda-core/v1/vendas",
        "Method": "POST"
      },
      "Next": "Sucesso"
    }
  }
}
```

### Endpoints Preparados para Step Functions

Todos os endpoints do cliente-core já estão prontos:

| Endpoint | Método | Idempotência | Retry-Safe |
|----------|--------|--------------|------------|
| `GET /v1/clientes/pf/{publicId}` | GET | ✅ Sim (read-only) | ✅ Sim |
| `GET /v1/clientes/pj/{publicId}` | GET | ✅ Sim (read-only) | ✅ Sim |
| `POST /v1/clientes/pf` | POST | ⚠️ Implementar (via X-Idempotency-Key) | ❌ Não (sem idempotency) |
| `PUT /v1/clientes/pf/{publicId}` | PUT | ✅ Sim (idempotente por natureza) | ✅ Sim |

**TODO: Implementar Idempotency-Key para POST (Feature DELETE).**

---

## Integrações Assíncronas (Kafka)

### Quando Usar Kafka

✅ **Use quando:**
- Operação é **informativa** (não afeta transação principal)
- Múltiplos consumidores interessados (**fan-out**)
- Precisa de **event replay** (analytics, auditoria)
- Eventual consistency é **aceitável**

❌ **NÃO use quando:**
- Usuário está **esperando resposta**
- Rollback é **crítico**
- Timeout < 5 segundos

### Topics Kafka do cliente-core

#### 📤 **Eventos PUBLICADOS** (Producer)

O cliente-core publica eventos quando o estado de um cliente muda:

| Topic | Event | Quando Publicar | Payload |
|-------|-------|-----------------|---------|
| `cliente-events` | `ClientePFCriado` | POST /v1/clientes/pf (sucesso) | `{clienteId, cpf, nome, email, timestamp, correlationId}` |
| `cliente-events` | `ClientePJCriado` | POST /v1/clientes/pj (sucesso) | `{clienteId, cnpj, razaoSocial, email, timestamp, correlationId}` |
| `cliente-events` | `ClientePFAtualizado` | PUT /v1/clientes/pf/{id} (sucesso) | `{clienteId, camposAlterados[], timestamp, correlationId}` |
| `cliente-events` | `ClientePJAtualizado` | PUT /v1/clientes/pj/{id} (sucesso) | `{clienteId, camposAlterados[], timestamp, correlationId}` |
| `cliente-events` | `ClienteDeletado` | DELETE /v1/clientes/{id} (futuro) | `{clienteId, motivo, timestamp, correlationId}` |

**Exemplo de Evento:**
```json
{
  "eventType": "ClientePFCriado",
  "eventVersion": "1.0",
  "correlationId": "abc-123-def-456",
  "timestamp": "2025-11-03T19:00:00.000Z",
  "payload": {
    "clienteId": "550e8400-e29b-41d4-a716-446655440000",
    "cpf": "***.***.789-10",
    "primeiroNome": "João",
    "sobrenome": "Silva",
    "email": "jo***@example.com",
    "tipoCliente": "COMPRADOR",
    "ativo": true
  },
  "metadata": {
    "service": "cliente-core",
    "version": "1.0.0",
    "environment": "production"
  }
}
```

#### 📥 **Eventos CONSUMIDOS** (Consumer)

O cliente-core consome eventos de outros microserviços para atualizar métricas agregadas:

| Topic | Event | Ação | Campo Atualizado |
|-------|-------|------|------------------|
| `venda-events` | `VendaConcluida` | Incrementar métricas do vendedor | `totalVendasRealizadas`, `valorTotalVendas` |
| `venda-events` | `CompraConcluida` | Incrementar métricas do comprador | `totalComprasRealizadas`, `valorTotalCompras` |
| `venda-events` | `VendaCancelada` | Decrementar métricas | Rollback dos contadores |

**Exemplo de Consumer:**
```java
@Service
public class VendaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(VendaEventConsumer.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @KafkaListener(
        topics = "venda-events",
        groupId = "cliente-core-metrics-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleVendaConcluida(VendaConcluidaEvent event) {
        MDC.put("correlationId", event.getCorrelationId());
        MDC.put("operationType", "CONSUME_VENDA_CONCLUIDA");

        try {
            log.info("Evento recebido - VendaId: {}, VendedorId: {}, CompradorId: {}",
                     event.getVendaId(),
                     event.getVendedorId(),
                     event.getCompradorId());

            // Idempotência: verifica se já processou
            if (eventoJaProcessado(event.getVendaId())) {
                log.warn("Evento já processado - VendaId: {}", event.getVendaId());
                return;
            }

            // Atualiza métricas do vendedor
            Cliente vendedor = clienteRepository.findByPublicId(event.getVendedorId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(event.getVendedorId()));

            vendedor.incrementarTotalVendas(event.getValorTotal());
            clienteRepository.save(vendedor);

            // Atualiza métricas do comprador
            Cliente comprador = clienteRepository.findByPublicId(event.getCompradorId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(event.getCompradorId()));

            comprador.incrementarTotalCompras(event.getValorTotal());
            clienteRepository.save(comprador);

            // Marca evento como processado
            marcarEventoProcessado(event.getVendaId());

            log.info("Métricas atualizadas - VendedorId: {}, CompradorId: {}",
                     event.getVendedorId(),
                     event.getCompradorId());

        } catch (Exception e) {
            log.error("Erro ao processar evento - VendaId: {}, Erro: {}",
                     event.getVendaId(),
                     e.getMessage(),
                     e);
            throw e; // Kafka vai reprocessar (retry)
        } finally {
            MDC.remove("correlationId");
            MDC.remove("operationType");
        }
    }

    private boolean eventoJaProcessado(UUID vendaId) {
        // TODO: Implementar tabela de idempotência
        // CREATE TABLE eventos_processados (
        //   evento_id UUID PRIMARY KEY,
        //   processado_em TIMESTAMP DEFAULT NOW()
        // );
        return false;
    }

    private void marcarEventoProcessado(UUID vendaId) {
        // TODO: INSERT INTO eventos_processados (evento_id) VALUES (?);
    }
}
```

---

## Eventos Publicados

### Estrutura Padrão de Evento

Todos os eventos seguem o schema:

```json
{
  "eventType": "string",           // Nome do evento (ex: ClientePFCriado)
  "eventVersion": "string",         // Versão do schema (ex: 1.0)
  "correlationId": "string (UUID)", // Correlation ID da requisição original
  "timestamp": "string (ISO8601)",  // Quando o evento ocorreu
  "payload": {                      // Dados específicos do evento
    // ... campos específicos
  },
  "metadata": {                     // Metadados do serviço
    "service": "cliente-core",
    "version": "1.0.0",
    "environment": "production"
  }
}
```

### Event Schema: ClientePFCriado

**Topic:** `cliente-events`
**Partition Key:** `clienteId`
**Retention:** 7 dias

```json
{
  "eventType": "ClientePFCriado",
  "eventVersion": "1.0",
  "correlationId": "uuid",
  "timestamp": "2025-11-03T19:00:00.000Z",
  "payload": {
    "clienteId": "uuid",
    "cpf": "string (masked)",
    "primeiroNome": "string",
    "nomeDoMeio": "string?",
    "sobrenome": "string",
    "email": "string (masked)",
    "dataNascimento": "string (ISO date)",
    "sexo": "MASCULINO|FEMININO|NAO_INFORMADO",
    "tipoCliente": "CONSIGNANTE|COMPRADOR|AMBOS",
    "ativo": true
  },
  "metadata": {
    "service": "cliente-core",
    "version": "1.0.0",
    "environment": "production"
  }
}
```

**Consumidores:**
- `analytics-core` - Armazena no Data Lake
- `notificacao-core` - Envia email de boas-vindas
- `auditoria-core` - Registra criação

### Event Schema: ClientePFAtualizado

**Topic:** `cliente-events`
**Partition Key:** `clienteId`

```json
{
  "eventType": "ClientePFAtualizado",
  "eventVersion": "1.0",
  "correlationId": "uuid",
  "timestamp": "2025-11-03T19:00:00.000Z",
  "payload": {
    "clienteId": "uuid",
    "camposAlterados": [
      {
        "campo": "email",
        "valorAnterior": "an***@example.com",
        "valorNovo": "no***@example.com"
      },
      {
        "campo": "telefone",
        "valorAnterior": "(11) ****-1234",
        "valorNovo": "(11) ****-5678"
      }
    ]
  },
  "metadata": {
    "service": "cliente-core",
    "version": "1.0.0",
    "environment": "production"
  }
}
```

**Consumidores:**
- `auditoria-core` - Registra alterações
- `analytics-core` - Atualiza perfil

---

## Eventos Consumidos

### Event Schema: VendaConcluida

**Topic:** `venda-events`
**Group ID:** `cliente-core-metrics-group`

```json
{
  "eventType": "VendaConcluida",
  "eventVersion": "1.0",
  "correlationId": "uuid",
  "timestamp": "2025-11-03T19:00:00.000Z",
  "payload": {
    "vendaId": "uuid",
    "vendedorId": "uuid",
    "compradorId": "uuid",
    "produtoId": "uuid",
    "valorProduto": 1000.00,
    "taxaPlataforma": 30.00,
    "valorTotal": 1030.00
  }
}
```

**Ação:** Atualizar campos no cliente:
- `vendedor.totalVendasRealizadas += 1`
- `vendedor.valorTotalVendas += valorProduto`
- `comprador.totalComprasRealizadas += 1`
- `comprador.valorTotalCompras += valorTotal`

### Event Schema: VendaCancelada

**Topic:** `venda-events`
**Group ID:** `cliente-core-metrics-group`

```json
{
  "eventType": "VendaCancelada",
  "eventVersion": "1.0",
  "correlationId": "uuid",
  "timestamp": "2025-11-03T19:00:00.000Z",
  "payload": {
    "vendaId": "uuid",
    "vendedorId": "uuid",
    "compradorId": "uuid",
    "motivo": "string",
    "valorProduto": 1000.00,
    "valorTotal": 1030.00
  }
}
```

**Ação:** Rollback das métricas:
- `vendedor.totalVendasRealizadas -= 1`
- `vendedor.valorTotalVendas -= valorProduto`
- `comprador.totalComprasRealizadas -= 1`
- `comprador.valorTotalCompras -= valorTotal`

---

## Idempotência

### Por que Idempotência é Crítica?

Kafka pode entregar a mesma mensagem **múltiplas vezes** devido a:
- Retry do producer
- Rebalanceamento de consumer group
- Network partition

**Sem idempotência:**
```
VendaConcluida (evento duplicado)
→ totalVendasRealizadas += 1 (2x)
→ DADOS CORROMPIDOS!
```

### Implementação de Idempotência

**Tabela de Eventos Processados:**
```sql
CREATE TABLE eventos_processados (
    evento_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processado_em TIMESTAMP DEFAULT NOW(),
    consumer_group VARCHAR(100) NOT NULL,
    INDEX idx_event_type_processado (event_type, processado_em)
);
```

**Repository:**
```java
@Repository
public interface EventoProcessadoRepository extends JpaRepository<EventoProcessado, UUID> {
    boolean existsByEventoId(UUID eventoId);
}
```

**Consumer com Idempotência:**
```java
@Transactional
public void handleVendaConcluida(VendaConcluidaEvent event) {
    // 1. Verifica se já processou
    if (eventoProcessadoRepository.existsByEventoId(event.getVendaId())) {
        log.warn("Evento duplicado ignorado - VendaId: {}", event.getVendaId());
        return; // PARA AQUI!
    }

    // 2. Processa evento
    atualizarMetricas(event);

    // 3. Marca como processado (MESMA TRANSAÇÃO)
    EventoProcessado registro = new EventoProcessado(
        event.getVendaId(),
        "VendaConcluida",
        "cliente-core-metrics-group"
    );
    eventoProcessadoRepository.save(registro);
}
```

**CRÍTICO:** `@Transactional` garante que se o processamento falhar, o registro de idempotência **NÃO** é salvo, permitindo retry.

---

## Correlation ID

### Propagação em Kafka

**Producer (cliente-core publica):**
```java
ClientePFCriadoEvent event = ClientePFCriadoEvent.builder()
    .correlationId(MDC.get("correlationId")) // ← Pega do MDC
    .clienteId(cliente.getPublicId())
    .build();

kafkaTemplate.send("cliente-events", event);
```

**Consumer (cliente-core consome):**
```java
@KafkaListener(topics = "venda-events")
public void handleVendaConcluida(VendaConcluidaEvent event) {
    MDC.put("correlationId", event.getCorrelationId()); // ← Adiciona ao MDC

    try {
        log.info("Processando venda - VendaId: {}", event.getVendaId());
        // Correlation ID está em TODOS os logs agora!
    } finally {
        MDC.remove("correlationId");
    }
}
```

### Rastreamento no CloudWatch

```sql
-- Busca TODA a jornada de uma transação
fields @timestamp, @message, correlationId, service
| filter correlationId = "abc-123"
| sort @timestamp asc
```

**Resultado:**
```
2025-11-03 19:00:00.000  cliente-core   Cliente PF criado - PublicId: uuid-456
2025-11-03 19:00:01.000  analytics-core Evento recebido - ClienteId: uuid-456
2025-11-03 19:00:05.000  venda-core     Venda criada - VendedorId: uuid-456
2025-11-03 19:00:06.000  cliente-core   Métricas atualizadas - VendedorId: uuid-456
```

---

## Diagramas

### Diagrama 1: Cliente-Core no Ecossistema

```
┌────────────────────────────────────────────────────────────┐
│ API Gateway (AWS)                                          │
│ • Gera Correlation ID se não existir                       │
│ • Rate limiting                                            │
└────────────────────────────────────────────────────────────┘
         ↓ (HTTP + X-Correlation-ID)
┌────────────────────────────────────────────────────────────┐
│ cliente-core                                               │
│ ┌────────────────────────────────────────────────────────┐ │
│ │ Endpoints REST (CRUD)                                  │ │
│ │ • POST /v1/clientes/pf                                 │ │
│ │ • PUT  /v1/clientes/pf/{id}                            │ │
│ │ • GET  /v1/clientes/pf/{id}                            │ │
│ └────────────────────────────────────────────────────────┘ │
│              ↓ (após sucesso)                              │
│ ┌────────────────────────────────────────────────────────┐ │
│ │ Kafka Producer                                         │ │
│ │ • Topic: cliente-events                                │ │
│ │ • Event: ClientePFCriado                               │ │
│ └────────────────────────────────────────────────────────┘ │
│              ↓                                             │
│ ┌────────────────────────────────────────────────────────┐ │
│ │ Kafka Consumer                                         │ │
│ │ • Topic: venda-events                                  │ │
│ │ • Event: VendaConcluida                                │ │
│ │ • Ação: Atualizar métricas                             │ │
│ └────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
         ↓ (Kafka: cliente-events)
┌─────────────────┬──────────────────┬──────────────────────┐
│ analytics-core  │ notificacao-core │ auditoria-core       │
│ (Data Lake)     │ (Email/Push)     │ (Audit Log)          │
└─────────────────┴──────────────────┴──────────────────────┘
```

### Diagrama 2: Fluxo de Venda com Step Functions

```
┌────────────────────────────────────────────────────────────┐
│ venda-core (inicia Step Function)                         │
└────────────────────────────────────────────────────────────┘
         ↓ (StartExecution)
┌────────────────────────────────────────────────────────────┐
│ AWS Step Functions: "ProcessarVenda"                       │
│ ┌────────────────────────────────────────────────────────┐ │
│ │ Step 1: Validar Comprador (GET cliente-core)          │ │
│ │         • X-Correlation-ID: abc-123                    │ │
│ │         • Retry: 3x com backoff                        │ │
│ │   ↓                                                    │ │
│ │ Step 2: Validar Vendedor (GET cliente-core)           │ │
│ │         • X-Correlation-ID: abc-123                    │ │
│ │   ↓                                                    │ │
│ │ Step 3: Criar Venda (POST venda-core)                 │ │
│ │   ↓                                                    │ │
│ │ Step 4: Reservar Produto (PUT produto-core)           │ │
│ │   ↓                                                    │ │
│ │ Step 5: Processar Pagamento (POST financeiro-core)    │ │
│ │   ↓ (Sucesso)                                          │ │
│ │ Step 6: Publicar Kafka (Lambda)                       │ │
│ │         • Event: VendaConcluida                        │ │
│ │         • Topic: venda-events                          │ │
│ └────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
         ↓ (Kafka: venda-events)
┌────────────────────────────────────────────────────────────┐
│ cliente-core (Consumer)                                    │
│ • Consome VendaConcluida                                   │
│ • Atualiza vendedor.totalVendasRealizadas                  │
│ • Atualiza comprador.totalComprasRealizadas                │
└────────────────────────────────────────────────────────────┘
```

---

## Próximas Implementações

### Feature 4: Kafka Event Integration (Roadmap)

**O que será implementado:**
1. ✅ Kafka Producer (publicar ClientePFCriado, ClientePJCriado)
2. ✅ Kafka Consumer (consumir VendaConcluida)
3. ✅ Tabela de idempotência (eventos_processados)
4. ✅ Configuração Kafka (application.yml)
5. ✅ Testes de integração (Testcontainers + Kafka)

**Arquivos a serem criados:**
- `infrastructure/event/producer/ClienteEventProducer.java`
- `infrastructure/event/consumer/VendaEventConsumer.java`
- `infrastructure/config/KafkaConfig.java`
- `domain/event/ClientePFCriadoEvent.java`
- `domain/event/VendaConcludaEvent.java`
- `infrastructure/persistence/EventoProcessadoRepository.java`

**Estimativa:** 18 horas (Day 8-10 do Roadmap)

---

**Última atualização:** 2025-11-03
**Mantido por:** Tech Lead
**Revisão:** Quando Kafka for implementado (Feature 4)
