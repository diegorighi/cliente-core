# Terraform Infrastructure - cliente-core

Complete AWS infrastructure for the cliente-core Spring Boot microservice MVP.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Cost Estimate](#cost-estimate)
- [Quick Start](#quick-start)
- [Deployment Steps](#deployment-steps)
- [Configuration](#configuration)
- [Outputs](#outputs)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Rollback Procedures](#rollback-procedures)
- [Production Readiness](#production-readiness)
- [Security Considerations](#security-considerations)
- [Maintenance](#maintenance)

---

## Overview

This Terraform configuration deploys a production-ready, highly available infrastructure for the cliente-core microservice on AWS.

**What gets deployed:**
- VPC with multi-AZ architecture (2 AZs)
- RDS PostgreSQL 16 database (db.t4g.micro)
- ECS Fargate cluster with auto-scaling (2-4 tasks)
- Application Load Balancer (public-facing)
- Security groups with least privilege
- Secrets Manager for database credentials
- CloudWatch logging and monitoring
- ECR repository for Docker images

**Key features:**
- High availability (multi-AZ ALB and ECS)
- Auto-scaling based on CPU/memory
- Automated backups (7-day retention)
- Container Insights enabled
- Performance Insights enabled
- CloudWatch alarms for critical metrics

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Internet                              │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application Load Balancer                  │
│                    (Public Subnets)                          │
│              10.0.1.0/24, 10.0.2.0/24                       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               ECS Fargate Tasks (2-4 instances)              │
│                   (Private Subnets)                          │
│              10.0.11.0/24, 10.0.12.0/24                     │
│                                                              │
│  ┌──────────────────────────────────────────────────┐      │
│  │  Spring Boot 3.5.7 + Java 21                     │      │
│  │  Port: 8080                                       │      │
│  │  CPU: 512 units (0.5 vCPU)                       │      │
│  │  Memory: 1024 MB (1 GB)                          │      │
│  └──────────────────────────────────────────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              RDS PostgreSQL 16 (db.t4g.micro)               │
│                  (Database Subnets)                          │
│              10.0.21.0/24, 10.0.22.0/24                     │
│                                                              │
│  Storage: 20 GB gp3 (auto-scales to 100 GB)                │
│  Backups: 7 days retention                                  │
│  Multi-AZ: Disabled (MVP cost optimization)                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Supporting Services                       │
├─────────────────────────────────────────────────────────────┤
│  • Secrets Manager (database credentials)                   │
│  • CloudWatch Logs (/ecs/cliente-core-prod)                │
│  • CloudWatch Alarms (CPU, Memory, 5xx, Latency)           │
│  • ECR Repository (cliente-core-prod)                       │
│  • NAT Gateway (single, for cost optimization)              │
└─────────────────────────────────────────────────────────────┘
```

**Network segmentation:**
- **Public subnets:** ALB only (internet-facing)
- **Private subnets:** ECS tasks (no direct internet access)
- **Database subnets:** RDS only (isolated, no internet)

**Security:**
- ALB SG: Allow HTTP (80) from internet
- ECS SG: Allow 8080 from ALB only
- RDS SG: Allow 5432 from ECS only

---

## Prerequisites

### Required Tools

1. **Terraform** >= 1.6.0
   ```bash
   terraform --version
   ```
   Install: https://developer.hashicorp.com/terraform/install

2. **AWS CLI** >= 2.0
   ```bash
   aws --version
   ```
   Install: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html

3. **Docker** (for building images)
   ```bash
   docker --version
   ```
   Install: https://docs.docker.com/get-docker/

### AWS Credentials

Configure AWS credentials with appropriate permissions:

```bash
aws configure
```

**Required IAM permissions:**
- VPC, Subnet, Route Table, NAT Gateway, Internet Gateway
- RDS, DB Subnet Group, DB Parameter Group
- ECS, ECR
- ALB, Target Group, Listener
- Security Groups
- IAM Roles and Policies
- Secrets Manager
- CloudWatch Logs and Alarms

**Recommended:** Use an IAM user with `AdministratorAccess` for initial setup, then restrict to least privilege.

### Backend Setup (Optional but Recommended)

Create S3 bucket and DynamoDB table for remote state:

```bash
# Create S3 bucket for Terraform state
aws s3api create-bucket \
  --bucket vanessa-mudanca-terraform-state \
  --region us-east-1

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket vanessa-mudanca-terraform-state \
  --versioning-configuration Status=Enabled

# Enable encryption
aws s3api put-bucket-encryption \
  --bucket vanessa-mudanca-terraform-state \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# Create DynamoDB table for state locking
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

Then uncomment the `backend "s3"` block in `main.tf`.

---

## Cost Estimate

### Monthly Costs - Free Tier Configuration (First 12 Months)

**AWS Free Tier includes (12 months from account creation):**
- RDS: 750 hours/month of db.t3.micro (Single-AZ)
- EC2: 750 hours/month of t3.micro (not used, we use Fargate)
- S3: 5GB standard storage
- CloudWatch: 10 custom metrics, 5GB logs
- Data Transfer: 100GB/month outbound
- ALB: 750 hours/month + 15GB processed

| Service | Specification | Free Tier | Monthly Cost (USD) |
|---------|--------------|-----------|-------------------|
| **RDS PostgreSQL** | db.t3.micro, 20 GB gp3, Single-AZ | 750h free | **$0** |
| **ECS Fargate** | 1 task × 256 CPU × 512 MB × 730 hrs | Not covered | $15-18 |
| **Application Load Balancer** | 1 ALB + data transfer | 750h + 15GB free | $0-5 |
| **NAT Gateway** | 1 NAT Gateway + data transfer | Not covered | $16-20 |
| **CloudWatch Logs** | 2-3 GB/month, 3-day retention | 5GB free | **$0** |
| **Secrets Manager** | 1 secret | Not covered | $0.40 |
| **ECR** | 3 images × 250 MB avg | 500MB free | **$0** |
| **Data Transfer** | Outbound to internet (2-5 GB) | 100GB free | **$0** |
| **Backups** | RDS snapshots (1 day) | Included | **$0** |

**Total Estimated Cost with Free Tier: $35-45 per month** ⭐

**After Free Tier expires (month 13+):** $55-70/month
- RDS db.t3.micro: +$12-15

### Monthly Costs - Standard MVP Configuration (No Free Tier)

| Service | Specification | Monthly Cost (USD) |
|---------|--------------|-------------------|
| **RDS PostgreSQL** | db.t4g.micro, 20 GB gp3, Single-AZ | $12-15 |
| **ECS Fargate** | 2 tasks × 512 CPU × 1024 MB × 730 hrs | $25-30 |
| **Application Load Balancer** | 1 ALB + data transfer | $16-20 |
| **NAT Gateway** | 1 NAT Gateway + data transfer | $32-40 |
| **CloudWatch Logs** | 5 GB/month, 7-day retention | $2-5 |
| **Secrets Manager** | 1 secret | $0.40 |
| **ECR** | 5 images × 500 MB avg | $0.50 |
| **Data Transfer** | Outbound to internet (5 GB) | $0.45 |
| **Backups** | RDS snapshots (7 days) | $2-3 |

**Total Estimated Cost: $90-115 per month**

### How to Enable Free Tier

Use the `terraform.tfvars.freetier` configuration:

```bash
cp terraform.tfvars.freetier terraform.tfvars
# Edit terraform.tfvars and set:
# enable_free_tier = true
# db_instance_class = "db.t3.micro"
# ecs_desired_count = 1
# ecs_task_cpu = 256
# ecs_task_memory = 512
```

**Free Tier optimizations:**
- RDS: db.t3.micro instead of db.t4g.micro (750h free)
- ECS: 1 task instead of 2 (reduces Fargate cost by 50%)
- CPU/Memory: Minimal (256 CPU, 512 MB)
- Logs: 3-day retention instead of 7 days
- Backups: 1-day retention instead of 7 days

### Cost Optimization Opportunities

**Immediate savings (MVP):**
- **Use Free Tier config:** Save $50-70/month for first 12 months
- Single NAT Gateway instead of multi-AZ (-$32/month)
- Single-AZ RDS instead of Multi-AZ (-$12/month)
- 7-day log retention instead of 30 days (-$5/month)

**Future savings (production):**
- **RDS Reserved Instances:** 40% savings (~$200/year)
- **Fargate Savings Plans:** 50% savings (~$180/year)
- **CloudWatch Logs compression:** 30% savings
- **S3 lifecycle policies:** Move old backups to Glacier

**Scaling costs:**
- Auto-scaling to 4 tasks: +$25-30/month
- Upgrade to db.t4g.small: +$12-15/month
- Multi-AZ RDS: +$12-15/month
- Second NAT Gateway (HA): +$32-40/month

**Expected production cost:** $250-350/month with HA configuration

---

## Quick Start

### 1. Clone and Navigate

```bash
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core/terraform
```

### 2. Create Configuration File

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` if you want to customize (or use defaults):

```hcl
project_name = "cliente-core"
environment  = "prod"
aws_region   = "us-east-1"
```

### 3. Initialize Terraform

```bash
terraform init
```

### 4. Preview Changes

```bash
terraform plan
```

### 5. Deploy Infrastructure

```bash
terraform apply
```

Type `yes` when prompted.

**Deployment time:** ~15-20 minutes (RDS takes longest)

### 6. Build and Push Docker Image

```bash
# Get ECR repository URL from Terraform output
ECR_URL=$(terraform output -raw ecr_repository_url)

# Authenticate Docker to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URL

# Build application
cd ..
mvn clean package -DskipTests

# Build Docker image
docker build -t cliente-core .

# Tag and push
docker tag cliente-core:latest $ECR_URL:latest
docker push $ECR_URL:latest
```

### 7. Verify Deployment

```bash
# Get ALB DNS name
ALB_DNS=$(terraform output -raw alb_dns_name)

# Wait for service to be healthy (may take 2-3 minutes)
sleep 120

# Test health endpoint
curl http://$ALB_DNS/api/clientes/actuator/health

# Expected response:
# {"status":"UP"}
```

### 8. Access Application

```bash
echo "Application URL: http://$(terraform output -raw alb_dns_name)/api/clientes"
```

---

## Deployment Steps

### Detailed Deployment Workflow

#### Phase 1: Infrastructure Provisioning

```bash
# 1. Initialize Terraform
terraform init

# 2. Validate configuration
terraform validate

# 3. Format code (optional)
terraform fmt -recursive

# 4. Plan deployment
terraform plan -out=tfplan

# 5. Review plan carefully
# Check resource counts, security groups, cost estimates

# 6. Apply infrastructure
terraform apply tfplan

# 7. Save outputs
terraform output > outputs.txt
```

#### Phase 2: Application Deployment

```bash
# 1. Get ECR URL
ECR_URL=$(terraform output -raw ecr_repository_url)

# 2. Authenticate to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URL

# 3. Build Spring Boot JAR
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
mvn clean package -DskipTests

# 4. Build Docker image
docker build -t cliente-core:latest .

# 5. Tag for ECR
docker tag cliente-core:latest $ECR_URL:latest

# 6. Push to ECR
docker push $ECR_URL:latest

# 7. Force new ECS deployment
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment \
  --region us-east-1
```

#### Phase 3: Verification

```bash
# 1. Check ECS service status
aws ecs describe-services \
  --cluster cliente-core-prod-cluster \
  --services cliente-core-prod-service \
  --region us-east-1

# 2. Check task status
aws ecs list-tasks \
  --cluster cliente-core-prod-cluster \
  --service-name cliente-core-prod-service \
  --region us-east-1

# 3. View logs
aws logs tail /ecs/cliente-core-prod --follow

# 4. Test health endpoint
ALB_DNS=$(cd terraform && terraform output -raw alb_dns_name)
curl -v http://$ALB_DNS/api/clientes/actuator/health

# 5. Check database connectivity
# (Health check will fail if database is unreachable)
```

---

## Configuration

### Key Variables

Edit `terraform.tfvars` to customize:

**Environment:**
```hcl
environment = "prod" # dev, staging, prod
```

**Region:**
```hcl
aws_region = "us-east-1"
```

**Database sizing:**
```hcl
db_instance_class = "db.t4g.micro" # Upgrade to db.t4g.small for production
db_allocated_storage = 20          # Initial size in GB
db_max_allocated_storage = 100     # Auto-scale up to 100 GB
```

**ECS sizing:**
```hcl
ecs_task_cpu    = 512  # 0.5 vCPU
ecs_task_memory = 1024 # 1 GB

ecs_desired_count = 2  # Initial task count
ecs_min_capacity  = 2  # Min for auto-scaling
ecs_max_capacity  = 4  # Max for auto-scaling
```

**Auto-scaling thresholds:**
```hcl
autoscaling_target_cpu    = 70 # Scale up when CPU > 70%
autoscaling_target_memory = 80 # Scale up when Memory > 80%
```

**Health check:**
```hcl
health_check_path     = "/api/clientes/actuator/health"
health_check_interval = 30  # seconds
health_check_timeout  = 5   # seconds
healthy_threshold     = 2   # consecutive successes
unhealthy_threshold   = 3   # consecutive failures
```

### Environment-Specific Configurations

**Development:**
```hcl
environment = "dev"
db_instance_class = "db.t4g.micro"
ecs_desired_count = 1
ecs_min_capacity = 1
ecs_max_capacity = 2
single_nat_gateway = true
db_multi_az = false
```

**Staging:**
```hcl
environment = "staging"
db_instance_class = "db.t4g.small"
ecs_desired_count = 2
ecs_min_capacity = 2
ecs_max_capacity = 4
single_nat_gateway = true
db_multi_az = false
```

**Production:**
```hcl
environment = "prod"
db_instance_class = "db.r6g.large"
ecs_desired_count = 4
ecs_min_capacity = 4
ecs_max_capacity = 10
single_nat_gateway = false  # 2 NAT Gateways for HA
db_multi_az = true          # Multi-AZ RDS
```

---

## Outputs

After deployment, Terraform provides these outputs:

```bash
# View all outputs
terraform output

# View specific output
terraform output alb_dns_name
```

**Key outputs:**

| Output | Description | Example |
|--------|-------------|---------|
| `alb_dns_name` | ALB DNS name (use to access app) | `cliente-core-prod-alb-123456789.us-east-1.elb.amazonaws.com` |
| `application_url` | Full application URL | `http://...elb.amazonaws.com/api/clientes` |
| `health_check_url` | Health check endpoint | `http://...elb.amazonaws.com/api/clientes/actuator/health` |
| `ecr_repository_url` | ECR URL for docker push | `123456789012.dkr.ecr.us-east-1.amazonaws.com/cliente-core-prod` |
| `rds_endpoint` | Database endpoint (sensitive) | `cliente-core-prod-db.abc123.us-east-1.rds.amazonaws.com:5432` |
| `database_secret_arn` | Secrets Manager ARN | `arn:aws:secretsmanager:us-east-1:123456789012:secret:...` |

---

## Monitoring

### CloudWatch Dashboards

Create a custom dashboard:

```bash
aws cloudwatch put-dashboard \
  --dashboard-name cliente-core-prod \
  --dashboard-body file://dashboard.json
```

**Metrics to monitor:**
- ECS CPU/Memory utilization
- ALB request count and latency
- ALB 5xx error rate
- RDS CPU, memory, storage, connections
- NAT Gateway data transfer

### CloudWatch Alarms

**Deployed alarms:**

| Alarm | Threshold | Action |
|-------|-----------|--------|
| `cliente-core-prod-db-cpu-high` | CPU > 80% for 10 min | Alert |
| `cliente-core-prod-db-memory-low` | Free memory < 100 MB | Alert |
| `cliente-core-prod-db-storage-low` | Free storage < 2 GB | Alert |
| `cliente-core-prod-db-connections-high` | Connections > 80 | Alert |
| `cliente-core-prod-ecs-cpu-high` | CPU > 85% for 10 min | Alert |
| `cliente-core-prod-ecs-memory-high` | Memory > 90% for 10 min | Alert |
| `cliente-core-prod-alb-high-response-time` | Latency > 1s for 10 min | Alert |
| `cliente-core-prod-alb-unhealthy-targets` | Unhealthy count > 0 | Alert |
| `cliente-core-prod-alb-http-5xx` | 5xx count > 10 in 5 min | Alert |

### Viewing Logs

**ECS application logs:**
```bash
# Tail logs (live)
aws logs tail /ecs/cliente-core-prod --follow

# Filter by error
aws logs tail /ecs/cliente-core-prod --filter-pattern "ERROR"

# Specific time range
aws logs tail /ecs/cliente-core-prod --since 1h
```

**RDS logs:**
```bash
# List log files
aws rds describe-db-log-files \
  --db-instance-identifier cliente-core-prod-db

# Download log file
aws rds download-db-log-file-portion \
  --db-instance-identifier cliente-core-prod-db \
  --log-file-name error/postgresql.log.2025-11-04-00
```

### CloudWatch Insights Queries

**Find errors in application logs:**
```sql
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc
| limit 100
```

**Count requests by status code:**
```sql
fields @timestamp, @message
| stats count() by status_code
| sort count desc
```

**P99 latency:**
```sql
fields @timestamp, duration
| stats percentile(duration, 99) as p99
```

---

## Troubleshooting

### Common Issues

#### 1. ECS Tasks Failing Health Checks

**Symptoms:**
- Tasks start but immediately fail health check
- Service keeps restarting tasks

**Diagnosis:**
```bash
# Check task logs
aws logs tail /ecs/cliente-core-prod --follow

# Check task status
aws ecs describe-tasks \
  --cluster cliente-core-prod-cluster \
  --tasks <task-id>
```

**Common causes:**
- Database connection failed (wrong credentials, security group)
- Liquibase migrations timeout (increase `startPeriod` in task definition)
- Application crash on startup (check logs)
- Wrong health check path

**Solutions:**
```bash
# Verify database connectivity from ECS
aws ecs execute-command \
  --cluster cliente-core-prod-cluster \
  --task <task-id> \
  --container cliente-core-container \
  --command "nc -zv <db-host> 5432" \
  --interactive

# Check secrets
aws secretsmanager get-secret-value \
  --secret-id cliente-core-prod-db-credentials

# Update health check grace period
# Edit terraform/modules/ecs/main.tf:
health_check_grace_period_seconds = 180 # Increase from 120
```

#### 2. Database Connection Timeout

**Symptoms:**
- Application logs show `java.sql.SQLException: Connection timeout`
- RDS security group blocking connections

**Diagnosis:**
```bash
# Check security group rules
aws ec2 describe-security-groups \
  --group-ids $(terraform output -raw rds_security_group_id)

# Check RDS status
aws rds describe-db-instances \
  --db-instance-identifier cliente-core-prod-db
```

**Solutions:**
```bash
# Verify ECS tasks can reach RDS
# Security group should allow:
# - Source: ECS security group
# - Port: 5432
# - Protocol: TCP
```

#### 3. Terraform State Lock

**Symptoms:**
- `Error acquiring the state lock`

**Solution:**
```bash
# Force unlock (use with caution!)
terraform force-unlock <lock-id>
```

#### 4. ECR Push Fails

**Symptoms:**
- `denied: Your authorization token has expired`

**Solution:**
```bash
# Re-authenticate
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $(terraform output -raw ecr_repository_url)
```

#### 5. ALB Returns 503 Service Unavailable

**Symptoms:**
- ALB health checks fail
- No healthy targets

**Diagnosis:**
```bash
# Check target health
aws elbv2 describe-target-health \
  --target-group-arn $(terraform output -raw target_group_arn)
```

**Solutions:**
- Verify ECS tasks are running
- Check security group allows ALB → ECS on port 8080
- Verify health check path is correct

---

## Rollback Procedures

### Rollback ECS Deployment

```bash
# List task definition revisions
aws ecs list-task-definitions \
  --family-prefix cliente-core-prod

# Rollback to previous revision
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --task-definition cliente-core-prod:4  # Previous revision
```

### Rollback Terraform Changes

```bash
# Revert to previous state
terraform state pull > current.tfstate
terraform state push previous.tfstate

# Or destroy and re-apply
terraform destroy -target=module.ecs
terraform apply
```

### Rollback RDS Snapshot

```bash
# List snapshots
aws rds describe-db-snapshots \
  --db-instance-identifier cliente-core-prod-db

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier cliente-core-prod-db-restored \
  --db-snapshot-identifier <snapshot-id>
```

### Emergency Rollback (Full Infrastructure)

```bash
# 1. Backup current state
terraform state pull > backup-$(date +%Y%m%d-%H%M%S).tfstate

# 2. Destroy all resources
terraform destroy

# 3. Restore from previous working configuration
git checkout <previous-commit>
terraform init
terraform apply
```

---

## Production Readiness

### Pre-Production Checklist

Before going to production, enable these features:

- [ ] **HTTPS/TLS:** Add ACM certificate and HTTPS listener
- [ ] **Custom domain:** Configure Route53 DNS
- [ ] **Multi-AZ RDS:** Set `db_multi_az = true`
- [ ] **Dual NAT Gateways:** Set `single_nat_gateway = false`
- [ ] **Secrets rotation:** Enable automatic rotation
- [ ] **Backup verification:** Test RDS snapshot restore
- [ ] **Disaster recovery:** Document RTO/RPO
- [ ] **SNS notifications:** Connect CloudWatch alarms to SNS
- [ ] **WAF:** Add AWS WAF rules
- [ ] **DDoS protection:** Enable AWS Shield Standard
- [ ] **VPC Flow Logs:** Review network traffic patterns
- [ ] **IAM audit:** Restrict permissions to least privilege
- [ ] **Cost monitoring:** Set up AWS Budgets alerts
- [ ] **Performance testing:** Load test with realistic traffic
- [ ] **Runbook:** Document incident response procedures

### Production Enhancements

**HTTPS Configuration:**
```hcl
# Request ACM certificate
resource "aws_acm_certificate" "main" {
  domain_name       = "api.vanessa-mudanca.com"
  validation_method = "DNS"
}

# Add HTTPS listener
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = aws_acm_certificate.main.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.main.arn
  }
}

# Redirect HTTP to HTTPS
resource "aws_lb_listener" "http_redirect" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}
```

**Route53 DNS:**
```hcl
resource "aws_route53_record" "api" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "api.vanessa-mudanca.com"
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = true
  }
}
```

---

## Security Considerations

### Network Security

**Implemented:**
- Private subnets for ECS (no direct internet access)
- Isolated database subnets (no internet access)
- Security groups with least privilege
- VPC Flow Logs enabled

**Recommendations:**
- Enable VPC endpoint for ECR (avoid NAT Gateway costs)
- Enable VPC endpoint for Secrets Manager
- Implement Network ACLs for additional defense

### Data Security

**Implemented:**
- RDS encryption at rest (KMS)
- Secrets Manager for credentials
- TLS for Secrets Manager API calls
- IAM roles for ECS tasks (no long-lived credentials)

**Recommendations:**
- Enable CloudTrail for API audit
- Rotate database credentials every 90 days
- Use AWS KMS customer-managed keys
- Enable S3 versioning for backups

### Application Security

**Recommendations:**
- Implement OAuth2 + JWT authentication
- Add rate limiting in Spring Boot
- Enable CORS with strict origins
- Validate all inputs (use Spring Validation)
- Implement SQL injection prevention (use JPA)
- Add security headers (Helmet.js equivalent)

### Compliance

**LGPD considerations:**
- RDS backups are encrypted
- Data residency in us-east-1 (configure region as needed)
- Audit trail via CloudTrail
- Data retention policies (7-day backups)

---

## Maintenance

### Regular Tasks

**Daily:**
- Review CloudWatch alarms
- Check ECS service health
- Monitor RDS performance metrics

**Weekly:**
- Review CloudWatch Logs for errors
- Check auto-scaling events
- Review cost usage

**Monthly:**
- Patch ECS task definition (update base image)
- Review IAM permissions
- Test backup restoration
- Review and optimize costs

**Quarterly:**
- Upgrade RDS minor version
- Review security groups
- Capacity planning
- Disaster recovery drill

### Updating Infrastructure

**Terraform changes:**
```bash
# 1. Create feature branch
git checkout -b infra/update-ecs-memory

# 2. Make changes
vim terraform/variables.tf

# 3. Test in dev/staging first
terraform plan

# 4. Apply carefully
terraform apply

# 5. Verify deployment
# (check health, logs, metrics)

# 6. Commit and push
git add .
git commit -m "feat: Increase ECS memory to 2048 MB"
git push
```

**Application updates:**
```bash
# 1. Build new image
mvn clean package -DskipTests
docker build -t cliente-core:v2.0.0 .

# 2. Tag and push
docker tag cliente-core:v2.0.0 $ECR_URL:v2.0.0
docker tag cliente-core:v2.0.0 $ECR_URL:latest
docker push $ECR_URL:v2.0.0
docker push $ECR_URL:latest

# 3. Force new deployment
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment

# 4. Monitor deployment
aws ecs wait services-stable \
  --cluster cliente-core-prod-cluster \
  --services cliente-core-prod-service
```

### Scaling Operations

**Manual scaling:**
```bash
# Scale up
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --desired-count 6

# Scale down
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --desired-count 2
```

**Update auto-scaling limits:**
```bash
# Edit terraform/terraform.tfvars
ecs_min_capacity = 4
ecs_max_capacity = 10

# Apply
terraform apply
```

---

## Support

**Documentation:**
- Terraform Registry: https://registry.terraform.io/providers/hashicorp/aws/latest/docs
- AWS ECS Best Practices: https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/
- Spring Boot on AWS: https://aws.amazon.com/blogs/opensource/

**Troubleshooting:**
- Check CloudWatch Logs: `/ecs/cliente-core-prod`
- Review CloudWatch Alarms
- Check ECS service events
- Review RDS Enhanced Monitoring

**Questions?**
- Team: Va Nessa Mudança Engineering
- Slack: #cliente-core-support
- On-call: PagerDuty escalation

---

## License

Internal use only - Va Nessa Mudança

**Last updated:** 2025-11-04
**Version:** 1.0.0
**Maintained by:** DevOps Team
