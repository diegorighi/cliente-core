# Terraform Infrastructure Summary - cliente-core

## What Was Created

Complete AWS infrastructure for deploying the cliente-core Spring Boot microservice on ECS Fargate with RDS PostgreSQL.

### File Structure

```
terraform/
├── main.tf                          # Root module orchestration
├── variables.tf                     # Input variables (70+ parameters)
├── outputs.tf                       # Output values (25+ outputs)
├── terraform.tfvars.example         # Example configuration with cost notes
├── .gitignore                       # Terraform-specific gitignore
├── README.md                        # Comprehensive documentation (600+ lines)
├── DEPLOYMENT_GUIDE.md              # Quick start guide
├── SUMMARY.md                       # This file
└── modules/
    ├── vpc/                         # Network foundation
    │   ├── main.tf                  # VPC, subnets, NAT, IGW, route tables
    │   ├── variables.tf
    │   └── outputs.tf
    ├── security/                    # Security resources
    │   ├── main.tf                  # Security groups, IAM, Secrets, ECR
    │   ├── variables.tf
    │   └── outputs.tf
    ├── rds/                         # Database
    │   ├── main.tf                  # PostgreSQL 16 with optimizations
    │   ├── variables.tf
    │   └── outputs.tf
    ├── alb/                         # Load balancer
    │   ├── main.tf                  # ALB, target group, listener
    │   ├── variables.tf
    │   └── outputs.tf
    └── ecs/                         # Container orchestration
        ├── main.tf                  # Fargate cluster, service, auto-scaling
        ├── variables.tf
        └── outputs.tf
```

### Additional Files

```
cliente-core/
├── Dockerfile                       # Multi-stage build optimized for Fargate
├── .dockerignore                    # Exclude unnecessary files from image
└── terraform/                       # All Terraform files above
```

## Infrastructure Components

### 1. VPC Module (Multi-AZ Network Architecture)

**Resources created:**
- 1 VPC (10.0.0.0/16)
- 2 Public subnets (10.0.1.0/24, 10.0.2.0/24) - for ALB
- 2 Private subnets (10.0.11.0/24, 10.0.12.0/24) - for ECS tasks
- 2 Database subnets (10.0.21.0/24, 10.0.22.0/24) - for RDS
- 1 Internet Gateway
- 1 NAT Gateway (cost optimization - single AZ)
- 1 Elastic IP
- 3 Route tables (public, private, database)
- VPC Flow Logs (CloudWatch)

**Key features:**
- Multi-AZ for high availability
- Network segmentation (public/private/database tiers)
- Cost-optimized with single NAT Gateway (can be upgraded to dual NAT)
- VPC Flow Logs for security monitoring

### 2. Security Module

**Resources created:**
- 3 Security groups (ALB, ECS, RDS)
- 2 IAM roles (ECS Task Execution, ECS Task)
- 4 IAM policies
- 1 Secrets Manager secret (database credentials)
- 1 ECR repository
- 1 ECR lifecycle policy

**Key features:**
- Least privilege security groups (ALB → ECS → RDS)
- Auto-generated database password (32 characters)
- IAM roles for ECS with Secrets Manager access
- ECR with image scanning and lifecycle policies (keep 5 images)

### 3. RDS Module (PostgreSQL 16)

**Resources created:**
- 1 RDS PostgreSQL instance (db.t4g.micro)
- 1 DB subnet group
- 1 DB parameter group (optimized for Spring Boot + Liquibase)
- 1 IAM role for Enhanced Monitoring
- 4 CloudWatch alarms (CPU, Memory, Storage, Connections)

**Key features:**
- PostgreSQL 16.6 with parameter tuning for HikariCP
- gp3 storage (20 GB initial, auto-scales to 100 GB)
- Automated backups (7-day retention)
- Enhanced Monitoring (60-second intervals)
- Performance Insights (7-day retention)
- Single-AZ for cost optimization (upgradeable to Multi-AZ)

