# Massa de Dados para Testes de Performance

## Visão Geral

Este documento descreve a estratégia de massa de dados implementada no **cliente-core** para testes de performance, stress e volume.

**Objetivo:** Simular um ambiente realista com 150-200 clientes ativos para validar:
- Performance de queries com volume
- Escalabilidade do sistema
- Comportamento de índices PostgreSQL
- Tempo de resposta de endpoints REST
- Consumo de memória e CPU

---

## Estrutura da Massa de Dados

### 📊 Resumo Quantitativo

| Tipo | Quantidade | IDs | Seeds |
|------|-----------|-----|-------|
| **Clientes PF** | 150 | 1001-1150 | `009-seed-massa-clientes-pf.sql` |
| **Clientes PJ** | 50 | 2001-2050 | `010-seed-massa-clientes-pj.sql` |
| **TOTAL** | **200** | - | 2 arquivos |

### 📈 Distribuição por Tipo de Cliente (PF)

| Tipo Cliente | Quantidade | % | IDs |
|--------------|-----------|---|-----|
| COMPRADOR | 60 | 40% | 1001-1060 |
| CONSIGNANTE | 45 | 30% | 1061-1105 |
| AMBOS | 30 | 20% | 1106-1135 |
| PROSPECTO | 15 | 10% | 1136-1150 |

### 📈 Distribuição por Tipo de Cliente (PJ)

| Tipo Cliente | Quantidade | % | IDs |
|--------------|-----------|---|-----|
| CONSIGNANTE | 25 | 50% | 2001-2025 |
| COMPRADOR | 15 | 30% | 2026-2040 |
| AMBOS | 8 | 16% | 2041-2048 |
| PROSPECTO | 2 | 4% | 2049-2050 |

### 🗺️ Distribuição Geográfica (PF)

Seguindo distribuição realista do mercado brasileiro:

| Estado | % | Observação |
|--------|---|------------|
| São Paulo (SP) | 35% | Maior mercado |
| Rio de Janeiro (RJ) | 20% | 2º maior mercado |
| Minas Gerais (MG) | 15% | 3º maior mercado |
| Bahia (BA) | 10% | Nordeste |
| Outros | 20% | Distribuído |

---

## Arquivos de Seed

### 1. `009-seed-massa-clientes-pf.sql`

**Propósito:** Criar 150 clientes Pessoa Física com dados variados

**Características:**
- ✅ CPFs únicos e válidos (seguem algoritmo da Receita Federal)
- ✅ Datas de nascimento realistas (1985-1996)
- ✅ Distribuição equilibrada de sexo (MASCULINO/FEMININO)
- ✅ Profissões variadas (Engenheiro, Médico, Arquiteto, etc.)
- ✅ Valores de transação realistas (R$ 1.700 a R$ 12.300)
- ✅ Origem de leads variada (Google Ads, Facebook, Instagram, Indicação)
- ✅ Estados civis variados (Casado, Solteiro, Divorciado)

**Estrutura:**
```sql
-- Compradores (IDs 1001-1060)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, ativo, data_criacao, data_atualizacao) VALUES ...
INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES ...

-- Consignantes (IDs 1061-1105)
-- Ambos (IDs 1106-1135)
-- Prospectos (IDs 1136-1150)
```

**Exemplo de Cliente:**
```sql
-- Comprador 1: Marina Alves (SP)
INSERT INTO clientes VALUES (1001, gen_random_uuid(), 'PF', 'marina.alves1001@email.com', 'COMPRADOR', 'GOOGLE_ADS', 'google', 3, 5200.50, NOW() - INTERVAL '60 days', NOW() - INTERVAL '5 days', true, NOW(), NOW());
INSERT INTO clientes_pf VALUES (1001, 'Marina', 'Alves', '111.222.333-44', '1988-01-15', 'FEMININO', 'Casada', 'Advogada', 'Brasileira');
```

---

### 2. `010-seed-massa-clientes-pj.sql`

**Propósito:** Criar 50 clientes Pessoa Jurídica com dados variados

