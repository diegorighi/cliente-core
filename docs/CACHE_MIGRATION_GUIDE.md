# Cache Migration Guide: DynamoDB → Redis

**Objetivo:** Guia completo para migrar cache backend de DynamoDB (MVP) para Redis (Produção) **sem alterar código de negócio**.

---

## Por Que Migrar?

### DynamoDB (MVP - Fase 1)

**Vantagens:**
- ✅ **Free Tier permanente:** 25 GB + 25 WCU/RCU = $0/month
- ✅ **Serverless:** Sem gestão de infraestrutura
- ✅ **TTL automático:** Deletions gratuitas
- ✅ **Durabilidade:** 99.999999999% (11 noves)

**Desvantagens:**
- ⚠️ **Latência maior:** 10-20ms (vs Redis 5-10ms)
- ⚠️ **Throughput limitado:** 25 WCU/RCU (Free Tier)
- ⚠️ **Sem data structures:** Apenas key-value

**Quando usar:** MVP até Month 3-6, baixo tráfego (<10k req/day)

---

### Redis (Produção - Fase 2)

**Vantagens:**
- ✅ **Latência ultra-baixa:** 5-10ms (sub-millisecond)
- ✅ **High throughput:** Milhões de ops/segundo
- ✅ **Data structures:** Lists, Sets, Sorted Sets, Hashes
- ✅ **Pub/Sub:** Real-time messaging
- ✅ **Lua scripts:** Operações atômicas complexas

**Desvantagens:**
- ❌ **Custo:** $12-25/month (ElastiCache cache.t4g.micro)
- ⚠️ **Gestão:** Requer configuração (replication, failover)
- ⚠️ **Volatilidade:** Dados em memória (RDB/AOF persistence necessário)

**Quando usar:** Month 3-6+, tráfego alto (>50k req/day), latência crítica

---

## Arquitetura da Solução (Backend-Agnostic)

```
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                          │
│   (FindClientePFByIdService, UpdateClientePFService, etc.) │
│                                                             │
│   @Cacheable("clientes:findById")                          │
│   @CacheEvict("clientes:findById")                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      │ Spring Cache Abstraction
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   CacheManager                              │
│              (Interface do Spring)                          │
└────────────┬────────────────────────────────────────────────┘
             │
      ┌──────▼────────┐
      │  @Conditional │
      │  OnProperty   │
      └──┬────────┬───┘
         │        │
         │        │
    ┌────▼───┐  ┌▼─────────┐
    │ Dynamo │  │  Redis   │
    │   DB   │  │  Cache   │
    │ Cache  │  │  Config  │
    │ Config │  │          │
    └────┬───┘  └┬─────────┘
         │       │
    ┌────▼───┐  ┌▼─────────┐
    │ Dynamo │  │  Redis   │
    │   DB   │  │  Cache   │
    │ Client │  │ Manager  │
    └────────┘  └──────────┘
```

**Chave:** Service layer usa apenas `@Cacheable` e `@CacheEvict`. Backend é trocado via **configuração**, não código.

---

## Passo a Passo: Migração Sem Downtime

### Fase 1: Preparação (Dev Environment)

**1. Adicionar dependências Redis ao `pom.xml`:**

```xml
<!-- Spring Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Lettuce client (default Redis client for Spring Boot 3.x) -->
<!-- Já incluído em spring-boot-starter-data-redis -->
```

**2. Criar `RedisCacheConfig.java`:**

```java
package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do Redis Cache (Produção - ElastiCache).
 * Ativado via: cache.backend=redis
 */
@Configuration
@ConditionalOnProperty(name = "cache.backend", havingValue = "redis")
public class RedisCacheConfig {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final Duration LIST_TTL = Duration.ofMinutes(1);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("clientes:findById", defaultConfig);
        cacheConfigurations.put("clientes:findByCpf", defaultConfig);
        cacheConfigurations.put("clientes:findByCnpj", defaultConfig);
        cacheConfigurations.put("clientes:findByEmail", defaultConfig.entryTtl(Duration.ofMinutes(3)));
        cacheConfigurations.put("clientes:list", defaultConfig.entryTtl(LIST_TTL));
        cacheConfigurations.put("clientes:count", defaultConfig.entryTtl(Duration.ofSeconds(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

**3. Criar `application-prod.yml` com configuração Redis:**

```yaml
spring:
  # Redis Cache Configuration (Produção - ElastiCache)
  data:
    redis:
      host: ${ELASTICACHE_ENDPOINT}  # Ex: cliente-core-prod.abc123.ng.0001.use1.cache.amazonaws.com
      port: 6379
      password: ${REDIS_PASSWORD}  # Secrets Manager
      ssl: true
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: -1ms
        shutdown-timeout: 100ms

  cache:
    type: redis

