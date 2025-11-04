# 🚀 Estratégia de CI/CD com JMeter

## 📋 Visão Geral

Este documento descreve a estratégia completa de CI/CD do **cliente-core**, incluindo testes de performance com JMeter antes de deploy em produção.

---

## 🌳 Estratégia de Branches (GitFlow Simplificado)

```
feature/* ────► developer ────► release ────► main (PROD)
   │               │               │             │
   ├─ CI básico    ├─ CI completo  ├─ Load test  └─ Deploy automático
   └─ Unit tests   ├─ Integration  └─ Aprovação
                   └─ Smoke test      manual
```

### **Branches e seus propósitos:**

| Branch | Ambiente | Propósito | Deploy |
|--------|----------|-----------|--------|
| `feature/*` | Local | Desenvolvimento de features | Manual (dev local) |
| `developer` | Homologação | Integração contínua + smoke tests | Automático (após CI) |
| `release` | Pre-Prod | Testes de carga + validação QA | Automático (após aprovação) |
| `main` | Produção | Código estável e testado | Automático (protegido) |

---

## 🔄 Workflows GitHub Actions

### **1. CI Básico (Feature Branches)**

**Trigger:** Push em `feature/*`, `bugfix/*`, `hotfix/*`

**Arquivo:** `.github/workflows/ci.yml`

**Jobs:**
1. ✅ Build com Maven
2. ✅ Testes unitários (195 testes)
3. ✅ Upload de relatórios

**Tempo estimado:** ~3 minutos

**Critérios de sucesso:**
- Build passa sem erros
- 100% dos testes unitários passam
- Código compila sem warnings críticos

---

### **2. CI + JMeter Smoke Test (Developer)**

**Trigger:** Push em `developer` ou PR para `release`

**Arquivo:** `.github/workflows/developer-with-jmeter.yml`

**Jobs:**

#### Job 1: Build and Test
1. ✅ Build com Maven
2. ✅ Testes unitários + integração
3. ✅ Gera artefato (.jar)

#### Job 2: JMeter Smoke Test
1. 🐘 Sobe PostgreSQL (TestContainer via Services)
2. ☕ Inicia aplicação Spring Boot
3. 🔥 Executa JMeter smoke test:
   - **Threads:** 10 usuários simultâneos
   - **Ramp-up:** 5 segundos
   - **Duração:** 30 segundos
4. 📊 Upload de relatórios JMeter

**Tempo estimado:** ~5-7 minutos

**Critérios de sucesso:**
- Todos os testes passam
- Aplicação responde health check
- JMeter: 0% error rate
- Latência P95 < 500ms

**Configuração JMeter:**
```bash
# Parâmetros via -J flags
-Jthreads=10
-Jrampup=5
-Jduration=30
-Jhost=localhost
-Jport=8081
```

---

### **3. Load Test Completo (Release)**

**Trigger:** Push em `release` ou PR para `main`

**Arquivo:** `.github/workflows/release-with-loadtest.yml`

**Jobs:**

#### Job 1: Build
- Mesmo que developer branch

#### Job 2: JMeter Load Test
1. 🐘 Sobe PostgreSQL
2. ☕ Inicia aplicação
3. 🚀 Executa JMeter load test:
   - **Threads:** 100 usuários simultâneos
   - **Ramp-up:** 30 segundos
   - **Duração:** 2 minutos (120s)
4. 📊 Valida thresholds de performance
5. ✅ Performance gate check

**Tempo estimado:** ~8-10 minutos

**Critérios de sucesso (Performance Gates):**
- ✅ Error rate < 1%
- ✅ Latência média < 200ms
- ✅ Latência P95 < 500ms
- ✅ Latência P99 < 1000ms
- ✅ Throughput > 100 req/s

**Configuração JMeter:**
```bash
-Jthreads=100
-Jrampup=30
-Jduration=120
```

---

### **4. Deploy Produção (Main)**

**Trigger:** Push em `main` (após merge de `release`)

**Arquivo:** `.github/workflows/main-deploy.yml`

**Jobs:**

#### Job 1: Build
- Build final com `-DskipTests` (já validado)

#### Job 2: Deploy Production
1. 🔐 **Aprovação manual** (GitHub Environment Protection)
2. 🐳 Build Docker image
3. ☁️ Push para AWS ECR
4. 🚀 Deploy no ECS Fargate
5. ✅ Health check em produção

**Tempo estimado:** ~10-15 minutos

**Proteções:**
- ✅ Requer aprovação manual de 2 revisores
- ✅ Deploy apenas após merge de `release` → `main`
- ✅ Rollback automático se health check falhar

---

## 📊 Arquivos JMeter

### **Smoke Test** (`.jmeter/tests/smoke-test.jmx`)

**Objetivo:** Validação rápida de disponibilidade

**Cenários:**
- Health check endpoint (`/actuator/health`)
- 10 usuários simultâneos
- 30 segundos de duração

**Assertions:**
- HTTP 200 OK
- Response time < 500ms

---

### **Load Test** (`.jmeter/tests/load-test.jmx`)

**Objetivo:** Validar performance sob carga

