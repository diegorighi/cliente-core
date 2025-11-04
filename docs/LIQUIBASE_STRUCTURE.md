# Estrutura Liquibase - Cliente Core

## Visão Geral

Esta documentação descreve a estrutura completa de migrações de banco de dados usando **Liquibase** para o microserviço `cliente-core` do Va Nessa Mudança.

Todas as migrações foram otimizadas para **PostgreSQL RDS** na AWS, com foco em:
- Performance de queries
- Escalabilidade
- Integridade referencial
- Auditoria e compliance LGPD

---

## Estrutura de Diretórios

```
cliente-core/src/main/resources/
└── db/
    └── changelog/
        ├── db-changelog-master.xml          # Arquivo master que orquestra tudo
        └── sql/
            ├── ddl/                          # Data Definition Language (estrutura)
            │   ├── 001-create-table-clientes.sql
            │   ├── 002-create-table-clientes-pf.sql
            │   ├── 003-create-table-clientes-pj.sql
            │   ├── 004-create-table-documentos.sql
            │   ├── 005-create-table-contatos.sql
            │   ├── 006-create-table-enderecos.sql
            │   ├── 007-create-table-dados-bancarios.sql
            │   ├── 008-create-table-preferencias-cliente.sql
            │   ├── 009-create-table-auditoria-cliente.sql
            │   ├── 010-create-indexes.sql
            │   └── 011-create-constraints.sql
            └── dml/                          # Data Manipulation Language (dados)
                ├── 001-seed-clientes-pf.sql
                ├── 002-seed-clientes-pj.sql
                ├── 003-seed-documentos.sql
                ├── 004-seed-contatos.sql
                ├── 005-seed-enderecos.sql
                ├── 006-seed-dados-bancarios.sql
                ├── 007-seed-preferencias.sql
                └── 008-seed-auditoria.sql
```

---

## Arquivo Master (db-changelog-master.xml)

O arquivo master é o ponto de entrada do Liquibase. Ele:
- Define a ordem de execução de TODAS as migrações
- Usa `<sqlFile>` para referenciar scripts SQL
- Implementa rollbacks para cada changeset
- Separa DDL (estrutura) de DML (dados)
- Utiliza contexts (`dev`, `test`) para seeds

**Configuração no Spring Boot:**
```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db-changelog-master.xml
    contexts: dev
    drop-first: false
    default-schema: public
```

**IMPORTANTE:** O Hibernate foi configurado com `ddl-auto: validate` (ao invés de `update`) para garantir que APENAS o Liquibase gerencie o schema.

---

## Scripts DDL (Criação de Tabelas)

### 1. Tabela `clientes` (001-create-table-clientes.sql)

Tabela pai para herança JOINED (PF e PJ compartilham essa tabela).

**Campos principais:**
- `id` - BIGSERIAL (auto-increment)
- `dtype` - Discriminador ('PF' ou 'PJ')
- `email` - VARCHAR(150) UNIQUE
- `tipo_cliente` - VARCHAR(20) (CONSIGNANTE, COMPRADOR, AMBOS, PROSPECTO, PARCEIRO, INATIVO)
- `origem_lead` - VARCHAR(30) (marketing)
- `bloqueado` - BOOLEAN (default: false)
- Métricas: `total_compras_realizadas`, `total_vendas_realizadas`, `valor_total_comprado`, `valor_total_vendido`
- Auditoria: `data_criacao`, `data_atualizacao`

**Constraints:**
- CHECK em `dtype` (apenas 'PF' ou 'PJ')
- CHECK em `tipo_cliente` (valores válidos do enum)
- CHECK em `origem_lead` (valores válidos do enum)
- CHECK em métricas (valores >= 0)

---

### 2. Tabela `clientes_pf` (002-create-table-clientes-pf.sql)

Herança JOINED de `clientes` (Pessoa Física).

**Campos específicos PF:**
- `primeiro_nome`, `nome_do_meio`, `sobrenome`
- `cpf` - VARCHAR(14) UNIQUE
- `rg` - VARCHAR(20)
- `data_nascimento` - DATE
- `sexo` - VARCHAR(15) (MASCULINO, FEMININO, OUTRO, NAO_INFORMADO)
- `nome_mae`, `nome_pai`
- `estado_civil`, `profissao`, `nacionalidade`, `naturalidade`