# Cache backend selection
cache:
  backend: redis  # Migrado de DynamoDB
```

**4. Testar localmente com Redis (Docker):**

```bash
# docker-compose-redis.yml (para testes)
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

# Iniciar Redis local
docker-compose -f docker-compose-redis.yml up -d

# Alterar application-dev.yml
cache:
  backend: redis

spring:
  data:
    redis:
      host: localhost
      port: 6379

# Rodar aplicação
mvn spring-boot:run

# Validar logs
# [INFO] Using Redis cache backend
```

**5. Validar comportamento idêntico:**

```bash
# Teste 1: Cache hit
curl http://localhost:8081/api/clientes/v1/pf/{uuid}
# Log: Cache miss - querying database
# Response time: ~150-200ms

curl http://localhost:8081/api/clientes/v1/pf/{uuid}
# Log: Cache hit - returning from Redis
# Response time: ~5-10ms (vs 10-20ms DynamoDB)

# Teste 2: Cache eviction
curl -X PUT http://localhost:8081/api/clientes/v1/pf/{uuid} -d '{...}'
# Log: Cache evicted - clientes:findById::{uuid}

curl http://localhost:8081/api/clientes/v1/pf/{uuid}
# Log: Cache miss - querying database
# Response time: ~150-200ms
```

---

### Fase 2: Deploy em Produção (Blue-Green)

**1. Criar ElastiCache Redis via Terraform:**

```bash
# infra-shared/modules/cache/main.tf
resource "aws_elasticache_replication_group" "redis" {
  replication_group_id       = "cliente-core-prod-redis"
  description                = "Redis cache for cliente-core"
  engine                     = "redis"
  engine_version             = "7.0"
  node_type                  = "cache.t4g.micro"  # $11.68/month
  num_cache_clusters         = 1
  port                       = 6379
  parameter_group_name       = "default.redis7"
  subnet_group_name          = aws_elasticache_subnet_group.redis.name
  security_group_ids         = [aws_security_group.redis.id]
  automatic_failover_enabled = false
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  auth_token                 = random_password.redis_auth.result
  snapshot_retention_limit   = 1
  snapshot_window            = "03:00-05:00"

  tags = {
    Name        = "cliente-core-prod-redis"
    Environment = "prod"
    CostCenter  = "shared-cache"
  }
}