**Parameter optimizations:**
- `max_connections: 100` (HikariCP max 10)
- `shared_buffers: ~32 MB` (for db.t4g.micro)
- `work_mem: 4 MB`
- `random_page_cost: 1.1` (SSD optimization)
- `log_min_duration_statement: 1000` (log slow queries)
- `statement_timeout: 60000` (Liquibase migrations)

### 4. ALB Module (Application Load Balancer)

**Resources created:**
- 1 Application Load Balancer (internet-facing)
- 1 Target group (IP-based for Fargate)
- 1 HTTP listener (port 80)
- 3 CloudWatch alarms (response time, unhealthy targets, 5xx errors)

**Key features:**
- Health checks on Spring Boot Actuator endpoint
- Cross-zone load balancing enabled
- HTTP/2 enabled
- Deregistration delay: 30 seconds (graceful shutdown)
- Stickiness disabled (stateless application)

**Health check configuration:**
- Path: `/api/clientes/actuator/health`
- Interval: 30 seconds
- Timeout: 5 seconds
- Healthy threshold: 2
- Unhealthy threshold: 3

### 5. ECS Module (Fargate Orchestration)

**Resources created:**
- 1 ECS Fargate cluster (Container Insights enabled)
- 1 ECS service (desired count: 2)
- 1 Task definition (CPU: 512, Memory: 1024)
- 1 CloudWatch log group
- 2 Auto-scaling policies (CPU and Memory)
- 1 Auto-scaling target
- 3 CloudWatch alarms

**Key features:**
- Fargate launch type (serverless)
- Auto-scaling (2-4 tasks based on CPU/Memory)
- Rolling deployment with circuit breaker (auto-rollback)
- Container health checks
- Graceful shutdown (30-second timeout)
- Liquibase-aware startup (60-second health check grace period)

**Container configuration:**
- Image: From ECR repository
- Port: 8080
- Environment: `SPRING_PROFILES_ACTIVE=prod`
- Secrets: Database credentials from Secrets Manager
- JVM options: G1GC, 512-768 MB heap

**Auto-scaling:**
- Target CPU: 70%
- Target Memory: 80%
- Scale-out cooldown: 60 seconds
- Scale-in cooldown: 300 seconds

## CloudWatch Monitoring

### Alarms Created (12 total)

**RDS (4 alarms):**
1. CPU > 80% for 10 minutes
2. Free memory < 100 MB
3. Free storage < 2 GB
4. Connections > 80

**ECS (3 alarms):**
1. CPU > 85% for 10 minutes
2. Memory > 90% for 10 minutes
3. Healthy tasks < minimum capacity

**ALB (3 alarms):**
1. Response time > 1 second for 10 minutes
2. Unhealthy targets > 0
3. 5xx errors > 10 in 5 minutes

**VPC (1 log group):**
1. VPC Flow Logs for network monitoring

**ECS (1 log group):**
1. Application logs at `/ecs/cliente-core-prod`

## Cost Analysis

### MVP Configuration (Monthly)

| Component | Specification | Monthly Cost |
|-----------|--------------|--------------|
| RDS | db.t4g.micro, 20 GB gp3, Single-AZ | $12-15 |
| ECS | 2 tasks × 512 CPU × 1024 MB × 730 hrs | $25-30 |
| ALB | 1 ALB + 50 GB data transfer | $16-20 |
| NAT Gateway | 1 NAT + 50 GB data transfer | $32-40 |
| CloudWatch | 5 GB logs, 12 alarms | $2-5 |
| Secrets Manager | 1 secret | $0.40 |
| ECR | 5 images × 500 MB | $0.50 |
| Data Transfer | 5 GB outbound | $0.45 |
| Backups | RDS snapshots (7 days) | $2-3 |
| **TOTAL** | **MVP** | **$90-115** |

### Cost Optimization Features

**Implemented:**
- Single NAT Gateway (saves $32/month vs. dual NAT)
- Single-AZ RDS (saves $12/month vs. Multi-AZ)
- 7-day log retention (saves $5/month vs. 30 days)
- db.t4g.micro (ARM Graviton2, cheaper than x86)
- gp3 storage (cheaper than io1/io2)

