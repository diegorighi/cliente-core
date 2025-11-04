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

**⚠️ MANDATORY POLICY: Test Coverage**

**CRITICAL:** Every new feature implementation MUST maintain or improve code coverage to at least 80%.

**When implementing new features:**

1. **Before coding:** Check current coverage baseline
   ```bash
   mvn clean test
   python3 << 'EOF'
   import xml.etree.ElementTree as ET
   tree = ET.parse('target/site/jacoco/jacoco.xml')
   root = tree.getroot()
   for counter in root.findall('.//counter[@type="INSTRUCTION"]'):
       missed, covered = int(counter.get('missed')), int(counter.get('covered'))
   print(f"Coverage: {covered/(missed+covered)*100:.2f}%")
   EOF
   ```

2. **During coding:** Write tests FIRST (TDD approach)
   - Unit tests for business logic
   - Integration tests for database operations
   - API tests for controller endpoints

3. **After coding:** Verify coverage INCREASED or maintained at 80%+
   ```bash
   mvn clean test
   # Check coverage report at: target/site/jacoco/index.html
   ```

4. **If coverage < 80%:** DO NOT commit/push code
   - Identify uncovered lines in JaCoCo report
   - Write missing tests
   - Re-run tests until threshold is met

**Why 80%?**
- Industry standard for production code
- Ensures critical business logic is tested
- Prevents regression bugs
- Forces thinking about edge cases

**What counts toward coverage:**
- Service layer methods (business logic)
- Repository adapters
- Controllers (endpoint handling)
- Validators
- Mappers/DTOs

**What is excluded:**
- Configuration classes (@Configuration)
- Entity classes (domain models)
- Main application class
- POJOs without logic

**Current coverage:** 80%+ ✅ (250 tests passing)
**JaCoCo check:** PASSING (excludes infrastructure/config and infrastructure/cache)
**Classes analyzed:** 74 of 82 (8 infrastructure classes excluded)
**Last checked:** 2025-11-04

**Excluded from coverage:**
- `infrastructure/config/**` - Spring @Configuration classes (hard to unit test)
- `infrastructure/cache/**` - DynamoDB cache implementation (infrastructure, not business logic)

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

## Spring Boot Banner Customization

**Custom Banner:** `src/main/resources/banner.txt`

O cliente-core utiliza banner customizado com ASCII art para identificação visual do microserviço:

```
   ██████╗ ██╗     ██╗ ███████╗ ███╗   ██╗ ████████╗ ███████╗
  ██╔════╝ ██║     ██║ ██╔════╝ ████╗  ██║ ╚══██╔══╝ ██╔════╝
  ██║      ██║     ██║ █████╗   ██╔██╗ ██║    ██║    █████╗
  ██║      ██║     ██║ ██╔══╝   ██║╚██╗██║    ██║    ██╔══╝
  ╚██████╗ ███████╗██║ ███████╗ ██║ ╚████║    ██║    ███████╗
   ╚═════╝ ╚══════╝╚═╝ ╚══════╝ ╚═╝  ╚═══╝    ╚═╝    ╚══════╝

   ██████╗  ██████╗  ██████╗  ███████╗
  ██╔════╝ ██╔═══██╗ ██╔══██╗ ██╔════╝
  ██║      ██║   ██║ ██████╔╝ █████╗
  ██║      ██║   ██║ ██╔══██╗ ██╔══╝
  ╚██████╗ ╚██████╔╝ ██║  ██║ ███████╗
   ╚═════╝  ╚═════╝  ╚═╝  ╚═╝ ╚══════╝

 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🚚 Microserviço de Gestão de Clientes | Va Nessa Mudança
  📦 Spring Boot ${spring-boot.version} | ☕ Java ${java.version}
  🔧 Ambiente: ${spring.profiles.active} | 🎯 Hexagonal Architecture
 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Variáveis dinâmicas disponíveis:**
- `${spring-boot.version}` - Versão do Spring Boot (ex: 3.5.7)
- `${spring-boot.formatted-version}` - Versão formatada com padding
- `${application.version}` - Versão da aplicação (pom.xml)
- `${application.name}` - Nome da aplicação
- `${java.version}` - Versão do Java Runtime
- `${spring.profiles.active}` - Perfis ativos (dev, prod, staging)

**Como customizar:**
1. Editar `src/main/resources/banner.txt`
2. Usar gerador ASCII: https://patorjk.com/software/taag/
3. Rebuildar: `mvn clean install`
4. Testar: `mvn spring-boot:run`

**Backup:** O banner original do Spring Boot está preservado em `banner-original.txt`

---

## Structured Logging & Observability

### Overview

O cliente-core utiliza **structured logging com JSON** para facilitar debug em ferramentas de observabilidade como **AWS CloudWatch Insights** e **Datadog**.

**Componentes principais:**
- `logback-spring.xml` - Configuração multi-ambiente (dev/prod/test)
- `CorrelationIdFilter` - Rastreamento de requisições (X-Correlation-ID)
- `MaskingUtil` - Mascaramento de PII (LGPD compliance)
- SLF4J MDC - Contexto distribuído nos logs

### Ambientes e Formatos

**Development (profile: dev)**
- Logs human-readable com cores no console
- SQL queries visíveis (`show-sql: true`)
- Level: DEBUG para aplicação, TRACE para Hibernate

**Production/Staging (profile: prod, staging)**
- Logs JSON estruturados (Logstash format)
- SQL queries desabilitadas
- Level: INFO para aplicação, WARN para frameworks
- Campos customizados: `application`, `environment`, `correlationId`, `clientId`, `operationType`

**Test (profile: test)**
- Logs mínimos (apenas ERRORs)
- Não polui output dos testes

### Correlation ID

Toda requisição HTTP recebe um **Correlation ID** único (UUID v4):

```
Request  →  X-Correlation-ID: 12345678-1234-1234-1234-123456789012
Response ←  X-Correlation-ID: 12345678-1234-1234-1234-123456789012
```

**Como funciona:**
1. `CorrelationIdFilter` intercepta requisição
2. Verifica header `X-Correlation-ID` (se vindo de API Gateway)
3. Se não existir, gera novo UUID
4. Adiciona ao MDC (disponível em TODOS os logs)
5. Adiciona ao response header (propagação para outros MS)
6. Cleanup do MDC no finally block (evita memory leaks)

**Busca no CloudWatch Insights:**
```sql
fields @timestamp, message, correlationId, operationType
| filter correlationId = "12345678-1234-1234-1234-123456789012"
| sort @timestamp desc
```

### Mascaramento de Dados Sensíveis (LGPD)

**NUNCA logue dados sensíveis sem mascaramento!**

Use `MaskingUtil` para mascarar PII antes de logar:

```java
import br.com.vanessa_mudanca.cliente_core.infrastructure.util.MaskingUtil;

