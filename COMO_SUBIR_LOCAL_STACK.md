# 🚀 Como Subir o cliente-core Localmente (Sem AWS)

Guia **super simplificado** para rodar o cliente-core localmente em **1 único comando**.

---

## ⚡ Quick Start (1 comando, 2 minutos)

### Pré-requisitos

Certifique-se de ter instalado:
- **Java 21+**
- **Maven 3.9+**
- **Docker Desktop** (rodando)
- **(Opcional) AWS CLI** - Para inspecionar cache
- **(Opcional) jq** - Para testes JSON

---

## 🎯 Setup Completo Automático

```bash
cd cliente-core
./setup-local.sh
```

**O que esse script faz automaticamente:**
1. ✅ Verifica todas as dependências (Java, Maven, Docker)
2. ✅ Para serviços anteriores (se existirem)
3. ✅ Sobe PostgreSQL + DynamoDB Local (Docker)
4. ✅ Cria tabela de cache automaticamente
5. ✅ Builda a aplicação (mvn clean install)
6. ✅ Inicia a aplicação (mvn spring-boot:run)
7. ✅ Roda smoke tests (health + database + cache)
8. ✅ Mostra URLs e comandos úteis

**Tempo total:** ~2 minutos ⏱️

---

## ✅ Output de Sucesso

Se tudo funcionar, você verá:

```
╔═══════════════════════════════════════════════════════════════════╗
║                                                                   ║
║  🎉 SUCESSO! Cliente-Core rodando localmente!                    ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝

📋 URLs Disponíveis:
   🌐 API Base:       http://localhost:8081/api/clientes
   💚 Health Check:   http://localhost:8081/api/clientes/actuator/health
   📊 Metrics:        http://localhost:8081/api/clientes/actuator/metrics

🗄️  Infraestrutura:
   🐘 PostgreSQL:     localhost:5432 (user/senha123)
   ⚡ DynamoDB Local:  http://localhost:8000

✨ Pronto para desenvolver!
```

---

## 🛠️ Comandos Úteis (Depois do Setup)

```bash
# Ver status dos serviços
./local-dev.sh status

# Testar cache em detalhes (10 chamadas consecutivas)
./local-dev.sh test-cache

# Ver logs dos containers (PostgreSQL + DynamoDB)
./local-dev.sh logs

# Parar tudo (containers + aplicação)
./local-dev.sh stop

# Reiniciar tudo
./local-dev.sh restart
```

---

## 📝 Exemplos de Chamadas API

```bash
# Listar todos os clientes PF
curl http://localhost:8081/api/clientes/v1/clientes/pf | jq

# Listar com paginação
curl "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=5" | jq

# Buscar por ID
curl http://localhost:8081/api/clientes/v1/clientes/pf/4e63f4ba-8efd-458d-9786-61a2c351621c | jq

# Listar clientes PJ
curl http://localhost:8081/api/clientes/v1/clientes/pj | jq

# Health check
curl http://localhost:8081/api/clientes/actuator/health | jq

# Métricas
curl http://localhost:8081/api/clientes/actuator/metrics | jq
```

---

## 🔍 Inspecionar Cache (Requer AWS CLI)

```bash
# Listar tabelas DynamoDB Local
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb list-tables --endpoint-url http://localhost:8000 --no-cli-pager

# Ver itens cached (até 5)
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb scan \
  --table-name cliente-core-cache \
  --endpoint-url http://localhost:8000 \
  --max-items 5 \
  --no-cli-pager | jq '.Items[].cacheKey.S'

# Contar itens no cache
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb scan \
  --table-name cliente-core-cache \
  --endpoint-url http://localhost:8000 \
  --no-cli-pager | jq '.Count'
```

---

## 🧪 Teste Manual de Cache (Passo a Passo)

### Teste Automatizado (RECOMENDADO)

```bash
./local-dev.sh test-cache
```

**🔒 Proteções de Segurança:**
- ✅ Verifica se DynamoDB Local está rodando (impede execução em PROD)
- ✅ Usa apenas clientes dos seeds (não polui banco com dados de teste)
- ✅ Somente leitura (não cria, atualiza ou deleta dados)
- ✅ Aborta se ambiente não for desenvolvimento