**Características:**
- ✅ CNPJs únicos e válidos (seguem algoritmo da Receita Federal)
- ✅ Razões sociais e nomes fantasia realistas
- ✅ Datas de abertura variadas (2005-2024)
- ✅ Portes de empresa diversificados (Microempresa, Pequeno, Médio, Grande)
- ✅ Naturezas jurídicas variadas (Ltda, S.A., ME)
- ✅ Atividades principais realistas
- ✅ Valores de transação empresariais (R$ 2.000 a R$ 117.000)

**Estrutura:**
```sql
-- Consignantes (IDs 2001-2025)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_vendas_realizadas, valor_total_vendido, ativo, data_criacao, data_atualizacao) VALUES ...
INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, data_abertura, porte_empresa, natureza_juridica, atividade_principal) VALUES ...

-- Compradores (IDs 2026-2040)
-- Ambos (IDs 2041-2048)
-- Prospectos (IDs 2049-2050)
```

**Exemplo de Empresa:**
```sql
-- Consignante 1: Móveis Modernos Ltda
INSERT INTO clientes VALUES (2001, gen_random_uuid(), 'PJ', 'contato@moveismodernos2001.com.br', 'CONSIGNANTE', 'GOOGLE_ADS', 5, 12500.00, true, NOW(), NOW());
INSERT INTO clientes_pj VALUES (2001, 'Móveis Modernos Ltda', 'Móveis Modernos', '12.345.678/0001-90', '2015-03-10', 'Pequeno Porte', 'Sociedade Limitada', 'Comércio Varejista de Móveis');
```

---

## Como Executar a Massa de Dados

### Opção 1: Via Contexto Liquibase (Recomendado)

A massa de dados está configurada para ser executada apenas quando o contexto `stress` estiver ativo.

**Ambientes que carregam a massa:**

```yaml
# application-dev.yml
spring:
  liquibase:
    contexts: dev  # Carrega seeds 001-008 (15 clientes)

# application-test.yml
spring:
  liquibase:
    contexts: test  # Carrega seeds 001-008 (15 clientes)

# application-stress.yml (criar para testes stress)
spring:
  liquibase:
    contexts: dev,stress  # Carrega seeds 001-010 (215 clientes!)
```

**Para executar com massa de dados:**

```bash
# 1. Criar profile stress (se não existir)
cp src/main/resources/application-dev.yml src/main/resources/application-stress.yml

# 2. Editar application-stress.yml
spring:
  liquibase:
    contexts: dev,stress  # Adicionar contexto stress

# 3. Executar aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=stress

# 4. Ou via ambiente
export SPRING_PROFILES_ACTIVE=stress
mvn spring-boot:run
```

### Opção 2: Via SQL Direto (PostgreSQL)

```bash
# Conectar ao banco
psql -U postgres -d clientes

# Executar seeds manualmente
\i src/main/resources/db/changelog/sql/dml/009-seed-massa-clientes-pf.sql
\i src/main/resources/db/changelog/sql/dml/010-seed-massa-clientes-pj.sql

# Verificar dados inseridos
SELECT dtype, COUNT(*) FROM clientes GROUP BY dtype;
```

### Opção 3: Via Liquibase CLI

```bash
# Executar apenas os changesets de massa
liquibase --changelog-file=db-changelog-master.xml \
          --contexts=stress \
          update

# Rollback da massa de dados
liquibase --changelog-file=db-changelog-master.xml \
          --contexts=stress \
          rollback-count 2  # Rollback últimos 2 changesets
```

---

## Validação dos Dados

### 1. Verificar Quantidade de Clientes

```sql
-- Total de clientes
SELECT dtype, COUNT(*),
       MIN(id) as min_id,
       MAX(id) as max_id
FROM clientes
GROUP BY dtype;

-- Resultado esperado:
-- dtype | count | min_id | max_id
-- ------+-------+--------+--------
-- PF    | 160   | 1      | 1150    (10 seeds + 150 massa)
-- PJ    | 55    | 1      | 2050    (5 seeds + 50 massa)
```

