# 🗺️ Cliente-Core Feature Roadmap - Q4 2025

**Project:** Va Nessa Mudança - Cliente Microservice
**Document Type:** Technical Roadmap
**Created:** 2025-11-03
**Status:** 🟢 APPROVED
**Owner:** Tech Lead + Product Owner
**Review Date:** 2025-12-01

---

## 📊 Executive Summary

This roadmap defines **5 strategic features** to evolve the cliente-core microservice from basic CRUD to a production-grade system with advanced search, data export, event-driven analytics integration, and enterprise logging.

### Key Metrics

| Metric | Value |
|--------|-------|
| **Total Features** | 5 |
| **Total Effort** | 64 hours (~8-10 days) |
| **New Files** | 35 files |
| **Modified Files** | 16 files |
| **New Dependencies** | 8 Maven artifacts |
| **Infrastructure Required** | Kafka MSK cluster |

### Business Value

- ✅ **Feature 1:** Complete CRUD (enables client lifecycle management)
- ✅ **Feature 2:** Reduce search time from manual queries to <1s (UX improvement)
- ✅ **Feature 3:** Enable business reports (CSV/PDF exports)
- ✅ **Feature 4:** Enable data-driven decisions (analytics integration)
- ✅ **Feature 5:** Reduce debugging time by 50% (structured logs)

---

## 🎯 Feature Overview

### Feature Priority Matrix

```
High Business Value
│
│  F3 Export        F2 Search
│  (Reports)        (UX)
│       ●               ●
│
│  F5 Logging      F1 Delete
│  (DevOps)        (CRUD)
│       ●               ●
│
│               F4 Kafka
│               (Analytics)
│                   ●
│
└──────────────────────────────── High Technical Complexity
```

### Implementation Order (Recommended)

```
Week 1: Foundation
├─ Day 1: F5 - Logging JSON (enables debugging for others)
├─ Day 2: F1 - DELETE (completes CRUD)
└─ Day 3-4: F2 - Advanced Search (high user demand)

Week 2: Integration & Reports
├─ Day 5-6: F3 - Export CSV/PDF (business reports)
└─ Day 7-10: F4 - Kafka Events (analytics integration)
```

**Rationale:**
1. **Logging first** = Foundation for debugging all other features
2. **DELETE next** = Quick win, completes CRUD
3. **Search** = High user impact
4. **Export** = Business value (reports)
5. **Kafka last** = Most complex, depends on stable CRUD

---

## 📋 Features Detail

---

## FEATURE 1: DELETE Cliente (Soft Delete)

### Overview

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐ Simples |
| **Effort** | 6 hours |
| **Priority** | HIGH |
| **Dependencies** | None |
| **Risk** | LOW |

### Description

Implement soft delete for Cliente entities (PF and PJ) by setting `ativo = false` instead of physical deletion. Preserves referential integrity and audit trail.

### Technical Decision

**Soft Delete > Hard Delete**

**Reasons:**
- ✅ Preserves audit trail (LGPD compliance)
- ✅ Maintains referential integrity (no orphan records)
- ✅ Enables "undo" functionality (future)
- ✅ Analytics can track churn (deleted clients)

**Trade-off:**
- ⚠️ Database grows larger over time (mitigated by archiving strategy)

### Implementation Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     HTTP Layer                              │
│  DELETE /v1/clientes/{pf|pj}/{publicId}                    │
│  → ClientePFController.deletar(UUID publicId)              │
│  → ClientePJController.deletar(UUID publicId)              │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│                  Application Layer                          │
│  DeleteCliente{PF|PJ}Service (@Transactional)              │
│  1. Find by publicId (404 if not found)                    │
│  2. Validate already deleted (409 if ativo=false)          │
│  3. Set ativo = false                                       │
│  4. Set dataInativacao = now()                             │
│  5. Set usuarioInativou = current user (future)            │
│  6. Save to repository                                      │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│                    Domain Layer                             │
│  Cliente entity adds:                                       │
│  - dataInativacao: LocalDateTime (nullable)                │
│  - usuarioInativou: String (nullable, future)              │
│  - motivoInativacao: String (nullable, future)             │
└─────────────────────────────────────────────────────────────┘
```

### Files to Create (7 files)

```
src/main/java/br/com/vanessa_mudanca/cliente_core/
├── application/ports/input/
│   ├── DeleteClientePFUseCase.java
│   └── DeleteClientePJUseCase.java
├── application/service/
│   ├── DeleteClientePFService.java
│   └── DeleteClientePJService.java
└── domain/exception/
    └── ClienteJaInativoException.java

src/test/java/br/com/vanessa_mudanca/cliente_core/
└── application/service/
    ├── DeleteClientePFServiceTest.java
    └── DeleteClientePJServiceTest.java
```

### Files to Modify (3 files)

```
1. Cliente.java (domain/entity/)
   + dataInativacao: LocalDateTime
   + usuarioInativou: String
   + motivoInativacao: String
   + isAtivo(): boolean

2. ClientePFController.java
   + DELETE /{publicId} endpoint

3. ClientePJController.java
   + DELETE /{publicId} endpoint
```

### Database Changes

**Liquibase Changeset:** `012-add-soft-delete-fields.sql`

```sql
ALTER TABLE clientes
ADD COLUMN data_inativacao TIMESTAMP,
ADD COLUMN usuario_inativou VARCHAR(100),
ADD COLUMN motivo_inativacao VARCHAR(500);

CREATE INDEX idx_clientes_ativo
ON clientes(ativo)
WHERE ativo = false;

