# QA Test Plan - UpdateClientePF Feature

**Feature:** Update Cliente Pessoa Física (Aggregate Update)
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

Single unified endpoint for updating ClientePF aggregate:
- **Endpoint:** `PUT /v1/clientes/pf/{publicId}`
- **Pattern:** Aggregate Update (DDD)
- **Scope:** Client basic data + documents + addresses + contacts
- **Update Strategy:** Selective (only present fields/entities are updated)

### Key Business Rules

1. **Immutability:** CPF, RG, CNPJ numbers cannot be changed
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

#### UPDATE_PF_001: Full Aggregate Update
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Update all client data including documents, addresses, and contacts in single request.

**Pre-conditions:**
- Client PF with publicId exists (use seed client #1)
- Client has at least 1 document, 1 address, 1 contact

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "primeiroNome": "João Atualizado",
    "sobrenome": "Silva Santos",
    "email": "joao.novo@email.com",
    "observacoes": "Cliente VIP - atualizado",
    "documentos": [{
      "id": 1,
      "dataValidade": "2030-12-31",
      "orgaoEmissor": "SSP/SP Atualizado",
      "observacoes": "Documento renovado"
    }],
    "enderecos": [{
      "id": 1,
      "cep": "01310-100",
      "logradouro": "Avenida Paulista Atualizada",
      "numero": "1578",
      "bairro": "Bela Vista",
      "cidade": "São Paulo",
      "estado": "SP"
    }],
    "contatos": [{
      "id": 1,
      "tipoContato": "CELULAR",
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

#### UPDATE_PF_002: Client Data Only Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update only basic client fields, no related entities.

**Pre-conditions:**
- Client PF exists

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "primeiroNome": "Maria",
    "nomeDoMeio": "de",
    "sobrenome": "Oliveira",
    "email": "maria.oliveira@example.com",
    "profissao": "Engenheira de Software",
    "observacoes": "Atualização de dados pessoais"
  }'
```

**Expected Result:**
- HTTP 200 OK
- Only client table updated
- Documents, addresses, contacts remain unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_003: Documents Only Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update only document dates/issuer, no client data or other entities.

**Pre-conditions:**
- Client has at least 2 documents

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [
      {
        "id": 1,
        "dataEmissao": "2024-01-15",
        "dataValidade": "2034-01-15",
        "orgaoEmissor": "SSP/RJ"
      },
      {
        "id": 2,
        "dataValidade": "2029-06-30",
        "observacoes": "Documento renovado antecipadamente"
      }
    ]
  }'
```

**Expected Result:**
- HTTP 200 OK
- Only specified documents updated
- Client basic data unchanged
- Other documents unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_004: Addresses Only Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update only addresses, no other entities.

**Pre-conditions:**
- Client has at least 1 address

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "enderecos": [{
      "id": 1,
      "cep": "22640-100",
      "logradouro": "Avenida das Américas",
      "numero": "4666",
      "complemento": "Bloco 2, Apto 304",
      "bairro": "Barra da Tijuca",
      "cidade": "Rio de Janeiro",
      "estado": "RJ",
      "enderecoPrincipal": true
    }]
  }'
```

**Expected Result:**
- HTTP 200 OK
- Address updated with all new values
- Other entities unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_005: Contacts Only Update
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Update only contacts, verify `verificado` flag reset when value changes.

**Pre-conditions:**
- Client has contact with `verificado = true`

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "contatos": [{
      "id": 1,
      "tipoContato": "CELULAR",
      "valor": "11999998888",
      "observacoes": "Número atualizado"
    }]
  }'
```

**Expected Result:**
- HTTP 200 OK
- Contact value updated
- `verificado` flag automatically set to `false` (behavioral method)
- Other entities unchanged

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_006: Multiple Entities Update
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Update multiple documents, addresses, and contacts in single request.

**Pre-conditions:**
- Client has multiple entities of each type

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [
      {"id": 1, "dataValidade": "2035-01-01"},
      {"id": 2, "orgaoEmissor": "DETRAN/SP"}
    ],
    "enderecos": [
      {"id": 1, "numero": "999", "complemento": "Fundos"},
      {"id": 2, "bairro": "Centro Histórico"}
    ],
    "contatos": [
      {"id": 1, "observacoes": "Contato principal"},
      {"id": 2, "valor": "contato@example.com"}
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

#### UPDATE_PF_007: Empty Request Body
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send empty JSON object to verify no changes occur.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Expected Result:**
- HTTP 200 OK
- No fields updated (idempotent operation)
- Response returns current state

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_008: Null Fields
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Explicitly send `null` values to verify they're ignored.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "primeiroNome": null,
    "email": null,
    "observacoes": null
  }'
```

**Expected Result:**
- HTTP 200 OK
- Null fields ignored (not updated)
- Existing values preserved

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_009: Empty Arrays
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send empty arrays for documents/addresses/contacts.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [],
    "enderecos": [],
    "contatos": []
  }'
```

**Expected Result:**
- HTTP 200 OK
- Empty arrays treated as "no updates" (not deletion)
- Existing entities preserved

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_010: Idempotent Update
**Priority:** LOW
**Status:** ⬜ PENDING

**Description:** Send same values that already exist in database.

**Test Steps:**
1. Query current client data
2. Send PUT with exact same values

**Expected Result:**
- HTTP 200 OK
- No database changes (detected as no-op)
- `dataAtualizacao` timestamp NOT updated

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_011: Minimal Update
**Priority:** LOW
**Status:** ⬜ PENDING

**Description:** Update single field only.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "observacoes": "Single field update test"
  }'