**O que o script faz:**
1. ✅ Busca cliente dos seeds do Liquibase (Ana Silva)
2. **1ª busca** (cache MISS - vai no PostgreSQL): mede tempo
3. **2ª busca** (cache HIT - vem do DynamoDB): mede tempo
4. Calcula melhoria percentual
5. Lista itens cached no DynamoDB

**Output esperado:**
```
🧪 Testando cache DynamoDB...

1️⃣  Buscando cliente para teste (usando seeds do Liquibase)...
   Cliente ID: 4e63f4ba-8efd-458d-9786-61a2c351621c

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

### Teste Manual (Usando Seeds Existentes)

```bash
# 1. Buscar cliente dos seeds (Ana Silva)
UUID=$(curl -s "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=1" | jq -r '.content[0].publicId')
echo "Cliente UUID: $UUID"

# 2. Primeira busca (cache MISS - ~15-30ms)
echo "Cache MISS:"
time curl -s "http://localhost:8081/api/clientes/v1/clientes/pf/$UUID" | jq '.nomeCompleto'

# 3. Segunda busca (cache HIT - ~5-15ms, 2-5x mais rápido!)
echo "Cache HIT:"
time curl -s "http://localhost:8081/api/clientes/v1/clientes/pf/$UUID" | jq '.nomeCompleto'

# 4. Verificar itens no cache
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb scan \
  --table-name cliente-core-cache \
  --endpoint-url http://localhost:8000 \
  --no-cli-pager | jq '.Count'
```

**⚠️ Nota:** Não é necessário criar clientes novos para testar cache. Use os **seeds existentes** para evitar poluição do banco.

---

## 🔧 Comandos Úteis

### Script Helper (local-dev.sh)

```bash
./local-dev.sh start       # Inicia DynamoDB Local + PostgreSQL
./local-dev.sh stop        # Para todos os serviços
./local-dev.sh restart     # Reinicia serviços
./local-dev.sh status      # Mostra status dos serviços
./local-dev.sh logs        # Tail logs dos containers
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

# Parar e RESETAR dados (CUIDADO!)
docker-compose down -v
```

### Maven

```bash
# Rodar aplicação (profile dev)
mvn spring-boot:run

# Rodar com profile staging
mvn spring-boot:run -Dspring-boot.run.profiles=hml

# Rodar com profile produção (não recomendado localmente)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Rodar testes
mvn test

# Rodar testes com coverage check
mvn clean verify

# Build sem testes
mvn clean package -DskipTests

# Limpar tudo e rebuildar
mvn clean install
```

---

## 🗂️ Estrutura de Arquivos

```
cliente-core/
├── docker-compose.yml                # PostgreSQL + DynamoDB Local
├── local-dev.sh                      # Script helper
├── COMO_SUBIR_LOCAL_STACK.md         # Este arquivo
├── LOCAL_DEVELOPMENT.md              # Guia detalhado de desenvolvimento local
├── DYNAMODB_CACHE_SUMMARY.md         # Overview da implementação de cache
├── docs/
│   ├── CACHE_MIGRATION_GUIDE.md      # Como migrar DynamoDB → Redis
│   └── CACHE_COST_COMPARISON.md      # Análise de custos (4 cenários)
└── src/main/resources/
    ├── application.yml               # Config base (porta, actuator, etc)
    ├── application-dev.yml           # Config desenvolvimento (DynamoDB Local)
    ├── application-hml.yml           # Config staging (DynamoDB AWS)
    └── application-prod.yml          # Config produção (DynamoDB AWS)