COMMENT ON COLUMN clientes.data_inativacao IS 'Data em que o cliente foi desativado (soft delete)';
```

### Dependencies (Maven)

None - uses existing dependencies.

### Configuration (application.yml)

None required.

### Testing Strategy

**Unit Tests (8 scenarios):**
1. ✅ Delete existing PF client successfully
2. ✅ Delete existing PJ client successfully
3. ❌ Delete non-existent client (404)
4. ❌ Delete already deleted client (409)
5. ✅ Verify ativo=false after delete
6. ✅ Verify dataInativacao set correctly
7. ✅ Related entities remain (not cascaded)
8. ✅ Can still query by publicId (but ativo=false)

**Integration Tests:**
- Controller test with MockMvc (HTTP 204)
- Repository test (verify database state)

### QA Test Plan

**File:** `docs/qa/DELETE_CLIENTE_TEST_PLAN.md` (to be created)

**Scenarios:**
1. Happy path (both PF and PJ)
2. Error cases (404, 409)
3. Idempotency (delete twice)
4. Performance (<100ms)
5. Security (cross-client deletion blocked)

### Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Accidentally expose deleted clients in listings | MEDIUM | HIGH | Update ListCliente services to filter `ativo = true` by default |
| Performance degradation with many deleted records | LOW | MEDIUM | Add database partition strategy (future) |
| Compliance (LGPD right to deletion) | LOW | HIGH | Document that physical deletion available on request |

### Success Criteria

- ✅ DELETE endpoints return HTTP 204 No Content
- ✅ Deleted clients have `ativo = false`
- ✅ Cannot delete twice (409 Conflict)
- ✅ Listings exclude deleted clients by default
- ✅ All 8 unit tests passing

### Effort Breakdown

- Design: 1h
- Implementation: 2h
- Testing: 2h
- Documentation: 1h
- **Total: 6h**

---

## FEATURE 2: Advanced Search

### Overview

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐ Média |
| **Effort** | 14 hours |
| **Priority** | HIGH |
| **Dependencies** | None |
| **Risk** | MEDIUM (query performance) |

### Description

Implement advanced search endpoint allowing queries by: name, email, phone, CPF/CNPJ with full-text search support. Uses PostgreSQL native full-text search (no ElasticSearch needed).

### Technical Decision: PostgreSQL Full-Text > ElasticSearch

**Decision:** Use PostgreSQL GIN indexes (already implemented!)

**Reasons:**
- ✅ **Indexes already exist:** `idx_clientes_pf_nome_completo` and `idx_clientes_pj_razao_social` (lines 53-77 in `010-create-indexes.sql`)
- ✅ **Zero infrastructure cost:** Included in RDS PostgreSQL
- ✅ **Sufficient performance:** <1s for 10k clients
- ✅ **Zero operational overhead:** No ElasticSearch cluster to manage
- ✅ **Portuguese language support:** to_tsvector('portuguese', ...) configured

**When to migrate to ElasticSearch:**
- ❌ Volume > 100k clients
- ❌ Need autocomplete <20ms response time
- ❌ Complex geospatial queries
- ❌ Multi-language full-text search

**Cost Comparison:**
- PostgreSQL: $0 additional (already using RDS)
- ElasticSearch: $200-300/month (AWS OpenSearch minimum)

### Implementation Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     HTTP Layer                              │
│  GET /v1/clientes/search?q=joao&tipo=PF&ativo=true         │
│  → ClienteSearchController.search(SearchRequest)           │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│                  Application Layer                          │
│  SearchClienteService                                       │
│  1. Parse search query                                      │
│  2. Build dynamic query (Specification pattern)            │
│  3. Call repository with filters                           │
│  4. Map to SearchResultDTO                                  │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│              Infrastructure Layer                           │
│  ClienteSearchRepository (custom interface)                │
│  → Native SQL query with to_tsvector()                     │
│  → Uses GIN indexes for fast full-text search              │
│                                                             │
│  SELECT c.public_id, c.email,                              │
│         pf.primeiro_nome, pf.sobrenome,                    │
│         pj.razao_social, pj.nome_fantasia                  │
│  FROM clientes c                                            │
│  LEFT JOIN clientes_pf pf ON c.id = pf.id                 │
│  LEFT JOIN clientes_pj pj ON c.id = pj.id                 │
│  WHERE                                                      │
│    to_tsvector('portuguese', pf.primeiro_nome || ' ' ||    │
│                pf.sobrenome) @@ plainto_tsquery('portuguese', :query) │
│    OR c.email ILIKE :email                                 │
│    OR EXISTS (SELECT 1 FROM contatos ct                    │
│                WHERE ct.cliente_id = c.id                  │
│                AND ct.valor ILIKE :phone)                  │
│  ORDER BY c.data_criacao DESC                              │
│  LIMIT :size OFFSET :offset                                │
└─────────────────────────────────────────────────────────────┘
```

### Search Filters Supported

| Filter | Type | Example | Index Used |
|--------|------|---------|------------|
| **q** (query) | Full-text | `q=joao silva` | `idx_clientes_pf_nome_completo` (GIN) |
| **email** | Exact/ILIKE | `email=joao@gmail.com` | `idx_clientes_email` (B-tree) |
| **cpf** | Exact | `cpf=12345678910` | `idx_clientes_pf_cpf` (UNIQUE) |
| **cnpj** | Exact | `cnpj=11222333000181` | `idx_clientes_pj_cnpj` (UNIQUE) |
| **telefone** | ILIKE | `telefone=11987654321` | `idx_contatos_valor` |
| **tipo** | Enum | `tipo=PF` or `tipo=PJ` | Discriminator column |
| **ativo** | Boolean | `ativo=true` | `idx_clientes_ativo` (partial) |
| **tipoCliente** | Enum | `tipoCliente=CONSIGNANTE` | `idx_clientes_tipo_cliente` |

### Files to Create (11 files)

```
src/main/java/br/com/vanessa_mudanca/cliente_core/
├── application/dto/input/
│   └── SearchClienteRequest.java (query params)
├── application/dto/output/
│   └── SearchClienteResponse.java (unified PF+PJ result)
├── application/ports/input/
│   └── SearchClienteUseCase.java
├── application/service/
│   └── SearchClienteService.java
├── infrastructure/repository/
│   ├── ClienteSearchRepository.java (custom interface)
│   └── ClienteSearchRepositoryImpl.java (native SQL)
└── infrastructure/controller/
    └── ClienteSearchController.java

src/test/java/br/com/vanessa_mudanca/cliente_core/
├── application/service/
│   └── SearchClienteServiceTest.java
├── infrastructure/repository/
│   └── ClienteSearchRepositoryTest.java
└── infrastructure/controller/
    └── ClienteSearchControllerTest.java
```

### Files to Modify (2 files)

```
1. application-dev.yml
   + search.max-results: 100 (limit per query)
   + search.default-page-size: 20

2. OpenApiConfig.java
   + Document search endpoint parameters
```

### Database Changes

**None required!** Indexes already exist:
- `idx_clientes_pf_nome_completo` (GIN full-text, Portuguese)
- `idx_clientes_pj_razao_social` (GIN full-text, Portuguese)
- `idx_clientes_email`
- `idx_contatos_valor`

### Dependencies (Maven)

None - uses existing Spring Data JPA.

### Configuration (application.yml)

```yaml
search:
  max-results: 100              # Maximum results per query
  default-page-size: 20         # Default pagination size
  full-text-language: portuguese # PostgreSQL FTS language
```

### Performance Considerations

**Expected Performance:**
- Simple query (name): <100ms
- Complex query (name + email + phone): <500ms
- Volume tested: 10k clients

**Query Optimization:**
- Use `EXPLAIN ANALYZE` to verify index usage
- Limit results to 100 per query
- Implement cursor-based pagination for large result sets (future)

**Monitoring:**
```sql
-- Verify index usage
EXPLAIN ANALYZE
SELECT ... WHERE to_tsvector(...) @@ plainto_tsquery(...);

-- Check for sequential scans (bad!)
-- Should show "Bitmap Index Scan on idx_clientes_pf_nome_completo"
```

### Testing Strategy

