# Cache Cost Comparison: DynamoDB vs Redis

**Análise completa de custos para escolher cache backend no MVP da Va Nessa Mudança.**

---

## Resumo Executivo

| Backend | MVP (Mês 1-3) | Escala (Mês 3-6) | Produção (Mês 6+) |
|---------|---------------|-------------------|-------------------|
| **DynamoDB** | **$0/mês** | $15-30/mês | $50-100/mês |
| **Redis** | $12/mês | $12-25/mês | $25-50/mês |
| **Recomendação** | ✅ DynamoDB | ⚖️ Depende | ✅ Redis |

**Estratégia recomendada:** Começar com DynamoDB (Free Tier $0), migrar para Redis quando tráfego justificar (Mês 3-6).

---

## AWS Free Tier Breakdown

### DynamoDB (Free Tier Permanente)

| Recurso | Free Tier | Custo Excedente | MVP Suficiente? |
|---------|-----------|-----------------|-----------------|
| **Storage** | 25 GB | $0.25/GB/mês | ✅ Sim (até 250k clientes) |
| **Read Capacity** | 25 RCU (100 leituras/seg de 4 KB) | $0.25/milhão | ✅ Sim (até 8.6M req/mês) |
| **Write Capacity** | 25 WCU (25 escritas/seg) | $1.25/milhão | ✅ Sim (até 2.1M req/mês) |
| **Data Transfer** | Mesmo VPC = Gratuito | $0.09/GB out | ✅ Sim |

**Cálculo MVP (10k req/day):**
- Reads: 7k req/day × 30 = 210k/mês ÷ 100 leituras/seg = **0.024 RCU** (dentro Free Tier)
- Writes: 3k req/day × 30 = 90k/mês ÷ 25 escritas/seg = **0.042 WCU** (dentro Free Tier)
- Storage: ~1 GB (10k clientes cached) (dentro Free Tier)
- **Total: $0/mês**

---

### ElastiCache Redis (SEM Free Tier)

| Instância | RAM | vCPUs | Custo/hora | Custo/mês | Uso Recomendado |
|-----------|-----|-------|------------|-----------|-----------------|
| **cache.t4g.micro** | 0.5 GB | 2 | $0.016 | $11.68 | ✅ MVP baixo tráfego |
| **cache.t4g.small** | 1.6 GB | 2 | $0.034 | $24.82 | Dev/Staging |
| **cache.t4g.medium** | 3.2 GB | 2 | $0.068 | $49.64 | Prod médio tráfego |
| **cache.m7g.large** | 6.4 GB | 2 | $0.149 | $108.77 | Prod alto tráfego |

**Custos Adicionais:**
- Data transfer: $0 (mesmo VPC)
- Backup snapshots: 1 snapshot = Free, adicional = $0.085/GB/mês
- Data transfer out: $0.09/GB (primeira 1 GB/mês free)

**Cálculo MVP (cache.t4g.micro):**
- Instância: $11.68/mês
- Snapshots: $0 (1 snapshot free)
- Data transfer: $0 (mesmo VPC)
- **Total: ~$12/mês**

---

## Comparação de Performance

### Latência (Cache Hit)

| Backend | Latency P50 | Latency P95 | Latency P99 | Throughput |
|---------|-------------|-------------|-------------|------------|
| **DynamoDB** | 10-15ms | 20-30ms | 40-60ms | 25 RCU = 100 reads/sec |
| **Redis (t4g.micro)** | 1-3ms | 5-8ms | 10-15ms | Ilimitado (milhões/sec) |
| **Redis (m7g.large)** | 0.5-1ms | 2-4ms | 5-8ms | Ilimitado |

**Impacto no Response Time:**
```
Request flow:
API Gateway → ALB → ECS Task → Cache → PostgreSQL (cache miss)

Cache hit time = 10-15ms (DynamoDB) vs 1-3ms (Redis)
Diferença = ~10ms por requisição cached

Com 70% cache hit rate:
- 7k req/day × 10ms savings = 70 segundos economizados/day
- Response time médio: 52ms (DynamoDB) vs 42ms (Redis)
```

