# 🚀 Getting Started - cliente-core

Guia **ultra-rápido** para rodar o projeto localmente após `git clone`.

---

## ⚡ TL;DR - Opção 1: WIZARD (1 clique, 3-5 minutos) 🧙

**👉 Recomendado para setup inicial:**

```bash
cd cliente-core
./WIZARD.sh
```

**O que o WIZARD faz automaticamente:**
- ✅ Valida pré-requisitos (Java 21+, Maven, Docker)
- ✅ Inicia PostgreSQL
- ✅ Build Maven (`mvn clean install`)
- ✅ Executa 250+ testes (coverage >=80%)
- ✅ Inicia aplicação Spring Boot
- ✅ Valida: health, database, seeds, cache Caffeine
- ✅ Testa métricas Prometheus
- ✅ Deixa aplicação rodando em background

**Tempo:** 3-5 minutos | **Logs:** `/tmp/cliente-core-wizard.log`

---

## ⚡ TL;DR - Opção 2: Manual (3 comandos, 1 minuto) ⚡

**Para quem já conhece o projeto:**

```bash
cd cliente-core

# 1. Subir PostgreSQL
docker-compose up -d

# 2. Rodar aplicação
mvn spring-boot:run

# 3. Testar
curl http://localhost:8081/api/clientes/actuator/health
```

✅ **Pronto!** Aplicação rodando em `http://localhost:8081/api/clientes`

---

## 📋 Pré-requisitos

Certifique-se de ter instalado:

| Software | Versão Mínima | Como verificar |
|----------|---------------|----------------|
| **Java** | 21+ | `java -version` |
| **Maven** | 3.9+ | `mvn -version` |
| **Docker** | 20+ | `docker --version` |

**Instalação rápida (macOS):**
```bash
# Java 21
brew install openjdk@21

# Maven
brew install maven

# Docker Desktop
brew install --cask docker
```

---

## 🏃 Passo a Passo Detalhado

### 1. Clonar o Repositório

**Se for MONOREPO (yukam-drighi):**
```bash
cd ~/Desenvolvimento
git clone --recurse-submodules git@github.com:diegorighi/yukam-drighi.git
cd yukam-drighi/services/cliente-core
```

**Se for STANDALONE:**
```bash
cd ~/Desenvolvimento
git clone git@github.com:diegorighi/cliente-core.git
cd cliente-core
```

---

### 2. Subir PostgreSQL

```bash
# Certifique-se de estar na RAIZ do cliente-core
cd ~/Desenvolvimento/yukam-drighi/services/cliente-core  # OU ~/Desenvolvimento/cliente-core

# Subir apenas PostgreSQL (porta 5432)
docker-compose up -d
```

**O que acontece:**
- ✅ PostgreSQL 16 sobe em `localhost:5432`
- ✅ Banco `vanessa_mudanca_clientes` criado automaticamente
- ✅ Credenciais: `user` / `senha123`

**Verificar se subiu:**
```bash
docker ps | grep cliente-core-postgres
# Output esperado: cliente-core-postgres ... Up
```

---

### 3. Rodar a Aplicação

```bash
# Build + Start (primeira vez ou após mudanças no código)
mvn clean install spring-boot:run

# Start apenas (se já fez build)
mvn spring-boot:run
```

**O que acontece automaticamente:**
1. Maven baixa dependências (~2 minutos na primeira vez)
2. Liquibase executa migrations (DDL + seeds com 15 clientes exemplo)
3. Cache Caffeine configurado (in-memory, <1ms latency)
4. Aplicação inicia em ~5 segundos
5. Endpoints disponíveis em `http://localhost:8081/api/clientes`

**Output esperado no console:**
```
  ██████╗ ██╗     ██╗ ███████╗ ███╗   ██╗ ████████╗ ███████╗
 ...
 🚚 Microserviço de Gestão de Clientes | Va Nessa Mudança
 📦 Spring Boot 3.5.7 | ☕ Java 21
 🔧 Ambiente: dev | 🎯 Hexagonal Architecture
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Started ClienteCoreApplication in 4.523 seconds
```

---

### 4. Validar Funcionamento

#### ✅ Health Check
```bash
curl http://localhost:8081/api/clientes/actuator/health
```

**Output esperado:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### ✅ Listar Clientes PF (seeds do Liquibase)
```bash
curl http://localhost:8081/api/clientes/v1/clientes/pf | jq
```

**Output esperado:** Array com 10 clientes PF (João Silva, Maria Santos, etc.)

#### ✅ Buscar Cliente PF por ID
```bash
# Pegar um publicId do endpoint anterior
curl http://localhost:8081/api/clientes/v1/clientes/pf/{publicId} | jq
```

#### ✅ Verificar Cache Caffeine
```bash
curl http://localhost:8081/api/clientes/actuator/caches | jq
```

**Output esperado:**
```json
{
  "cacheManagers": {
    "cacheManager": {
      "caches": {
        "clientes": { "target": "com.github.benmanes.caffeine.cache..." },
        "documentos": { "target": "..." }
      }
    }
  }
}
```

---

## 🧪 Executar Testes

```bash
# Todos os testes (250+)
mvn test

# Testes com coverage (JaCoCo)
mvn clean test

# Verificar coverage >=80%
mvn clean verify

# Ver relatório de coverage
open target/site/jacoco/index.html
```

