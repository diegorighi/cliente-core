# QA Test Plan - UpdateClientePJ Feature

**Feature:** Update Cliente Pessoa Jurídica (Aggregate Update)
**Version:** 1.0
**Created:** 2025-11-03
**Last Updated:** 2025-11-03
**Status:** Ready for Execution

---

## Table of Contents

1. [Overview](#overview)
2. [Test Scenarios](#test-scenarios)
   - [Category 1: Happy Path](#category-1-happy-path)
   - [Category 2: Edge Cases](#category-2-edge-cases)
   - [Category 3: Business Rule Validations](#category-3-business-rule-validations)
   - [Category 4: Security & Ownership](#category-4-security--ownership)
   - [Category 5: Error Handling](#category-5-error-handling)
   - [Category 6: Data Integrity](#category-6-data-integrity)
3. [Test Data Requirements](#test-data-requirements)
4. [Acceptance Criteria](#acceptance-criteria)
5. [Test Execution Summary](#test-execution-summary)
6. [Appendix](#appendix)

---

## Overview

### Feature Description

Single unified endpoint for updating ClientePJ aggregate:
- **Endpoint:** `PUT /v1/clientes/pj/{publicId}`
- **Pattern:** Aggregate Update (DDD)
- **Scope:** Client company data + documents + addresses + contacts
- **Update Strategy:** Selective (only present fields/entities are updated)

### Key Business Rules

1. **Immutability:** CNPJ number cannot be changed after creation
2. **Principal Uniqueness:**
   - Only 1 principal document per client
   - Only 1 principal address per type (RESIDENCIAL, COMERCIAL, etc.)
   - Only 1 principal contact per client
3. **Date Validation:** Document expiration dates cannot be > 50 years in future
4. **Contact Verification:** Changing contact value/type invalidates verification flag
5. **Ownership:** Clients can only update their own entities

### Test Environment

- **Database:** PostgreSQL 16.9 (local dev)
- **Seed Data:** 15 clients (10 PF + 5 PJ)
- **Base URL:** `http://localhost:8081/api/clientes`

---

## Test Scenarios

### Category 1: Happy Path

#### UPDATE_PJ_001: Full Aggregate Update
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Update all company data including documents, addresses, and contacts in single request.

**Pre-conditions:**
- Client PJ with publicId exists (use seed client PJ #1)
- Client has at least 1 document, 1 address, 1 contact

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Empresa XYZ Ltda Atualizada",
    "nomeFantasia": "XYZ Comércio Premium",
    "email": "contato@xyzatualizado.com",
    "porteEmpresa": "EPP",
    "capitalSocial": 500000.00,
    "nomeResponsavel": "João Silva Santos",
    "cargoResponsavel": "Diretor Executivo",
    "site": "https://xyzatualizado.com.br",
    "observacoes": "Cliente Premium - Atualizado",
    "documentos": [{
      "id": 11,
      "dataValidade": "2030-12-31",
      "orgaoEmissor": "Junta Comercial SP",
      "observacoes": "Contrato social atualizado"
    }],
    "enderecos": [{
      "id": 16,
      "cep": "01310-100",
      "logradouro": "Avenida Paulista",
      "numero": "1578",
      "bairro": "Bela Vista",
      "cidade": "São Paulo",
      "estado": "SP"
    }],
    "contatos": [{
      "id": 16,
      "tipoContato": "CELULAR_CORPORATIVO",
      "valor": "11987654321",
      "contatoPrincipal": true
    }]
  }'
```

**Expected Result:**
- HTTP 200 OK
- Response contains updated data
- Database reflects all changes
- `dataAtualizacao` timestamp updated for all entities

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_002: Company Data Only Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update only company fields, no related entities.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Nova Razão Social Ltda",
    "nomeFantasia": "Nova Fantasia",
    "inscricaoEstadual": "123.456.789.012",
    "porteEmpresa": "ME",
    "naturezaJuridica": "Sociedade Limitada",
    "capitalSocial": 100000.00
  }'
```

**Expected Result:**
- HTTP 200 OK
- Only company data updated
- Documents, addresses, contacts remain unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_003: Responsible Person Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update legal responsible person data only.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "nomeResponsavel": "Maria Oliveira Costa",
    "cpfResponsavel": "98765432100",
    "cargoResponsavel": "Diretora Financeira"
  }'
```

**Expected Result:**
- HTTP 200 OK
- Responsible person data updated
- Other company data unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_004: Classification Data Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update company classification fields (porte, natureza jurídica, etc).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "porteEmpresa": "Grande Porte",
    "naturezaJuridica": "Sociedade Anônima",
    "atividadePrincipal": "Comércio Varejista de Móveis",
    "capitalSocial": 5000000.00
  }'
```

**Expected Result:**
- HTTP 200 OK
- Classification data updated
- Business rules validated

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_005: Documents Only Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update only document dates/issuer (CNPJ number immutable).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [
      {
        "id": 11,
        "dataEmissao": "2024-01-15",
        "dataValidade": "2034-01-15",
        "orgaoEmissor": "Receita Federal"
      },
      {
        "id": 12,
        "dataValidade": "2029-06-30",
        "observacoes": "Certidão renovada"
      }
    ]
  }'
```

**Expected Result:**
- HTTP 200 OK
- Only specified documents updated
- Company data unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_006: Multiple Entities Batch Update
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Update multiple documents, addresses, and contacts in single atomic transaction.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [
      {"id": 11, "dataValidade": "2035-01-01"},
      {"id": 12, "orgaoEmissor": "Junta Comercial RJ"}
    ],
    "enderecos": [
      {"id": 16, "numero": "1500", "complemento": "Andar 15"},
      {"id": 17, "bairro": "Centro Empresarial"}
    ],
    "contatos": [
      {"id": 16, "observacoes": "Contato principal comercial"},
      {"id": 17, "valor": "contato.comercial@empresa.com"}
    ]
  }'
```

**Expected Result:**
- HTTP 200 OK
- All 6 entities updated atomically
- Transaction commits only if all succeed

**Actual Result:**
_[To be filled during execution]_

---

### Category 2: Edge Cases

#### UPDATE_PJ_007: Empty Request Body
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send empty JSON object to verify idempotent behavior.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Expected Result:**
- HTTP 200 OK
- No fields updated (idempotent)
- Response returns current state

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_008: Null Fields Ignored
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Explicitly send null values to verify they're ignored.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": null,
    "nomeFantasia": null,
    "email": null,
    "porteEmpresa": null
  }'
```

**Expected Result:**
- HTTP 200 OK
- Null fields ignored
- Existing values preserved

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_009: Empty Arrays Behavior
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send empty arrays for related entities.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [],
    "enderecos": [],
    "contatos": []
  }'
```

**Expected Result:**
- HTTP 200 OK
- Empty arrays treated as "no updates"
- Existing entities preserved (not deleted)

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_010: Capital Social Zero
**Priority:** LOW
**Status:** ⬜ PENDING

**Description:** Test edge case with zero capital social (valid for some company types).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "capitalSocial": 0.00
  }'
```

**Expected Result:**
- HTTP 200 OK
- Zero accepted (valid for MEI, individual entrepreneurs)

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_011: Very Long Company Names
**Priority:** LOW
**Status:** ⬜ PENDING

**Description:** Test maximum length for razaoSocial (200 chars).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "A'.repeat(200) + ' Ltda"
  }'
```

**Expected Result:**
- HTTP 200 OK if ≤ 200 chars
- HTTP 400 if > 200 chars

**Actual Result:**
_[To be filled during execution]_

---

### Category 3: Business Rule Validations

#### UPDATE_PJ_012: Document Date > 50 Years
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Attempt to set document expiration > 50 years in future.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 11,
      "dataValidade": "2099-12-31"
    }]
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Error: "Data de validade não pode ser superior a 50 anos no futuro"
- No changes persisted

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_013: Data Validade Before Data Emissao
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Set expiration date before issue date.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 11,
      "dataEmissao": "2025-06-01",
      "dataValidade": "2025-01-01"
    }]
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Error: "Data de validade não pode ser anterior à data de emissão"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_014: Duplicate Principal Address (Same Type)
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Attempt to mark second COMERCIAL address as principal.

**Pre-conditions:**
- Client has 2 COMERCIAL addresses
- Address ID 16 is already `enderecoPrincipal = true`

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "enderecos": [{
      "id": 17,
      "tipoEndereco": "COMERCIAL",
      "enderecoPrincipal": true,
      "cep": "01310-100",
      "logradouro": "Avenida Paulista",
      "numero": "2000",
      "bairro": "Bela Vista",
      "cidade": "São Paulo",
      "estado": "SP"
    }]
  }'
```

**Expected Result:**
- HTTP 409 Conflict
- Error: "Já existe um endereço principal do tipo COMERCIAL"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_015: Duplicate Principal Contact
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Attempt to mark second contact as principal.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "contatos": [{
      "id": 17,
      "tipoContato": "EMAIL_CORPORATIVO",
      "valor": "secundario@empresa.com",
      "contatoPrincipal": true
    }]
  }'
```

**Expected Result:**
- HTTP 409 Conflict
- Error: "Já existe um contato principal para este cliente"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_016: Multiple Principal Addresses (Different Types)
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Verify company CAN have principal address for each type.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "enderecos": [
      {
        "id": 16,
        "tipoEndereco": "COMERCIAL",
        "enderecoPrincipal": true,
        "cep": "01310-100",
        "logradouro": "Avenida Paulista",
        "numero": "1000",
        "bairro": "Bela Vista",
        "cidade": "São Paulo",
        "estado": "SP"
      },
      {
        "id": 17,
        "tipoEndereco": "FISCAL",
        "enderecoPrincipal": true,
        "cep": "04578-000",
        "logradouro": "Avenida Brigadeiro Faria Lima",
        "numero": "2000",
        "bairro": "Itaim Bibi",
        "cidade": "São Paulo",
        "estado": "SP"
      }
    ]
  }'
```

**Expected Result:**
- HTTP 200 OK
- Both addresses updated with `enderecoPrincipal = true`
- Validation allows 1 principal per TYPE

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_017: Contact Value Change Resets Verification
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Verify `verificado` flag is reset when contact value changes.

**Pre-conditions:**
- Contact ID 16 has `verificado = true`

**Test Steps:**
1. Query: `SELECT verificado FROM contatos WHERE id = 16` → expect `true`
2. Update contact:
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "contatos": [{
      "id": 16,
      "tipoContato": "EMAIL_CORPORATIVO",
      "valor": "novoemail@empresa.com"
    }]
  }'
```
3. Query: `SELECT verificado FROM contatos WHERE id = 16` → expect `false`

**Expected Result:**
- HTTP 200 OK
- `verificado` automatically set to `false`

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_018: Negative Capital Social
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Attempt to set negative capital social (should fail).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "capitalSocial": -50000.00
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Bean Validation error for negative value

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_019: Website URL Validation
**Priority:** LOW
**Status:** ⬜ PENDING

**Description:** Test website field accepts valid URLs.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "site": "https://empresa.com.br"
  }'
```

**Expected Result:**
- HTTP 200 OK
- URL stored correctly

**Actual Result:**
_[To be filled during execution]_

---

### Category 4: Security & Ownership

#### UPDATE_PJ_020: Cross-Client Document Attack
**Priority:** CRITICAL (SECURITY)
**Status:** ⬜ PENDING

**Description:** Attempt to update document belonging to different client.

**Pre-conditions:**
- Client PJ A has publicId `aaaa-bbbb-cccc-dddd`
- Client PJ B has document ID 99

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/aaaa-bbbb-cccc-dddd \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 99,
      "observacoes": "Tentativa de ataque"
    }]
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Error: "Documento com ID não pertence ao cliente sendo atualizado"
- No changes to document 99

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_021: Cross-Client Address Attack
**Priority:** CRITICAL (SECURITY)
**Status:** ⬜ PENDING

**Description:** Attempt to update address belonging to different client.

**Test Steps:**
(Same pattern as UPDATE_PJ_020, targeting `enderecos`)

**Expected Result:**
- HTTP 400/403
- Error: "Endereço com ID não pertence ao cliente sendo atualizado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_022: Cross-Client Contact Attack
**Priority:** CRITICAL (SECURITY)
**Status:** ⬜ PENDING

**Description:** Attempt to update contact belonging to different client.

**Test Steps:**
(Same pattern as UPDATE_PJ_020, targeting `contatos`)

**Expected Result:**
- HTTP 400/403
- Error: "Contato com ID não pertence ao cliente sendo atualizado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_023: PublicId Mismatch (Path vs Body)
**Priority:** HIGH (SECURITY)
**Status:** ⬜ PENDING

**Description:** Send different publicId in request body than URL path.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/aaaa-bbbb-cccc-dddd \
  -H "Content-Type: application/json" \
  -d '{
    "publicId": "zzzz-yyyy-xxxx-wwww",
    "razaoSocial": "Hacker Corp"
  }'
```

**Expected Result:**
- HTTP 200 OK
- Path publicId takes precedence (security measure)
- Update applies to `aaaa-bbbb-cccc-dddd` ONLY

**Actual Result:**
_[To be filled during execution]_

---

### Category 5: Error Handling

#### UPDATE_PJ_024: Cliente Not Found
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Attempt to update non-existent client.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/00000000-0000-0000-0000-000000000000 \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Ghost Company"
  }'