```

---

## 🎯 Ambientes e Configurações

### DEV (Local)

**Profile:** `dev` (padrão)

**Infraestrutura:**
- PostgreSQL: Docker (localhost:5432)
- DynamoDB: DynamoDB Local via Docker (localhost:8000)
- Credenciais: Fake credentials (`fakeAccessKey`/`fakeSecretKey`)

**Como rodar:**
```bash
./local-dev.sh start
mvn spring-boot:run
```

**Logs:** DEBUG level, SQL queries visíveis

**Seeds:** Liquibase cria 15 clientes de teste (10 PF + 5 PJ)

---

### HML (Staging)

**Profile:** `hml`

**Infraestrutura:**
- PostgreSQL: AWS RDS (endpoint configurado via ENV)
- DynamoDB: AWS DynamoDB (tabela: `cliente-core-cache-hml`)
- Credenciais: IAM Role do EC2/ECS

**Como rodar:**
```bash
# Localmente (para testar config)
export DATABASE_URL="jdbc:postgresql://hml.rds.amazonaws.com:5432/vanessa"
export DATABASE_PASSWORD="senha-segura"
export AWS_REGION="us-east-1"

mvn spring-boot:run -Dspring-boot.run.profiles=hml
```

**Logs:** INFO level, SQL queries desabilitadas

**Seeds:** APENAS DDL (sem seeds)

---

### PROD (Production)

**Profile:** `prod`

**Infraestrutura:**
- PostgreSQL: AWS RDS Multi-AZ (endpoint configurado via ENV)
- DynamoDB: AWS DynamoDB (tabela: `cliente-core-cache-prod`)
- Credenciais: IAM Role do ECS Task

**Como rodar:**
```bash
# Deploy via ECS Fargate (Terraform)
# Env vars injetadas via AWS Systems Manager Parameter Store
```

**Logs:** WARN level, SQL queries desabilitadas, stack traces NUNCA expostos

**Seeds:** APENAS DDL (sem seeds)

**Health checks:** `/actuator/health` via VPN (não exposto publicamente)

---

## 🔍 Acessar DynamoDB Local

### Via AWS CLI

```bash
# Listar tabelas
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb list-tables \
    --endpoint-url http://localhost:8000

# Ver itens cached (primeiros 10)
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb scan \
    --table-name cliente-core-cache \
    --endpoint-url http://localhost:8000 \
    --max-items 10

# Ver item específico
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb get-item \
    --table-name cliente-core-cache \
    --key '{"cacheKey": {"S": "clientes:findById::550e8400-e29b-41d4-a716-446655440000"}}' \
    --endpoint-url http://localhost:8000

# Deletar tabela (reset cache)
AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
aws dynamodb delete-table \
    --table-name cliente-core-cache \
    --endpoint-url http://localhost:8000
```

### Via DynamoDB Admin (GUI)

```bash
# Iniciar GUI opcional
docker-compose --profile debug up -d dynamodb-admin

# Acessar: http://localhost:8001
```

**Features:**
- Visualizar tabelas e itens
- Editar/deletar itens manualmente
- Query por partition key
- Scan completo da tabela

---

## 🐛 Troubleshooting

### Porta 5432 já em uso

**Problema:** PostgreSQL já está rodando fora do Docker.

**Solução 1:** Parar PostgreSQL local
```bash
# macOS
brew services stop postgresql@16

# Linux
sudo systemctl stop postgresql
```

**Solução 2:** Subir apenas DynamoDB
```bash
docker-compose up -d dynamodb-local
# Usar PostgreSQL local existente
```

---

### Porta 8000 já em uso

**Problema:** Outra aplicação está usando porta 8000.

**Solução:**
```bash
# Verificar o que está usando porta 8000
lsof -i :8000

# Matar processo
kill -9 <PID>

