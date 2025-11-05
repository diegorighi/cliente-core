# 🚀 Guia de Deploy - cliente-core (PRODUCTION READY)

**Status:** ✅ AWS Secrets Manager configurado e funcionando
**Data:** 2025-11-05
**Ambiente:** Produção (sa-east-1 - São Paulo)

---

## ✅ O Que Já Foi Feito

### 1. RDS PostgreSQL Criado
- **Instance ID:** `cliente-core-prod`
- **Endpoint:** `cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com:5432`
- **Username:** `dbadmin`
- **Region:** `sa-east-1` (São Paulo)
- **Status:** ✅ AVAILABLE

### 2. AWS Secrets Manager Configurado
- **Secret (Database):** `cliente-core/prod/database`
  - ARN: `arn:aws:secretsmanager:sa-east-1:530184476864:secret:cliente-core/prod/database-xkfVWU`
  - Contém: host, port, database, username, password, url

- **Secret (JWT):** `cliente-core/prod/jwt-key`
  - ARN: `arn:aws:secretsmanager:sa-east-1:530184476864:secret:cliente-core/prod/jwt-key-EP9wIV`
  - Contém: signing_key, algorithm, expiration

### 3. IAM Role Criado
- **Role Name:** `cliente-core-secrets-role-prod`
- **ARN:** `arn:aws:iam::530184476864:role/cliente-core-secrets-role-prod`
- **Permissions:**
  - `secretsmanager:GetSecretValue` (para os 2 secrets acima)
  - `secretsmanager:DescribeSecret`
  - `kms:Decrypt` (para Secrets Manager encryption)

### 4. Spring Boot Configurado
- ✅ `AwsSecretsManagerConfig.java` criado
- ✅ Dependências AWS adicionadas ao `pom.xml`
- ✅ `application.yml` configurado para prod/staging profiles
- ✅ Dev profile continua usando localhost (sem AWS)

---

## 📦 Próximos Passos - Deploy no ECS

### Opção A: Deploy via Console AWS

#### Passo 1: Criar o Banco de Dados no RDS

```bash
# Conectar ao RDS via psql (de uma máquina com acesso)
psql -h cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com \
     -U dbadmin \
     -d postgres

# Dentro do psql, criar o database
CREATE DATABASE vanessa_mudanca_clientes;

# Conectar ao database criado
\c vanessa_mudanca_clientes

# Verificar
\l
```

**Nota:** O Liquibase vai criar as tabelas automaticamente no primeiro startup da aplicação.

#### Passo 2: Fazer Build da Aplicação

```bash
cd /Users/diegorighi/Desenvolvimento/yukam-drighi/services/cliente-core

# Build com Maven
mvn clean package -DskipTests

# Docker build (criar imagem)
docker build -t cliente-core:latest .

# Tag para ECR
docker tag cliente-core:latest 530184476864.dkr.ecr.sa-east-1.amazonaws.com/cliente-core:latest

# Push para ECR (primeiro fazer login)
aws ecr get-login-password --region sa-east-1 | \
  docker login --username AWS --password-stdin 530184476864.dkr.ecr.sa-east-1.amazonaws.com

docker push 530184476864.dkr.ecr.sa-east-1.amazonaws.com/cliente-core:latest
```

**Nota:** Se o repositório ECR não existe, criar antes:
```bash
aws ecr create-repository --repository-name cliente-core --region sa-east-1
```

#### Passo 3: Criar ECS Task Definition

Salve este JSON como `cliente-core-task-def.json`:

```json
{
  "family": "cliente-core-prod",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "taskRoleArn": "arn:aws:iam::530184476864:role/cliente-core-secrets-role-prod",
  "executionRoleArn": "arn:aws:iam::530184476864:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "cliente-core",
      "image": "530184476864.dkr.ecr.sa-east-1.amazonaws.com/cliente-core:latest",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "AWS_REGION",
          "value": "sa-east-1"
        },
        {
          "name": "AWS_SECRETS_NAME",
          "value": "cliente-core/prod/database"
        },
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "JAVA_TOOL_OPTIONS",
          "value": "-Xms512m -Xmx768m"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/cliente-core-prod",
          "awslogs-region": "sa-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8081/api/clientes/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

Registrar a task definition:

```bash
# Criar CloudWatch Log Group primeiro
aws logs create-log-group \
  --log-group-name /ecs/cliente-core-prod \
  --region sa-east-1