log.info("Cliente criado - CPF: {}", MaskingUtil.maskCpf("12345678910"));
// Output: Cliente criado - CPF: ***.***.789-10

log.info("Email: {}", MaskingUtil.maskEmail("joao.silva@example.com"));
// Output: Email: jo***@example.com

log.info("Telefone: {}", MaskingUtil.maskPhone("11987654321"));
// Output: Telefone: (11) ****-4321
```

**Métodos disponíveis:**
- `MaskingUtil.maskCpf(cpf)` - Preserva últimos 3 dígitos + DV
- `MaskingUtil.maskCnpj(cnpj)` - Preserva últimos 4 dígitos + DV
- `MaskingUtil.maskEmail(email)` - Preserva 2 primeiras letras
- `MaskingUtil.maskName(nome)` - Preserva primeira letra de cada palavra
- `MaskingUtil.maskPhone(telefone)` - Preserva 4 últimos dígitos
- `MaskingUtil.maskGeneric(data)` - Genérico (primeiros 2 + últimos 2)

### MDC (Mapped Diagnostic Context)

Use MDC para adicionar contexto a TODOS os logs subsequentes:

```java
import org.slf4j.MDC;

@Transactional
public ClientePFResponse criar(CreateClientePFRequest request) {
    MDC.put("operationType", "CREATE_CLIENTE_PF");

    try {
        log.info("Iniciando criação - CPF: {}", MaskingUtil.maskCpf(request.cpf()));

        ClientePF cliente = save(...);

        // Adiciona clientId após criação
        MDC.put("clientId", cliente.getPublicId().toString());

        log.info("Cliente criado com sucesso - PublicId: {}", cliente.getPublicId());

        return toResponse(cliente);

    } finally {
        // CRÍTICO: sempre limpar MDC no finally
        MDC.remove("operationType");
        MDC.remove("clientId");
    }
}
```

**Campos MDC disponíveis:**
- `correlationId` - Adicionado automaticamente pelo CorrelationIdFilter
- `operationType` - Tipo de operação (CREATE, UPDATE, FIND, etc.)
- `clientId` - UUID do cliente sendo processado
- `userId` - UUID do usuário autenticado (futuro, quando auth estiver implementado)

**CloudWatch query com MDC:**
```sql
fields @timestamp, message, clientId, operationType
| filter clientId = "uuid-do-cliente"
| filter operationType = "UPDATE_CLIENTE_PF"
| sort @timestamp desc
```

### Níveis de Log

**DEBUG** - Informações detalhadas para debugging (apenas em dev)
```java
log.debug("Buscando cliente PF por PublicId: {}", publicId);
```

**INFO** - Eventos importantes do fluxo normal
```java
log.info("Cliente PF criado com sucesso - PublicId: {}", publicId);
```

**WARN** - Situações anormais mas recuperáveis (validações falhadas)
```java
log.warn("CPF inválido - CPF: {}", MaskingUtil.maskCpf(cpf));
```

**ERROR** - Erros que impedem operação (exceções inesperadas)
```java
log.error("Erro ao criar cliente - CPF: {}", MaskingUtil.maskCpf(cpf), exception);
```

### Structured Arguments

Use argumentos estruturados ao invés de concatenação:

```java
// ❌ BAD - Concatenação de strings
log.info("Cliente " + publicId + " criado com CPF " + cpf);