```

**Expected Result:**
- HTTP 200 OK
- Only `observacoes` field updated
- All other fields unchanged

**Actual Result:**
_[To be filled during execution]_

---

### Category 3: Business Rule Validations

#### UPDATE_PF_012: Document Date > 50 Years
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Attempt to set document expiration > 50 years in future.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 1,
      "dataValidade": "2099-12-31"
    }]
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Error message: "Data de validade não pode ser superior a 50 anos no futuro"
- No changes persisted

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_013: Data Validade Before Data Emissao
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Set expiration date before issue date.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 1,
      "dataEmissao": "2025-06-01",
      "dataValidade": "2025-01-01"
    }]
  }'
```

**Expected Result:**
- HTTP 400 Bad Request
- Error message: "Data de validade não pode ser anterior à data de emissão"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_014: Duplicate Principal Address (Same Type)
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Attempt to mark second address as principal when one already exists for same type.

**Pre-conditions:**
- Client has 2 RESIDENCIAL addresses
- Address ID 1 is already `enderecoPrincipal = true`

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "enderecos": [{
      "id": 2,
      "tipoEndereco": "RESIDENCIAL",
      "enderecoPrincipal": true,
      "cep": "01310-100",
      "logradouro": "Avenida Paulista",
      "numero": "100",
      "bairro": "Bela Vista",
      "cidade": "São Paulo",
      "estado": "SP"
    }]
  }'
```

**Expected Result:**
- HTTP 409 Conflict
- Error message: "Já existe um endereço principal do tipo RESIDENCIAL"
- No changes persisted

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_015: Duplicate Principal Contact
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Attempt to mark second contact as principal.

**Pre-conditions:**
- Client has 2 contacts
- Contact ID 1 is already `contatoPrincipal = true`

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "contatos": [{
      "id": 2,
      "tipoContato": "EMAIL",
      "valor": "secundario@example.com",
      "contatoPrincipal": true
    }]
  }'
```

**Expected Result:**
- HTTP 409 Conflict
- Error message: "Já existe um contato principal para este cliente"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_016: Multiple Principal Addresses (Different Types)
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Verify client CAN have principal address for each type (RESIDENCIAL, COMERCIAL, ENTREGA).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "enderecos": [
      {
        "id": 1,
        "tipoEndereco": "RESIDENCIAL",
        "enderecoPrincipal": true,
        "cep": "01310-100",
        "logradouro": "Avenida Paulista",
        "numero": "100",
        "bairro": "Bela Vista",
        "cidade": "São Paulo",
        "estado": "SP"
      },
      {
        "id": 2,
        "tipoEndereco": "COMERCIAL",
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

#### UPDATE_PF_017: Contact Value Change Resets Verification
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Verify `verificado` flag is automatically reset to `false` when contact value changes.

**Pre-conditions:**
- Contact ID 1 has `verificado = true`

**Test Steps:**
1. Query contact: `SELECT verificado FROM contatos WHERE id = 1` → expect `true`
2. Update contact value:
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "contatos": [{
      "id": 1,
      "tipoContato": "CELULAR",
      "valor": "11888887777"
    }]
  }'
```
3. Query again: `SELECT verificado FROM contatos WHERE id = 1` → expect `false`

**Expected Result:**
- HTTP 200 OK
- `verificado` automatically set to `false` by behavioral method
- Requires re-verification

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_018: Contact Type Change Resets Verification
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Verify changing contact type also resets verification flag.

**Pre-conditions:**
- Contact has `verificado = true`

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "contatos": [{
      "id": 1,
      "tipoContato": "EMAIL",
      "valor": "unchanged@example.com"
    }]
  }'
```

**Expected Result:**
- HTTP 200 OK
- `verificado = false` (type changed)

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_019: Unmark Principal Address
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Change principal address to non-principal.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "enderecos": [{
      "id": 1,
      "enderecoPrincipal": false,
      "cep": "01310-100",
      "logradouro": "Avenida Paulista",
      "numero": "100",
      "bairro": "Bela Vista",
      "cidade": "São Paulo",
      "estado": "SP"
    }]
  }'