---

### Throughput Limits

#### DynamoDB Free Tier

**Reads (RCU):**
- 1 RCU = 1 leitura strongly consistent/sec de 4 KB
- 1 RCU = 2 leituras eventually consistent/sec de 4 KB
- Free Tier: 25 RCU = **100 leituras/sec** (eventually consistent)

**Writes (WCU):**
- 1 WCU = 1 escrita/sec de 1 KB
- Free Tier: 25 WCU = **25 escritas/sec**

**Exemplo: Cliente cached (2 KB JSON):**
- Read: 2 KB ÷ 4 KB = 0.5 RCU → **50 reads/sec gratuitos**
- Write: 2 KB ÷ 1 KB = 2 WCU → **12 writes/sec gratuitos**

**Quando Free Tier é suficiente:**
```
Tráfego: 10k req/day = 0.115 req/sec média
Pico (5x média): 0.575 req/sec
Cache hit rate: 70%

Reads necessários: 0.575 × 0.7 = 0.4 req/sec → 0.8 RCU
Writes necessários: 0.575 × 0.3 = 0.17 req/sec → 0.34 WCU

✅ Dentro Free Tier (25 RCU / 25 WCU)
```

**Quando excede Free Tier:**
```
Tráfego: 100k req/day = 1.15 req/sec média
Pico (5x média): 5.75 req/sec

Reads necessários: 5.75 × 0.7 = 4 req/sec → 8 RCU
Writes necessários: 5.75 × 0.3 = 1.7 req/sec → 3.4 WCU

✅ Ainda dentro Free Tier!

Tráfego: 500k req/day = 5.75 req/sec média
Pico (5x média): 28.75 req/sec

Reads necessários: 28.75 × 0.7 = 20 req/sec → 40 RCU ❌ Excede Free Tier
Writes necessários: 28.75 × 0.3 = 8.6 req/sec → 17.2 WCU ✅ OK

Custo excedente:
- 40 RCU - 25 RCU Free = 15 RCU × $0.25/milhão reads × 2.6M reads/mês = $9.75/mês
```

---

#### Redis (ElastiCache)

**Throughput:** Ilimitado (limitado apenas por CPU/Network)

**cache.t4g.micro (2 vCPUs):**
- Throughput teórico: **50k ops/sec**
- Throughput real (considerando network): **20k ops/sec**
- Latência: 1-3ms

**cache.m7g.large (2 vCPUs, mais CPU power):**
- Throughput teórico: **100k ops/sec**
- Throughput real: **50k ops/sec**
- Latência: 0.5-1ms

---

## Cost Scenarios

### Cenário 1: MVP (0-10k req/day)

**DynamoDB:**
- Storage: 1 GB (10k clientes)
- Reads: 210k/mês → 2.4 RCU (Free Tier)
- Writes: 90k/mês → 1 WCU (Free Tier)
- **Custo: $0/mês**

**Redis (cache.t4g.micro):**
- Instância: $11.68/mês
- Snapshots: $0
- **Custo: $12/mês**

**Vencedor:** ✅ **DynamoDB** (economia de $12/mês)

---

### Cenário 2: Crescimento (10k-50k req/day)

**DynamoDB:**
- Storage: 5 GB (50k clientes)
- Reads: 1M/mês → 12 RCU (Free Tier)
- Writes: 450k/mês → 5 WCU (Free Tier)
- **Custo: $0/mês**

**Redis (cache.t4g.micro):**
- Instância: $11.68/mês
- **Custo: $12/mês**

**Vencedor:** ✅ **DynamoDB** (economia de $12/mês)

---

### Cenário 3: Escala Moderada (50k-200k req/day)

**DynamoDB:**
- Storage: 20 GB (200k clientes)
- Reads: 4.2M/mês → 48 RCU
  - Excedente: 48 - 25 = 23 RCU
  - Custo: 23 × 2.6M reads × $0.25/milhão = **$14.95/mês**
