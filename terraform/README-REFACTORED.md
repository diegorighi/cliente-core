# cliente-core Infrastructure (Refactored)

## Overview

This directory contains **microservice-specific infrastructure** for cliente-core. It consumes shared infrastructure (VPC, ECR, KMS) via Terraform remote state.

**Owner:** Development Team
**Layer:** Microservice Infrastructure
**Dependencies:** infra-shared (must be deployed first)

---

## What's Included (Microservice-Specific)

### Database
- **RDS PostgreSQL 16:** Dedicated database instance for cliente-core
- **Multi-AZ:** Optional high availability
- **Automated Backups:** 7-day retention (configurable)
- **Encryption:** Using shared KMS key

### Compute
- **ECS Fargate:** Serverless container orchestration
- **Auto-scaling:** CPU and memory-based scaling
- **CloudWatch Logs:** Structured logging with JSON

### Load Balancer
- **Application Load Balancer:** HTTP/HTTPS traffic distribution
- **Target Group:** Health check configuration
- **SSL/TLS:** Certificate integration (when configured)

### Security
- **Security Groups:** ALB, ECS, RDS (microservice-specific)
- **IAM Roles:** ECS task execution and task roles
- **Secrets Manager:** Database credentials with automatic rotation

---

## What's NOT Included (In Shared Infrastructure)

- VPC, subnets, NAT Gateway, Internet Gateway → `infra-shared`
- ECR repository → `infra-shared`
- KMS encryption key → `infra-shared`
- Base security groups → `infra-shared`

**See:** [infra-shared/INTEGRATION.md](../../infra-shared/INTEGRATION.md) for integration guide.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│ Shared Infrastructure (infra-shared)                                │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐                   │
│ │ VPC     │ │ Subnets │ │ ECR     │ │ KMS Key │                   │
│ └─────────┘ └─────────┘ └─────────┘ └─────────┘                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↓ (Consumed via remote state)
┌─────────────────────────────────────────────────────────────────────┐
│ cliente-core Infrastructure (this terraform)                        │
│                                                                     │
│  ┌──────────────────┐                                              │
│  │ Application      │                                              │
│  │ Load Balancer    │ ← Public subnets (from shared)               │
│  └────────┬─────────┘                                              │
│           │                                                         │
│  ┌────────▼──────────────┐                                         │
│  │ ECS Fargate Service   │                                         │
│  │ - Task Definition     │ ← Private subnets (from shared)         │
│  │ - Auto Scaling        │ ← ECR image (from shared)               │
│  │ - CloudWatch Logs     │                                         │
│  └────────┬──────────────┘                                         │
│           │                                                         │
│  ┌────────▼──────────────┐                                         │
│  │ RDS PostgreSQL 16     │                                         │
│  │ - db.t4g.micro        │ ← Database subnets (from shared)        │
│  │ - 20 GB storage       │ ← KMS encryption (from shared)          │
│  │ - Automated backups   │                                         │
│  └───────────────────────┘                                         │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

### 1. Shared Infrastructure Deployed

```bash
# Verify shared infrastructure exists
cd /path/to/infra-shared
terraform output

# Should show: vpc_id, public_subnet_ids, ecr_repositories, etc.
```

If not deployed, deploy shared infrastructure first:
```bash
cd /path/to/infra-shared
terraform init
terraform apply
```

### 2. Tools Installed

- Terraform >= 1.6.0
- AWS CLI configured
- Docker (for building images)

---

## Directory Structure

```
cliente-core/terraform/
├── README-REFACTORED.md       # This file
├── main-refactored.tf         # Root configuration (consumes shared state)
├── variables.tf               # Input variables (MS-specific)
├── outputs.tf                 # Outputs (ALB DNS, RDS endpoint, etc.)
├── terraform.tfvars.example   # Example configuration
└── modules/
    ├── database/              # RDS PostgreSQL (MS-specific)
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── compute/               # ECS Fargate (MS-specific)
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── loadbalancer/          # ALB (MS-specific)
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── security/              # Security groups, IAM, Secrets (MS-specific)
        ├── main-refactored.tf
        ├── variables-refactored.tf
        └── outputs-refactored.tf
```

---

## Key Changes from Monolithic Pattern

### What Was Removed
- ❌ `modules/vpc/` - Now in infra-shared
- ❌ ECR repository creation in `modules/security/` - Now in infra-shared
- ❌ KMS key creation - Now in infra-shared

### What Was Added
- ✅ `terraform_remote_state` data source for shared infrastructure
- ✅ References to shared VPC, subnets, ECR, KMS
- ✅ Cleaner separation of concerns

### What Stayed the Same
- ✅ RDS module (microservice-specific)
- ✅ ECS module (microservice-specific)
- ✅ ALB module (microservice-specific)
- ✅ Security groups and IAM roles (microservice-specific)

---

## Configuration

### 1. Copy Example Configuration

```bash
cd cliente-core/terraform
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars
```