**Unit Tests (12 scenarios):**
1. ✅ Search by name (PF) - exact match
2. ✅ Search by name (PF) - partial match
3. ✅ Search by razão social (PJ)
4. ✅ Search by email (exact)
5. ✅ Search by CPF (sanitized)
6. ✅ Search by CNPJ (sanitized)
7. ✅ Search by phone (contatos)
8. ✅ Combined filters (name + email)
9. ✅ Filter by tipo (PF only)
10. ✅ Filter by ativo (active only)
11. ✅ Pagination works correctly
12. ❌ Empty query returns 400

**Performance Tests:**
- 1k concurrent searches
- Response time p95 < 1s
- No database connection pool exhaustion

### QA Test Plan

**File:** `docs/qa/SEARCH_CLIENTE_TEST_PLAN.md` (to be created)

**Scenarios:**
1. Happy path (all filter combinations)
2. Edge cases (empty query, special characters)
3. Performance (1k clients, 10k clients)
4. Security (SQL injection attempts)
5. Relevance ranking (most relevant first)

### Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Query timeout (>30s) | LOW | HIGH | Set `statement_timeout = 5s` in PostgreSQL |
| Index not used (seq scan) | MEDIUM | HIGH | Monitor with EXPLAIN ANALYZE, add missing indexes |
| SQL injection | LOW | CRITICAL | Use parameterized queries only (JPA @Query) |
| PII exposure in logs | MEDIUM | HIGH | Mask CPF/CNPJ/email in logs |

### Success Criteria

- ✅ Search returns results in <1s (95th percentile)
- ✅ GIN indexes are used (verify with EXPLAIN)
- ✅ Supports all 8 filter types
- ✅ Results are paginated correctly
- ✅ All 12 unit tests passing

### Effort Breakdown

- Design: 2h
- Repository (SQL query): 4h
- Service + Controller: 4h
- Testing: 3h
- Documentation: 1h
- **Total: 14h**

---

## FEATURE 3: Export Data (CSV/PDF)

### Overview

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐ Média |
| **Effort** | 14 hours |
| **Priority** | MEDIUM |
| **Dependencies** | Feature 2 (Search) recommended |
| **Risk** | MEDIUM (timeout for large exports) |

### Description

Implement data export endpoints for generating CSV and PDF reports of client data. Supports filtered exports (based on search criteria) and full exports.

### Technical Decision: Synchronous vs Asynchronous

**Decision:** Hybrid approach

| Export Type | Sync/Async | Max Records | Reason |
|-------------|------------|-------------|--------|
| **CSV** | Synchronous | 10,000 | Fast streaming, low memory |
| **PDF (small)** | Synchronous | 100 | Quick generation |
| **PDF (large)** | Async (Future) | Unlimited | Requires background job |

**Rationale:**
- CSV is lightweight (streaming, no DOM)
- PDF for 100 records = ~3s generation time (acceptable)
- Async adds complexity (job queue, polling)

**When to migrate to Async:**
- User requests exports > 100 records
- Average generation time > 10s

### Implementation Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     HTTP Layer                              │
│  GET /v1/clientes/export/csv?q=joao&tipo=PF               │
│  GET /v1/clientes/export/pdf?q=joao&tipo=PF               │
│  → ExportClienteController                                  │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│                  Application Layer                          │
│  ExportClienteService                                       │
│  1. Validate export size (<100 for PDF)                    │
│  2. Fetch data via SearchClienteService                    │
│  3. Delegate to appropriate exporter                       │
│  4. Return StreamingResponseBody                           │
└──────────────┬────────────────────┬─────────────────────────┘
               │                    │
      ┌────────▼────────┐  ┌────────▼────────┐
      │  CSV Exporter   │  │  PDF Exporter   │
      │                 │  │                 │
      │ • Streaming     │  │ • iText 7       │
      │ • RFC 4180      │  │ • Template      │
      │ • UTF-8 BOM     │  │ • Logo + Header │
      └─────────────────┘  └─────────────────┘
```

### Export Formats

#### CSV Format

**Spec:** RFC 4180 compliant
**Encoding:** UTF-8 with BOM
**Separator:** `,` (comma)
**Quote:** `"` (double quote)

**Columns (PF):**
```csv
publicId,primeiroNome,sobrenome,cpf,email,telefone,dataNascimento,tipoCliente,ativo,dataCriacao
550e8400-...,João,Silva,123.456.789-10,joao@email.com,11987654321,1990-01-15,CONSIGNANTE,true,2025-11-01T10:00:00
```

**Columns (PJ):**
```csv
publicId,razaoSocial,nomeFantasia,cnpj,email,telefone,nomeResponsavel,tipoCliente,ativo,dataCriacao
550e8400-...,Empresa XYZ Ltda,XYZ Store,11.222.333/0001-81,contato@xyz.com,1133334444,José Silva,COMPRADOR,true,2025-11-01T10:00:00
```

#### PDF Format

**Library:** iText 7 (Open Source, MPL/LGPL)
**Page Size:** A4 Landscape
**Fonts:** Helvetica (embedded)
**Logo:** Va Nessa Mudança (top left)

**Layout:**
```
┌───────────────────────────────────────────────────────────┐
│ [Logo] Va Nessa Mudança                      2025-11-03  │
│                                                            │
│ Relatório de Clientes                                     │
│ Filtros: tipo=PF, ativo=true                             │
│                                                            │
├────────┬──────────────┬────────────────┬─────────────────┤
│ Nome   │ CPF/CNPJ     │ Email          │ Tipo Cliente    │
├────────┼──────────────┼────────────────┼─────────────────┤
│ João   │ 123.456.789  │ joao@email.com │ CONSIGNANTE     │
│ Maria  │ 987.654.321  │ maria@mail.com │ COMPRADOR       │
│ ...    │ ...          │ ...            │ ...             │
└────────┴──────────────┴────────────────┴─────────────────┘
│                                                            │
│ Total de registros: 10                                    │
│ Gerado em: 2025-11-03 14:35:22                           │
└───────────────────────────────────────────────────────────┘
```

### Files to Create (9 files)

```
src/main/java/br/com/vanessa_mudanca/cliente_core/
├── application/dto/output/
│   ├── ExportClienteDTO.java (simplified DTO for exports)
│   └── ExportMetadata.java (report metadata)
├── application/ports/output/
│   ├── ClienteExporter.java (interface)
│   ├── CsvClienteExporter.java
│   └── PdfClienteExporter.java
├── application/service/
│   └── ExportClienteService.java
└── infrastructure/controller/
    └── ExportClienteController.java

src/test/java/br/com/vanessa_mudanca/cliente_core/
└── application/service/
    ├── CsvClienteExporterTest.java
    └── PdfClienteExporterTest.java
```

### Files to Modify (1 file)

```
1. application-dev.yml
   + export.csv.max-records: 10000
   + export.pdf.max-records: 100
   + export.pdf.logo-path: classpath:static/logo.png
```

### Database Changes

None required.

### Dependencies (Maven)

```xml
<!-- CSV Export -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>

<!-- PDF Export -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>
```

### Configuration (application.yml)

