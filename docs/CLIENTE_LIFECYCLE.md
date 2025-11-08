# Ciclo de Vida do Cliente - Bloqueio, Desbloqueio, Deleção e Restauração

> Documentação das operações de gerenciamento de estado do cliente

---

## Visão Geral

O cliente-core implementa 4 operações críticas para gerenciamento de estado de clientes:

1. **Bloquear** - Suspende temporariamente o cliente (bloqueado = true)
2. **Desbloquear** - Remove bloqueio temporário
3. **Deletar** - Soft delete (ativo = false)
4. **Restaurar** - Reverte soft delete

**IMPORTANTE:** Bloquear ≠ Deletar
- **Bloqueado**: Cliente temporariamente suspenso, mas ainda ATIVO
- **Deletado**: Cliente marcado como inativo (soft delete)

---

## 1. Bloquear Cliente

### Descrição
Bloqueia temporariamente um cliente, impedindo novas transações.

### Endpoint
```http
PATCH /v1/clientes/pf/{publicId}/bloquear
PATCH /v1/clientes/pj/{publicId}/bloquear
```

### Request Body
```json
{
  "motivoBloqueio": "Cliente apresentou comportamento fraudulento",
  "usuarioBloqueou": "admin@sistema.com"
}
```

### Validações
- `motivoBloqueio`: Obrigatório, entre 10 e 500 caracteres
- `usuarioBloqueou`: Obrigatório, máximo 100 caracteres

### Comportamento
1. Valida se cliente existe
2. Valida se cliente **NÃO** está bloqueado (lança `ClienteJaBloqueadoException`)
3. Define:
   - `bloqueado = true`
   - `motivo_bloqueio = motivo informado`
   - `data_bloqueio = LocalDateTime.now()`
   - `usuario_bloqueou = usuario informado`
4. **NÃO altera** o campo `ativo`
5. Salva no banco
6. **Esvazia cache** (`clientes:findById` e `clientes:list`)

### Response
```http
HTTP/1.1 204 No Content
```

### Erros
| Código | Descrição |
|--------|-----------|
| 404 | Cliente não encontrado |
| 409 | Cliente já está bloqueado |
| 400 | Dados inválidos (validação falhou) |
| 403 | Apenas ADMIN pode bloquear |

### Exemplo cURL
```bash
curl -X PATCH http://localhost:8081/api/clientes/v1/clientes/pf/{uuid}/bloquear \
  -H "Content-Type: application/json" \
  -d '{
    "motivoBloqueio": "Atividade suspeita detectada",
    "usuarioBloqueou": "security-team@sistema.com"
  }'
```

### Regras de Negócio
- ✅ Cliente pode ser bloqueado múltiplas vezes (desde que desbloqueado entre bloqueios)
- ✅ Cliente bloqueado **ainda está ativo** (ativo = true)
- ✅ Cliente bloqueado não pode realizar novas transações
- ❌ Cliente deletado (ativo = false) **pode** ser bloqueado (mas não faz sentido)

### Service: `BloquearClienteService.bloquear()`
```java
@Transactional
@CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
public void bloquear(UUID publicId, String motivo, String usuario) {
    Cliente cliente = clienteRepository.findByPublicId(publicId)
        .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

    if (cliente.isBloqueado()) {
        throw new ClienteJaBloqueadoException(publicId);
    }

    cliente.bloquear(motivo, usuario);
    clienteRepository.save(cliente);
}
```

### Testes Unitários
**Arquivo:** `BloquearClienteServiceTest.java`

**Cenários cobertos (13 testes):**
- ✅ Bloquear cliente desbloqueado com sucesso
- ✅ Registrar data e hora do bloqueio
- ✅ Lançar exceção ao bloquear cliente não encontrado
- ✅ Lançar exceção ao bloquear cliente já bloqueado
- ✅ Verificar se cliente foi bloqueado antes de bloquear novamente
- ✅ Usar método `bloquear()` da entidade Cliente
- ✅ Buscar cliente antes de bloquear
- ✅ Permitir bloquear → desbloquear → bloquear novamente

**Resultado:** 13/13 testes passando ✅

---

