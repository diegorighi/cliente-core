# 🚀 Como Subir o cliente-core Localmente

Guia **super simplificado** para rodar o cliente-core localmente.

---

## ⚡ Quick Start (2 comandos, 1 minuto)

### Pré-requisitos

Certifique-se de ter instalado:
- **Java 21+**
- **Maven 3.9+**
- **Docker Desktop** (rodando)
- **(Opcional) jq** - Para testes JSON

---

## 🎯 Setup Completo

### 1. Subir PostgreSQL

```bash
# Na raiz do cliente-core
cd ~/Desenvolvimento/va-nessa-mudanca/cliente-core

# Subir apenas PostgreSQL
docker-compose up -d
```

**O que acontece:**
- ✅ PostgreSQL 16 sobe em `localhost:5432`
- ✅ Banco `vanessa_mudanca_clientes` criado automaticamente
- ✅ Credenciais: `user` / `senha123`

### 2. Iniciar Aplicação

```bash
# Na raiz do cliente-core
mvn spring-boot:run
```

**O que acontece:**
- ✅ Liquibase executa migrations (DDL + seeds)
- ✅ Cache Caffeine configurado automaticamente (in-memory)
- ✅ Aplicação disponível em `http://localhost:8081/api/clientes`
- ✅ Tempo de startup: ~4 segundos

---

## ✅ Validar Funcionamento

```bash
# Health check
curl http://localhost:8081/api/clientes/actuator/health

# Listar clientes PF (seeds do Liquibase)
curl http://localhost:8081/api/clientes/v1/clientes/pf | jq

# Ver caches disponíveis
curl http://localhost:8081/api/clientes/actuator/caches | jq
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

---

## 📊 Testar Cache (Caffeine In-Memory)

### Teste Automático de Performance

```bash
# Buscar primeiro cliente dos seeds
UUID=$(curl -s "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=1" | jq -r '.content[0].publicId')

echo "UUID do cliente: $UUID"

# 1ª busca (cache MISS - vai no PostgreSQL)
echo "Cache MISS:"
time curl -s "http://localhost:8081/api/clientes/v1/clientes/pf/$UUID" > /dev/null

# 2ª busca (cache HIT - Caffeine in-memory)
echo "Cache HIT:"
time curl -s "http://localhost:8081/api/clientes/v1/clientes/pf/$UUID" > /dev/null
```

**Resultado esperado:**
```
Cache MISS: 150-200ms  (busca no PostgreSQL)
Cache HIT:  <1ms       (busca no Caffeine)
```

**Melhoria:** 150-200x mais rápido! 🚀

### Verificar Métricas do Cache

```bash
# Ver estatísticas do Caffeine
curl http://localhost:8081/api/clientes/actuator/metrics/cache.gets | jq
curl http://localhost:8081/api/clientes/actuator/metrics/cache.puts | jq
curl http://localhost:8081/api/clientes/actuator/metrics/cache.evictions | jq
```

---

## 🔧 Comandos Úteis

### Docker Compose

```bash
# Iniciar PostgreSQL
docker-compose up -d

# Ver logs
docker-compose logs -f postgres

# Parar PostgreSQL
docker-compose down

# Reset completo (DELETA DADOS!)
docker-compose down -v
```

### Maven

```bash
# Rodar aplicação (dev profile)
mvn spring-boot:run

# Rodar com profile staging
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Rodar testes
mvn test

# Rodar testes com coverage
mvn clean verify

# Build sem testes
mvn clean package -DskipTests

# Limpar e rebuildar
mvn clean install
```

### PostgreSQL (psql)

```bash
# Conectar ao banco
psql -h localhost -p 5432 -U user -d vanessa_mudanca_clientes

# Ver tabelas
\dt

# Ver índices
\di

# Ver dados de seeds
SELECT public_id, primeiro_nome, sobrenome FROM clientes_pf LIMIT 5;

# Sair
\q
```

---

## 📝 Exemplos de Chamadas API

```bash
# Listar todos os clientes PF
curl http://localhost:8081/api/clientes/v1/clientes/pf | jq

# Listar com paginação
curl "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=5" | jq

# Buscar por ID específico
curl http://localhost:8081/api/clientes/v1/clientes/pf/4e63f4ba-8efd-458d-9786-61a2c351621c | jq

# Listar clientes PJ
curl http://localhost:8081/api/clientes/v1/clientes/pj | jq

# Health check
curl http://localhost:8081/api/clientes/actuator/health | jq

# Métricas
curl http://localhost:8081/api/clientes/actuator/metrics | jq
```

---

## 🗂️ Estrutura de Arquivos

```
cliente-core/
├── docker-compose.yml                # PostgreSQL apenas
├── docs/
│   ├── setup/
│   │   ├── COMO_SUBIR_LOCAL_STACK.md         # Este arquivo
│   │   └── LOCAL_DEVELOPMENT.md              # Guia detalhado
│   ├── cache/
│   │   └── CACHE.md                          # Documentação do Caffeine
│   └── development/
│       └── VIRTUAL_THREADS.md                # Java 21 concurrency
└── src/main/resources/
    ├── application.yml               # Config base
    ├── application-dev.yml           # Config desenvolvimento (Caffeine)
    ├── application-staging.yml       # Config staging
    └── application-prod.yml          # Config produção