```yaml
export:
  csv:
    max-records: 10000
    encoding: UTF-8
    include-bom: true
  pdf:
    max-records: 100
    page-size: A4_LANDSCAPE
    logo-path: classpath:static/logo-vanessa.png
    font-size: 10
```

### HTTP Response Headers

**CSV:**
```http
Content-Type: text/csv; charset=UTF-8
Content-Disposition: attachment; filename="clientes_2025-11-03_14-35-22.csv"
Transfer-Encoding: chunked
```

**PDF:**
```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="clientes_2025-11-03_14-35-22.pdf"
Content-Length: 45678
```

### Testing Strategy

**Unit Tests (10 scenarios):**
1. ✅ CSV export with 10 records
2. ✅ CSV export with special characters (quotes, commas)
3. ✅ CSV export with UTF-8 characters (ã, é, ç)
4. ✅ PDF export with 10 records
5. ✅ PDF export with logo rendered
6. ❌ PDF export with >100 records (400 Bad Request)
7. ✅ CSV export streams correctly (no OOM)
8. ✅ Filename contains timestamp
9. ✅ Empty result exports empty file
10. ✅ Export respects filters (ativo=true)

**Performance Tests:**
- CSV with 10k records < 10s
- PDF with 100 records < 5s
- Memory usage < 100MB during export

### QA Test Plan

**File:** `docs/qa/EXPORT_CLIENTE_TEST_PLAN.md` (to be created)

**Scenarios:**
1. Happy path (CSV + PDF)
2. Edge cases (empty, 1 record, max records)
3. Special characters (CSV escaping)
4. Performance (10k CSV, 100 PDF)
5. Error handling (invalid filters, timeout)

### Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Timeout for large exports | MEDIUM | HIGH | Limit to 100 PDF, 10k CSV + streaming |
| OutOfMemory during PDF generation | LOW | HIGH | Use iText streaming mode |
| Invalid CSV (Excel compatibility) | MEDIUM | MEDIUM | Add UTF-8 BOM, test with Excel/LibreOffice |
| PII exposure in exports | HIGH | CRITICAL | **Require authentication** (future), log export events |

### Security Considerations

**Current (MVP):**
- ⚠️ No authentication (public endpoint)
- ⚠️ No rate limiting

**Future (Production):**
- ✅ Require OAuth2 token
- ✅ Rate limit: 10 exports/hour per user
- ✅ Log export events to AuditoriaCliente
- ✅ Watermark PDF with user email

### Success Criteria

- ✅ CSV exports stream correctly (no OOM)
- ✅ PDF exports render logo and table
- ✅ Filenames include timestamp
- ✅ Excel can open CSV without errors
- ✅ All 10 unit tests passing
- ✅ Export 10k CSV in <10s

### Effort Breakdown

- Design: 2h
- CSV Exporter: 3h
- PDF Exporter: 5h
- Controller + Service: 2h
- Testing: 2h
- **Total: 14h**

---

## FEATURE 4: Kafka Event Integration (Analytics)

### Overview

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐⭐⭐ Alta |
| **Effort** | 24 hours |
| **Priority** | LOW (but strategic) |
| **Dependencies** | Kafka MSK cluster required |
| **Risk** | HIGH (event loss, infrastructure) |

### Description

Implement event-driven integration with analytics microservice using Apache Kafka (Amazon MSK). Publishes domain events (CREATED, UPDATED, DELETED) for client entities.

### Technical Decision: Kafka > SNS/SQS

**Decision:** Use Apache Kafka (Amazon MSK)

**Comparison Table:**

| Criterion | Kafka MSK | SNS/SQS | Winner |
|-----------|-----------|---------|--------|
| **Event Replay** | ✅ Yes (replay from offset) | ❌ No (1-14 day retention) | Kafka |
| **Ordering** | ✅ Partition-level | ⚠️ FIFO queue only | Kafka |
| **Throughput** | ✅ Millions/sec | ⚠️ 3k/sec (SQS) | Kafka |
| **Latency** | ✅ <10ms | ⚠️ ~100ms | Kafka |
| **Cost (10k msg/day)** | ⚠️ $200-500/month | ✅ $10-50/month | SNS/SQS |
| **Ops Complexity** | ⚠️ High | ✅ Low (managed) | SNS/SQS |
| **Industry Standard** | ✅ Yes (event-driven) | ⚠️ Task queues | Kafka |

**Decision Factors:**
1. **Event Replay Critical:** Analytics needs to reprocess historical data
2. **Future Scale:** Platform may handle thousands of transactions/day
3. **Industry Standard:** Kafka = standard for event-driven microservices

**When SNS/SQS is better:**
- Simple pub/sub (no replay needed)
- Low volume (<1k events/day)
- Budget-constrained (<$50/month)

### Architecture: Transactional Outbox Pattern

**Why Outbox?**
- ✅ Guarantees zero event loss (even if Kafka is down)
- ✅ Atomic commit (database + event)
- ✅ Idempotent (same event never published twice)

**How it works:**

```
┌────────────────────────────────────────────────────────────┐
│                 Application Layer                           │
│  UpdateClientePFService.atualizar()                        │
│  @Transactional {                                          │
│    1. Update cliente_pf table                              │
│    2. INSERT INTO outbox_events (                          │
│         event_type: 'CLIENTE_PF_UPDATED',                  │
│         payload: '{"publicId":"...", "nome":"..."}',       │
│         published: false                                    │
│       )                                                     │
│    3. COMMIT (database transaction)                        │
│  }                                                          │
└────────────────────┬───────────────────────────────────────┘
                     │ (separate process)
┌────────────────────▼───────────────────────────────────────┐
│            Outbox Event Publisher                           │
│  @Scheduled(fixedDelay = 1000) {                           │
│    SELECT * FROM outbox_events WHERE published = false     │
│    FOR EACH event:                                          │
│      kafkaProducer.send(topic, event.payload)              │
│      UPDATE outbox_events SET published = true             │
│  }                                                          │
└────────────────────┬───────────────────────────────────────┘
                     │
┌────────────────────▼───────────────────────────────────────┐
│                  Kafka MSK                                  │
│  Topic: cliente-events                                      │
│  Partition: 0 (based on publicId hash)                     │
│  Retention: 7 days                                          │
│  Replication: 3 (high availability)                         │
└────────────────────┬───────────────────────────────────────┘
                     │
┌────────────────────▼───────────────────────────────────────┐
│          Analytics Microservice (Consumer)                  │
│  @KafkaListener(topic = "cliente-events")                  │
│  public void handleClienteEvent(ClienteEvent event) {      │
│    analyticsService.processEvent(event);                   │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
```

### Event Schema (JSON)