### 2. Key Configuration Variables

```hcl
# General
project_name = "cliente-core"
environment  = "prod"  # dev, staging, or prod
aws_region   = "us-east-1"

# RDS Configuration
db_instance_class        = "db.t4g.micro"   # db.t3.micro for Free Tier
db_allocated_storage     = 20
db_max_allocated_storage = 100
db_multi_az              = false  # true for production HA

# ECS Configuration
ecs_task_cpu     = 512   # 0.5 vCPU
ecs_task_memory  = 1024  # 1 GB
ecs_desired_count = 2    # 1 for dev, 2+ for production
ecs_min_capacity  = 2
ecs_max_capacity  = 4

# ALB Health Check
health_check_path = "/api/clientes/actuator/health"
```

---

## Deployment

### 1. Initialize Terraform

```bash
cd cliente-core/terraform
terraform init
```

This will:
- Download AWS provider
- Configure S3 backend
- Load remote state from infra-shared

### 2. Review Plan

```bash
terraform plan -out=tfplan
```

**Verify:**
- No changes to shared infrastructure (VPC, ECR, KMS)
- Only cliente-core resources are created (RDS, ECS, ALB)
- Security groups reference correct VPC ID

### 3. Apply Infrastructure

```bash
terraform apply tfplan
```

**Expected resources created:**
- 1 RDS PostgreSQL instance (with subnet group)
- 1 Secrets Manager secret (database credentials)
- 1 ECS cluster
- 1 ECS task definition
- 1 ECS service (with auto-scaling)
- 1 Application Load Balancer
- 1 ALB target group
- 3-5 Security groups (ALB, ECS, RDS)
- 2-3 IAM roles (ECS task execution, ECS task)
- 2-3 CloudWatch log groups

**Duration:** ~10-15 minutes (RDS takes the longest)

### 4. Verify Deployment

```bash
# Get ALB DNS name
terraform output alb_dns_name

# Test health endpoint
curl http://<alb-dns-name>/api/clientes/actuator/health

# Get RDS endpoint
terraform output rds_endpoint

# Get ECR repository URL (from shared)
terraform output ecr_repository_url
```

---

## Building and Deploying Application

### 1. Build Docker Image

```bash
cd /path/to/cliente-core

# Build Spring Boot application
mvn clean package -DskipTests

# Build Docker image
docker build -t cliente-core:latest .
```

### 2. Push to ECR

```bash
# Get ECR repository URL from shared infrastructure
ECR_URL=$(terraform output -state=../../infra-shared/terraform.tfstate -json ecr_repositories | jq -r '.["cliente-core"]')

# Authenticate Docker to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ECR_URL

# Tag image
docker tag cliente-core:latest $ECR_URL:latest

# Push image
docker push $ECR_URL:latest
```

### 3. Update ECS Service

```bash
# ECS will automatically pull new image on next deployment
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment \
  --region us-east-1
```

---

## Integration with Shared Infrastructure

### Consuming Shared Resources

```hcl
# main-refactored.tf

data "terraform_remote_state" "shared" {
  backend = "s3"
  config = {
    bucket = "vanessa-mudanca-terraform-state"
    key    = "infra-shared/terraform.tfstate"
    region = "us-east-1"
  }
}

# Use shared VPC
module "security" {
  source = "./modules/security"
  vpc_id = data.terraform_remote_state.shared.outputs.vpc_id
}

# Use shared database subnets
module "rds" {
  source        = "./modules/rds"
  db_subnet_ids = data.terraform_remote_state.shared.outputs.database_subnet_ids
}

# Use shared ECR repository
module "ecs" {
  source = "./modules/ecs"
  ecr_repository_url = data.terraform_remote_state.shared.outputs.ecr_repositories["cliente-core"]
}

# Use shared KMS key
module "rds" {
  source      = "./modules/rds"
  kms_key_arn = data.terraform_remote_state.shared.outputs.kms_key_arn
}
```

---

## Cost Breakdown

### Monthly Costs (us-east-1, estimated)

| Resource | Dev | Staging | Prod | Notes |
|----------|-----|---------|------|-------|
| **RDS PostgreSQL** | $12-15 | $12-15 | $25-50 | db.t3.micro (Free Tier), db.t4g.micro (MVP), db.t4g.small (Prod) |
| **ECS Fargate** | $15-20 | $30-40 | $60-120 | 1 task (dev), 2 tasks (staging/prod), 0.5 vCPU, 1 GB RAM |
| **ALB** | $16-20 | $16-20 | $16-20 | Base cost + LCU charges |
| **CloudWatch Logs** | $2-5 | $5-10 | $10-20 | 7 days retention (dev), 30 days (prod) |
| **Data Transfer** | $5-10 | $10-20 | $20-50 | NAT Gateway, ALB, ECS |
| **Secrets Manager** | $0.40 | $0.40 | $0.40 | $0.40/secret per month |
| **TOTAL** | **~$51-70** | **~$74-105** | **~$132-260** | Excludes shared infrastructure costs |