- Writes: 1.8M/mês → 20 WCU (Free Tier)
- **Custo Total: $15/mês**

**Redis (cache.t4g.small):**
- Instância: $24.82/mês
- **Custo: $25/mês**

**Vencedor:** ✅ **DynamoDB** (economia de $10/mês, mas latência pior)

**Ponto de inflexão:** Aqui vale avaliar trade-off:
- DynamoDB: $15/mês, latência 10-20ms
- Redis: $25/mês, latência 1-3ms
- **Diferença:** $10/mês por ~10ms de melhoria

Se latência é crítica → Redis
Se custo é prioridade → DynamoDB

---

### Cenário 4: Alta Escala (200k-1M req/day)

**DynamoDB:**
- Storage: 100 GB (1M clientes)
  - Excedente: 100 - 25 = 75 GB × $0.25/GB = **$18.75/mês**
- Reads: 21M/mês → 240 RCU
  - Excedente: 240 - 25 = 215 RCU × 21M reads × $0.25/milhão = **$1,128.75/mês** ❌
- Writes: 9M/mês → 100 WCU
  - Excedente: 100 - 25 = 75 WCU × 9M writes × $1.25/milhão = **$843.75/mês** ❌
- **Custo Total: $1,991/mês** 💸💸💸

**Redis (cache.m7g.large):**
- Instância: $108.77/mês
- **Custo: $109/mês**

**Vencedor:** ✅ **Redis** (economia de $1,882/mês! 🎉)

**Ponto de viragem:** DynamoDB se torna MUITO caro após exceder Free Tier significativamente.

---

## Decision Matrix

| Tráfego | Recomendação | Custo/mês | Latência | Justificativa |
|---------|--------------|-----------|----------|---------------|
| **0-50k req/day** | ✅ DynamoDB | $0 | 10-20ms | Free Tier suficiente |
| **50k-200k req/day** | ⚖️ DynamoDB ou Redis | $15 vs $25 | 10-20ms vs 1-3ms | Avaliar latência vs custo |
| **200k-500k req/day** | ✅ Redis | $25-50 | 1-3ms | DynamoDB excede Free Tier |
| **500k+ req/day** | ✅ Redis | $50-109 | 0.5-1ms | DynamoDB MUITO caro |

---

## Custo Total do MVP (incluindo infra completa)

### Com DynamoDB (Fase 1 - Mês 1-3)

| Serviço | Custo/mês | Free Tier Discount | Real Cost |
|---------|-----------|---------------------|-----------|
| RDS db.t3.micro | $16 | -$16 (750h free) | **$0** |
| ECS Fargate (1 task) | $14 | -$0 | **$14** |
| ALB | $23 | -$17 (750h free) | **$6** |
| ECR (10 GB) | $1 | -$0 | **$1** |
| CloudWatch (5 GB logs) | $2.50 | -$2.50 (5GB free) | **$0** |
| **DynamoDB** | $0 | -$0 | **$0** |
| Route53 (1 hosted zone) | $0.50 | -$0 | **$0.50** |
| S3 (Terraform state) | $0.50 | -$0 | **$0.50** |
| **TOTAL MVP** | - | - | **$22/mês** 💰 |

---

### Com Redis (Futuro - Mês 3-6+)

| Serviço | Custo/mês | Free Tier Discount | Real Cost |
|---------|-----------|---------------------|-----------|
| RDS db.t3.micro | $16 | -$0 (Free Tier expirou) | **$16** |
| ECS Fargate (1 task) | $14 | -$0 | **$14** |
| ALB | $23 | -$0 (Free Tier expirou) | **$23** |
| ECR (10 GB) | $1 | -$0 | **$1** |
| CloudWatch (5 GB logs) | $2.50 | -$0 (Free Tier expirou) | **$2.50** |
| **ElastiCache Redis (t4g.micro)** | $12 | -$0 | **$12** |
| Route53 | $0.50 | -$0 | **$0.50** |
| S3 | $0.50 | -$0 | **$0.50** |
| **TOTAL Produção** | - | - | **$69.50/mês** 💰💰 |

