# 🚀 Deploy em Andamento - cliente-core

**Início:** 2025-11-05 17:16 BRT
**Status:** 🟡 EM PROGRESSO

---

## ✅ Recursos Criados

### AWS Secrets Manager
- [x] Secret: `cliente-core/prod/database`
- [x] Secret: `cliente-core/prod/jwt-key`
- [x] IAM Role: `cliente-core-secrets-role-prod`

### RDS PostgreSQL
- [x] Instance: `cliente-core-prod`
- [x] Endpoint: `cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com:5432`
- [ ] Database: `vanessa_mudanca_clientes` (será criado via Liquibase no primeiro startup)

### ECR (Container Registry)
- [x] Repository: `530184476864.dkr.ecr.sa-east-1.amazonaws.com/cliente-core`
- [x] Image scan on push: Enabled

### ECS Cluster
- [x] Cluster: `cliente-core-prod-cluster`
- [x] Type: FARGATE
- [x] Status: ACTIVE

### Networking
- [x] VPC: `vpc-0b338b69a3ddac5da` (default, 172.31.0.0/16)
- [x] Subnets disponíveis:
  - `subnet-02fa56b41afd95fbc` (sa-east-1a)
  - `subnet-09294063c722eea99` (sa-east-1b)
  - `subnet-0a1f1d6dc0865086f` (sa-east-1c)

### Security Groups
- [x] ECS SG: `sg-0707b6a856ca85fae`
  - Egress: HTTPS (443) → 0.0.0.0/0 (Secrets Manager, ECR)
  - Egress: PostgreSQL (5432) → 172.31.0.0/16 (RDS)

- [x] RDS SG: `sg-061e5ac3b723492ad`
  - Ingress: PostgreSQL (5432) ← `sg-0707b6a856ca85fae` (ECS)

### IAM Roles
- [x] Task Role: `arn:aws:iam::530184476864:role/cliente-core-secrets-role-prod`
  - Permissions: GetSecretValue, DescribeSecret, KMS Decrypt

- [x] Execution Role: `arn:aws:iam::530184476864:role/ecsTaskExecutionRole`
  - Permissions: AmazonECSTaskExecutionRolePolicy (ECR pull, CloudWatch logs)

### CloudWatch
- [x] Log Group: `/ecs/cliente-core-prod`

### Application Build
- [x] Maven build: SUCCESS (5.4 segundos)
- [x] JAR criado: `target/cliente-core-0.0.1-SNAPSHOT.jar`

---

## 🟡 Em Progresso

### Docker Build & Push
- Status: **EM ANDAMENTO** (background process 017866)
- Etapa atual: Baixando dependências Maven dentro do container
- Estimativa: 5-10 minutos restantes

---

## ⏳ Pendente

### ECS Task Definition
- [ ] Registrar task definition com:
  - Image: `530184476864.dkr.ecr.sa-east-1.amazonaws.com/cliente-core:latest`
  - CPU: 512 (0.5 vCPU)
  - Memory: 1024 MB
  - Task Role: `cliente-core-secrets-role-prod`
  - Execution Role: `ecsTaskExecutionRole`
  - Environment: AWS_REGION, AWS_SECRETS_NAME, SPRING_PROFILES_ACTIVE=prod

### ECS Service
- [ ] Criar service:
  - Cluster: `cliente-core-prod-cluster`
  - Task Definition: `cliente-core-prod:1`
  - Desired Count: 1
  - Launch Type: FARGATE
  - Network: Subnets + Security Group `sg-0707b6a856ca85fae`
  - Assign Public IP: ENABLED (para acesso aos endpoints AWS)

### Verificação
- [ ] Aguardar task entrar em status RUNNING
- [ ] Verificar logs: `aws logs tail /ecs/cliente-core-prod --follow`
- [ ] Procurar por:
  - `Successfully fetched database credentials from Secrets Manager`
  - `HikariPool-1 - Start completed`
  - `Liquibase Update to...` (criação de tabelas)
  - `Tomcat started on port(s): 8081`
- [ ] Testar health check: `curl http://<PUBLIC_IP>:8081/api/clientes/actuator/health`

---

## 📊 Timeline Estimado

```
17:16 ✅ Início do deploy
17:17 ✅ ECR repository criado
17:17 ✅ Maven build completo
17:17 ✅ ECS Cluster criado
17:18 ✅ Security Groups configurados
17:19 ✅ IAM Roles criados
17:19 🟡 Docker build iniciado (em andamento)
17:25 🔜 Docker build completo (estimativa)
17:26 🔜 Docker push completo
17:27 🔜 ECS Task Definition registrado
17:28 🔜 ECS Service criado
17:30 🔜 Task em RUNNING
17:31 🔜 Health check OK
17:32 ✅ DEPLOY COMPLETO
```

**Tempo total estimado:** ~15-20 minutos

---

## 🔍 Como Acompanhar

### Ver progresso do Docker build:
```bash
# Via Claude Code (já em execução)
# Processo em background: 017866
```

### Verificar status dos recursos:
```bash
# Cluster ECS
aws ecs describe-clusters --clusters cliente-core-prod-cluster --region sa-east-1

# Secrets Manager
aws secretsmanager list-secrets --region sa-east-1 | grep cliente-core

# ECR images
aws ecr list-images --repository-name cliente-core --region sa-east-1

# RDS
aws rds describe-db-instances --db-instance-identifier cliente-core-prod --region sa-east-1
```

### Ver logs quando aplicação estiver rodando:
```bash
aws logs tail /ecs/cliente-core-prod --follow --region sa-east-1
```

---

## 🐛 Troubleshooting

### Se Docker build falhar:
- Verificar Dockerfile está correto
- Verificar `target/` tem o JAR: `ls -lh target/*.jar`
- Re-executar manualmente: `docker build -t cliente-core:latest .`

### Se push para ECR falhar:
- Re-fazer login: `aws ecr get-login-password --region sa-east-1 | docker login --username AWS --password-stdin 530184476864.dkr.ecr.sa-east-1.amazonaws.com`
- Verificar permissões IAM do usuário AWS

### Se task não iniciar:
- Verificar Security Group permite egress HTTPS (443)
- Verificar Execution Role tem permissão para pull do ECR
- Verificar Task Role tem permissão para ler secrets

### Se aplicação não conectar no RDS:
- Verificar Security Group do RDS permite ingress do ECS SG
- Verificar credentials no secret estão corretas
- Testar conexão: `aws secretsmanager get-secret-value --secret-id cliente-core/prod/database --region sa-east-1`

---

**Última Atualização:** 2025-11-05 17:20 BRT
**Responsável:** Claude Code
**Próxima Ação:** Aguardar Docker build terminar, então registrar Task Definition