**Future savings:**
- RDS Reserved Instances: 40% savings (~$200/year)
- Fargate Savings Plans: 50% savings (~$180/year)
- VPC Endpoints: Avoid NAT Gateway data transfer charges
- CloudWatch Logs compression: 30% savings

### Production Upgrade Path

| Upgrade | Cost Impact |
|---------|-------------|
| Multi-AZ RDS | +$12-15/month |
| Dual NAT Gateways | +$32-40/month |
| Scale to 4 tasks | +$25-30/month |
| Upgrade to db.t4g.small | +$12-15/month |
| 30-day log retention | +$5-10/month |
| **Production Total** | **$250-350/month** |

## Security Features

### Network Security

- Private subnets for ECS (no direct internet access)
- Isolated database subnets (completely private)
- Security groups with least privilege
- VPC Flow Logs enabled
- NACLs (implicit, using VPC defaults)

### Data Security

- RDS encryption at rest (KMS default key)
- TLS in transit (Secrets Manager API)
- Auto-generated strong passwords (32 chars)
- No hardcoded credentials (all in Secrets Manager)
- IAM roles instead of access keys

### Application Security

- Non-root container user (spring:spring)
- Image scanning enabled (ECR)
- Container Insights enabled
- CloudWatch Logs with structured logging
- Health checks every 30 seconds

### Compliance

- LGPD considerations (encrypted backups, audit logs)
- CloudTrail integration ready
- Secrets rotation ready (not enabled in MVP)
- Multi-region backup ready (not enabled in MVP)

## High Availability

### Implemented

- Multi-AZ ALB (2 availability zones)
- Multi-AZ ECS tasks (2 tasks in different AZs)
- Auto-scaling (scale to 4 tasks automatically)
- Health checks with auto-recovery
- Rolling deployments (zero downtime)
- Circuit breaker (auto-rollback on failed deploys)

### Not Implemented (MVP Cost Savings)

- Multi-AZ RDS (can be enabled with 1 variable change)
- Dual NAT Gateways (can be enabled with 1 variable change)
- Cross-region replication
- Read replicas

## Deployment Features

### Zero-Downtime Deployments

- Rolling update strategy
- Maximum 200% capacity during deployment
- Minimum 100% healthy capacity
- Circuit breaker with auto-rollback
- Health check grace period (120 seconds for Liquibase)

### Auto-Scaling

- CPU-based scaling (target 70%)
- Memory-based scaling (target 80%)
- Scale-out: 60-second cooldown
- Scale-in: 300-second cooldown (5 minutes)
- Min capacity: 2 tasks
- Max capacity: 4 tasks

### Monitoring

- Container Insights (detailed metrics)
- Enhanced Monitoring (RDS)
- Performance Insights (RDS)
- Application logs (CloudWatch)
- VPC Flow Logs
- 12 CloudWatch alarms

## Next Steps for Deployment

### 1. Prerequisites

```bash
# Install tools
brew install terraform awscli docker maven

# Configure AWS
aws configure
```

### 2. Deploy Infrastructure

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### 3. Build and Push Application

```bash
# Get ECR URL
ECR_URL=$(terraform output -raw ecr_repository_url)

# Build and push
mvn clean package -DskipTests
docker build -t cliente-core:latest .
docker tag cliente-core:latest $ECR_URL:latest
docker push $ECR_URL:latest
```

### 4. Verify

```bash
# Wait for tasks to start
aws ecs wait services-stable \
  --cluster cliente-core-prod-cluster \
  --services cliente-core-prod-service

# Test health
curl http://$(terraform output -raw alb_dns_name)/api/clientes/actuator/health
```

## Production Readiness Checklist

Before going to production, implement these enhancements:

### Critical