```

**Expected Result:**
- HTTP 200 OK
- `enderecoPrincipal = false`
- Allowed (client may have 0 principal addresses temporarily)

**Actual Result:**
_[To be filled during execution]_

---

### Category 4: Security & Ownership

#### UPDATE_PF_020: Cross-Client Document Attack
**Priority:** CRITICAL (SECURITY)
**Status:** ⬜ PENDING

**Description:** Attempt to update document belonging to different client.

**Pre-conditions:**
- Client A has publicId `aaaa-bbbb-cccc-dddd`
- Client B has document ID 99

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/aaaa-bbbb-cccc-dddd \
  -H "Content-Type: application/json" \
  -d '{
    "documentos": [{
      "id": 99,
      "observacoes": "Tentativa de ataque"
    }]
  }'
```

**Expected Result:**
- HTTP 400 Bad Request OR 403 Forbidden
- Error: "Documento com ID não pertence ao cliente sendo atualizado"
- No changes to document 99

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_021: Cross-Client Address Attack
**Priority:** CRITICAL (SECURITY)
**Status:** ⬜ PENDING

**Description:** Attempt to update address belonging to different client.

**Test Steps:**
(Same pattern as UPDATE_PF_020, targeting `enderecos`)

**Expected Result:**
- HTTP 400/403
- Error: "Endereço com ID não pertence ao cliente sendo atualizado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_022: Cross-Client Contact Attack
**Priority:** CRITICAL (SECURITY)
**Status:** ⬜ PENDING

**Description:** Attempt to update contact belonging to different client.

**Test Steps:**
(Same pattern as UPDATE_PF_020, targeting `contatos`)

**Expected Result:**
- HTTP 400/403
- Error: "Contato com ID não pertence ao cliente sendo atualizado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_023: PublicId Mismatch (Path vs Body)
**Priority:** HIGH (SECURITY)
**Status:** ⬜ PENDING

**Description:** Send different publicId in request body than URL path.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/aaaa-bbbb-cccc-dddd \
  -H "Content-Type: application/json" \
  -d '{
    "publicId": "zzzz-yyyy-xxxx-wwww",
    "primeiroNome": "Hacker"
  }'
```

**Expected Result:**
- HTTP 200 OK
- Path publicId takes precedence (security measure in controller line 133)
- Update applies to `aaaa-bbbb-cccc-dddd` ONLY

**Actual Result:**
_[To be filled during execution]_

---

### Category 5: Error Handling

#### UPDATE_PF_024: Cliente Not Found
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Attempt to update non-existent client.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/00000000-0000-0000-0000-000000000000 \
  -H "Content-Type: application/json" \
  -d '{
    "primeiroNome": "Ghost"
  }'
```

**Expected Result:**
- HTTP 404 Not Found
- Error: "Cliente com publicId ... não encontrado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_025: Documento Not Found
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Reference non-existent document ID in update request.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{validPublicId} \
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

#### UPDATE_PF_026: Endereco Not Found
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Reference non-existent address ID.

**Expected Result:**
- HTTP 404 Not Found
- Error: "Endereço com ID ... não encontrado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_027: Contato Not Found
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Reference non-existent contact ID.

**Expected Result:**
- HTTP 404 Not Found
- Error: "Contato com ID ... não encontrado"

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_028: Invalid Email Format
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Send invalid email format (Bean Validation).

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
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

### Category 6: Data Integrity & Transactions

#### UPDATE_PF_029: Transaction Rollback on Partial Failure
**Priority:** CRITICAL
**Status:** ⬜ PENDING

**Description:** Verify entire transaction rolls back if any entity update fails.

**Test Steps:**
```bash
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{
    "primeiroNome": "Valid Update",
    "documentos": [{
      "id": 1,
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
- **CRITICAL:** Client name remains unchanged (rollback worked)
- Document validade remains unchanged (rollback worked)

**Verification Query:**
```sql
SELECT primeiro_nome, data_atualizacao
FROM clientes
WHERE public_id = '{publicId}';