# OU alterar porta no docker-compose.yml:
# ports:
#   - "8001:8000"  # Mapeia porta 8001 do host → 8000 do container
```

---

### Tabela cliente-core-cache não existe

**Problema:** Aplicação não criou tabela automaticamente.

**Causa:** `DynamoDbTableInitializer` não rodou (erro de conexão com DynamoDB Local).

**Solução:**
1. Verificar se DynamoDB Local está rodando:
   ```bash
   docker-compose ps dynamodb-local
   # ou verificar o container diretamente:
   docker ps --filter "name=cliente-core-dynamodb"
   ```

2. Verificar logs da aplicação:
   ```bash
   grep "DynamoDB" target/spring-boot.log
   ```

3. Criar tabela manualmente:
   ```bash
   aws dynamodb create-table \
       --table-name cliente-core-cache \
       --attribute-definitions AttributeName=cacheKey,AttributeType=S \
       --key-schema AttributeName=cacheKey,KeyType=HASH \
       --billing-mode PAY_PER_REQUEST \
       --endpoint-url http://localhost:8000 \
       --region us-east-1
   ```

4. Habilitar TTL manualmente:
   ```bash
   aws dynamodb update-time-to-live \
       --table-name cliente-core-cache \
       --time-to-live-specification Enabled=true,AttributeName=expirationTime \
       --endpoint-url http://localhost:8000 \
       --region us-east-1
   ```

---

### Cache não está funcionando

**Verificar se cache está ativo:**

```bash
# Criar cliente
RESPONSE=$(curl -s -X POST http://localhost:8081/api/clientes/v1/clientes/pf \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "11111111111",
    "nomeCompleto": "Teste Cache",
    "dataNascimento": "1990-01-01",
    "sexo": "MASCULINO",
    "email": "teste@cache.com",
    "telefone": "11999999999"
  }')

UUID=$(echo "$RESPONSE" | jq -r '.publicId')

# Buscar 2 vezes
time curl -s http://localhost:8081/api/clientes/v1/clientes/pf/$UUID > /dev/null
time curl -s http://localhost:8081/api/clientes/v1/clientes/pf/$UUID > /dev/null
```

**Se 2ª busca NÃO for mais rápida:**

1. Verificar logs da aplicação:
   ```bash
   grep "Cache" target/spring-boot.log
   ```

2. Verificar se anotações @Cacheable estão aplicadas:
   ```bash
   grep -r "@Cacheable" src/main/java/
   ```

3. Verificar se DynamoDB Local tem itens:
   ```bash
   aws dynamodb scan \
       --table-name cliente-core-cache \
       --endpoint-url http://localhost:8000 \
       --region us-east-1
   ```

4. Verificar configuração do cache:
   ```bash
   grep "cache" src/main/resources/application-dev.yml
   ```

---

### Application não inicia - Erro Liquibase

**Problema:** Liquibase validation error

**Solução (APENAS EM DEV):**
```bash
# Deletar histórico Liquibase
psql -U postgres -d vanessa_mudanca_clientes
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;
\q

# Reiniciar aplicação
mvn spring-boot:run
```

**ATENÇÃO:** NUNCA fazer isso em HML ou PROD!

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

# Métrica específica (pool de conexões)
curl http://localhost:8081/api/clientes/actuator/metrics/hikaricp.connections.active | jq

# Info da aplicação
curl http://localhost:8081/api/clientes/actuator/info | jq

# Prometheus scraping endpoint
curl http://localhost:8081/api/clientes/actuator/prometheus
```

### Logs Estruturados

```bash
# Tail logs da aplicação
tail -f target/spring-boot.log

# Filtrar logs por correlation ID
grep "correlationId=abc-123" target/spring-boot.log

# Filtrar logs de erro
grep "ERROR" target/spring-boot.log

# Filtrar logs de cache
grep "DynamoDb" target/spring-boot.log
```

---

## 🔒 Proteções de Segurança para PROD

### ⚠️ IMPORTANTE: Scripts de Teste NÃO devem rodar em PROD

**O que está protegido:**

1. **`local-dev.sh test-cache`** - Verifica presença do DynamoDB Local
   - ✅ Aborta se não detectar container `cliente-core-dynamodb`
   - ✅ Usa apenas dados dos seeds (não cria dados novos)
   - ✅ Operações READ-ONLY (não modifica/deleta)

2. **Seeds do Liquibase** - Controlados por contexts
   - ✅ Profile `dev`: `contexts: dev` (executa seeds)
   - ✅ Profile `hml/prod`: `contexts: ""` (NÃO executa seeds)

