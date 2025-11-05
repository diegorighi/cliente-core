# 🚀 Local Development Setup

Guia rápido para rodar cliente-core localmente com **PostgreSQL + Caffeine cache in-memory**.

---

## ✅ Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- (Opcional) jq - Para testes JSON (`brew install jq`)

---

## 🏃 Quick Start (2 minutos)

> **⚠️ IMPORTANTE:** Todos os comandos devem ser executados da **raiz do projeto cliente-core**.
>
> Navegue até a raiz: `cd ~/Desenvolvimento/va-nessa-mudanca/cliente-core`

### 1. Iniciar PostgreSQL

```bash
# Opção A: Script helper (recomendado)
./local-dev.sh start

# Opção B: Docker Compose manual
docker-compose up -d
```

**O que sobe:**
- ✅ **PostgreSQL 16** (porta 5432) - Database principal
- ✅ **Cache:** Caffeine in-memory (configurado automaticamente no Spring Boot)

### 2. Rodar aplicação

```bash
mvn spring-boot:run
```

**Aplicação disponível em:** http://localhost:8081/api/clientes

**O que acontece no startup:**
- ✅ Liquibase executa migrations (DDL + seeds)
- ✅ Cache Caffeine configurado automaticamente (in-memory)
- ✅ Tempo de startup: ~4 segundos

### 3. (Opcional) Testar cache

```bash
./local-dev.sh test-cache
```

---

## 🔧 Comandos Úteis

### Script Helper

```bash
./local-dev.sh start       # Inicia PostgreSQL
./local-dev.sh stop        # Para tudo
./local-dev.sh restart     # Reinicia
./local-dev.sh status      # Mostra status (PostgreSQL + Spring Boot + Cache)
./local-dev.sh logs        # Tail logs do PostgreSQL
./local-dev.sh test-cache  # Testa performance do cache Caffeine
```

### Docker Compose Manual

```bash
# Iniciar PostgreSQL
docker-compose up -d

# Ver logs
docker-compose logs -f postgres

# Parar tudo
docker-compose down

# Parar e resetar dados
docker-compose down -v
```

### Maven

```bash
# Rodar aplicação
mvn spring-boot:run

# Rodar testes
mvn test

# Rodar testes com coverage check
mvn clean verify

# Build sem testes
mvn clean package -DskipTests
```

---

## 🗂️ Estrutura Local

```
cliente-core/
├── docker-compose.yml           # PostgreSQL apenas
├── local-dev.sh                 # Script helper
├── setup-local.sh               # Setup completo automatizado
├── LOCAL_DEVELOPMENT.md         # Este arquivo
└── src/main/resources/
    ├── application.yml          # Config base
    └── application-dev.yml      # Config desenvolvimento (Caffeine)
```

---

## 🎯 Cache Caffeine (In-Memory)

### Configuração Automática

O `application-dev.yml` já está configurado com Caffeine:

```yaml
spring:
  cache:
    type: caffeine
    cache-names: clientes
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=5m
```

**Características:**
- ✅ **TTL:** 5 minutos (dados de cliente mudam raramente)
- ✅ **Max Size:** 10.000 entradas (~100 MB RAM)
- ✅ **Latency:** <1ms (vs 150-200ms PostgreSQL)
- ✅ **Zero infra:** Roda na JVM, sem containers externos

### Verificar Cache via Actuator

**Ver todos os caches:**
```bash
curl http://localhost:8081/api/clientes/actuator/caches | jq
```

**Cache gets (total de leituras):**
```bash
curl http://localhost:8081/api/clientes/actuator/metrics/cache.gets | jq
```

**Cache puts (total de escritas):**
```bash
curl http://localhost:8081/api/clientes/actuator/metrics/cache.puts | jq
```

**Cache evictions (itens removidos):**
```bash
curl http://localhost:8081/api/clientes/actuator/metrics/cache.evictions | jq
```