**Cenários:**
- Health check endpoint
- 100 usuários simultâneos
- 2 minutos de duração sustentada
- Ramp-up gradual (30s)

**Assertions:**
- HTTP 200 OK
- Response time < 500ms
- Error rate < 1%

**Métricas coletadas:**
- Latência (avg, min, max, p95, p99)
- Throughput (requests/sec)
- Error rate (%)
- Concurrent users

---

## 🎯 Como Usar

### **Desenvolver nova feature:**

```bash
# 1. Criar branch feature
git checkout -b feature/novo-endpoint

# 2. Desenvolver e testar localmente
mvn clean test

# 3. Commit e push
git add .
git commit -m "feat: adiciona novo endpoint"
git push origin feature/novo-endpoint

# 4. CI básico roda automaticamente
# Aguardar ✅ no GitHub Actions
```

---

### **Integrar em developer (homologação):**

```bash
# 1. Criar PR para developer
gh pr create --base developer --title "Feature: novo endpoint"

# 2. CI + Smoke Test roda automaticamente
# Aguardar:
# - ✅ Build and tests
# - ✅ JMeter smoke test (10 users, 30s)

# 3. Merge após aprovação
gh pr merge
```

---

### **Preparar release para produção:**

```bash
# 1. Criar PR de developer → release
gh pr create --base release --title "Release v1.2.0"

# 2. Load Test COMPLETO roda automaticamente
# Aguardar:
# - ✅ Build and tests
# - ✅ JMeter load test (100 users, 2min)
# - ✅ Performance gates validados

# 3. QA valida em ambiente de homologação
# 4. Merge após aprovação dupla
```

---

### **Deploy em produção:**

```bash
# 1. Criar PR de release → main
gh pr create --base main --title "Deploy v1.2.0 to Production"

# 2. Aprovação manual REQUERIDA (2 revisores)
# 3. Após aprovação, deploy automático roda:
#    - Build Docker image
#    - Push para ECR
#    - Deploy ECS Fargate
#    - Health check produção

# 4. Monitorar deploy:
gh run list --workflow=main-deploy.yml
```

---

## 🛡️ Proteções de Branch

### **Branch `developer`:**
- ✅ Require PR antes de merge
- ✅ CI deve passar
- ✅ Smoke test deve passar
- ✅ 1 aprovação requerida

### **Branch `release`:**
- ✅ Require PR apenas de `developer`
- ✅ Load test completo deve passar
- ✅ 1 aprovação + QA sign-off
- ✅ Performance gates validados

### **Branch `main` (Produção):**
- ✅ Require PR apenas de `release`
- ✅ 2 aprovações requeridas (ex: Tech Lead + DevOps)
- ✅ Aprovação manual de deploy (GitHub Environment)
- ✅ Rollback automático se falhar

---

## 📈 Métricas e Monitoramento

### **Durante CI/CD:**
- Build time
- Test execution time
- Code coverage
- JMeter metrics (latency, throughput, errors)

### **Pós-deploy:**
- CloudWatch metrics (CPU, Memory, Request count)
- Dynatrace APM (response time, error rate)
- Logs agregados (Kibana/CloudWatch Insights)

---

## 🔧 Configuração Local do JMeter

### **Instalar JMeter:**
```bash
# macOS
brew install jmeter

# Linux
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.2.tgz
tar -xzf apache-jmeter-5.6.2.tgz
```

### **Rodar smoke test localmente:**
```bash
# 1. Iniciar aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 2. Rodar JMeter (headless)
jmeter -n -t .jmeter/tests/smoke-test.jmx \
  -Jthreads=10 \
  -Jrampup=5 \
  -Jduration=30 \
  -Jhost=localhost \
  -Jport=8081 \
  -l results/smoke-test-results.jtl \
  -e -o results/smoke-test-report/

# 3. Visualizar relatório
open results/smoke-test-report/index.html
```

### **Rodar load test localmente:**
```bash
jmeter -n -t .jmeter/tests/load-test.jmx \
  -Jthreads=100 \
  -Jrampup=30 \
  -Jduration=120 \
  -Jhost=localhost \
  -Jport=8081 \
  -l results/load-test-results.jtl \
  -e -o results/load-test-report/

open results/load-test-report/index.html
```

---

## 🚨 Troubleshooting

### **JMeter test falha no CI:**

**Erro:** `Connection refused`
- **Causa:** Aplicação não iniciou completamente
- **Solução:** Aumentar sleep time de 30s para 40s no workflow

**Erro:** `High error rate (>1%)`
- **Causa:** Pool de conexões insuficiente ou queries lentas
- **Solução:** Verificar logs, otimizar queries, aumentar pool Hikari

### **Performance gates falhando:**

**Erro:** `P95 latency > 500ms`
- **Causa:** Banco de dados lento ou queries N+1
- **Solução:** Adicionar índices, usar JOIN FETCH, cache

---

## 📚 Referências

- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [GitHub Actions Workflows](https://docs.github.com/en/actions/using-workflows)
- [Virtual Threads Performance](https://openjdk.org/jeps/444)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**Última atualização:** 2025-11-03
**Versão:** 1.0
**Responsável:** Equipe DevOps Va Nessa Mudança