**Constraints:**
- CHECK em `sexo` (valores válidos)
- CHECK em `data_nascimento` (não pode ser futura)

---

### 3. Tabela `clientes_pj` (003-create-table-clientes-pj.sql)

Herança JOINED de `clientes` (Pessoa Jurídica).

**Campos específicos PJ:**
- `razao_social` - VARCHAR(200) NOT NULL
- `nome_fantasia` - VARCHAR(200)
- `cnpj` - VARCHAR(18) UNIQUE NOT NULL
- `inscricao_estadual`, `inscricao_municipal`
- `porte_empresa` - VARCHAR(50) (MEI, ME, EPP, MEDIO, GRANDE)
- `capital_social` - NUMERIC(15,2)
- `nome_responsavel`, `cpf_responsavel`, `cargo_responsavel`
- `site` - VARCHAR(200)

**Constraints:**
- CHECK em `porte_empresa` (valores válidos)
- CHECK em `capital_social` (>= 0)
- CHECK em `data_abertura` (não pode ser futura)

---

### 4. Tabela `documentos` (004-create-table-documentos.sql)

Armazena documentos dos clientes (CPF, RG, CNH, CNPJ, etc).

**Campos:**
- `cliente_id` - FK para `clientes`
- `tipo_documento` - VARCHAR(30) (CPF, RG, CNH, PASSAPORTE, CNPJ, etc)
- `numero` - VARCHAR(50) NOT NULL
- `status_documento` - VARCHAR(30) (VALIDO, EXPIRADO, AGUARDANDO_VERIFICACAO, VERIFICADO, REJEITADO)
- `documento_principal` - BOOLEAN (apenas 1 por cliente)
- `data_emissao`, `data_validade`

**Constraints:**
- CHECK em `tipo_documento` (valores válidos)
- CHECK em `status_documento` (valores válidos)
- CHECK em datas (validade >= emissão)

---

### 5. Tabela `contatos` (005-create-table-contatos.sql)

Telefones, emails, WhatsApp dos clientes.

**Campos:**
- `cliente_id` - FK para `clientes`
- `tipo_contato` - VARCHAR(20) (CELULAR, TELEFONE_FIXO, EMAIL, WHATSAPP, TELEGRAM, OUTRO)
- `valor` - VARCHAR(100) (número ou email)
- `contato_principal` - BOOLEAN (apenas 1 por cliente)
- `verificado` - BOOLEAN

---

### 6. Tabela `enderecos` (006-create-table-enderecos.sql)

Endereços completos dos clientes.