```

**Expected Result:**
- HTTP 404 Not Found
- Error: "Cliente com publicId ... não encontrado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_025: Documento Not Found
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Reference non-existent document ID.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{validPublicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 999999,
      "observacoes": "Non-existent"
    }]
  }'
```

**Expected Result:**
- HTTP 404 Not Found
- Error: "Documento com ID 999999 não encontrado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_026: Invalid Email Format
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send invalid email format (Bean Validation).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "email": "not-an-email"
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Validation error for email field

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_027: Malformed JSON
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send invalid JSON syntax.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Missing closing quote
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- JSON parsing error

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_028: Missing Content-Type Header
**Priority:** LOW
**Status:** ⬜ PENDING

**Description:** Send request without Content-Type header.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -d '{"razaoSocial": "Test"}'
```

**Expected Result:**
- HTTP 415 Unsupported Media Type

**Actual Result:**
_[To be filled during execution]_

---

### Category 6: Data Integrity & Transactions

#### UPDATE_PJ_029: Transaction Rollback on Partial Failure
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Verify entire transaction rolls back if any entity update fails.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Valid Update",
    "documentos": [{
      "id": 11,
      "dataValidade": "2035-01-01"
    }],
    "enderecos": [{
      "id": 999999,
      "cep": "01310-100"
    }]
  }'
```