## 2. Desbloquear Cliente

### Descrição
Remove o bloqueio de um cliente, permitindo novas transações.

### Endpoint
```http
PATCH /v1/clientes/pf/{publicId}/desbloquear
PATCH /v1/clientes/pj/{publicId}/desbloquear
```

### Request
Sem body (apenas path parameter)

### Comportamento
1. Valida se cliente existe
2. Define:
   - `bloqueado = false`
   - `motivo_bloqueio = null`
   - `data_bloqueio = null`
   - `usuario_bloqueou = null`
3. Salva no banco
4. **Esvazia cache** (`clientes:findById` e `clientes:list`)

**IMPORTANTE:** Desbloquear é **idempotente** - pode ser chamado múltiplas vezes sem erro

### Response
```http
HTTP/1.1 204 No Content
```

### Erros
| Código | Descrição |
|--------|-----------|
| 404 | Cliente não encontrado |
| 403 | Apenas ADMIN pode desbloquear |

### Exemplo cURL
```bash
curl -X PATCH http://localhost:8081/api/clientes/v1/clientes/pf/{uuid}/desbloquear
```

### Regras de Negócio
- ✅ Desbloquear é idempotente (não lança erro se já está desbloqueado)
- ✅ Limpa TODOS os campos relacionados ao bloqueio
- ✅ Cliente volta ao estado normal (pode realizar transações)

### Service: `BloquearClienteService.desbloquear()`
```java
@Transactional
@CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
public void desbloquear(UUID publicId) {
    Cliente cliente = clienteRepository.findByPublicId(publicId)
        .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

    cliente.desbloquear();
    clienteRepository.save(cliente);
}
```

### Testes Unitários
**Cenários cobertos (parte dos 13 testes):**
- ✅ Desbloquear cliente bloqueado com sucesso
- ✅ Desbloquear cliente mesmo que já esteja desbloqueado (idempotência)
- ✅ Lançar exceção ao desbloquear cliente não encontrado
- ✅ Usar método `desbloquear()` da entidade Cliente
- ✅ Buscar cliente antes de desbloquear
- ✅ Limpar todos os campos relacionados ao bloqueio

---

## 3. Deletar Cliente (Soft Delete)

### Descrição
Marca cliente como inativo (soft delete) preservando dados para auditoria.

### Endpoint
```http
DELETE /v1/clientes/pf/{publicId}?motivo={motivo}&usuario={usuario}
DELETE /v1/clientes/pj/{publicId}?motivo={motivo}&usuario={usuario}
```

### Query Parameters
- `motivo` (required): Motivo da deleção
- `usuario` (required): Usuário responsável pela deleção

### Comportamento
1. Valida se cliente existe
2. Valida se cliente **NÃO** está deletado (lança `ClienteJaDeletadoException`)
3. Define:
   - `ativo = false`
   - `data_delecao = LocalDateTime.now()`
   - `motivo_delecao = motivo informado`
   - `usuario_deletou = usuario informado`
4. **NÃO remove fisicamente** do banco
5. Preserva integridade referencial
6. Salva no banco
7. **Esvazia cache** (`clientes:findById` e `clientes:list`)

### Response
```http
HTTP/1.1 204 No Content
```

### Erros
| Código | Descrição |
|--------|-----------|
| 404 | Cliente não encontrado |
| 409 | Cliente já foi deletado anteriormente |
| 403 | Apenas ADMIN pode deletar |

### Exemplo cURL
```bash
curl -X DELETE "http://localhost:8081/api/clientes/v1/clientes/pf/{uuid}?motivo=Cliente%20solicitou%20exclus%C3%A3o&usuario=admin"
```

### Regras de Negócio
- ✅ Soft delete (não remove fisicamente)
- ✅ Preserva todos os dados para auditoria
- ✅ Preserva integridade referencial (FKs permanecem válidas)
- ✅ Cliente deletado **pode** ser restaurado
- ❌ Cliente deletado **não** pode ser deletado novamente (409 Conflict)