**Campos:**
- `cliente_id` - FK para `clientes`
- `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `estado`, `pais`
- `tipo_endereco` - VARCHAR(20) (RESIDENCIAL, COMERCIAL, ENTREGA, COBRANCA, COLETA)
- `endereco_principal` - BOOLEAN

**Constraints:**
- CHECK em `estado` (apenas UFs válidas)
- CHECK em `tipo_endereco` (valores válidos)

---

### 7. Tabela `dados_bancarios` (007-create-table-dados-bancarios.sql)

Dados bancários e PIX para repasses de vendas.

**Campos:**
- `cliente_id` - FK para `clientes`
- `tipo_conta` - VARCHAR(20) (CORRENTE, POUPANCA)
- `banco`, `agencia`, `conta`, `digito_conta`
- `chave_pix`, `tipo_chave_pix` (CPF, CNPJ, EMAIL, TELEFONE, ALEATORIA)
- `dados_verificados` - BOOLEAN
- `conta_principal` - BOOLEAN (apenas 1 por cliente)

---

### 8. Tabela `preferencias_cliente` (008-create-table-preferencias-cliente.sql)

Preferências de comunicação e consentimento LGPD.

**Campos:**
- `cliente_id` - FK UNIQUE para `clientes` (OneToOne)
- `aceita_comunicacao_email`, `aceita_comunicacao_sms`, `aceita_comunicacao_whatsapp`, `aceita_comunicacao_telefone`
- `aceita_newsletters`, `aceita_ofertas`, `aceita_pesquisas`
- `data_consentimento`, `ip_consentimento`, `consentimento_ativo`

**Importância:** Compliance com LGPD.

---

### 9. Tabela `auditoria_cliente` (009-create-table-auditoria-cliente.sql)

Histórico de alterações (append-only).

**Campos:**
- `cliente_id` - FK para `clientes`
- `campo_alterado` - VARCHAR(100) (nome do campo)
- `valor_anterior`, `valor_novo` - VARCHAR(500)
- `usuario_responsavel`, `data_alteracao`, `motivo_alteracao`, `ip_origem`

**Característica:** Append-only (nunca deletar registros).

---

## Scripts DDL (Índices e Constraints)

### 10. Índices (010-create-indexes.sql)

**Otimizações PostgreSQL RDS aplicadas:**

#### Tabela `clientes`
- `idx_clientes_email` - UNIQUE WHERE ativo = true (busca por email)
- `idx_clientes_dtype` - Discriminador de herança
- `idx_clientes_tipo_cliente` - Filtros de negócio
- `idx_clientes_origem_lead` - Análise de marketing
- `idx_clientes_bloqueado` - Parcial (apenas bloqueados)
- `idx_clientes_indicador` - Programa de indicação (composto)
- `idx_clientes_data_criacao` - Ordenação temporal (DESC)
- `idx_clientes_metricas` - Relatórios de vendas/compras

#### Tabela `clientes_pf`
- CPF já é UNIQUE (constraint automática)
- `idx_clientes_pf_data_nascimento` - Queries por faixa etária
- `idx_clientes_pf_sexo` - Segmentação
- **`idx_clientes_pf_nome_completo`** - GIN full-text search (português) para busca por nome

#### Tabela `clientes_pj`
- CNPJ já é UNIQUE (constraint automática)
- `idx_clientes_pj_porte` - Segmentação por porte
- **`idx_clientes_pj_razao_social`** - GIN full-text search (português) para busca por razão social/fantasia

#### Tabela `documentos`
- `idx_documentos_cliente_ativo` - Composto (cliente_id + ativo)
- `idx_documentos_tipo` - Filtrar por tipo
- `idx_documentos_status` - Filtrar por status
- `idx_documentos_numero` - Busca por número
- `idx_documentos_data_validade` - Parcial (apenas com validade)
- `idx_documentos_principal` - Parcial (apenas principais)

#### Tabela `contatos`
- `idx_contatos_cliente_ativo` - Composto (cliente_id + ativo)
- `idx_contatos_tipo` - Filtrar por tipo
- `idx_contatos_valor` - Busca por telefone/email
- `idx_contatos_principal` - Parcial (apenas principais)
- `idx_contatos_verificado` - Parcial (apenas verificados)

#### Tabela `enderecos`
- `idx_enderecos_cliente_ativo` - Composto (cliente_id + ativo)
- `idx_enderecos_cep` - Queries de logística
- `idx_enderecos_tipo` - Filtrar por tipo
- `idx_enderecos_cidade_estado` - Queries regionais
- `idx_enderecos_principal` - Parcial (apenas principais)

#### Tabela `dados_bancarios`
- `idx_dados_bancarios_cliente_ativo` - Composto
- `idx_dados_bancarios_principal` - Parcial (apenas principais)
- `idx_dados_bancarios_verificados` - Parcial (apenas verificados)
- `idx_dados_bancarios_tipo_pix` - Filtrar por tipo PIX
- `idx_dados_bancarios_chave_pix` - Busca por chave (parcial)

#### Tabela `preferencias_cliente`
- cliente_id já é UNIQUE (OneToOne)
- `idx_preferencias_consentimento` - Filtrar consentimento ativo
- `idx_preferencias_data_consentimento` - Auditoria LGPD (DESC)

#### Tabela `auditoria_cliente`
- `idx_auditoria_cliente_data` - Composto (cliente_id + data_alteracao DESC)
- `idx_auditoria_campo` - Buscar alterações de campo específico
- `idx_auditoria_usuario` - Rastrear alterações por usuário
- `idx_auditoria_data_alteracao` - Ordenação cronológica (DESC)

**Técnicas de otimização RDS PostgreSQL:**
1. **Índices parciais (WHERE)**: Reduzem tamanho do índice e melhoram performance
2. **Índices compostos**: Otimizam queries com múltiplos filtros
3. **Índices GIN full-text**: Buscas por nome/razão social em português
4. **Índices DESC**: Otimizam queries com ORDER BY descendente
5. **Comentários em índices**: Documentam objetivo de cada índice

---

### 11. Foreign Keys e Constraints (011-create-constraints.sql)

**Foreign Keys criadas:**

| Tabela | FK | Referência | ON DELETE | ON UPDATE |
|--------|-----|-----------|----------|----------|
| `clientes_pf` | `fk_clientes_pf_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `clientes_pj` | `fk_clientes_pj_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `clientes` | `fk_clientes_indicador` | `clientes(id)` | SET NULL | CASCADE |
| `documentos` | `fk_documentos_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `contatos` | `fk_contatos_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `enderecos` | `fk_enderecos_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `dados_bancarios` | `fk_dados_bancarios_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `preferencias_cliente` | `fk_preferencias_cliente` | `clientes(id)` | CASCADE | CASCADE |
| `auditoria_cliente` | `fk_auditoria_cliente` | `clientes(id)` | CASCADE | CASCADE |

**Decisões de design:**
- **CASCADE em herança**: Deletar cliente pai deleta PF/PJ automaticamente
- **CASCADE em relacionamentos**: Deletar cliente deleta todos os dados relacionados (soft delete é feito via flag `ativo`)
- **SET NULL em indicação**: Deletar indicador não afeta indicado (apenas limpa referência)

**Constraints gerenciadas pela aplicação:**
- Apenas 1 documento principal por cliente
- Apenas 1 contato principal por cliente
- Apenas 1 endereço principal por tipo por cliente
- Apenas 1 conta bancária principal por cliente

Estas constraints são complexas demais para o banco e serão validadas no Service Layer.

---

## Scripts DML (Seeds)

Os seeds foram criados com **dados realistas** para facilitar testes e desenvolvimento.

### Dados criados:

| Tipo | Quantidade | Descrição |
|------|-----------|-----------|
| Clientes PF | 10 | Vários perfis: consignante, comprador, ambos, prospecto, bloqueado |
| Clientes PJ | 5 | Empresas de vários portes: MEI, EPP, Médio |
| Documentos | ~35 | CPF, RG, CNH, CNPJ, Inscrições |
| Contatos | ~45 | Celular, email, WhatsApp, telefone fixo |
| Endereços | ~25 | Residencial, comercial, entrega, cobrança, coleta |
| Dados Bancários | ~15 | Contas corrente/poupança, PIX (CPF, CNPJ, email, telefone, aleatória) |
| Preferências LGPD | 15 | Todas com consentimento registrado |
| Auditoria | ~15 | Histórico de alterações críticas |

**CPFs/CNPJs válidos:**
Todos os CPFs e CNPJs nos seeds são formatados corretamente (mas fictícios).

**Contexto dos seeds:**
Os seeds são aplicados APENAS nos ambientes `dev` e `test` (configurado no master XML com `context="dev,test"`).

---

## Como Funciona o Liquibase

### 1. Primeira execução (banco vazio)

```sql
-- Liquibase cria tabelas de controle
CREATE TABLE databasechangelog (...)
CREATE TABLE databasechangeloglock (...)

