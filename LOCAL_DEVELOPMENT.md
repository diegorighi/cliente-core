# 🚀 Local Development Setup

Guia rápido para rodar cliente-core localmente **sem credenciais AWS**.

---

## ✅ Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- (Opcional) AWS CLI v2 (para inspeção de DynamoDB Local)

---

## 🏃 Quick Start (3 minutos)

### 1. Iniciar infraestrutura local

```bash
# Opção A: Script helper (recomendado)
./local-dev.sh start

# Opção B: Docker Compose manual
docker-compose up -d
```

**O que sobe:**
- ✅ **DynamoDB Local** (porta 8000) - Cache backend
- ✅ **PostgreSQL 16** (porta 5432) - Database principal

### 2. Rodar aplicação

```bash
mvn spring-boot:run
```

**Aplicação disponível em:** http://localhost:8081/api/clientes

### 3. (Opcional) Testar cache

```bash
./local-dev.sh test-cache
```

---

## 🔧 Comandos Úteis

### Script Helper

```bash
./local-dev.sh start       # Inicia infraestrutura
./local-dev.sh stop        # Para tudo
./local-dev.sh restart     # Reinicia
./local-dev.sh status      # Mostra status
./local-dev.sh logs        # Tail logs
./local-dev.sh test-cache  # Testa performance do cache
```

### Docker Compose Manual

```bash
# Iniciar todos os serviços
docker-compose up -d

# Iniciar apenas DynamoDB (se PostgreSQL já está rodando)
docker-compose up -d dynamodb-local

# Ver logs
docker-compose logs -f dynamodb-local

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
├── docker-compose.yml           # Infraestrutura local
├── local-dev.sh                 # Script helper
├── LOCAL_DEVELOPMENT.md         # Este arquivo
└── src/main/resources/
    └── application-dev.yml      # Config para desenvolvimento
```

---

## 🎯 DynamoDB Local

### Configuração Automática

O `application-dev.yml` já está configurado para DynamoDB Local:

```yaml
aws:
  region: us-east-1
  dynamodb:
    endpoint: http://localhost:8000  # DynamoDB Local
```

**Credenciais:** Fake credentials são usadas automaticamente no ambiente dev (ver `DynamoDbCacheConfig.java`).

### Acessar DynamoDB Local

**Via AWS CLI:**
```bash
# Listar tabelas
aws dynamodb list-tables \
    --endpoint-url http://localhost:8000 \
    --region us-east-1

# Ver itens cached
aws dynamodb scan \
    --table-name cliente-core-cache \
    --endpoint-url http://localhost:8000 \
    --region us-east-1 \
    --max-items 10

# Deletar tabela (reset)
aws dynamodb delete-table \
    --table-name cliente-core-cache \
    --endpoint-url http://localhost:8000 \
    --region us-east-1
```

**Via DynamoDB Admin (GUI):**
```bash
# Iniciar GUI opcional
docker-compose --profile debug up -d

# Acessar: http://localhost:8001
```

---

## 🧪 Testando Cache

### Teste Manual

```bash
# 1. Criar cliente
curl -X POST http://localhost:8081/api/clientes/v1/pf \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "12345678910",
    "nomeCompleto": "João da Silva",
    "dataNascimento": "1990-01-15",
    "sexo": "MASCULINO",
    "email": "joao@test.com",
    "telefone": "11987654321"
  }'

# Copiar UUID retornado

# 2. Primeira busca (cache MISS - ~150-200ms)
time curl http://localhost:8081/api/clientes/v1/pf/{UUID}

# 3. Segunda busca (cache HIT - ~10-20ms)
time curl http://localhost:8081/api/clientes/v1/pf/{UUID}
```

### Teste Automatizado

```bash
./local-dev.sh test-cache
```

**Output esperado:**
```
🧪 Testando cache DynamoDB...

1️⃣  Criando cliente de teste...
   Cliente ID: 550e8400-e29b-41d4-a716-446655440000

2️⃣  Primeira busca (cache MISS - vai no banco)...
   ⏱️  Tempo: 187ms

3️⃣  Segunda busca (cache HIT - do DynamoDB)...
   ⏱️  Tempo: 15ms

📊 Resultados:
   1ª busca (DB):    187ms
   2ª busca (Cache): 15ms
   ✅ Cache mais rápido em 92.0%

🔍 Verificar tabela DynamoDB:
  - clientes:findById::550e8400-e29b-41d4-a716-446655440000
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

**Solução 2:** Subir apenas DynamoDB
```bash
docker-compose up -d dynamodb-local
```

### DynamoDB Local não sobe

**Problema:** Porta 8000 em uso.

**Solução:**
```bash
# Verificar o que está usando porta 8000
lsof -i :8000

# Matar processo
kill -9 <PID>

# Tentar novamente
docker-compose up -d dynamodb-local
```

### Tabela cliente-core-cache não existe

**Problema:** Aplicação não criou tabela automaticamente.

**Causa:** `DynamoDbTableInitializer` não rodou (erro de conexão).

**Solução:**
1. Verificar logs da aplicação
2. Verificar se DynamoDB Local está rodando: `docker-compose ps`
3. Reiniciar aplicação: `mvn spring-boot:run`

### Cache não está funcionando

**Verificar se @Cacheable está aplicado:**
```bash
# Buscar anotações @Cacheable no código
grep -r "@Cacheable" src/main/java/
```

Se não retornar nada, significa que **cache não está ativo** (annotations precisam ser adicionadas aos services).

---

## 📚 Referências

- **DYNAMODB_CACHE_SUMMARY.md** - Overview da implementação de cache
- **docs/CACHE_MIGRATION_GUIDE.md** - Guia de migração DynamoDB → Redis
- **docs/CACHE_COST_COMPARISON.md** - Análise de custos

---

## ✨ Features

✅ **Zero configuração AWS** - Usa DynamoDB Local com credenciais fake
✅ **Script helper** - `local-dev.sh` para facilitar operações
✅ **Auto table creation** - Tabela criada automaticamente no startup
✅ **GUI opcional** - DynamoDB Admin em http://localhost:8001
✅ **Cache testing** - Script automatizado para testar performance

---

**Última atualização:** 2025-11-04
**Versão:** 1.0.0