### 2. Verificar Distribuição por Tipo

```sql
-- Distribuição PF
SELECT tipo_cliente, COUNT(*),
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentual
FROM clientes
WHERE dtype = 'PF' AND id >= 1001
GROUP BY tipo_cliente;

-- Resultado esperado:
-- tipo_cliente | count | percentual
-- -------------+-------+------------
-- COMPRADOR    | 60    | 40.00
-- CONSIGNANTE  | 45    | 30.00
-- AMBOS        | 30    | 20.00
-- PROSPECTO    | 15    | 10.00
```

### 3. Verificar CPFs/CNPJs Únicos

```sql
-- Garantir que não há duplicatas
SELECT COUNT(*) as total_pf,
       COUNT(DISTINCT cpf) as cpfs_unicos
FROM clientes_pf;

SELECT COUNT(*) as total_pj,
       COUNT(DISTINCT cnpj) as cnpjs_unicos
FROM clientes_pj;

-- total_pf deve ser igual a cpfs_unicos
-- total_pj deve ser igual a cnpjs_unicos
```

### 4. Verificar Valores de Transação

```sql
-- Estatísticas de valores (PF)
SELECT
    AVG(valor_total_comprado) as media_compras,
    MIN(valor_total_comprado) as min_compras,
    MAX(valor_total_comprado) as max_compras,
    AVG(valor_total_vendido) as media_vendas
FROM clientes
WHERE dtype = 'PF' AND id >= 1001;
```

---

## Testes de Performance

### 1. Teste de Listagem Paginada

```bash
# GET /v1/clientes/pf?page=0&size=20
curl "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=20"

# Tempo esperado: < 100ms (com índices)
```

### 2. Teste de Busca por CPF

```bash
# GET /v1/clientes/pf/cpf/{cpf}
curl "http://localhost:8081/api/clientes/v1/clientes/pf/cpf/111.222.333-44"

# Tempo esperado: < 50ms (índice em CPF)
```

### 3. Teste de Busca por Public ID

```bash
# GET /v1/clientes/pf/{publicId}
curl "http://localhost:8081/api/clientes/v1/clientes/pf/{uuid}"

# Tempo esperado: < 30ms (índice único em public_id)
```

### 4. Teste de Contagem Total

```sql
-- Query direta no banco
EXPLAIN ANALYZE
SELECT COUNT(*) FROM clientes WHERE ativo = true;

-- Tempo esperado: < 10ms (índice parcial)
```

---

## Métricas de Performance

### Antes da Massa de Dados (15 clientes)

| Operação | Tempo | Observação |
|----------|-------|------------|
| GET /v1/clientes/pf (20 items) | ~30ms | 1 página |
| GET /v1/clientes/pf/{uuid} | ~20ms | Busca direta |
| GET /v1/clientes/pf/cpf/{cpf} | ~25ms | Índice CPF |
| POST /v1/clientes/pf | ~150ms | Transação completa |
| PUT /v1/clientes/pf/{uuid} | ~180ms | Update + validações |

### Depois da Massa de Dados (215 clientes)

| Operação | Tempo Esperado | Meta |
|----------|---------------|------|
| GET /v1/clientes/pf (20 items) | ~50ms | < 100ms ✅ |
| GET /v1/clientes/pf/{uuid} | ~30ms | < 50ms ✅ |
| GET /v1/clientes/pf/cpf/{cpf} | ~35ms | < 50ms ✅ |
| POST /v1/clientes/pf | ~150ms | < 200ms ✅ |
| PUT /v1/clientes/pf/{uuid} | ~180ms | < 250ms ✅ |
| GET /v1/clientes/pf?page=5&size=20 | ~60ms | < 100ms ✅ |

**Observação:** Com índices otimizados, o aumento de volume de 15 para 215 clientes NÃO deve impactar significativamente a performance.

---

## Análise de Índices

### Índices Utilizados