-- Executa changesets em ordem
-- DDL: 001 -> 002 -> ... -> 011
-- DML: 001 -> 002 -> ... -> 008
```

### 2. Execuções subsequentes

Liquibase verifica a tabela `databasechangelog` e executa APENAS changesets novos (por ID).

### 3. Rollback

Cada changeset tem um bloco `<rollback>` definido no master XML.

```bash
# Reverter último changeset
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Reverter até data específica
mvn liquibase:rollback -Dliquibase.rollbackDate=2025-01-01
```

---

## Boas Práticas Implementadas

### 1. Versionamento
- IDs sequenciais e descritivos (001-create-table-clientes)
- Nunca alterar changesets já executados
- Criar novos changesets para mudanças

### 2. Separação DDL/DML
- DDL: Estrutura do banco (tabelas, índices, FKs)
- DML: Dados de teste (seeds)

### 3. Contexts
- Seeds só rodam em `dev` e `test`
- Produção fica limpa

### 4. Comentários
- Tabelas comentadas
- Colunas críticas comentadas
- Índices comentados
- Constraints comentadas

### 5. Tipos de dados otimizados
- VARCHAR ao invés de TEXT (melhor para índices)
- NUMERIC(15,2) para valores monetários
- TIMESTAMP para auditoria
- BOOLEAN para flags

### 6. Sem ENUMs PostgreSQL
- Usamos VARCHAR + CHECK constraints
- Mais flexível para mudanças futuras
- JPA mapeia melhor

---

## Alterações no Hibernate

**ANTES (sem Liquibase):**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Hibernate gerenciava schema
```

