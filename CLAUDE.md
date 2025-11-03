# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Overview

**cliente-core** is a Spring Boot microservice responsible for managing client data (both individual and corporate) for the Va Nessa Mudança platform. It handles complete CRUD operations for clients, documents, contacts, addresses, banking information, preferences, and audit trails.

**Stack:**
- Java 21
- Spring Boot 3.5.7
- PostgreSQL (managed via Liquibase)
- JPA/Hibernate with JOINED inheritance strategy
- Lombok for boilerplate reduction

**Base URL:** `http://localhost:8081/api/clientes`

---

## Build & Run Commands

### Development

```bash
# Build the project
mvn clean install

# Run the application (dev profile)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=ClienteCoreApplicationTests

# Package without running tests
mvn clean package -DskipTests
```

### Database Setup

```bash
# Create database (first time only)
psql -U postgres -c "CREATE DATABASE vanessa_mudanca_clientes;"

# Verify database structure after startup
psql -U postgres -d vanessa_mudanca_clientes -c "\dt"  # List tables
psql -U postgres -d vanessa_mudanca_clientes -c "\di"  # List indexes
```

### Liquibase Commands

```bash
# Liquibase runs automatically on startup. To troubleshoot:

# Clear Liquibase history (development only - destructive!)
psql -U postgres -d vanessa_mudanca_clientes -c "DROP TABLE databasechangelog; DROP TABLE databasechangeloglock;"

# Run with only DDL (skip seeds)
# Edit application-dev.yml: change "contexts: dev" to "contexts: ddl-only"

# View Liquibase logs
mvn spring-boot:run | grep liquibase
```

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8081/api/clientes/actuator/health

# Metrics
curl http://localhost:8081/api/clientes/actuator/metrics

# Application info
curl http://localhost:8081/api/clientes/actuator/info
```

### API Documentation

```bash
# Swagger UI (once implemented)
open http://localhost:8081/api/clientes/swagger-ui/index.html