**Expected Result:**
- HTTP 404 Not Found (address not found)
- **CRITICAL:** Company name remains unchanged (rollback worked)
- Document validade remains unchanged (rollback worked)

**Verification Query:**
```sql
SELECT razao_social, data_atualizacao
FROM clientes
WHERE public_id = '{publicId}';

SELECT data_validade, data_atualizacao
FROM documentos
WHERE id = 11;
```

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_030: Cascade Update Verification
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Verify all entities updated atomically with same timestamp.

**Test Steps:**
1. Note current `dataAtualizacao` for all entities
2. Execute full aggregate update (UPDATE_PJ_001)
3. Query all `dataAtualizacao` timestamps

**Expected Result:**
- All timestamps updated
- All within same second (atomic commit)

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_031: Concurrent Update Handling
**Priority:** CRITICAL
**Status:** ⚠️ BLOCKED - Optimistic Locking Not Implemented

**Description:** Two simultaneous updates to same client.

**Test Steps:**
1. Open 2 terminal windows
2. Execute same update simultaneously

**Expected Result:**
- ⚠️ **RISK:** No `@Version` field on Cliente entity
- Current: Last write wins (data corruption risk)
- **RECOMMENDED:** Add `@Version` field

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PJ_032: PreUpdate Hook Execution
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Verify `@PreUpdate` hook updates timestamps.