# Registrar task definition
aws ecs register-task-definition \
  --cli-input-json file://cliente-core-task-def.json \
  --region sa-east-1
```

#### Passo 4: Criar ECS Service

```bash
# Criar ECS Cluster (se não existe)
aws ecs create-cluster \
  --cluster-name cliente-core-prod-cluster \
  --region sa-east-1

# Criar ECS Service
aws ecs create-service \
  --cluster cliente-core-prod-cluster \
  --service-name cliente-core-prod-service \
  --task-definition cliente-core-prod \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-zzzzz],assignPublicIp=ENABLED}" \
  --region sa-east-1
```

**⚠️ IMPORTANTE:** Substitua:
- `subnet-xxxxx,subnet-yyyyy`: IDs das suas subnets privadas
- `sg-zzzzz`: Security group que permite:
  - Inbound: porta 8081 (ou só do ALB)
  - Outbound: porta 5432 para RDS, porta 443 para Secrets Manager

#### Passo 5: Configurar Security Groups

**Security Group do ECS (sg-zzzzz):**
```bash
# Permitir tráfego do ALB (se usar Load Balancer)
aws ec2 authorize-security-group-ingress \
  --group-id sg-zzzzz \
  --protocol tcp \
  --port 8081 \
  --source-group sg-alb-xxxxx \
  --region sa-east-1

# Permitir saída para RDS
aws ec2 authorize-security-group-egress \
  --group-id sg-zzzzz \
  --protocol tcp \
  --port 5432 \
  --cidr 10.0.0.0/16 \
  --region sa-east-1

# Permitir saída HTTPS para Secrets Manager
aws ec2 authorize-security-group-egress \
  --group-id sg-zzzzz \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0 \
  --region sa-east-1
```

**Security Group do RDS:**
```bash
# Obter o security group do RDS
RDS_SG=$(aws rds describe-db-instances \
  --db-instance-identifier cliente-core-prod \
  --region sa-east-1 \
  --query 'DBInstances[0].VpcSecurityGroups[0].VpcSecurityGroupId' \
  --output text)

# Permitir acesso do ECS ao RDS
aws ec2 authorize-security-group-ingress \
  --group-id $RDS_SG \
  --protocol tcp \
  --port 5432 \
  --source-group sg-zzzzz \
  --region sa-east-1
```

#### Passo 6: Verificar Logs

```bash
# Ver logs do container em tempo real
aws logs tail /ecs/cliente-core-prod --follow --region sa-east-1