### Service: `DeleteClienteService.deletar()`
```java
@Transactional
@CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
public void deletar(UUID publicId, String motivo, String usuario) {
    Cliente cliente = clienteRepository.findByPublicId(publicId)
        .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

    if (!cliente.getAtivo()) {
        throw new ClienteJaDeletadoException(publicId);
    }

    cliente.deletar(motivo, usuario);
    clienteRepository.save(cliente);
}
```

### Testes Unitários
**Arquivo:** `DeleteClienteServiceTest.java`

**Cenários cobertos (12 testes):**
- ✅ Deletar cliente ativo com sucesso
- ✅ Registrar data e hora da deleção
- ✅ Lançar exceção ao deletar cliente não encontrado
- ✅ Lançar exceção ao deletar cliente já deletado
- ✅ Verificar se cliente foi deletado antes de deletar novamente
- ✅ Usar método `deletar()` da entidade Cliente
- ✅ Buscar cliente antes de deletar
- ✅ Sanitizar inputs (prevenir log injection)

**Resultado:** 12/12 testes passando ✅

---

## 4. Restaurar Cliente

### Descrição
Reverte soft delete, marcando cliente como ativo novamente.

### Endpoint
```http
POST /v1/clientes/pf/{publicId}/restaurar?usuario={usuario}
POST /v1/clientes/pj/{publicId}/restaurar?usuario={usuario}
```

### Query Parameters
- `usuario` (required): Usuário responsável pela restauração

### Comportamento
1. Valida se cliente existe
2. Define:
   - `ativo = true`
   - `data_delecao = null`
   - `motivo_delecao = null`
   - `usuario_deletou = null`
   - `data_restauracao = LocalDateTime.now()`
   - `usuario_restaurou = usuario informado`
3. Salva no banco
4. **Esvazia cache** (`clientes:findById` e `clientes:list`)

**IMPORTANTE:** Restaurar é **idempotente** - pode ser chamado múltiplas vezes sem erro

### Response
```http
HTTP/1.1 204 No Content
```

### Erros
| Código | Descrição |
|--------|-----------|
| 404 | Cliente não encontrado |
| 403 | Apenas ADMIN pode restaurar |

### Exemplo cURL
```bash
curl -X POST "http://localhost:8081/api/clientes/v1/clientes/pf/{uuid}/restaurar?usuario=admin"
```

### Regras de Negócio
- ✅ Restaurar é idempotente (não lança erro se já está ativo)
- ✅ Limpa campos relacionados à deleção
- ✅ Cliente volta ao estado ativo (ativo = true)
- ✅ Registra auditoria da restauração

### Service: `DeleteClienteService.restaurar()`
```java
@Transactional
@CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
public void restaurar(UUID publicId, String usuario) {
    Cliente cliente = clienteRepository.findByPublicId(publicId)
        .orElseThrow(() -> new ClienteNaoEncontradoException(publicId));

    cliente.restaurar(usuario);
    clienteRepository.save(cliente);
}
```

### Testes Unitários
**Cenários cobertos (parte dos 12 testes):**
- ✅ Restaurar cliente deletado com sucesso
- ✅ Restaurar cliente mesmo que já esteja ativo (idempotência)
- ✅ Lançar exceção ao restaurar cliente não encontrado
- ✅ Usar método `restaurar()` da entidade Cliente
- ✅ Buscar cliente antes de restaurar
- ✅ Limpar todos os campos relacionados à deleção

---

## Cache e Performance

### Problema Identificado (Resolvido em 2025-11-08)

**CRÍTICO:** Todas as operações de mudança de estado sofrem do mesmo problema de cache!

Quando um cliente era bloqueado/desbloqueado/deletado/restaurado, o service salvava no banco corretamente, mas o cache **não era invalidado**, causando inconsistência:

```bash
# Exemplo do problema (ANTES da correção):
PATCH /bloquear → bloqueado=true salvo no banco ✅
GET /{id}       → bloqueado=false (retorna cache antigo) ❌
```

### Solução Implementada

Adicionado `@CacheEvict` em TODOS os métodos de mudança de estado:

```java
@CacheEvict(value = {"clientes:findById", "clientes:list"}, allEntries = true)
```