output "redis_endpoint" {
  value = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "redis_port" {
  value = aws_elasticache_replication_group.redis.port
}

# Deploy infra
cd infra-shared
terraform init
terraform plan -out=plan.tfplan
terraform apply plan.tfplan
```

**2. Armazenar senha Redis no Secrets Manager:**

```bash
aws secretsmanager create-secret \
    --name /prod/cliente-core/redis-password \
    --secret-string "$(terraform output -raw redis_auth_token)" \
    --region us-east-1

# Atualizar ECS Task Definition para injetar secret
```

**3. Deploy Blue-Green (Zero Downtime):**

```bash
# Step 1: Deploy nova versão com Redis (GREEN)
# application-prod.yml já configurado com cache.backend=redis

cd cliente-core
mvn clean package -DskipTests
docker build -t cliente-core:v2.0.0-redis .

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ECR_URL>
docker tag cliente-core:v2.0.0-redis <ECR_URL>/cliente-core:v2.0.0-redis
docker push <ECR_URL>/cliente-core:v2.0.0-redis

# Step 2: Atualizar ECS Service (Blue-Green deployment)
aws ecs update-service \
    --cluster cliente-core-prod \
    --service cliente-core-service \
    --force-new-deployment \
    --desired-count 2  # Manter 2 tasks durante migração

# Step 3: Monitorar health checks
aws ecs describe-services \
    --cluster cliente-core-prod \
    --services cliente-core-service

# Step 4: Validar logs
aws logs tail /ecs/cliente-core-prod --follow | grep "Using Redis cache backend"

# Step 5: Testar endpoints
curl https://api.vanessa-mudanca.com/clientes/v1/pf/{uuid}
# Response time deve ser ~5-10ms (cache hit)

# Step 6: Se tudo OK, escalar para 1 task (remover BLUE)
aws ecs update-service \
    --cluster cliente-core-prod \
    --service cliente-core-service \
    --desired-count 1
```

**4. Monitorar métricas pós-migração:**

```bash
# CloudWatch Metrics
# - CacheHitRate (target: >70%)
# - CacheMissRate
# - Redis: CPUUtilization, NetworkBytesIn/Out
# - ECS: ResponseTime (deve cair ~50%)

# Grafana Dashboard Query (Prometheus)
# Cache hit rate
sum(rate(cache_gets_total{result="hit"}[5m]))
/ sum(rate(cache_gets_total[5m]))

# Response time P95
histogram_quantile(0.95,
  sum(rate(http_request_duration_seconds_bucket[5m])) by (le)
)
```

---

### Fase 3: Cleanup (Opcional)

**Após 30 dias de Redis estável em produção:**

```bash
# 1. Remover dependências DynamoDB do pom.xml
# Comentar ou deletar:
# - software.amazon.awssdk:dynamodb
# - com.amazonaws:DynamoDBLocal

# 2. Remover classes DynamoDB
rm src/main/java/.../cache/DynamoDbCache.java
rm src/main/java/.../cache/DynamoDbCacheManager.java
rm src/main/java/.../cache/DynamoDbTableInitializer.java
rm src/main/java/.../config/DynamoDbCacheConfig.java

# 3. Remover DynamoDB do docker-compose.yml
# Comentar ou deletar serviço dynamodb-local

# 4. Deletar tabela DynamoDB (se não usada)
aws dynamodb delete-table --table-name cliente-core-cache --region us-east-1

# 5. Commit e deploy
git add .
git commit -m "chore: Remove DynamoDB cache backend (migrated to Redis)"
git push origin main
```

---

## Comparação de Performance

| Métrica | DynamoDB | Redis | Melhoria |
|---------|----------|-------|----------|
| **Cache hit latency** | 10-20ms | 5-10ms | **2x mais rápido** |
| **Cache miss latency** | ~200ms | ~150ms | 25% mais rápido |
| **Throughput (Free Tier)** | 25 WCU/RCU | Ilimitado | **Infinito** |
| **Throughput (Paid)** | $0.25/million | cache.t4g.micro | **50x mais barato** |
| **Custo (MVP)** | $0/month | $12/month | +$12 |
| **Custo (Scale)** | $50-100/month | $12-25/month | **Economia 50-75%** |
| **Data structures** | Key-value | Lists/Sets/Hashes | **Mais flexível** |
| **Persistência** | 99.999999999% | RDB/AOF (config) | DynamoDB vence |

---

## Troubleshooting

### Problema: Cache hit rate baixo após migração

**Causa:** Cache warming não foi feito, Redis vazio

**Solução:**
```bash
# Script de cache warming (popular cache proativamente)
#!/bin/bash
# warm-cache.sh

ENDPOINT="https://api.vanessa-mudanca.com/clientes/v1"

# Buscar todos os UUIDs de clientes
UUIDS=$(psql -h $DB_HOST -U $DB_USER -d $DB_NAME \
  -t -c "SELECT public_id FROM clientes WHERE ativo = true LIMIT 1000")

# Popular cache
for uuid in $UUIDS; do
  curl -s "$ENDPOINT/pf/$uuid" > /dev/null
  echo "Warmed cache for client: $uuid"
done
```

---

### Problema: ElastiCache connection timeout

**Causa:** Security group bloqueando acesso do ECS

**Solução:**
```bash
# Verificar security groups
aws elasticache describe-replication-groups \
  --replication-group-id cliente-core-prod-redis

# Adicionar regra de ingress
aws ec2 authorize-security-group-ingress \
  --group-id <REDIS_SG_ID> \
  --protocol tcp \
  --port 6379 \
  --source-group <ECS_TASK_SG_ID>
```

---

### Problema: OutOfMemoryException no Redis

**Causa:** maxmemory atingido, eviction policy inadequada

**Solução:**
```bash
# Verificar uso de memória
redis-cli --tls -h $REDIS_ENDPOINT -a $REDIS_PASSWORD
> INFO memory

# Ajustar maxmemory-policy
# infra-shared/modules/cache/main.tf
parameter_group_name = aws_elasticache_parameter_group.redis.name

resource "aws_elasticache_parameter_group" "redis" {
  name   = "cliente-core-redis-params"
  family = "redis7"

  parameter {
    name  = "maxmemory-policy"
    value = "allkeys-lru"  # LRU eviction
  }
}
```

---

## Checklist de Migração

**Pré-Migração:**
- [ ] Adicionar dependências Redis ao pom.xml
- [ ] Criar RedisCacheConfig.java com @ConditionalOnProperty
- [ ] Testar localmente com Redis Docker
- [ ] Validar comportamento idêntico (cache hit/miss/evict)
- [ ] Criar ElastiCache via Terraform
- [ ] Armazenar senha no Secrets Manager
- [ ] Atualizar ECS Task Definition com secrets

**Durante Migração:**
- [ ] Deploy Blue-Green (2 tasks simultâneas)
- [ ] Monitorar logs: "Using Redis cache backend"
- [ ] Validar health checks
- [ ] Testar endpoints em produção
- [ ] Monitorar CloudWatch Metrics
- [ ] Executar cache warming script

**Pós-Migração:**
- [ ] Validar cache hit rate >70%
- [ ] Monitorar response time (deve cair ~50%)
- [ ] Escalar para 1 task (remover BLUE)
- [ ] Observar por 7 dias
- [ ] Após 30 dias: remover código DynamoDB (opcional)
- [ ] Deletar tabela DynamoDB (opcional)

---

## Rollback Plan

**Se algo der errado:**

```bash
# Step 1: Reverter application.yml
cache:
  backend: dynamodb  # Era: redis

# Step 2: Rebuild imagem
mvn clean package -DskipTests
docker build -t cliente-core:v1.9.0-dynamodb .
docker push <ECR_URL>/cliente-core:v1.9.0-dynamodb

# Step 3: Rollback ECS Service
aws ecs update-service \
    --cluster cliente-core-prod \
    --service cliente-core-service \
    --force-new-deployment

# Step 4: Validar logs
aws logs tail /ecs/cliente-core-prod --follow | grep "Using DynamoDB cache backend"

# Step 5: Manter ElastiCache rodando (não deletar)
# Pode ser útil para próxima tentativa
```

---

## Custos

### DynamoDB (Free Tier)
- Storage: 25 GB = **$0/month** (Free Tier permanente)
- Reads: 25 RCU = **$0/month** (Free Tier)
- Writes: 25 WCU = **$0/month** (Free Tier)
- **Total: $0/month**

### Redis (ElastiCache)
- cache.t4g.micro (0.5 GB RAM): **$11.68/month**
- Data transfer: **$0** (mesmo VPC)
- Snapshots: **$0** (1 snapshot = Free Tier)
- **Total: ~$12/month**

### Quando migrar?
- **Mês 1-3:** DynamoDB ($0)
- **Mês 3-6:** Avaliar tráfego
  - Se >50k req/day → Redis
  - Se <50k req/day → Manter DynamoDB
- **Mês 6+:** Redis ($12/month) + otimizações

---

## Conclusão

✅ **Migração Zero-Friction:** Service layer não muda (apenas @Cacheable/@CacheEvict)

✅ **Zero Downtime:** Blue-Green deployment via ECS

✅ **Rollback Seguro:** Trocar 1 linha em application.yml

✅ **Custo Controlado:** DynamoDB $0 → Redis $12 (quando justificado)

✅ **Performance 2x:** Latência cai de 10-20ms para 5-10ms

**Próximo Passo:** Testar localmente com Redis Docker antes de ElastiCache!