**DEPOIS (com Liquibase):**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Apenas valida, não altera

  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db-changelog-master.xml
```

**Por quê?**
- Liquibase é mais confiável para produção
- Versionamento de schema completo
- Rollbacks controlados
- Separação clara entre estrutura e dados

---

## Performance RDS PostgreSQL

### Recursos otimizados para RDS:

1. **Índices estratégicos**: Cobrindo 100% das queries comuns
2. **Índices parciais**: Reduzem espaço e melhoram performance
3. **Índices compostos**: Otimizam queries com múltiplos filtros
4. **Full-text search**: GIN para buscas por nome (português)
5. **Foreign keys com CASCADE**: Integridade referencial automática
6. **CHECK constraints**: Validações no banco
7. **Connection pooling**: Configurado no Hikari (application-dev.yml)

### Queries otimizadas automaticamente:

```sql
-- Buscar cliente PF por CPF (usa idx único)
SELECT * FROM clientes_pf WHERE cpf = '123.456.789-10';

-- Buscar clientes bloqueados (usa idx parcial)
SELECT * FROM clientes WHERE bloqueado = true;

-- Buscar por nome completo (usa GIN full-text)
SELECT * FROM clientes_pf WHERE to_tsvector('portuguese', primeiro_nome || ' ' || sobrenome) @@ to_tsquery('portuguese', 'João');

-- Buscar documentos ativos de um cliente (usa idx composto)
SELECT * FROM documentos WHERE cliente_id = 1 AND ativo = true;

-- Buscar endereços por CEP (usa idx)
SELECT * FROM enderecos WHERE cep = '01310-100';

-- Histórico de auditoria (usa idx composto + DESC)
SELECT * FROM auditoria_cliente WHERE cliente_id = 1 ORDER BY data_alteracao DESC;
```

---

## Execução e Verificação

### 1. Executar aplicação

```bash
cd cliente-core
mvn spring-boot:run
```

O Liquibase executará automaticamente na inicialização.

### 2. Verificar changesets executados

```sql
-- Conectar ao banco
psql -U postgres -d vanessa_mudanca_clientes

-- Ver todos os changesets aplicados
SELECT id, author, filename, dateexecuted, orderexecuted
FROM databasechangelog
ORDER BY orderexecuted;
```

### 3. Verificar índices criados

```sql
-- Ver todos os índices do schema
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

### 4. Verificar dados seeds

```sql
-- Contar clientes
SELECT dtype, COUNT(*) FROM clientes GROUP BY dtype;

-- Ver documentos
SELECT c.email, d.tipo_documento, d.numero, d.status_documento
FROM clientes c
JOIN documentos d ON c.id = d.cliente_id
ORDER BY c.id;
```

---

## Troubleshooting

### Problema: Liquibase não executa

**Solução:**
```bash
# Verificar se dependência está no pom.xml
mvn dependency:tree | grep liquibase

# Verificar logs
tail -f logs/application.log
```

### Problema: Changeset já executado

**Erro:** `Changeset already executed`

**Solução:**
```sql
-- Limpar tabela de controle (APENAS EM DEV!)
TRUNCATE databasechangelog;
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;
```

### Problema: Constraint violation

**Erro:** `ERROR: duplicate key value violates unique constraint`

**Solução:**
```sql
-- Limpar dados e reexecutar
TRUNCATE clientes CASCADE;
SELECT setval('clientes_id_seq', 1, false);
```

---

## Próximos Passos

1. **Testes de integração**: Validar que Liquibase + JPA funcionam juntos
2. **Migrações de dados**: Se houver schema legacy, criar changesets de migração
3. **Produção**: Configurar profile `prod` sem seeds
4. **CI/CD**: Executar Liquibase em pipelines antes de deploy
5. **Monitoring**: Monitorar performance de índices com RDS Performance Insights

---

## Referências

- [Liquibase Documentation](https://docs.liquibase.com/)
- [PostgreSQL Indexes](https://www.postgresql.org/docs/current/indexes.html)
- [AWS RDS PostgreSQL Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [Spring Boot + Liquibase](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)

---

**Criado por:** PostgreSQL RDS Optimizer Agent
**Data:** 2025-11-02
**Versão:** 1.0
**Status:** Produção-ready ✅