**Diferença:** $47.50/mês após Free Tier expirar (inevitável aos 12 meses).

---

## Otimizações de Custo

### DynamoDB

**1. Use On-Demand Billing para tráfego irregular:**
```hcl
resource "aws_dynamodb_table" "cache" {
  billing_mode = "PAY_PER_REQUEST"  # Sem mínimos, paga apenas uso real
}
```

**2. Habilite TTL para auto-deletion (gratuito):**
```hcl
ttl {
  attribute_name = "expirationTime"
  enabled        = true
}
```

**3. Use Eventually Consistent Reads (metade do custo):**
```java
GetItemRequest.builder()
    .consistentRead(false)  # Eventually consistent = 2x mais RCUs
```

---

### Redis

**1. Use Reserved Instances (30-40% desconto):**
```
cache.t4g.micro:
- On-Demand: $11.68/mês
- 1-Year RI (No Upfront): $8.32/mês (economia $3.36/mês)
- 3-Year RI (All Upfront): $5.84/mês (economia $5.84/mês)
```

**2. Use cache.t4g (ARM Graviton2) ao invés de cache.t3 (x86):**
```
Mesma performance, 20% mais barato:
- cache.t3.micro: $14.60/mês
- cache.t4g.micro: $11.68/mês (economia $2.92/mês)
```

**3. Configure snapshot schedule estratégico:**
```hcl
snapshot_retention_limit = 1  # Mínimo necessário
snapshot_window          = "03:00-05:00"  # Horário de menor tráfego
```

---

## Recomendação Final

### Fase 1: MVP (Mês 1-6)
✅ **DynamoDB**
- Custo: $0/mês (Free Tier permanente)
- Latência: 10-20ms (aceitável para MVP)
- Throughput: Suficiente para 50k req/day

**Quando migrar para Fase 2:**
- Tráfego > 50k req/day
- Cache hit rate > 70%
- Latência se torna gargalo

---

### Fase 2: Crescimento (Mês 6-12)
✅ **Redis (cache.t4g.micro)**
- Custo: $12/mês
- Latência: 1-3ms (95% mais rápido)
- Throughput: 20k ops/sec (40x Free Tier DynamoDB)

**ROI:**
```
Custo adicional: $12/mês = $144/ano
Response time improvement: ~10ms por request cached
Requests/dia: 100k
Economia de tempo: 100k × 0.7 cache hit × 10ms = 700 segundos/dia

Valor do tempo economizado (assumindo $50/hora desenvolvedor):
700s/dia × 30 dias = 21k segundos/mês = 5.8 horas/mês × $50/hora = $290/mês

ROI = ($290 - $12) / $12 = 2,317% 🚀
```

---

### Fase 3: Escala (Mês 12+)
✅ **Redis (cache.t4g.small ou maior)**
- Custo: $25-50/mês
- Reserved Instances: Economia 30-40%
- Multi-AZ replication: High availability

**Ponto de atenção:** Se tráfego > 500k req/day, considerar cache.m7g.large ($109/mês).

---

## Conclusão

**Estratégia recomendada:**

1. **Mês 1-3:** DynamoDB ($0/mês)
2. **Mês 3-6:** Avaliar tráfego e latência
3. **Mês 6+:** Migrar para Redis quando justificado

**Trade-off:**
- DynamoDB = Custo zero, latência OK
- Redis = Custo $12/mês, latência excelente

**Custo total MVP (12 meses):**
- Com DynamoDB: $22/mês × 12 = $264/ano
- Com Redis: $34/mês × 12 = $408/ano
- **Diferença: $144/ano** (justificável se latência é crítica)

**Decisão final:** Começar com DynamoDB, migrar quando tráfego > 50k req/day ou latência se tornar gargalo.