# Procurar por estas linhas (indica SUCESSO):
# INFO AwsSecretsManagerConfig - Successfully fetched database credentials
# INFO HikariPool - HikariPool-1 - Start completed.
# INFO Tomcat started on port(s): 8081 (http)
```

#### Passo 7: Testar Aplicação

```bash
# Se o ECS tem IP público, testar diretamente
PUBLIC_IP=$(aws ecs describe-tasks \
  --cluster cliente-core-prod-cluster \
  --tasks $(aws ecs list-tasks --cluster cliente-core-prod-cluster --region sa-east-1 --query 'taskArns[0]' --output text) \
  --region sa-east-1 \
  --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' \
  --output text)

# Health check
curl http://$PUBLIC_IP:8081/api/clientes/actuator/health

# Resultado esperado:
# {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},...}}
```

---

### Opção B: Deploy via Terraform (Recomendado para produção)

Vou criar um módulo Terraform para deploy completo do ECS.

**Aguarde - vou criar o módulo agora...**

---

## 🔍 Troubleshooting

### Erro: "Application won't start - connection refused"

**Verificar:**
```bash
# 1. Security groups permitem ECS → RDS
aws ec2 describe-security-groups --group-ids sg-zzzzz --region sa-east-1

# 2. RDS está acessível da subnet do ECS
aws rds describe-db-instances \
  --db-instance-identifier cliente-core-prod \
  --region sa-east-1 \
  --query 'DBInstances[0].[PubliclyAccessible,VpcSecurityGroups]'

# 3. Verificar logs do container
aws logs tail /ecs/cliente-core-prod --follow --region sa-east-1
```

### Erro: "Access Denied to Secrets Manager"

**Verificar:**
```bash
# 1. Task Role está correto na task definition
aws ecs describe-task-definition \
  --task-definition cliente-core-prod \
  --region sa-east-1 \
  --query 'taskDefinition.taskRoleArn'

# Deve retornar: arn:aws:iam::530184476864:role/cliente-core-secrets-role-prod

# 2. IAM Role tem a policy correta
aws iam get-role-policy \
  --role-name cliente-core-secrets-role-prod \
  --policy-name SecretsManagerReadPolicy
```

### Erro: "Secret not found"

**Verificar:**
```bash
# 1. Secret existe na região correta
aws secretsmanager list-secrets --region sa-east-1 | grep cliente-core

# 2. Environment variable está correta
# Ver task definition: AWS_SECRETS_NAME deve ser "cliente-core/prod/database"
```

---

## 📊 Monitoramento

### CloudWatch Logs

```bash
# Ver logs em tempo real
aws logs tail /ecs/cliente-core-prod --follow --region sa-east-1

# Buscar erros
aws logs filter-log-events \
  --log-group-name /ecs/cliente-core-prod \
  --filter-pattern "ERROR" \
  --region sa-east-1
```

### CloudWatch Metrics

```bash
# CPU utilization do ECS service
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name CPUUtilization \
  --dimensions Name=ServiceName,Value=cliente-core-prod-service Name=ClusterName,Value=cliente-core-prod-cluster \
  --start-time 2025-11-05T00:00:00Z \
  --end-time 2025-11-05T23:59:59Z \
  --period 300 \
  --statistics Average \
  --region sa-east-1
```

### Application Metrics (Prometheus)

```bash
# Exposto via Actuator
curl http://$PUBLIC_IP:8081/api/clientes/actuator/prometheus
```

---

## 🔄 Rotação de Senhas (A cada 90 dias)

```bash
# 1. Gerar nova senha
NEW_PASSWORD=$(openssl rand -base64 24)

# 2. Atualizar RDS
aws rds modify-db-instance \
  --db-instance-identifier cliente-core-prod \
  --master-user-password "$NEW_PASSWORD" \
  --apply-immediately \
  --region sa-east-1

# 3. Atualizar secret no Secrets Manager
aws secretsmanager update-secret \
  --secret-id cliente-core/prod/database \
  --secret-string "{\"host\":\"cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com\",\"port\":5432,\"database\":\"vanessa_mudanca_clientes\",\"username\":\"dbadmin\",\"password\":\"$NEW_PASSWORD\",\"url\":\"jdbc:postgresql://cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com:5432/vanessa_mudanca_clientes\"}" \
  --region sa-east-1

# 4. Reiniciar ECS service (vai buscar nova senha)
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment \
  --region sa-east-1
```

---

## 📝 Valores Importantes (Para Referência)

```bash
# RDS
export RDS_ENDPOINT="cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com"
export RDS_PORT="5432"
export RDS_DATABASE="vanessa_mudanca_clientes"
export RDS_USERNAME="dbadmin"

# Secrets Manager
export SECRET_DB_NAME="cliente-core/prod/database"
export SECRET_DB_ARN="arn:aws:secretsmanager:sa-east-1:530184476864:secret:cliente-core/prod/database-xkfVWU"
export SECRET_JWT_NAME="cliente-core/prod/jwt-key"
export SECRET_JWT_ARN="arn:aws:secretsmanager:sa-east-1:530184476864:secret:cliente-core/prod/jwt-key-EP9wIV"

# IAM
export TASK_ROLE_ARN="arn:aws:iam::530184476864:role/cliente-core-secrets-role-prod"

# AWS
export AWS_REGION="sa-east-1"
export AWS_ACCOUNT="530184476864"
```

---

**Última Atualização:** 2025-11-05
**Status:** ✅ Pronto para Deploy no ECS
**Próximo Passo:** Criar ECS Task Definition e Service (Passo 3-4 acima)