**Test Steps:**
1. Query: `SELECT data_atualizacao FROM clientes WHERE public_id = '{publicId}'`
2. Execute any update
3. Query again and compare

**Expected Result:**
- `dataAtualizacao` automatically updated
- New timestamp > old timestamp

**Actual Result:**
_[To be filled during execution]_

---

## Test Data Requirements

### Seed Clients to Use

```sql
-- Cliente PJ #1 (seed 001)
publicId: Use from seeds
- Has CNPJ document
- Has 1+ commercial addresses
- Has corporate contacts

-- Cliente PJ #2 (seed 002)
- For cross-client attack tests

-- Cliente PJ #3 (seed 003)
- For concurrent update tests
```

### Database Verification Queries

```sql
-- Get company with all entities
SELECT
  c.public_id, c.razao_social, c.nome_fantasia, c.data_atualizacao,
  d.id as doc_id, d.data_validade,
  e.id as end_id, e.endereco_principal,
  co.id as cont_id, co.verificado
FROM clientes c
LEFT JOIN documentos d ON d.cliente_id = c.id
LEFT JOIN enderecos e ON e.cliente_id = c.id
LEFT JOIN contatos co ON co.cliente_id = c.id
WHERE c.public_id = '{publicId}';

-- Check principal uniqueness
SELECT tipo_endereco, COUNT(*)
FROM enderecos
WHERE cliente_id = ? AND endereco_principal = true
GROUP BY tipo_endereco
HAVING COUNT(*) > 1;
```