// ✅ GOOD - Argumentos estruturados
log.info("Cliente criado - PublicId: {}, CPF: {}",
         publicId,
         MaskingUtil.maskCpf(cpf));
```

**Vantagens:**
- Lazy evaluation (não processa strings se log está desabilitado)
- CloudWatch parseia automaticamente os campos
- Melhor performance

### CloudWatch Insights Queries

**Buscar erros de um cliente específico:**
```sql
fields @timestamp, message, severity, exception
| filter clientId = "uuid-do-cliente"
| filter severity = "ERROR"
| sort @timestamp desc
| limit 50
```

**Buscar todas operações de CREATE com sucesso:**
```sql
fields @timestamp, clientId, message
| filter operationType = "CREATE_CLIENTE_PF"
| filter message like /sucesso/
| sort @timestamp desc
```

**Agrupar erros por tipo:**
```sql
fields @timestamp, message, exception
| filter severity = "ERROR"
| stats count() by exception
| sort count desc
```

**Latência por operação:**
```sql
fields operationType, @timestamp, @duration
| stats avg(@duration), max(@duration), count() by operationType
| sort avg(@duration) desc
```

### Exemplos Práticos

**Service Layer:**
```java
@Service
public class CreateClientePFService {
    private static final Logger log = LoggerFactory.getLogger(CreateClientePFService.class);

    @Transactional
    public ClientePFResponse criar(CreateClientePFRequest request) {
        MDC.put("operationType", "CREATE_CLIENTE_PF");

        try {
            log.info("Iniciando criação - CPF: {}", MaskingUtil.maskCpf(request.cpf()));

            validarCpf(request.cpf());

            ClientePF cliente = save(request);
            MDC.put("clientId", cliente.getPublicId().toString());

            log.info("Cliente criado - PublicId: {}", cliente.getPublicId());

            return toResponse(cliente);

        } catch (CpfInvalidoException e) {
            log.warn("CPF inválido - CPF: {}", MaskingUtil.maskCpf(request.cpf()));
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar cliente - CPF: {}",
                     MaskingUtil.maskCpf(request.cpf()), e);
            throw e;
        } finally {
            MDC.remove("operationType");
            MDC.remove("clientId");
        }
    }
}
```

**Exception Handler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCpfInvalido(CpfInvalidoException e) {
        log.warn("Validação falhou - {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
```

### Testing

**Testar Correlation ID:**
```java
@Test
void deveAdicionarCorrelationIdAoMDC() {
    // Verifica que MDC contém correlationId durante processamento
    assertThat(MDC.get("correlationId")).isNotNull();
}
```

**Testar Mascaramento:**
```java
@Test
void deveMascararCpf() {
    String resultado = MaskingUtil.maskCpf("12345678910");
    assertThat(resultado).isEqualTo("***.***.789-10");
}
```

### Checklist de Logging

Ao criar novos services, sempre:

- [ ] Declarar `private static final Logger log = LoggerFactory.getLogger()`
- [ ] Adicionar `MDC.put("operationType", "NOME_OPERACAO")` no início
- [ ] Usar `MaskingUtil` para mascarar CPF, CNPJ, email, nome, telefone
- [ ] Logar início e fim de operações importantes (INFO level)
- [ ] Logar exceções de negócio com WARN (validações falhadas)
- [ ] Logar exceções técnicas com ERROR (incluir stack trace)
- [ ] Limpar MDC no `finally` block (CRÍTICO!)
- [ ] Usar argumentos estruturados, não concatenação
- [ ] Testar que logs estão corretos e dados mascarados

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

## Integration Architecture (Hybrid: Step Functions + Kafka)

### Overview

O cliente-core utiliza **arquitetura híbrida** para integração com outros microserviços:

- **Step Functions (AWS)**: Cliente-core é **chamado** por Step Functions de outros MS (validação de cliente em fluxos transacionais)
- **Kafka (MSK)**: Cliente-core **publica** eventos quando dados mudam e **consome** eventos para atualizar métricas

**📄 Documentação Completa:** `docs/INTEGRATION_ARCHITECTURE.md`

### Papel do cliente-core