**Métricas esperadas:**
- ✅ 250+ testes passando
- ✅ Coverage ≥ 80%
- ✅ Tempo de execução: ~30 segundos

---

## 🔧 Scripts Úteis

### WIZARD.sh (Validação Completa) 🧙

**👉 Recomendado para setup inicial:**

```bash
./WIZARD.sh
```

**O que valida (8 etapas):**
1. ✅ Pré-requisitos (Java 21+, Maven 3.9+, Docker)
2. ✅ Diretório do projeto
3. ✅ PostgreSQL (startup + conectividade)
4. ✅ Build Maven (mvn clean install)
5. ✅ Testes (250+ tests, coverage >=80%)
6. ✅ Aplicação Spring Boot (startup + health check)
7. ✅ Validações funcionais (DB, seeds, cache Caffeine)
8. ✅ Observabilidade (Prometheus metrics)

**Tempo:** 3-5 minutos | **Logs:** `/tmp/cliente-core-wizard.log`

**Após execução:**
- Aplicação rodando em background
- PostgreSQL ativo
- Todos os testes passados
- Coverage validado (>=80%)

---

### setup-local.sh (Setup Rápido)
```bash
./setup-local.sh
```

**Alternativa mais rápida (sem testes):**
1. Valida dependências (Java, Maven, Docker)
2. Limpa containers órfãos
3. Sobe PostgreSQL
4. Faz build (mvn clean install -DskipTests)
5. Inicia aplicação em background
6. Executa 4 smoke tests (health, db, cache MISS, cache HIT)

**Tempo:** ~2 minutos

---

### local-dev.sh (Gerenciamento Diário)
```bash
# Subir PostgreSQL
./local-dev.sh start

# Parar tudo
./local-dev.sh stop

# Reiniciar
./local-dev.sh restart

# Ver status (PostgreSQL + Spring Boot)
./local-dev.sh status

# Ver logs do PostgreSQL
./local-dev.sh logs

# Testar cache Caffeine
./local-dev.sh test-cache
```

---

## 🐛 Troubleshooting

### 1. Porta 5432 já está em uso
```bash
# Ver o que está usando a porta
lsof -i :5432

# Parar PostgreSQL local (se for MacOS com Homebrew)
brew services stop postgresql@16

# Ou matar o processo
kill -9 $(lsof -t -i:5432)
```

---

### 2. Erro "Container name already in use"
```bash
# Parar e remover containers órfãos
docker stop cliente-core-postgres 2>/dev/null
docker rm cliente-core-postgres 2>/dev/null

# Subir novamente
docker-compose up -d
```

---

### 3. Liquibase validation error
```bash
# Resetar banco (APENAS EM DEV!)
docker-compose down -v  # Remove volumes

# Subir novamente
docker-compose up -d
mvn spring-boot:run
```

---

### 4. Maven Central intermittent failures
Se o build falhar com "Could not transfer artifact...", rode novamente:
```bash
mvn clean install -U  # Força atualização de dependências
```

O CI/CD já tem retry automático configurado, mas localmente você pode precisar rodar 2x.

---

### 5. Aplicação não inicia (porta 8081 em uso)
```bash
# Ver o que está usando a porta
lsof -i :8081

# Matar processo
kill -9 $(lsof -t -i:8081)
```

---

## 📚 Próximos Passos

Após setup concluído:

1. **Explorar Swagger UI** (quando implementado):
   - `http://localhost:8081/api/clientes/swagger-ui/index.html`

2. **Ver Métricas do Prometheus**:
   - `http://localhost:8081/api/clientes/actuator/prometheus`

3. **Ler Documentação Técnica**:
   - `README.md` - Arquitetura, entidades, regras de negócio
   - `CLAUDE.md` - Guia para Claude Code
   - `docs/CACHE.md` - Detalhes sobre Caffeine cache

4. **Contribuir**:
   - Seguir convenções no `CLAUDE.md`
   - Rodar testes antes de commit
   - Manter coverage ≥ 80%

---

## 🚨 Importante: Filosofia do Monorepo

Se você está trabalhando no **monorepo yukam-drighi**:

### 95% do Tempo: Trabalhe no Microserviço
```bash
cd ~/Desenvolvimento/yukam-drighi/services/cliente-core

# Desenvolva aqui normalmente
docker-compose up -d  # Apenas PostgreSQL
mvn spring-boot:run
```

### 5% do Tempo: Teste Integrações
```bash
cd ~/Desenvolvimento/yukam-drighi  # Raiz do monorepo

# Subir infraestrutura compartilhada (Kafka, Redis, Prometheus)
docker-compose up -d kafka

# Iniciar microserviços manualmente
cd services/cliente-core && mvn spring-boot:run &
cd services/vendas-core && mvn spring-boot:run &
```

**❌ NUNCA faça:**
- `docker-compose up` na raiz para desenvolvimento diário
- Coloque PostgreSQL no docker-compose.yml da raiz
- Rode wizard da raiz (use o wizard do MS)

---

## 📞 Suporte

**Problemas?**
1. Verificar logs: `/tmp/cliente-core-startup.log`
2. Verificar README.md seção "Troubleshooting"
3. Abrir issue: https://github.com/diegorighi/cliente-core/issues

---

**Última atualização:** 2025-11-05
**Versão:** 1.0.0 (Pós DynamoDB removal)