```

---

## 🎯 Ambientes e Configurações

### DEV (Local)

**Profile:** `dev` (padrão)

**Infraestrutura:**
- PostgreSQL: Docker (localhost:5432)
- Cache: Caffeine in-memory
  - TTL: 5 minutos
  - Max size: 10.000 clientes (~100 MB RAM)

**Como rodar:**
```bash
docker-compose up -d
mvn spring-boot:run
```

**Logs:** DEBUG level, SQL queries visíveis

**Seeds:** Liquibase cria 15 clientes de teste (10 PF + 5 PJ)

---

### STAGING

**Profile:** `staging`

**Infraestrutura:**
- PostgreSQL: AWS RDS
- Cache: Caffeine in-memory (migrar para Redis quando necessário)

**Como rodar:**
```bash
export DATABASE_URL="jdbc:postgresql://staging.rds.amazonaws.com:5432/vanessa"
export DATABASE_PASSWORD="senha-segura"

mvn spring-boot:run -Dspring-boot.run.profiles=staging
```

**Logs:** INFO level, SQL queries desabilitadas

**Seeds:** APENAS DDL (sem seeds)

---

### PROD (Production)

**Profile:** `prod`

**Infraestrutura:**
- PostgreSQL: AWS RDS Multi-AZ
- Cache: Redis ElastiCache (quando escalar)

**Deploy:** Via ECS Fargate (Terraform)

**Logs:** WARN level, stack traces NUNCA expostos

**Seeds:** APENAS DDL (sem seeds)

---

## 🐛 Troubleshooting

### Porta 5432 já em uso

**Problema:** PostgreSQL já está rodando localmente.

**Solução 1:** Parar PostgreSQL local
```bash
# macOS
brew services stop postgresql@16

# Linux
sudo systemctl stop postgresql
```

**Solução 2:** Mudar porta no docker-compose.yml
```yaml
ports:
  - "5433:5432"  # Mapeia porta 5433 → 5432
```

Depois alterar `application-dev.yml`:
```yaml
datasource:
  url: jdbc:postgresql://localhost:5433/vanessa_mudanca_clientes
```

---

### Cache não está funcionando

**Verificar configuração:**

```bash
# Ver caches ativos
curl http://localhost:8081/api/clientes/actuator/caches | jq

# Ver métricas de cache
curl http://localhost:8081/api/clientes/actuator/metrics/cache.gets | jq
```

**Verificar anotações @Cacheable:**
```bash
grep -r "@Cacheable" src/main/java/
```

**Verificar logs:**
```bash
grep -i "cache" target/spring-boot.log
```

---

### Application não inicia - Erro Liquibase

**Problema:** Database schema doesn't match Liquibase changesets

**Solução (APENAS EM DEV):**
```bash
# Deletar histórico Liquibase
psql -U user -d vanessa_mudanca_clientes -c "DROP TABLE databasechangelog; DROP TABLE databasechangeloglock;"

# Reiniciar aplicação
mvn spring-boot:run
```

**⚠️ ATENÇÃO:** NUNCA fazer isso em STAGING ou PROD!

---

### Application não inicia - Porta 8081 em uso

**Problema:** Outra instância do cliente-core está rodando.

**Solução:**
```bash
# Encontrar processo
lsof -i :8081

# Matar processo
kill -9 <PID>

# OU alterar porta temporariamente
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

---

## 📊 Monitoramento Local

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8081/api/clientes/actuator/health | jq

# Métricas gerais
curl http://localhost:8081/api/clientes/actuator/metrics | jq

# Pool de conexões
curl http://localhost:8081/api/clientes/actuator/metrics/hikaricp.connections.active | jq

# Info da aplicação
curl http://localhost:8081/api/clientes/actuator/info | jq

# Prometheus scraping
curl http://localhost:8081/api/clientes/actuator/prometheus
```

### Logs Estruturados

```bash
# Tail logs
tail -f target/spring-boot.log

# Filtrar por correlation ID
grep "correlationId=abc-123" target/spring-boot.log

# Filtrar erros
grep "ERROR" target/spring-boot.log

# Filtrar logs de cache
grep -i "Caffeine\|Cache" target/spring-boot.log
```

---

## 🔄 Quando Migrar para Redis

**Caffeine in-memory é adequado até:**
- ✅ 10.000 clientes ativos (~100 MB RAM)
- ✅ Single instance (1 container)
- ✅ Reinícios raros (cache loss aceitável)

**Migrar para Redis quando:**
- ❌ >50.000 clientes ativos
- ❌ Múltiplas instâncias (horizontal scaling)
- ❌ Cache deve sobreviver a restarts

**Esforço de migração:** ~2-3 horas (Spring Cache abstraction facilita)

---

## 📚 Documentação Adicional

- **LOCAL_DEVELOPMENT.md** - Guia detalhado de workflows diários
- **CACHE.md** - Documentação completa do sistema de cache
- **CLAUDE.md** - Convenções, checklist, troubleshooting
- **README.md** - Documentação completa do microserviço

---

## ✨ Features

✅ **Zero configuração externa** - Apenas PostgreSQL no Docker
✅ **Cache ultra-rápido** - Caffeine in-memory (<1ms latência)
✅ **Auto migrations** - Liquibase executa DDL + seeds automaticamente
✅ **Multi-ambiente** - Profiles para dev/staging/prod
✅ **Structured logging** - Logs JSON para CloudWatch (staging/prod)
✅ **Health checks** - Actuator endpoints para k8s probes
✅ **Metrics** - Prometheus scraping para Grafana

---

**Última atualização:** 2025-11-05
**Versão:** 2.0.0 (Simplificado - Caffeine in-memory)
**Autor:** Equipe Va Nessa Mudança