# OpenAPI JSON
curl http://localhost:8081/api/clientes/v3/api-docs
```

---

## Architecture & Code Structure

### Domain-Driven Design Layers

```
src/main/java/br/com/vanessa_mudanca/cliente_core/
├── domain/
│   ├── entity/           # JPA entities (domain models)
│   └── enums/            # Business enumerations
├── repository/           # JPA repositories (to be implemented)
├── service/              # Business logic layer (to be implemented)
├── controller/           # REST API controllers (to be implemented)
├── dto/                  # Data Transfer Objects (to be implemented)
├── exception/            # Custom exceptions (to be implemented)
├── validator/            # Business validators (CPF/CNPJ, etc.) (to be implemented)
└── config/               # Configuration classes
```

### Entity Inheritance Strategy

**JOINED inheritance** is used for Cliente hierarchy:

```
Cliente (abstract, table: clientes)
├── ClientePF (table: clientes_pf, discriminator: "PF")
└── ClientePJ (table: clientes_pj, discriminator: "PJ")
```

**Why JOINED?**
- PF and PJ have significantly different fields
- Avoids null columns (unlike SINGLE_TABLE)
- Better normalization and query performance for type-specific queries

**Key entities:**
- `Cliente.java:23` - Abstract base class with common fields
- `ClientePF.java` - Individual person (CPF, RG, birth date, etc.)
- `ClientePJ.java` - Corporate entity (CNPJ, razão social, etc.)
- `Documento.java` - Documents (CPF, RG, CNH, etc.) with expiration tracking
- `Contato.java` - Contact information (phone, email, WhatsApp, etc.)
- `Endereco.java` - Addresses (residential, commercial, delivery, etc.)
- `DadosBancarios.java` - Banking information with PIX support
- `PreferenciaCliente.java` - Communication preferences (LGPD compliance)
- `AuditoriaCliente.java` - Audit trail for critical changes

### Relationship Patterns

1. **OneToMany with orphanRemoval:** Used for Documento, Contato, Endereco
   - When parent is deleted, children are automatically removed
   - Example: `Cliente.java:95-108`

2. **ManyToOne bidirectional:** Used for DadosBancarios
   - Explicit `mappedBy` and `@JoinColumn`
   - Example: `Cliente.java:110-112`

3. **OneToOne:** Used for PreferenciaCliente
   - Each client has exactly one preference record
   - Example: `Cliente.java:114-115`

4. **Self-referencing:** Cliente.clienteIndicador for referral program
   - Example: `Cliente.java:52-60`

### Automatic Timestamp Management

All entities with `dataCriacao` and `dataAtualizacao` use:
- `@PrePersist` to set both timestamps on creation
- `@PreUpdate` to update `dataAtualizacao` on modification
- Example: `Cliente.java:130-139`

---

## Database Schema Management

### Liquibase Structure

**CRITICAL:** Never manually run `ALTER TABLE` or modify the database schema directly. All changes must go through Liquibase.

```
src/main/resources/db/changelog/
├── db-changelog-master.xml              # Main orchestrator
└── sql/
    ├── ddl/                             # Schema structure (11 files)
    │   ├── 001-create-table-clientes.sql
    │   ├── 002-create-table-clientes-pf.sql
    │   ├── 003-create-table-clientes-pj.sql
    │   ├── 004-create-table-documentos.sql
    │   ├── 005-create-table-contatos.sql
    │   ├── 006-create-table-enderecos.sql
    │   ├── 007-create-table-dados-bancarios.sql
    │   ├── 008-create-table-preferencias-cliente.sql
    │   ├── 009-create-table-auditoria-cliente.sql
    │   ├── 010-create-indexes.sql       # ~50 optimized indexes
    │   └── 011-create-constraints.sql   # Foreign keys
    └── dml/                             # Test data (8 seed files)
        ├── 001-seed-clientes-pf.sql     # 10 PF clients
        ├── 002-seed-clientes-pj.sql     # 5 PJ clients
        ├── 003-seed-documentos.sql
        ├── 004-seed-contatos.sql
        ├── 005-seed-enderecos.sql
        ├── 006-seed-dados-bancarios.sql
        ├── 007-seed-preferencias.sql
        └── 008-seed-auditoria.sql