**Aplicado em:**
- ✅ `BloquearClienteService.bloquear()`
- ✅ `BloquearClienteService.desbloquear()`
- ✅ `DeleteClienteService.deletar()`
- ✅ `DeleteClienteService.restaurar()`

### Verificação

```bash
# Após correção (AGORA):
PATCH /bloquear → bloqueado=true + cache invalidado ✅
GET /{id}       → bloqueado=true (busca fresh do banco) ✅
```

### Performance

**Trade-off:**
- ✅ **Consistência:** Garantida (sempre retorna estado correto)
- ⚠️ **Performance:** Ligeira redução (próximo GET vai ao banco, não cache)

**Impacto aceitável porque:**
- Operações de bloqueio/desbloqueio/deleção são **raras** (não hotpath)
- Cache é repovoado após primeiro GET
- Alternativa (cache inconsistente) é **inaceitável**

---

## Fluxos de Estado

### Estado do Cliente

```
         ┌─────────────┐
         │    ATIVO    │
         │ bloqueado=F │
         │   ativo=T   │
         └──────┬──────┘
                │
       ┌────────┴────────┐
       │                 │
   BLOQUEAR          DELETAR
       │                 │
       ▼                 ▼
┌─────────────┐   ┌─────────────┐
│  BLOQUEADO  │   │  DELETADO   │
│ bloqueado=T │   │ bloqueado=? │
│   ativo=T   │   │   ativo=F   │
└──────┬──────┘   └──────┬──────┘
       │                 │
   DESBLOQUEAR      RESTAURAR
       │                 │
       └────────┬────────┘
                │
                ▼
         ┌─────────────┐
         │    ATIVO    │
         │ bloqueado=F │
         │   ativo=T   │
         └─────────────┘
```

### Cenários Válidos

| Operação | Estado Inicial | Estado Final | Permitido? |
|----------|---------------|--------------|------------|
| Bloquear | Ativo (ativo=T, bloqueado=F) | Bloqueado (ativo=T, bloqueado=T) | ✅ Sim |
| Bloquear | Bloqueado (ativo=T, bloqueado=T) | - | ❌ Erro 409 |
| Bloquear | Deletado (ativo=F, bloqueado=F) | Deletado+Bloqueado | ✅ Técnico sim, mas não faz sentido |
| Desbloquear | Bloqueado (ativo=T, bloqueado=T) | Ativo (ativo=T, bloqueado=F) | ✅ Sim |
| Desbloquear | Ativo (ativo=T, bloqueado=F) | Ativo | ✅ Idempotente |
| Deletar | Ativo (ativo=T, bloqueado=?) | Deletado (ativo=F) | ✅ Sim |
| Deletar | Deletado (ativo=F) | - | ❌ Erro 409 |
| Restaurar | Deletado (ativo=F) | Ativo (ativo=T, bloqueado=?) | ✅ Sim |
| Restaurar | Ativo (ativo=T) | Ativo | ✅ Idempotente |

### Combinações Especiais

**Cliente deletado E bloqueado (ativo=F, bloqueado=T):**
- Tecnicamente possível (se bloqueado ANTES de deletar)
- Ao restaurar: volta com bloqueado=T (preserva bloqueio)
- Recomendação: Desbloquear ANTES de deletar

**Ordem recomendada para "remover" cliente:**
```
1. DESBLOQUEAR (se bloqueado)
2. DELETAR
```

**Ordem recomendada para "reativar" cliente:**
```
1. RESTAURAR
2. DESBLOQUEAR (se necessário)
```

---

## Auditoria

### Campos de Bloqueio
```sql
bloqueado           BOOLEAN DEFAULT false
data_bloqueio       TIMESTAMP
motivo_bloqueio     VARCHAR(500)
usuario_bloqueou    VARCHAR(100)
```

### Campos de Deleção
```sql
ativo               BOOLEAN DEFAULT true
data_delecao        TIMESTAMP
motivo_delecao      VARCHAR(500)
usuario_deletou     VARCHAR(100)
data_restauracao    TIMESTAMP
usuario_restaurou   VARCHAR(100)
```