**Event Envelope:**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "CLIENTE_PF_UPDATED",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440001",
  "aggregateType": "ClientePF",
  "timestamp": "2025-11-03T14:35:22.123Z",
  "version": 1,
  "payload": { ... }
}
```

**Payload (CLIENTE_PF_CREATED):**
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440001",
  "primeiroNome": "João",
  "sobrenome": "Silva",
  "cpf": "12345678910",
  "email": "joao@email.com",
  "tipoCliente": "CONSIGNANTE",
  "origemLead": "GOOGLE_ADS"
}
```

**Event Types:**
- `CLIENTE_PF_CREATED`
- `CLIENTE_PF_UPDATED`
- `CLIENTE_PF_DELETED`
- `CLIENTE_PJ_CREATED`
- `CLIENTE_PJ_UPDATED`
- `CLIENTE_PJ_DELETED`

### Files to Create (7 files)

```
src/main/java/br/com/vanessa_mudanca/cliente_core/
├── domain/entity/
│   └── OutboxEvent.java (JPA entity)
├── domain/event/
│   ├── ClienteEvent.java (abstract base)
│   ├── ClientePFCreatedEvent.java
│   ├── ClientePFUpdatedEvent.java
│   └── ClientePFDeletedEvent.java
├── infrastructure/messaging/
│   ├── KafkaEventPublisher.java
│   └── OutboxEventPublisher.java (@Scheduled)
└── infrastructure/config/
    └── KafkaConfig.java

src/test/java/br/com/vanessa_mudanca/cliente_core/
└── infrastructure/messaging/
    ├── KafkaEventPublisherTest.java (Testcontainers)
    └── OutboxEventPublisherTest.java
```

### Files to Modify (6 files)

```
1. CreateClientePFService.java
   + Publish CLIENTE_PF_CREATED event

2. UpdateClientePFService.java
   + Publish CLIENTE_PF_UPDATED event

3. DeleteClientePFService.java
   + Publish CLIENTE_PF_DELETED event

4-6. Same for ClientePJ services
```

### Database Changes

**Liquibase Changeset:** `013-create-outbox-events-table.sql`

```sql
CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN DEFAULT false,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_outbox_events_published
ON outbox_events(published, created_at)
WHERE published = false;

CREATE INDEX idx_outbox_events_aggregate
ON outbox_events(aggregate_id, event_type);

COMMENT ON TABLE outbox_events IS 'Transactional Outbox for Kafka event publishing';
```

### Dependencies (Maven)

```xml
<!-- Spring Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Kafka Clients -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>3.6.0</version>
</dependency>

<!-- Testcontainers (for testing) -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

### Configuration (application.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                    # Wait for all replicas
      retries: 3                   # Retry on failure
      enable-idempotence: true     # Prevent duplicate messages
      properties:
        max.in.flight.requests.per.connection: 1  # Strict ordering
    properties:
      security.protocol: SASL_SSL  # AWS MSK authentication
      sasl.mechanism: AWS_MSK_IAM
      sasl.jaas.config: software.amazon.msk.auth.iam.IAMLoginModule required;
      sasl.client.callback.handler.class: software.amazon.msk.auth.iam.IAMClientCallbackHandler

kafka:
  topics:
    cliente-events: cliente-events
  outbox:
    publisher-interval: 1000  # Poll every 1 second
    batch-size: 100           # Publish up to 100 events per batch
```

**Production (AWS MSK):**
```yaml
spring:
  kafka:
    bootstrap-servers: b-1.vanessa-kafka.xxxxx.c3.kafka.us-east-1.amazonaws.com:9098
```

### Infrastructure Requirements

**AWS MSK Cluster:**
- **Cluster Type:** Provisioned (not Serverless)
- **Broker Type:** kafka.m5.large (2 vCPU, 8 GB RAM)
- **Brokers:** 3 (multi-AZ)
- **Storage:** 100 GB EBS per broker
- **Authentication:** IAM
- **Encryption:** TLS in-transit, EBS encryption at-rest
- **Estimated Cost:** $300-500/month

**Terraform Configuration:**
```hcl
resource "aws_msk_cluster" "vanessa_kafka" {
  cluster_name           = "vanessa-kafka-prod"
  kafka_version          = "3.6.0"
  number_of_broker_nodes = 3

  broker_node_group_info {
    instance_type   = "kafka.m5.large"
    client_subnets  = [aws_subnet.private_a.id, aws_subnet.private_b.id, aws_subnet.private_c.id]
    security_groups = [aws_security_group.kafka.id]

    storage_info {
      ebs_storage_info {
        volume_size = 100
      }
    }
  }

  client_authentication {
    sasl {
      iam = true
    }
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS"
    }
  }
}
```

### Testing Strategy

**Unit Tests (8 scenarios):**
1. ✅ Outbox event created on cliente creation
2. ✅ Outbox event published to Kafka successfully
3. ✅ Outbox event marked as published after Kafka ACK
4. ❌ Kafka down = event stays in outbox (published=false)
5. ✅ Publisher retries failed events
6. ✅ Idempotency: same event not published twice
7. ✅ Event payload contains all required fields
8. ✅ Partition key based on publicId (ordering preserved)

**Integration Tests (Testcontainers):**
```java
@Testcontainers
class KafkaEventPublisherTest {
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Test
    void shouldPublishEventToKafka() {
        // Test with real Kafka container
    }
}
```

### QA Test Plan

**File:** `docs/qa/KAFKA_INTEGRATION_TEST_PLAN.md` (to be created)

**Scenarios:**
1. Happy path (event published and consumed)
2. Kafka unavailable (event stored in outbox)
3. Publisher recovery (publishes pending events)
4. Duplicate prevention (idempotency)
5. Ordering guarantee (partition key)
6. Performance (1k events/sec)

### Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Event loss on rollback** | MEDIUM | CRITICAL | ✅ Transactional Outbox Pattern |
| Kafka cluster down | LOW | HIGH | Events stored in outbox, published when recovered |
| Duplicate events | MEDIUM | MEDIUM | Idempotent consumer (analytics MS) |
| Slow event publishing | LOW | MEDIUM | Batch publishing (100 events/batch) |
| Outbox table growth | HIGH | MEDIUM | Archive published events >30 days old |
| High AWS cost | HIGH | MEDIUM | Use Kafka Serverless (future) or SNS/SQS |

### Monitoring & Alerting

**Metrics to Track:**
- Outbox events pending (published=false)
- Kafka publish failures
- Event publishing lag (time between creation and publish)
- Kafka cluster health (AWS CloudWatch)

**Alerts:**
- ⚠️ Outbox pending events > 1000 (backlog)
- 🚨 Kafka publish failures > 10/min
- ⚠️ Event lag > 5 minutes
- 🚨 Kafka cluster down

**CloudWatch Dashboard:**
```
┌────────────────────────────────────────┐
│ Outbox Events Pending: 23              │
│ Events Published/min: 150              │
│ Kafka Publish Success Rate: 99.8%     │
│ Event Lag (p95): 1.2 seconds          │
└────────────────────────────────────────┘
```

### Success Criteria