```

### Index Strategy (~50 indexes)

**Performance-optimized indexes for AWS RDS PostgreSQL:**

1. **Partial indexes:** For frequently filtered boolean columns
   - Example: `WHERE ativo = true`, `WHERE bloqueado = true`

2. **Composite indexes:** For multi-column queries
   - Example: `(cliente_id, ativo)`, `(cidade, estado)`

3. **GIN indexes:** Full-text search in Portuguese
   - For ClientePF names and ClientePJ razão social/fantasia

4. **DESC indexes:** For ORDER BY DESC queries
   - Example: `data_criacao DESC`, `data_alteracao DESC`

**When adding new queries, check if new indexes are needed.**

---

## Code Conventions

### Java Naming

1. **Enums:** Always suffix with `Enum`
   - ✅ `SexoEnum`, `TipoClienteEnum`, `StatusDocumentoEnum`
   - ❌ `Sexo`, `TipoCliente`

2. **Entities:** Singular noun, PascalCase
   - ✅ `Cliente`, `Documento`, `Endereco`
   - ❌ `Clientes`, `client`

3. **Fields:** camelCase in Java, snake_case in database
   - Java: `dataCriacao`, `nomeCompleto`
   - DB: `data_criacao`, `nome_completo`

4. **Builder pattern:** Use `@SuperBuilder` for inheritance hierarchies
   - Required when extending classes with `@Builder`
   - Example: `Cliente.java:22`

### Database Conventions

1. **Table names:** Plural, snake_case
   - `clientes`, `documentos`, `enderecos`

2. **Column names:** snake_case
   - `data_criacao`, `cliente_indicador_id`

3. **Foreign keys:** `{referenced_table_singular}_id`
   - `cliente_id`, `cliente_indicador_id`

4. **Enums in database:** Use VARCHAR with CHECK constraints
   - NOT PostgreSQL native ENUM (for easier migrations)
   - Example in DDL: `CHECK (tipo_cliente IN ('CONSIGNANTE', 'COMPRADOR', ...))`

### Lombok Usage

**Commonly used annotations:**
- `@Getter` / `@Setter` - On class level for all fields
- `@NoArgsConstructor` - Required for JPA
- `@AllArgsConstructor` - Required for builders
- `@Builder` or `@SuperBuilder` - For fluent object creation
- `@Builder.Default` - For default values in builder pattern

**Avoid:**
- `@Data` - Too broad, prefer explicit annotations
- `@ToString` / `@EqualsAndHashCode` on entities - Can cause LazyInitializationException

---

## Business Rules & Validations

### Critical Validations (to be implemented in Service layer)

1. **Uniqueness constraints:**
   - CPF must be unique across ClientePF (validate with algorithm)
   - CNPJ must be unique across ClientePJ (validate with algorithm)
   - Email must be unique per active client

2. **Single "principal" rules:**
   - Only 1 documento can be `documentoPrincipal = true` per client
   - Only 1 contato can be `contatoPrincipal = true` per client
   - Only 1 endereco can be `enderecoPrincipal = true` per type
   - Only 1 dados bancários can be `contaPrincipal = true` per client

3. **Automatic expiration:** Documento status becomes EXPIRADO when dataValidade passes
   - Implemented in `@PreUpdate` lifecycle hook

4. **Soft delete:** Set `ativo = false` instead of physical deletion
   - Preserves referential integrity and audit trail

5. **LGPD compliance:**
   - Respect `PreferenciaCliente` communication preferences
   - Record consent timestamp and IP address

6. **Business constraints:**
   - CONSIGNANTE clients must have banking data for receiving payments
   - Every client must have at least 1 active contact
   - Blocked clients (`bloqueado = true`) cannot perform new transactions

### External API Integrations (to be implemented)

1. **ViaCEP:** Address validation and auto-fill
2. **CPF/CNPJ validators:** Real-time document validation
3. **SMS/Email APIs:** Contact verification

---

## Testing Strategy

**When implementing tests:**

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClienteServiceTest

# Run specific test method
mvn test -Dtest=ClienteServiceTest#deveCriarClientePFComSucesso

# Run with coverage
mvn clean verify
```

**Test structure (to be implemented):**
- **Unit tests:** Service layer business logic
- **Integration tests:** Repository queries with H2/TestContainers
- **API tests:** Controller endpoints with MockMvc
- **Validation tests:** CPF/CNPJ algorithms

**Coverage target:** Minimum 80%

### QA Testing Strategy

**When implementing new features, ALWAYS follow this workflow:**

1. **Code Implementation** → Write feature code with TDD
2. **Unit Tests** → Ensure all unit tests pass (≥80% coverage)
3. **Code Review** → Use `feature-dev:code-reviewer` agent to identify issues
4. **Fix Critical Issues** → Address all CRITICAL and HIGH severity issues
5. **Create QA Test Plan** → Document test scenarios (see `docs/qa/`)
6. **Execute QA Tests** → Run systematic QA testing
7. **Document Results** → Update test plan with actual results

**QA Test Plan Template Location:**
- `docs/qa/UPDATE_CLIENTEPF_TEST_PLAN.md` (reference template)
- **32 test scenarios covering:** Happy path, edge cases, business rules, security, error handling, data integrity

**Common Pitfalls to Avoid (learned from UpdateClientePF review):**