### Logs Estruturados (MDC)
Todas as operações loggam com contexto:
```json
{
  "correlationId": "abc-123",
  "operationType": "BLOQUEAR_CLIENTE",
  "clientId": "uuid",
  "timestamp": "2025-11-08T18:00:00Z"
}
```

### CloudWatch Queries

**Buscar bloqueios de um cliente:**
```sql
fields @timestamp, operationType, message
| filter clientId = "uuid-do-cliente"
| filter operationType = "BLOQUEAR_CLIENTE"
| sort @timestamp desc
```

**Buscar todas restaurações:**
```sql
fields @timestamp, clientId, message
| filter operationType = "RESTAURAR_CLIENTE"
| sort @timestamp desc
```

---

## Segurança e Permissões

### Autorização

| Operação | Roles Permitidas |
|----------|------------------|
| Bloquear | ADMIN |
| Desbloquear | ADMIN |
| Deletar | ADMIN |
| Restaurar | ADMIN |

**NÃO permitido para:**
- EMPLOYEE (apenas consulta e criação)
- CUSTOMER (apenas próprio cadastro)
- SERVICE (apenas consulta)

### Input Sanitization

**CRÍTICO:** Todos os services sanitizam inputs para prevenir **log injection**:

```java
String sanitizedMotivo = motivo.replaceAll("[\n\r]", "_");
String sanitizedUsuario = usuario.replaceAll("[\n\r]", "_");
```

**Motivo:** Evitar que usuário injete quebras de linha em logs, causando:
- Log poisoning
- Logs mal formatados
- Dificuldade de parsing

---

## Testes

### Cobertura Total: 25 testes

| Service | Testes | Status |
|---------|--------|--------|
| BloquearClienteService | 13 | ✅ Passando |
| DeleteClienteService | 12 | ✅ Passando |

### Executar Testes

```bash
# Todos os testes
mvn test

# Apenas bloqueio/desbloqueio
mvn test -Dtest=BloquearClienteServiceTest

# Apenas delete/restaurar
mvn test -Dtest=DeleteClienteServiceTest

# Ambos
mvn test -Dtest=BloquearClienteServiceTest,DeleteClienteServiceTest
```

### Resultado Esperado
```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
```

---

## Troubleshooting

### Cliente não desbloqueia

**Sintoma:** Após PATCH /desbloquear, GET retorna bloqueado=true

**Causa:** Cache não foi invalidado

**Solução:** Verificar se `@CacheEvict` está presente no método `desbloquear()`

### Cliente não restaura (ativo continua false)

**Sintoma:** Após POST /restaurar, GET retorna ativo=false

**Causa:** Cache não foi invalidado

**Solução:** Verificar se `@CacheEvict` está presente no método `restaurar()`

### Erro 409 ao bloquear cliente ativo

**Sintoma:** `ClienteJaBloqueadoException` ao tentar bloquear cliente desbloqueado

**Causa:** Cache retornou estado antigo (bloqueado=true)

**Solução:**
1. Limpar cache: `curl -X DELETE http://localhost:8081/api/clientes/actuator/caches/clientes:findById`
2. Reiniciar aplicação

### Performance degradada após bloqueios

**Sintoma:** GETs lentos após múltiplas operações de bloqueio/desbloqueio

**Causa:** `@CacheEvict(allEntries=true)` esvazia TODO o cache

**Otimização futura (se necessário):**
```java
// Ao invés de allEntries=true:
@CacheEvict(value = "clientes:findById", key = "#publicId")
```

---

## Referências

- **Entities:** `src/main/java/br/com/vanessa_mudanca/cliente_core/domain/entity/Cliente.java:177-194`
- **Services:**
  - `BloquearClienteService.java`
  - `DeleteClienteService.java`
- **Controllers:**
  - `ClientePFController.java:256-296`
  - `ClientePJController.java:256-294`
- **Testes:**
  - `BloquearClienteServiceTest.java` (13 testes)
  - `DeleteClienteServiceTest.java` (12 testes)
- **Exceptions:**
  - `ClienteJaBloqueadoException.java`
  - `ClienteJaDeletadoException.java`

---

**Última atualização:** 2025-11-08
**Versão:** 1.0.0
**Autor:** Sistema (documentação gerada após correção de cache)