SELECT data_validade, data_atualizacao
FROM documentos
WHERE id = 1;
```

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_030: Cascade Update Verification
**Priority:** HIGH
**Status:** ⬜ PENDING

**Description:** Verify all entities in request are updated atomically.

**Test Steps:**
1. Note current `dataAtualizacao` for client, documents, addresses, contacts
2. Execute full aggregate update (UPDATE_PF_001)
3. Query all `dataAtualizacao` timestamps

**Expected Result:**
- All timestamps updated
- All within same second (atomic commit)

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_031: Concurrent Update Handling
**Priority:** CRITICAL
**Status:** ⚠️ BLOCKED - Optimistic Locking Not Implemented

**Description:** Two simultaneous updates to same client.

**Test Steps:**
1. Open 2 terminal windows
2. Execute same update simultaneously

**Expected Result:**
- ⚠️ **RISK IDENTIFIED:** No `@Version` field on Cliente entity
- Current implementation: Last write wins (data corruption risk)
- **RECOMMENDED:** Add `@Version` field for optimistic locking

**Actual Result:**
_[To be filled during execution]_

---

#### UPDATE_PF_032: PreUpdate Hook Execution
**Priority:** MEDIUM
**Status:** ⬜ PENDING

**Description:** Verify `@PreUpdate` hook updates `dataAtualizacao` timestamp.

**Test Steps:**
1. Query current timestamp: `SELECT data_atualizacao FROM clientes WHERE public_id = '{publicId}'`
2. Execute any update
3. Query again and compare

**Expected Result:**
- `dataAtualizacao` automatically updated by `@PreUpdate` hook
- New timestamp > old timestamp

**Actual Result:**
_[To be filled during execution]_

---

## Test Data Requirements

### Seed Clients to Use

```sql
-- Cliente PF #1 (seed 001)
publicId: Use from seeds
- Has CPF document
- Has RG document
- Has 1+ addresses
- Has 1+ contacts

-- Cliente PF #2 (seed 002)
publicId: Use from seeds
- For cross-client attack tests

-- Cliente PF #4 (seed 004)
publicId: Use from seeds
- For concurrent update tests

-- Cliente PF #5 (seed 005)
publicId: Use from seeds
- Backup for edge case tests
```

### Database Verification Queries

```sql
-- Get client with all entities
SELECT
  c.public_id, c.primeiro_nome, c.data_atualizacao,
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

-- Check contact verification after update
SELECT id, valor, verificado, data_atualizacao
FROM contatos
WHERE cliente_id = ?;
```

---

## Acceptance Criteria

### Functional Requirements

- [ ] All Happy Path scenarios (6) pass
- [ ] All Business Rule validations (8) pass
- [ ] All Security tests (4) pass
- [ ] Transaction rollback verified (UPDATE_PF_029)

### Non-Functional Requirements

- [ ] Average response time < 500ms (aggregate update)
- [ ] No SQL N+1 queries (check logs)
- [ ] Proper error messages (user-friendly)
- [ ] HTTP status codes correct (200/400/404/409)

### Security Requirements

- [ ] Cross-client attacks blocked (UPDATE_PF_020, 021, 022)
- [ ] Path publicId enforcement (UPDATE_PF_023)
- [ ] Ownership validation works

### Code Quality

- [ ] Test coverage ≥ 80% (run `mvn clean verify`)
- [ ] No critical SonarQube issues
- [ ] Swagger documentation accurate

### Known Blockers

- [ ] ⚠️ Optimistic locking not implemented (UPDATE_PF_031)
- [ ] ⚠️ Audit trail integration pending
- [ ] ⚠️ OAuth2/JWT authorization pending

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
**Executed By:** _[Pending]_

---

## Appendix

### A. Quick Reference - Curl Commands

```bash
# Basic client update
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{"primeiroNome": "Updated"}'

# Document date update
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{"documentos": [{"id": 1, "dataValidade": "2030-12-31"}]}'

# Address update
curl -X PUT http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} \
  -H "Content-Type: application/json" \
  -d '{"enderecos": [{"id": 1, "numero": "999"}]}'
```

### B. Response Format

**Success (200 OK):**
```json
{
  "publicId": "uuid",
  "primeiroNome": "João",
  "nomeCompleto": "João da Silva",
  "cpf": "123.456.789-09",
  "documentos": [...],
  "enderecos": [...],
  "contatos": [...]
}
```

**Error (400/404/409):**
```json
{
  "timestamp": "2025-11-03T14:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Specific error message",
  "path": "/v1/clientes/pf/{publicId}"
}
```

### C. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-03 | QA Agent | Initial test plan created |

---

**END OF TEST PLAN**