---

## Acceptance Criteria

### Functional Requirements

- [ ] All Happy Path scenarios (6) pass
- [ ] All Business Rule validations (8) pass
- [ ] All Security tests (4) pass
- [ ] Transaction rollback verified (UPDATE_PJ_029)

### Non-Functional Requirements

- [ ] Average response time < 500ms (aggregate update)
- [ ] No SQL N+1 queries
- [ ] Proper error messages
- [ ] HTTP status codes correct

### Security Requirements

- [ ] Cross-client attacks blocked (UPDATE_PJ_020-022)
- [ ] Path publicId enforcement (UPDATE_PJ_023)
- [ ] Ownership validation works

### Code Quality

- [ ] Test coverage ≥ 80%
- [ ] No critical SonarQube issues
- [ ] Swagger documentation accurate

---

## Test Execution Summary

| Category | Total | Pass | Fail | Blocked | Pass Rate |
|----------|-------|------|------|---------|-----------|
| Happy Path | 6 | 0 | 0 | 0 | 0% |
| Edge Cases | 5 | 0 | 0 | 0 | 0% |
| Business Rules | 8 | 0 | 0 | 0 | 0% |
| Security | 4 | 0 | 0 | 0 | 0% |
| Error Handling | 5 | 0 | 0 | 0 | 0% |
| Data Integrity | 4 | 0 | 0 | 1 | 0% |
| **TOTAL** | **32** | **0** | **0** | **1** | **0%** |

**Execution Status:** ⬜ NOT STARTED
**Last Execution Date:** _[Pending]_

---

## Appendix

### A. Quick Reference - Curl Commands

```bash
# Basic company update
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{"razaoSocial": "Updated Corp"}'

# Document update
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pj/{publicId} \
  -H "Content-Type: application/json" \
  -d '{"documentos": [{"id": 11, "dataValidade": "2030-12-31"}]}'
```

### B. Response Format

**Success (200 OK):**
```json
{
  "publicId": "uuid",
  "razaoSocial": "Empresa XYZ Ltda",
  "nomeFantasia": "XYZ Comércio",
  "cnpj": "12.345.678/0001-90",
  "documentos": [...],
  "enderecos": [...],
  "contatos": [...]
}
```

### C. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-03 | QA Team | Initial test plan for PJ |

---

**END OF TEST PLAN**