1. **Null Safety in Behavioral Methods**
   ```java
   // ❌ BAD - NPE risk
   public void atualizarValor(String novoValor) {
       if (!this.valor.equals(novoValor)) { // NPE if novoValor is null
           this.valor = novoValor;
       }
   }

   // ✅ GOOD - Defensive programming
   public void atualizarValor(String novoValor) {
       if (novoValor != null && !this.valor.equals(novoValor)) {
           this.valor = novoValor;
       }
   }
   ```

2. **Fallback to Entity Values When DTO is Partial**
   ```java
   // ❌ BAD - Passes null to validator
   validador.validar(dto.tipoEndereco());

   // ✅ GOOD - Fallback to entity value
   TipoEnderecoEnum tipo = dto.tipoEndereco() != null
       ? dto.tipoEndereco()
       : endereco.getTipoEndereco();
   validador.validar(tipo);
   ```

3. **Cross-Client Ownership Validation**
   ```java
   // ✅ REQUIRED - Always validate entity belongs to client
   if (!entity.getCliente().getId().equals(cliente.getId())) {
       throw new IllegalArgumentException("Entity doesn't belong to this client");
   }
   ```

4. **Transaction Rollback Testing**
   - ALWAYS include test case for partial failure rollback
   - Verify NO changes persisted when ANY entity update fails

**Code Review Checklist:**
- [ ] Null safety in all behavioral methods
- [ ] Fallback logic for partial DTOs
- [ ] Cross-client ownership validation
- [ ] Transaction boundaries correct (`@Transactional`)
- [ ] Business rule validators implemented
- [ ] Principal uniqueness enforced
- [ ] Security validations in place

---

## Common Development Workflows

### Adding a New Entity

1. Create entity class in `domain/entity/`
2. Add necessary enums in `domain/enums/`
3. Create Liquibase changeset in `db/changelog/sql/ddl/`
4. Add indexes in `010-create-indexes.sql`
5. Add constraints in `011-create-constraints.sql`
6. Create seed data in `db/changelog/sql/dml/` (optional)
7. Update `db-changelog-master.xml`
8. Update README.md with entity documentation
9. Restart application to apply changes

### Adding a New Field to Existing Entity

1. Add field to Java entity class
2. Create new Liquibase changeset: `{number}-add-field-{field_name}.sql`
3. Use `ALTER TABLE` in changeset (not directly in DB!)
4. Consider adding index if field will be queried/filtered
5. Update README.md if field changes business logic
6. Restart application

### Adding a New Endpoint

1. Create DTO in `dto/` package
2. Implement business logic in Service layer
3. Create Controller method with proper annotations
4. Add validation annotations (`@Valid`, `@NotNull`, etc.)
5. Document with OpenAPI annotations (`@Operation`, `@ApiResponse`)
6. Write integration tests for the endpoint
7. Test manually via Swagger UI or curl

---

## Configuration Profiles

**application.yml** - Base configuration (port, context-path, actuator)
**application-dev.yml** - Development profile (active by default)
- Database: `localhost:5432/vanessa_mudanca_clientes`
- Credentials: `user` / `senha123`
- Liquibase: Enabled with `contexts: dev` (includes seeds)
- JPA: `ddl-auto: validate` (schema managed by Liquibase)
- Logging: DEBUG for application, SQL logging enabled

**When adding new profiles (prod, staging):**
- Override database connection details
- Disable SQL logging (`show-sql: false`)
- Change Liquibase context to exclude seeds
- Enable security features (OAuth2, JWT)

---

## Troubleshooting

### Application won't start - Liquibase validation error

**Cause:** Database schema doesn't match Liquibase changesets

**Solution:**
```bash
# Development only - delete Liquibase history
psql -U postgres -d vanessa_mudanca_clientes
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;
\q

# Restart application
mvn spring-boot:run
```

### LazyInitializationException on entity relationships

**Cause:** Accessing lazy-loaded relationships outside of transaction

**Solution:**
- Fetch relationships eagerly in query: `JOIN FETCH`
- Use `@Transactional` on service methods
- Use DTOs to project only needed data