**Calcular Hit Rate:**
```bash
# Cache hits
HITS=$(curl -s "http://localhost:8081/api/clientes/actuator/metrics/cache.gets?tag=result:hit" | jq -r '.measurements[0].value')

# Cache misses
MISSES=$(curl -s "http://localhost:8081/api/clientes/actuator/metrics/cache.gets?tag=result:miss" | jq -r '.measurements[0].value')

# Hit rate
echo "scale=2; $HITS / ($HITS + $MISSES) * 100" | bc
# Output: 73.25% (exemplo)
```

---

## 🧪 Testando Cache

### Teste Manual

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

### Teste Automatizado

```bash
./local-dev.sh test-cache
```

**Output esperado:**
```
🧪 Testando cache Caffeine in-memory...

1️⃣  Buscando cliente para teste (usando seeds do Liquibase)...
   Cliente ID: 4e63f4ba-8efd-458d-9786-61a2c351621c

2️⃣  Primeira busca (cache MISS - vai no banco)...
   ⏱️  Tempo: 187ms

3️⃣  Segunda busca (cache HIT - do Caffeine in-memory)...
   ⏱️  Tempo: 0.8ms

📊 Resultados:
   1ª busca (DB):    187ms
   2ª busca (Cache): 0.8ms (esperado <1ms)
   ✅ Cache mais rápido em 99.6%

🔍 Métricas do Caffeine (Spring Actuator):
   Cache Gets:       42
   Cache Puts:       15
   Cache Evictions:  0

📦 Caches ativos:
   - clientes
```

---

## 🐛 Troubleshooting

### Porta 5432 já em uso

**Problema:** PostgreSQL já está rodando fora do Docker.

**Solução 1:** Parar PostgreSQL local
```bash
# macOS
brew services stop postgresql

# Linux
sudo systemctl stop postgresql
```

**Solução 2:** Alterar porta no `docker-compose.yml`
```yaml
ports:
  - "5433:5432"  # Mapeia porta 5433 → 5432
```

Depois alterar `application-dev.yml`:
```yaml
datasource:
  url: jdbc:postgresql://localhost:5433/vanessa_mudanca_clientes
```

### Cache não está funcionando

**Verificar se @Cacheable está aplicado:**
```bash
# Buscar anotações @Cacheable no código
grep -r "@Cacheable" src/main/java/
```

Se não retornar nada, significa que **cache não está ativo** (annotations precisam ser adicionadas aos services).

**Verificar configuração do cache:**
```bash
# Ver caches ativos
curl http://localhost:8081/api/clientes/actuator/caches | jq

# Ver métricas de cache
curl http://localhost:8081/api/clientes/actuator/metrics/cache.gets | jq
```

**Verificar logs:**
```bash
# Filtrar logs de cache
grep -i "Caffeine\|Cache" target/spring-boot.log
```

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

### Liquibase validation error

**Problema:** Database schema doesn't match Liquibase changesets.

**Solução (APENAS EM DEV):**
```bash
# Deletar histórico Liquibase
psql -U user -d vanessa_mudanca_clientes -c "DROP TABLE databasechangelog; DROP TABLE databasechangeloglock;"

# Reiniciar aplicação
mvn spring-boot:run
```

**⚠️ ATENÇÃO:** NUNCA fazer isso em STAGING ou PROD!

---

## 📚 Referências

- **docs/CACHE.md** - Documentação completa do cache Caffeine
- **docs/setup/COMO_SUBIR_LOCAL_STACK.md** - Guia detalhado com exemplos
- **README.md** - Documentação completa do microserviço

---

## ✨ Features

✅ **Zero configuração externa** - Apenas PostgreSQL no Docker
✅ **Cache ultra-rápido** - Caffeine in-memory (<1ms latência)
✅ **Auto migrations** - Liquibase executa DDL + seeds automaticamente
✅ **Script helper** - `local-dev.sh` para facilitar operações
✅ **Health checks** - Actuator endpoints para k8s probes
✅ **Metrics** - Prometheus scraping para Grafana

---

**Última atualização:** 2025-11-05
**Versão:** 2.0.0 (Simplificado - Caffeine in-memory)
**Autor:** Equipe Va Nessa Mudança