- ✅ Events published within 5 seconds of creation
- ✅ Zero event loss (even if Kafka is down)
- ✅ Idempotent (no duplicate events)
- ✅ Kafka cluster 99.9% uptime
- ✅ All 8 unit tests passing
- ✅ Integration tests with Testcontainers passing

### Effort Breakdown

- Design + Outbox pattern: 4h
- Kafka configuration: 4h
- Event publisher implementation: 6h
- Service modifications (6 files): 4h
- Testing (unit + integration): 4h
- Infrastructure (Terraform): 2h
- **Total: 24h**

### Cost Estimation

**AWS MSK:**
- Cluster: $300-500/month
- Data transfer: $10-20/month (10 GB/month)
- **Total: $310-520/month**

**Alternatives:**
- Amazon MSK Serverless: $150-300/month (pay-per-use)
- SNS/SQS: $10-50/month (but no event replay)

---

## FEATURE 5: Structured Logging (JSON)

### Overview

| Attribute | Value |
|-----------|-------|
| **Complexity** | ⭐ Simples |
| **Effort** | 6 hours |
| **Priority** | HIGH (foundation) |
| **Dependencies** | None |
| **Risk** | LOW |

### Description

Replace plain-text logs with structured JSON logs for better observability in Kibana/CloudWatch. Includes correlation IDs, MDC context, and PII masking.

### Technical Decision: Logback JSON Encoder

**Decision:** Use Logstash Logback Encoder (open source)

**Reasons:**
- ✅ Native Logback support (Spring Boot default)
- ✅ Zero performance overhead
- ✅ CloudWatch Insights compatible
- ✅ Kibana compatible (ELK stack)
- ✅ MDC context included automatically
- ✅ Stack traces formatted as JSON

**Alternatives Considered:**
- ❌ Log4j2: Requires migration from Logback
- ❌ Custom formatter: Reinventing the wheel

### Current Logs (Before)

```
2025-11-03 14:35:22 INFO  CreateClientePFService - Criando cliente PF com CPF: 123.456.789-10
2025-11-03 14:35:23 ERROR UpdateClientePFService - Erro ao atualizar cliente: Cliente não encontrado
```