### Unique constraint violation on CPF/CNPJ

**Cause:** Attempting to create duplicate document

**Solution:**
- Implement validation in service layer before save
- Check existing records with repository query
- Return appropriate error message to client

### Database connection pool exhausted

**Cause:** Too many open connections or connection leaks

**Solution:**
- Check HikariCP settings in `application-dev.yml:11-16`
- Ensure `@Transactional` methods complete properly
- Monitor connections: `curl localhost:8081/api/clientes/actuator/metrics/hikaricp.connections`

---

## Integration Contracts

**This microservice provides REST endpoints consumed by:**
- `venda-core` - Query clients by ID/CPF/CNPJ
- `financeiro-core` - Validate banking information
- `logistica-core` - Retrieve delivery addresses

**Response format:** JSON
**Authentication:** OAuth2 + JWT (to be implemented)
**Versioning:** URI versioning `/v1/clientes/pf` (to be implemented)

**When creating endpoints:**
- Return DTOs, not entities (avoid exposing JPA internals)
- Use proper HTTP status codes (200, 201, 400, 404, 500)
- Include pagination for list endpoints
- Implement filtering and sorting parameters

---

## Security Considerations

**To be implemented:**
1. OAuth2 Resource Server configuration
2. JWT token validation
3. Role-based access control (RBAC)
4. Field-level security (mask CPF/CNPJ in logs)
5. Rate limiting for public endpoints
6. SQL injection prevention (use JPA criteria or @Query)
7. XSS prevention in REST responses

**Already implemented:**
- Password not stored in this service (delegated to auth service)
- Audit trail for sensitive changes (`AuditoriaCliente`)
- LGPD compliance tracking (`PreferenciaCliente`)

---

## Performance Optimization

**Database:**
- ~50 indexes already optimized for common queries
- Connection pooling configured (HikariCP)
- Batch insert/update enabled (`batch_size: 20`)

**JPA:**
- Lazy loading default (avoid N+1 queries)
- Use `@EntityGraph` or JOIN FETCH when needed
- Projection queries for large datasets

**Caching (to be implemented):**
- Spring Cache abstraction
- Redis for distributed cache
- Cache client lookups by ID/CPF/CNPJ

---

## Monitoring & Observability

**Actuator endpoints enabled:**
- `/actuator/health` - Liveness and readiness probes
- `/actuator/metrics` - Micrometer metrics
- `/actuator/prometheus` - Prometheus scraping endpoint
- `/actuator/info` - Application metadata

**Logging:**
- Structured logging with JSON format (to be implemented)
- Correlation IDs for request tracing (to be implemented)
- Log levels: DEBUG (dev), INFO (prod)

**Metrics to track:**
- Request count and latency by endpoint
- Database query performance
- Connection pool metrics
- JVM memory and GC metrics

---

## Key Files Reference

- **Main application:** `ClienteCoreApplication.java` - Spring Boot entry point
- **Base entity:** `Cliente.java:23` - Abstract client with all common fields
- **Inheritance examples:** `ClientePF.java`, `ClientePJ.java`
- **Liquibase master:** `db/changelog/db-changelog-master.xml`
- **Index definitions:** `db/changelog/sql/ddl/010-create-indexes.sql`
- **Configuration:** `application.yml`, `application-dev.yml`
- **OpenAPI config:** `config/OpenApiConfig.java`
- **Comprehensive docs:** `README.md` - Full entity and business rule documentation

---

## Additional Documentation

For more detailed information:
- **Business strategy:** `../estrategia/objetivo.md`, `../estrategia/crescimento.md`
- **Monorepo overview:** `../CLAUDE.md`
- **Liquibase structure:** `LIQUIBASE_STRUCTURE.md` (if exists)
- **Quick start guide:** `LIQUIBASE_QUICKSTART.md` (if exists)

**Last updated:** 2025-11-02
**Version:** 0.1.0