❌ **NÃO inicia Step Functions** - Cliente-core é apenas CRUD
✅ **É chamado POR Step Functions** - Outros MS validam se cliente existe
✅ **Publica eventos Kafka** - Notifica quando cliente é criado/atualizado
✅ **Consome eventos Kafka** - Atualiza métricas quando venda é concluída

### Endpoints Consumidos por Step Functions

Outros microserviços chamam cliente-core via Step Functions:

| Endpoint | Usado Por | Quando |
|----------|-----------|--------|
| `GET /v1/clientes/pf/{publicId}` | venda-core | Validar comprador/vendedor antes de criar venda |
| `GET /v1/clientes/pj/{publicId}` | venda-core | Validar empresa antes de criar venda |

**Exemplo Step Function (venda-core):**
```json
{
  "ValidarCompradorExiste": {
    "Type": "Task",
    "Resource": "arn:aws:states:::http:invoke",
    "Parameters": {
      "ApiEndpoint": "https://cliente-core/v1/clientes/pf/${compradorId}",
      "Method": "GET",
      "Headers": {
        "X-Correlation-ID.$": "$.correlationId"
      }
    },
    "Retry": [{"ErrorEquals": ["States.TaskFailed"], "MaxAttempts": 3}],
    "Next": "CriarVenda"
  }
}
```

### Eventos Kafka Publicados

Cliente-core publica eventos quando o estado muda:

| Topic | Event | Quando | Consumidores |
|-------|-------|--------|--------------|
| `cliente-events` | `ClientePFCriado` | POST /v1/clientes/pf (sucesso) | analytics-core, notificacao-core, auditoria-core |
| `cliente-events` | `ClientePJCriado` | POST /v1/clientes/pj (sucesso) | analytics-core, notificacao-core, auditoria-core |
| `cliente-events` | `ClientePFAtualizado` | PUT /v1/clientes/pf/{id} (sucesso) | auditoria-core, analytics-core |
| `cliente-events` | `ClienteDeletado` | DELETE /v1/clientes/{id} (futuro) | auditoria-core |

**Exemplo de Evento:**
```json
{
  "eventType": "ClientePFCriado",
  "correlationId": "abc-123",
  "timestamp": "2025-11-03T19:00:00Z",
  "payload": {
    "clienteId": "uuid",
    "cpf": "***.***.789-10",
    "email": "jo***@example.com"
  }
}
```

### Eventos Kafka Consumidos

Cliente-core consome eventos de outros MS para atualizar métricas:

| Topic | Event | Ação |
|-------|-------|------|
| `venda-events` | `VendaConcluida` | Incrementa `totalVendasRealizadas` do vendedor e `totalComprasRealizadas` do comprador |
| `venda-events` | `VendaCancelada` | Rollback das métricas (decrementa contadores) |

**Consumer com Idempotência:**
```java
@KafkaListener(topics = "venda-events", groupId = "cliente-core-metrics-group")
@Transactional
public void handleVendaConcluida(VendaConcluidaEvent event) {
    MDC.put("correlationId", event.getCorrelationId());

    // Idempotência: evita processar evento duplicado
    if (eventoProcessadoRepository.existsByEventoId(event.getVendaId())) {
        log.warn("Evento duplicado ignorado - VendaId: {}", event.getVendaId());
        return;
    }

    // Atualiza métricas
    vendedor.incrementarTotalVendas(event.getValorProduto());
    comprador.incrementarTotalCompras(event.getValorTotal());

    // Marca como processado (mesma transação!)
    eventoProcessadoRepository.save(new EventoProcessado(event.getVendaId()));
}
```

### Correlation ID Propagation

**Em Step Functions:**
- Cliente-core RECEBE `X-Correlation-ID` no header
- CorrelationIdFilter adiciona ao MDC automaticamente
- Todos os logs incluem o Correlation ID
- Response retorna o mesmo header (propagação)

**Em Kafka:**
- Producer: Correlation ID do MDC incluído no evento
- Consumer: Correlation ID do evento adicionado ao MDC
- CloudWatch: Rastreamento completo da jornada

**Query CloudWatch (jornada completa):**
```sql
fields @timestamp, @message, correlationId, service
| filter correlationId = "abc-123"
| sort @timestamp asc
```

### Integration Contracts

**Response format:** JSON
**Authentication:** OAuth2 + JWT (to be implemented)
**Versioning:** URI versioning `/v1/clientes/pf`
**Idempotency:** Via `X-Idempotency-Key` header (to be implemented)

**When creating endpoints:**
- Return DTOs, not entities (avoid exposing JPA internals)
- Use proper HTTP status codes (200, 201, 400, 404, 500)
- Include pagination for list endpoints
- Implement filtering and sorting parameters
- Add `X-Correlation-ID` to all responses

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