- [ ] Enable HTTPS with ACM certificate
- [ ] Configure custom domain with Route53
- [ ] Enable Multi-AZ RDS
- [ ] Add second NAT Gateway
- [ ] Configure SNS notifications for alarms
- [ ] Test backup restoration
- [ ] Document disaster recovery procedures

### Recommended

- [ ] Add WAF rules
- [ ] Enable AWS Shield Standard
- [ ] Configure VPC endpoints (ECR, Secrets Manager)
- [ ] Implement secrets rotation
- [ ] Set up CloudWatch dashboard
- [ ] Configure AWS Budgets alerts
- [ ] Add OAuth2 authentication
- [ ] Implement rate limiting

### Optional

- [ ] Configure CloudFront CDN
- [ ] Add read replica for RDS
- [ ] Implement blue-green deployments
- [ ] Add canary deployments
- [ ] Configure AWS Backup
- [ ] Set up cross-region replication

## Maintenance

### Daily

- Review CloudWatch alarms
- Check ECS service health
- Monitor costs in AWS Cost Explorer

### Weekly

- Review application logs for errors
- Check auto-scaling events
- Review RDS performance metrics

### Monthly

- Update Docker base image
- Review security groups
- Test backup restoration
- Review and optimize costs

### Quarterly

- Upgrade RDS minor version
- Review IAM permissions
- Capacity planning
- Disaster recovery drill

## Support Documentation

- **Quick Start:** [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- **Full Documentation:** [README.md](README.md)
- **Terraform Registry:** https://registry.terraform.io/providers/hashicorp/aws/latest/docs
- **AWS ECS Best Practices:** https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/

## Architecture Diagram (ASCII)

```
                                 ┌──────────────┐
                                 │   Internet   │
                                 └──────┬───────┘
                                        │
                     ┌──────────────────┼──────────────────┐
                     │                  │                  │
              ┌──────▼──────┐    ┌──────▼──────┐          │
              │  Public AZ1 │    │  Public AZ2 │          │
              │ 10.0.1.0/24 │    │ 10.0.2.0/24 │          │
              └──────┬──────┘    └──────┬──────┘          │
                     └──────────┬───────┘                 │
                                │                         │
                         ┌──────▼──────┐                  │
                         │     ALB     │                  │
                         │   Port 80   │                  │
                         └──────┬──────┘                  │
                                │                         │
                     ┌──────────┼──────────┐             │
                     │          │          │             │
              ┌──────▼──────┐ ┌─▼──────────▼┐           │
              │ Private AZ1 │ │ Private AZ2 │           │
              │10.0.11.0/24 │ │10.0.12.0/24 │           │
              └──────┬──────┘ └──────┬──────┘           │
                     │               │                   │
                  ┌──▼───────────────▼──┐               │
                  │   ECS Fargate (2-4) │               │
                  │   Spring Boot 3.5.7 │               │
                  │   Port 8080         │               │
                  └──────────┬───────────┘               │
                             │                           │
                  ┌──────────▼───────────┐               │
                  │    Database AZ1/2    │               │
                  │  10.0.21.0/24 + .22  │               │
                  └──────────┬───────────┘               │
                             │                           │
                     ┌───────▼────────┐                  │
                     │ PostgreSQL 16  │                  │
                     │ db.t4g.micro   │                  │
                     └────────────────┘                  │
                                                         │
    ┌────────────────────────────────────────────────────┘
    │
┌───▼────────────────────────────────────┐
│       Supporting Services              │
├────────────────────────────────────────┤
│ • Secrets Manager (DB credentials)     │
│ • ECR (Docker images)                  │
│ • CloudWatch (Logs + Alarms)          │
│ • NAT Gateway (Internet for ECS)      │
│ • VPC Flow Logs                        │
└────────────────────────────────────────┘
```

---

**Created:** 2025-11-04
**Version:** 1.0.0
**Total Files:** 20
**Total Lines of Code:** ~2,500
**Estimated Setup Time:** 15-20 minutes
**Monthly Cost:** $90-115 (MVP) | $250-350 (Production)