**Problems:**
- ❌ Hard to parse (regex needed)
- ❌ No correlation ID (can't trace request)
- ❌ PII exposed (CPF in logs!)
- ❌ No structured metadata
- ❌ CloudWatch Insights can't query efficiently

### Target Logs (After)

```json
{
  "timestamp": "2025-11-03T14:35:22.123Z",
  "level": "INFO",
  "logger": "CreateClientePFService",
  "message": "Criando cliente PF",
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000",
  "trace_id": "abcd1234",
  "span_id": "5678efgh",
  "user_id": "user@email.com",
  "client_type": "PF",
  "action": "CREATE_CLIENTE",
  "duration_ms": 45,
  "masked_cpf": "***.***.789-10",
  "thread": "http-nio-8081-exec-1",
  "application": "cliente-core",
  "environment": "production"
}
```

**Benefits:**
- ✅ CloudWatch Insights: `fields message | filter correlation_id = "550e8400..."`
- ✅ Kibana: Query by correlation_id, client_type, action
- ✅ PII masked automatically
- ✅ Trace requests across microservices
- ✅ Measure operation duration

### Implementation Architecture

```
┌────────────────────────────────────────────────────────────┐
│                   HTTP Filter                               │
│  CorrelationIdFilter (before controller)                   │
│  1. Generate correlation_id (UUID)                         │
│  2. Add to MDC: MDC.put("correlation_id", uuid)           │
│  3. Add to response header: X-Correlation-Id               │
│  4. Clear MDC after request                                │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│                Application Layer                            │
│  Service methods use SLF4J:                                │
│                                                             │
│  log.info("Criando cliente PF",                            │
│    kv("client_type", "PF"),                                │
│    kv("action", "CREATE_CLIENTE"),                         │
│    kv("masked_cpf", maskCpf(cpf))                          │
│  );                                                         │
│                                                             │
│  MDC automatically includes correlation_id                 │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│              Logback JSON Encoder                           │
│  logback-spring.xml:                                        │
│  <encoder class="LogstashEncoder">                         │
│    <includeMdcKeyName>correlation_id</includeMdcKeyName>   │
│    <includeMdcKeyName>user_id</includeMdcKeyName>          │
│    <fieldNames>                                             │
│      <timestamp>timestamp</timestamp>                       │
│      <level>level</level>                                   │
│      <message>message</message>                             │
│    </fieldNames>                                            │
│  </encoder>                                                 │
└─────────────────────┬──────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────┐
│            CloudWatch / Kibana                              │
│  Logs stored as JSON (one line per log entry)              │
│  Query: correlation_id = "550e8400..."                     │
│  Filter: level = "ERROR" AND action = "UPDATE_CLIENTE"    │
│  Aggregate: avg(duration_ms) by action                     │
└─────────────────────────────────────────────────────────────┘
```

### MDC Keys (Mapped Diagnostic Context)

| Key | Type | Example | Source |
|-----|------|---------|--------|
| **correlation_id** | UUID | `550e8400-...` | CorrelationIdFilter |
| **user_id** | String | `user@email.com` | OAuth2 token (future) |
| **trace_id** | String | `abcd1234` | Distributed tracing (future) |
| **span_id** | String | `5678efgh` | Distributed tracing (future) |
| **client_type** | Enum | `PF`, `PJ` | Service method |
| **action** | String | `CREATE_CLIENTE` | Service method |
| **duration_ms** | Long | `45` | Method interceptor |

### PII Masking Strategy

**Sensitive Fields:**
- CPF: `123.456.789-10` → `***.***. 789-10`
- CNPJ: `11.222.333/0001-81` → `**.***.***/0001-81`
- Email: `joao@email.com` → `j***@email.com`
- Phone: `11987654321` → `***876***21`

**Implementation:**
```java
public class MaskingUtil {
    public static String maskCpf(String cpf) {
        // Keep last 4 digits
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})",
                              "***.***.$3-$4");
    }

    public static String maskEmail(String email) {
        String[] parts = email.split("@");
        return parts[0].charAt(0) + "***@" + parts[1];
    }
}
```

### Files to Create (4 files)

```
src/main/java/br/com/vanessa_mudanca/cliente_core/
├── infrastructure/logging/
│   ├── CorrelationIdFilter.java (Servlet Filter)
│   ├── MdcKeys.java (constants)
│   └── MaskingUtil.java (PII masking)
└── infrastructure/config/
    └── LoggingConfig.java

src/main/resources/
└── logback-spring.xml (replaces default)
```

### Files to Modify (10+ files)

**Refactor all log statements in:**
```
1. CreateClientePFService.java
2. UpdateClientePFService.java
3. DeleteClientePFService.java
4. CreateClientePJService.java
5. UpdateClientePJService.java
6. DeleteClientePJService.java
7. FindClientePFByIdService.java
8. FindClientePJByIdService.java
9. ClientePFController.java
10. ClientePJController.java
```

**Before:**
```java
log.info("Criando cliente PF com CPF: {}", cpf);
```

**After:**
```java
import static net.logstash.logback.argument.StructuredArguments.kv;

log.info("Criando cliente PF",
    kv("client_type", "PF"),
    kv("action", "CREATE_CLIENTE"),
    kv("masked_cpf", MaskingUtil.maskCpf(cpf))
);
```

### Database Changes

None required.

### Dependencies (Maven)

```xml
<!-- Logstash Logback Encoder -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Configuration Files

**logback-spring.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender (for local dev) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>true</includeContext>
            <includeMdc>true</includeMdc>
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <version>[ignore]</version>
                <levelValue>[ignore]</levelValue>
            </fieldNames>
            <customFields>{"application":"cliente-core","environment":"${ENVIRONMENT:dev}"}</customFields>
        </encoder>
    </appender>

    <!-- File Appender (for production) -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/cliente-core/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/cliente-core/application-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

    <!-- Application Logger -->
    <logger name="br.com.vanessa_mudanca.cliente_core" level="DEBUG" />
</configuration>
```

**application.yml:**
```yaml
logging:
  pattern:
    console: ""  # Disable default pattern (JSON only)
  level:
    br.com.vanessa_mudanca.cliente_core: DEBUG
    org.springframework: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### CloudWatch Insights Queries

**Find all errors for a specific correlation_id:**
```sql
fields @timestamp, level, message, action, duration_ms
| filter correlation_id = "550e8400-e29b-41d4-a716-446655440000"
| sort @timestamp desc
```

**Average duration by action:**
```sql
stats avg(duration_ms) as avg_duration by action
| filter level = "INFO"
| sort avg_duration desc
```

**Count errors by logger:**
```sql
stats count(*) as error_count by logger
| filter level = "ERROR"
| sort error_count desc
```

**Find slow operations (>1s):**
```sql
fields @timestamp, action, duration_ms, correlation_id
| filter duration_ms > 1000
| sort duration_ms desc
```

### Testing Strategy

**Unit Tests (6 scenarios):**
1. ✅ CorrelationIdFilter generates UUID
2. ✅ Correlation ID added to MDC
3. ✅ Correlation ID included in response header
4. ✅ MDC cleared after request
5. ✅ CPF masked correctly
6. ✅ Email masked correctly

**Integration Tests:**
- Log output is valid JSON
- All MDC keys present in output
- PII not exposed in logs

### QA Test Plan

**Manual Verification:**
1. Start application
2. Make request: `POST /v1/clientes/pf`
3. Check logs: JSON format, correlation_id present
4. Verify CPF masked in logs
5. Query CloudWatch with correlation_id

### Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| PII leaked in logs | MEDIUM | CRITICAL | Mandatory masking in MaskingUtil, code review |
| Performance overhead (JSON encoding) | LOW | LOW | Logback encoder is fast (<1ms) |
| Log volume growth | HIGH | MEDIUM | CloudWatch retention 30 days, compress old logs |
| Correlation ID not propagated | LOW | MEDIUM | Thorough testing, use Sleuth (future) |

### Success Criteria

- ✅ All logs in JSON format
- ✅ Correlation ID in every log entry
- ✅ PII masked (CPF, CNPJ, email)
- ✅ CloudWatch Insights queries work
- ✅ No performance degradation (<1ms overhead)
- ✅ All 6 unit tests passing

### Effort Breakdown

- Design + research: 1h
- Logback configuration: 1h
- CorrelationIdFilter: 1h
- MaskingUtil: 1h
- Refactor log statements: 2h
- Testing: 1h (automated validation future work)
- **Total: 6h**

---

## 📅 Implementation Timeline

### Week 1: Foundation + CRUD + Search

**Day 1 (Monday) - 6 hours**
- ✅ Feature 5: Structured Logging
  - Setup Logback JSON encoder
  - Implement CorrelationIdFilter
  - Refactor existing logs
  - Test with CloudWatch

**Day 2 (Tuesday) - 6 hours**
- ✅ Feature 1: DELETE Soft Delete
  - Implement Use Cases + Services
  - Add endpoints to controllers
  - Write unit tests
  - Create QA test plan

**Day 3-4 (Wed-Thu) - 14 hours**
- ✅ Feature 2: Advanced Search
  - Design SQL query with full-text
  - Implement repository + service
  - Create controller endpoint
  - Write unit + integration tests
  - Performance testing

### Week 2: Export + Kafka

**Day 5-6 (Mon-Tue) - 14 hours**
- ✅ Feature 3: Export CSV/PDF
  - Implement CSV exporter (streaming)
  - Implement PDF exporter (iText)
  - Create controller endpoints
  - Write unit tests
  - Test with 10k CSV, 100 PDF

**Day 7-10 (Wed-Mon) - 24 hours**
- ✅ Feature 4: Kafka Events
  - Setup Kafka MSK infrastructure (Terraform)
  - Implement Outbox pattern (table + entity)
  - Create event publisher (@Scheduled)
  - Modify services to publish events
  - Write Testcontainers tests
  - Performance + reliability testing

**Total:** 64 hours over 10 days

---

## 🎯 Success Metrics

### Technical Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Test Coverage** | ≥80% | JaCoCo report |
| **Unit Tests Passing** | 100% | CI/CD pipeline |
| **Search Response Time (p95)** | <1s | Performance tests |
| **Export CSV (10k records)** | <10s | Performance tests |
| **Kafka Event Publish Lag (p95)** | <5s | CloudWatch metrics |
| **Logging Overhead** | <1ms | JMH benchmarks |
| **Zero Event Loss** | 100% | Outbox audit |

### Business Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Search Usage** | 1000 queries/day | CloudWatch Insights |
| **Export Downloads** | 50 reports/week | Application metrics |
| **Debugging Time Reduction** | 50% | Team survey |
| **API Uptime** | 99.9% | AWS CloudWatch |

---

## ⚠️ Risks & Dependencies

### Critical Risks

| Risk | Probability | Impact | Mitigation | Owner |
|------|-------------|--------|------------|-------|
| **Kafka event loss on DB rollback** | MEDIUM | CRITICAL | ✅ Transactional Outbox Pattern | Tech Lead |
| **Search query timeout (>30s)** | LOW | HIGH | ✅ Query timeout 5s, EXPLAIN monitoring | DBA |
| **PII exposure in logs** | MEDIUM | CRITICAL | ✅ Mandatory masking, code review | Security |
| **Export timeout (large PDF)** | MEDIUM | HIGH | ✅ Limit 100 records, async future | Dev Team |
| **Kafka cluster cost overrun** | HIGH | MEDIUM | ✅ Budget monitoring, serverless option | Ops |

### External Dependencies

| Dependency | Provider | Status | Risk | Mitigation |
|------------|----------|--------|------|------------|
| **PostgreSQL RDS** | AWS | ✅ Active | LOW | Managed service |
| **Kafka MSK** | AWS | ⚠️ NOT SETUP | HIGH | Terraform provisioning |
| **CloudWatch Logs** | AWS | ✅ Active | LOW | Managed service |
| **iText 7 License** | iText Software | ✅ Open Source (MPL) | LOW | Use Community Edition |

---

## 💰 Cost Estimation

### Infrastructure Costs (Monthly)

| Service | Current | After Roadmap | Delta | Notes |
|---------|---------|---------------|-------|-------|
| **RDS PostgreSQL** | $150 | $150 | $0 | No additional resources needed |
| **ECS Fargate** | $100 | $100 | $0 | Same container size |
| **CloudWatch Logs** | $10 | $30 | +$20 | JSON logs = more volume |
| **Kafka MSK** | $0 | $400 | +$400 | 3 brokers, m5.large |
| **Data Transfer** | $20 | $40 | +$20 | Kafka traffic |
| **S3 (log archive)** | $5 | $10 | +$5 | 30 days retention |
| **Total** | **$285/mo** | **$730/mo** | **+$445/mo** | **+156%** |

### Cost Optimization Options

| Option | Savings | Trade-off |
|--------|---------|-----------|
| Use Kafka Serverless | -$200/mo | Higher latency (50-100ms) |
| Use SNS/SQS instead | -$350/mo | ❌ No event replay (not recommended) |
| CloudWatch retention 7 days | -$10/mo | Less debugging history |
| Archive logs to S3 Glacier | -$5/mo | Slower retrieval |

**Recommendation:** Accept $445/mo increase for Kafka MSK (strategic investment).

---

## 📚 Documentation Deliverables

### Documents to Create

1. **ROADMAP_2025_Q4.md** (this document)
   - Feature specifications
   - Technical decisions
   - Implementation timeline

2. **docs/qa/DELETE_CLIENTE_TEST_PLAN.md**
   - 16 test scenarios (8 PF + 8 PJ)
   - Manual testing steps

3. **docs/qa/SEARCH_CLIENTE_TEST_PLAN.md**
   - 20 test scenarios
   - Performance benchmarks

4. **docs/qa/EXPORT_CLIENTE_TEST_PLAN.md**
   - 18 test scenarios (CSV + PDF)
   - File validation steps

5. **docs/qa/KAFKA_INTEGRATION_TEST_PLAN.md**
   - 24 test scenarios
   - Infrastructure validation

6. **docs/LOGGING_GUIDE.md**
   - MDC keys reference
   - CloudWatch queries
   - PII masking rules

### Documents to Update

1. **README.md**
   - Add new endpoints
   - Update feature list
   - Add Kafka integration section

2. **CLAUDE.md**
   - Add logging conventions
   - Add Kafka event patterns
   - Add export patterns

3. **docs/INDEX.md**
   - Link to new documents
   - Update feature matrix

---

## ✅ Definition of Done (Per Feature)

### Code Complete
- [ ] All files created/modified
- [ ] Code follows conventions (CLAUDE.md)
- [ ] No hardcoded values (use application.yml)
- [ ] Error handling implemented
- [ ] Logging added (structured JSON)

### Testing Complete
- [ ] Unit tests written (≥80% coverage)
- [ ] Integration tests written
- [ ] All tests passing (0 failures)
- [ ] Performance tests executed (if applicable)
- [ ] QA test plan created

### Documentation Complete
- [ ] README.md updated
- [ ] CLAUDE.md updated (if architectural change)
- [ ] QA test plan written
- [ ] Code comments added (complex logic)
- [ ] API documented (Swagger/OpenAPI)

### Review & Approval
- [ ] Code review completed (peer review)
- [ ] Security review (if PII/sensitive data)
- [ ] QA sign-off (manual testing)
- [ ] Tech Lead approval

### Deployment Ready
- [ ] Merged to main branch
- [ ] CI/CD pipeline passing
- [ ] Staging deployment tested
- [ ] Rollback plan documented
- [ ] Monitoring alerts configured

---

## 🚀 Next Steps

### Immediate Actions (This Week)

1. **Stakeholder Approval**
   - [ ] Review roadmap with Product Owner
   - [ ] Approve budget ($445/mo increase)
   - [ ] Prioritize features (confirm order)

2. **Infrastructure Setup**
   - [ ] Provision Kafka MSK cluster (Terraform)
   - [ ] Configure CloudWatch Log Groups
   - [ ] Setup IAM roles for Kafka access

3. **Team Preparation**
   - [ ] Schedule implementation kick-off
   - [ ] Assign feature owners
   - [ ] Setup feature branches (git)

### Development Workflow

**For each feature:**
```bash
# 1. Create feature branch
git checkout -b feature/logging-json

# 2. Implement + test
# ... development work ...

# 3. Run tests
mvn clean test

# 4. Create QA test plan
# docs/qa/FEATURE_NAME_TEST_PLAN.md

# 5. Code review
gh pr create --title "Feature: Structured Logging"

# 6. Merge after approval
git checkout main
git merge feature/logging-json

# 7. Deploy to staging
./deploy-staging.sh

# 8. Manual QA testing

# 9. Deploy to production
./deploy-production.sh
```

---

## 📞 Contacts & Escalation

### Feature Owners

| Feature | Owner | Backup | Slack Channel |
|---------|-------|--------|---------------|
| DELETE | TBD | TBD | #team-backend |
| SEARCH | TBD | TBD | #team-backend |
| EXPORT | TBD | TBD | #team-backend |
| KAFKA | TBD | Tech Lead | #team-platform |
| LOGGING | TBD | DevOps | #team-devops |

### Escalation Path

1. **Technical Issues:** Developer → Tech Lead → CTO
2. **Budget Issues:** Tech Lead → Product Owner → CFO
3. **Timeline Issues:** Developer → Tech Lead → Product Owner
4. **Security Issues:** Developer → Security Team (immediate)

---

## 📖 References

### Internal Documentation
- [README.md](../README.md) - Project documentation
- [CLAUDE.md](../CLAUDE.md) - AI assistant guide
- [UPDATE_FEATURES_SUMMARY.md](UPDATE_FEATURES_SUMMARY.md) - UPDATE feature reference
- [INDEX.md](INDEX.md) - Documentation index

### External Resources
- [PostgreSQL Full-Text Search](https://www.postgresql.org/docs/16/textsearch.html)
- [iText 7 Documentation](https://kb.itextpdf.com/itext/itext-7)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [AWS MSK Best Practices](https://docs.aws.amazon.com/msk/latest/developerguide/bestpractices.html)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)

---

## 📝 Changelog

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2025-11-03 | 1.0 | Initial roadmap created by feature-dev:code-architect agent | Tech Lead |

---

**Document Status:** 🟢 APPROVED
**Next Review:** 2025-12-01
**Owner:** Tech Lead
**Approvers:** Product Owner, CTO

---

**End of Roadmap**