3. **Endpoints da aplicação** - Sem proteção especial
   - ⚠️ Todos endpoints (GET/POST/PUT/DELETE) funcionam em PROD
   - ⚠️ Não há bloqueio de operações destrutivas
   - ✅ Use IAM Roles + OAuth2 para controle de acesso

**Checklist antes de deploy:**

- [ ] Confirmar `contexts: ""` em `application-hml.yml` e `application-prod.yml`
- [ ] Verificar que seeds não estão sendo executados
- [ ] Revisar logs: `grep "Liquibase.*seed" logs/application.log`
- [ ] Confirmar profile ativo: `curl https://api.vanessa.com/clientes/actuator/env | jq '.activeProfiles'`

**⚠️ Se executar `local-dev.sh test-cache` em PROD (sem DynamoDB Local):**
```
╔════════════════════════════════════════════════════════════╗
║  ⚠️  ATENÇÃO: Este script é apenas para DESENVOLVIMENTO  ║
║  DynamoDB Local não detectado - Ambiente pode ser PROD!   ║
║  ABORTANDO por segurança para evitar poluir dados reais   ║
╚════════════════════════════════════════════════════════════╝
```

---

## 🚀 Deploy em HML/PROD

### Pré-requisitos

1. **Criar RDS PostgreSQL** (via Terraform - infra-shared)
2. **Criar DynamoDB table** (via Terraform - infra do MS)
3. **Configurar IAM Role** com permissões:
   - `dynamodb:GetItem`
   - `dynamodb:PutItem`
   - `dynamodb:DeleteItem`
   - `dynamodb:DescribeTable`
   - `dynamodb:UpdateTimeToLive`
   - `rds:DescribeDBInstances` (para RDS IAM auth - futuro)

### Build da aplicação

```bash
# Build com profile específico
mvn clean package -Dspring-boot.run.profiles=hml

# Gerar imagem Docker (via Jib)
mvn compile jib:dockerBuild -Dspring-boot.run.profiles=hml
```

### Variáveis de ambiente (HML/PROD)

```bash
# Database
DATABASE_URL="jdbc:postgresql://cliente-core-rds.xyz.rds.amazonaws.com:5432/vanessa"
DATABASE_USERNAME="vanessa_app"
DATABASE_PASSWORD="senha-segura-from-ssm"

# AWS
AWS_REGION="us-east-1"

# Spring
SPRING_PROFILES_ACTIVE="hml"  # ou "prod"
SERVER_PORT="8081"
```

### Health checks

```bash
# Liveness probe (K8s/ECS)
curl http://localhost:8081/api/clientes/actuator/health/liveness

# Readiness probe (K8s/ECS)
curl http://localhost:8081/api/clientes/actuator/health/readiness
```

---

## 📚 Documentação Adicional

- **LOCAL_DEVELOPMENT.md** - Guia detalhado de desenvolvimento local
- **DYNAMODB_CACHE_SUMMARY.md** - Overview da implementação de cache
- **docs/CACHE_MIGRATION_GUIDE.md** - Como migrar DynamoDB → Redis (quando necessário)
- **docs/CACHE_COST_COMPARISON.md** - Análise de custos (4 cenários de tráfego)
- **CLAUDE.md** - Guia para Claude Code (convenções, checklist, troubleshooting)
- **README.md** - Documentação completa do microserviço

---

## ✨ Features

✅ **Zero configuração AWS** - Usa DynamoDB Local com credenciais fake
✅ **Script helper** - `local-dev.sh` para facilitar operações
✅ **Auto table creation** - Tabela DynamoDB criada automaticamente no startup
✅ **GUI opcional** - DynamoDB Admin em http://localhost:8001
✅ **Cache testing** - Script automatizado para testar performance
✅ **Multi-ambiente** - Profiles para dev/hml/prod com configs otimizadas
✅ **Structured logging** - Logs JSON para CloudWatch Insights (hml/prod)
✅ **Health checks** - Actuator endpoints para liveness/readiness probes
✅ **Metrics** - Prometheus scraping endpoint para Grafana

---

**Última atualização:** 2025-11-04
**Versão:** 1.0.0
**Autor:** Equipe Va Nessa Mudança