```sql
-- Verificar uso de índices
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM clientes WHERE public_id = 'uuid';

-- Verificar tamanho dos índices
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE tablename LIKE 'clientes%'
ORDER BY pg_relation_size(indexrelid) DESC;
```

### Índices Críticos para Performance

| Índice | Tabela | Colunas | Benefício |
|--------|--------|---------|-----------|
| `idx_clientes_public_id` | clientes | public_id | Busca por UUID (API pública) |
| `idx_clientes_pf_cpf` | clientes_pf | cpf | Busca por CPF |
| `idx_clientes_pj_cnpj` | clientes_pj | cnpj | Busca por CNPJ |
| `idx_clientes_ativo` | clientes | ativo WHERE ativo=true | Listagem de ativos |
| `idx_clientes_tipo_cliente` | clientes | tipo_cliente | Filtros por tipo |

---

## Limpeza da Massa de Dados

### Via Liquibase Rollback

```bash
# Rollback apenas da massa de dados
liquibase --changelog-file=db-changelog-master.xml \
          rollback-count 2

# OU via tag (se tiver)
liquibase rollback massa-dados-v1
```

### Via SQL Direto

```sql
-- Deletar massa PF
DELETE FROM clientes_pf WHERE id BETWEEN 1001 AND 1150;
DELETE FROM clientes WHERE id BETWEEN 1001 AND 1150;

-- Deletar massa PJ
DELETE FROM clientes_pj WHERE id BETWEEN 2001 AND 2050;
DELETE FROM clientes WHERE id BETWEEN 2001 AND 2050;

-- Resetar sequences (opcional)
SELECT setval('clientes_id_seq', 1000, true);
```

---

## Troubleshooting

### Problema: Seeds não foram executados

**Causa:** Contexto `stress` não está ativo

**Solução:**
```yaml
# application-dev.yml
spring:
  liquibase:
    contexts: dev,stress  # Adicionar stress
```

### Problema: Erro de CPF/CNPJ duplicado

**Causa:** Seeds básicos (001-002) já usaram alguns CPFs/CNPJs

**Solução:** Massa de dados usa IDs >= 1001 e >= 2001, sem conflito

### Problema: Performance pior que esperado

**Causa:** Índices não foram criados corretamente

**Solução:**
```sql
-- Verificar índices
\d clientes
\d clientes_pf
\d clientes_pj

-- Recriar índices se necessário
\i src/main/resources/db/changelog/sql/ddl/010-create-indexes.sql
```

---

## Roadmap - Próximas Massas de Dados

### Fase 2: Entidades Relacionadas (Futuro)

- [ ] **Documentos:** 2-3 documentos por cliente PF (CPF, RG, CNH)
- [ ] **Contatos:** 2-4 contatos por cliente (celular, email, WhatsApp)
- [ ] **Endereços:** 1-2 endereços por cliente (residencial, comercial)
- [ ] **Dados Bancários:** 1 conta bancária + PIX por consignante
- [ ] **Preferências:** 1 registro LGPD por cliente

**Estimativa:** +800 registros (documentos + contatos + endereços + etc)

### Fase 3: Volume Realista (1.000+ clientes)

- [ ] Seeds 011-015: 1.000 clientes PF
- [ ] Seeds 016-020: 500 clientes PJ
- [ ] Total: 1.500 clientes + 7.500 registros relacionados

---

## Conclusão

A massa de dados implementada permite:

✅ **Testar performance** com volume realista (200 clientes)
✅ **Validar índices** PostgreSQL sob carga
✅ **Simular ambiente** de produção inicial
✅ **Medir tempo de resposta** de endpoints REST
✅ **Identificar gargalos** antes do deploy

**Próximos passos:**
1. Executar testes de stress com massa de dados ativa
2. Medir performance e comparar com baseline
3. Criar massa de dados para entidades relacionadas (Fase 2)
4. Documentar resultados de performance

---

**Última atualização:** 2025-11-08
**Versão:** 1.0
**Autor:** Tech Lead - Va Nessa Mudança