**Shared infrastructure costs** (~$47-63 for dev, ~$168-298 for prod) are split across all microservices.

---

## Monitoring

### CloudWatch Dashboards

```bash
# View ECS metrics
aws cloudwatch get-dashboard \
  --dashboard-name cliente-core-prod

# View RDS metrics
aws cloudwatch get-dashboard \
  --dashboard-name cliente-core-prod-rds
```

### CloudWatch Alarms

- **RDS CPU > 80%:** Triggers SNS notification
- **RDS Free Storage < 5 GB:** Triggers SNS notification
- **ECS CPU > 70%:** Triggers auto-scaling
- **ECS Memory > 80%:** Triggers auto-scaling
- **ALB 5xx errors > 10:** Triggers SNS notification

### CloudWatch Insights Queries

```sql
# Find all ERROR logs in last hour
fields @timestamp, @message, level, correlationId
| filter level = "ERROR"
| sort @timestamp desc
| limit 100

# Find slow database queries
fields @timestamp, @message, duration
| filter @message like /query took/
| parse @message "query took * ms" as duration
| filter duration > 1000
| sort duration desc
```

---

## Maintenance

### Database Backup/Restore

```bash
# Manual backup
aws rds create-db-snapshot \
  --db-instance-identifier cliente-core-prod-rds \
  --db-snapshot-identifier cliente-core-prod-manual-backup-$(date +%Y%m%d)

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier cliente-core-prod-rds-restored \
  --db-snapshot-identifier cliente-core-prod-manual-backup-20251104
```

### Scaling

```bash
# Scale ECS tasks manually
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --desired-count 4

# Scale RDS instance (requires downtime)
terraform apply -var="db_instance_class=db.t4g.small"
```

### Updating Configuration

```bash
# Edit terraform.tfvars
vim terraform.tfvars

# Review changes
terraform plan

# Apply changes
terraform apply
```

---

## Disaster Recovery

### Complete Infrastructure Loss

1. **Restore Terraform state from S3**
   ```bash
   aws s3 cp s3://vanessa-mudanca-terraform-state/cliente-core/terraform.tfstate .
   ```

2. **Restore database from snapshot**
   ```bash
   aws rds restore-db-instance-from-db-snapshot \
     --db-instance-identifier cliente-core-prod-rds \
     --db-snapshot-identifier <latest-snapshot>
   ```

3. **Redeploy infrastructure**
   ```bash
   terraform init
   terraform apply
   ```

4. **Deploy application**
   ```bash
   # Push Docker image to ECR
   # Update ECS service
   ```

---

## Troubleshooting

### Issue: "Error reading remote state"

**Cause:** Shared infrastructure not deployed or state file missing.

**Solution:**
```bash
# Verify shared infrastructure exists
aws s3 ls s3://vanessa-mudanca-terraform-state/infra-shared/terraform.tfstate

# If not, deploy shared infrastructure first
cd ../../infra-shared
terraform init
terraform apply
```

### Issue: ECS tasks failing to start

**Causes:**
1. ECR image not found
2. Database connection failure
3. Secrets Manager access denied

**Solution:**
```bash
# Check ECS task logs
aws logs tail /ecs/cliente-core-prod --follow

# Verify ECR image exists
aws ecr describe-images --repository-name cliente-core-prod

# Test database connectivity
psql -h <rds-endpoint> -U clientecore_admin -d vanessa_mudanca_clientes
```

### Issue: High costs

**Solution:**
- Set `db_multi_az = false` for non-production
- Reduce `ecs_desired_count` to 1 for dev
- Set `cloudwatch_retention_days = 7` for dev
- Enable VPC Endpoints to reduce NAT Gateway data transfer

---

## Security Best Practices

- ✅ Database credentials stored in Secrets Manager (encrypted with KMS)
- ✅ RDS in database subnets (no internet access)
- ✅ ECS tasks in private subnets (internet via NAT Gateway)
- ✅ Security groups follow least privilege principle
- ✅ IAM roles scoped to specific resources
- ✅ CloudWatch Logs encrypted with KMS
- ✅ VPC Flow Logs enabled (in shared infrastructure)

---

## Related Documentation

- [infra-shared/README.md](../../infra-shared/README.md) - Shared infrastructure overview
- [infra-shared/INTEGRATION.md](../../infra-shared/INTEGRATION.md) - Integration guide
- [../docs/INTEGRATION_ARCHITECTURE.md](../docs/INTEGRATION_ARCHITECTURE.md) - Step Functions + Kafka integration

---

## Support

**Owner:** Development Team (cliente-core)
**Slack:** #cliente-core
**On-Call:** See PagerDuty rotation

**Infrastructure Questions:**
- DevOps Team: `#devops-infrastructure`

---

**Last Updated:** 2025-11-04
**Version:** 1.0.0 (Refactored)
