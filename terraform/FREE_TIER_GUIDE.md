# AWS Free Tier Guide - cliente-core

Complete guide to deploying cliente-core using AWS Free Tier benefits.

---

## Table of Contents

- [Overview](#overview)
- [What is AWS Free Tier?](#what-is-aws-free-tier)
- [Free Tier Benefits for cliente-core](#free-tier-benefits-for-cliente-core)
- [Cost Comparison](#cost-comparison)
- [Deployment Steps](#deployment-steps)
- [Limitations & Tradeoffs](#limitations--tradeoffs)
- [Monitoring Free Tier Usage](#monitoring-free-tier-usage)
- [Upgrade Path](#upgrade-path)
- [FAQ](#faq)

---

## Overview

This guide helps you deploy the cliente-core microservice using **AWS Free Tier**, reducing costs from **$90-115/month to $35-45/month** for the first 12 months.

**Key savings:**
- RDS PostgreSQL: $0 (vs $12-15)
- ECS Fargate: $15-18 (vs $25-30) - reduced to 1 task
- CloudWatch Logs: $0 (vs $2-5)
- Total: **$50-70/month savings**

---

## What is AWS Free Tier?

AWS offers **three types of free tier offers**:

### 1. 12 Months Free (from account creation)

These services are free for 12 months starting from your AWS account creation date:

| Service | Free Tier Limit | cliente-core Usage |
|---------|----------------|-------------------|
| **RDS** | 750 hours/month of db.t3.micro (Single-AZ) | ✅ 730 hours = FREE |
| **EC2** | 750 hours/month of t3.micro | ❌ Not used (we use Fargate) |
| **S3** | 5GB standard storage | ✅ ~1GB backups = FREE |
| **CloudWatch** | 10 custom metrics, 5GB logs | ✅ 8 metrics, 2-3GB logs = FREE |
| **Data Transfer** | 100GB/month outbound | ✅ ~2-5GB = FREE |
| **ALB** | 750 hours/month + 15GB processed | ✅ Partially free |

### 2. Always Free (unlimited duration)

These limits apply even after 12 months:

| Service | Always Free Limit | cliente-core Usage |
|---------|------------------|-------------------|
| **Lambda** | 1M requests/month | ❌ Not used yet |
| **DynamoDB** | 25GB storage, 200M requests | ❌ Not used yet |
| **CloudWatch** | 10 custom metrics, 5GB logs, 1M API requests | ✅ Within limits |

### 3. Trials

Short-term free trials for specific services (not applicable to cliente-core).

---

## Free Tier Benefits for cliente-core

### ✅ What's Covered (FREE)

1. **RDS PostgreSQL db.t3.micro**
   - Free Tier: 750 hours/month (Single-AZ only)
   - Our usage: 730 hours/month (always-on)
   - Savings: $12-15/month
   - Storage: 20GB gp3 included

2. **CloudWatch Logs**
   - Free Tier: 5GB ingestion/month
   - Our usage: 2-3GB/month (with 3-day retention)
   - Savings: $2-5/month

3. **CloudWatch Metrics**
   - Free Tier: 10 custom metrics
   - Our usage: 8 metrics (CPU, memory, 5xx, latency, etc.)
   - Savings: $3/month

4. **ECR Repository**
   - Free Tier: 500MB storage/month
   - Our usage: ~250MB (Spring Boot image)
   - Savings: $0.50/month

5. **Data Transfer**
   - Free Tier: 100GB/month outbound
   - Our usage: ~2-5GB/month
   - Savings: $0.45/month

6. **ALB (partial)**
   - Free Tier: 750 hours/month + 15GB processed
   - Our usage: 730 hours + 5-10GB
   - Savings: ~$10/month (partial)

### ❌ What's NOT Covered (PAID)

1. **ECS Fargate**
   - Free Tier: Not covered
   - Cost: $15-18/month (1 task × 256 CPU × 512 MB)
   - Note: EC2 t3.micro IS free tier, but requires managing servers

2. **NAT Gateway**
   - Free Tier: Not covered
   - Cost: $16-20/month
   - Note: Required for ECS tasks to pull images from ECR

3. **Secrets Manager**
   - Free Tier: 30-day trial only
   - Cost: $0.40/month/secret
   - Note: Could use AWS Systems Manager Parameter Store (free) instead

4. **ALB (over limits)**
   - Free Tier: Only 750 hours + 15GB
   - Cost: ~$5-10/month (for additional data processing)

---

## Cost Comparison

### Free Tier Configuration (First 12 Months)

```
┌──────────────────────────────────────────────────────────┐
│ AWS Free Tier - cliente-core Monthly Cost               │
├──────────────────────────────────────────────────────────┤
│ RDS db.t3.micro (Single-AZ)          $0.00   (FREE)     │
│ ECS Fargate (1 task, 256/512)        $15-18             │
│ ALB (partial free tier)              $0-5               │
│ NAT Gateway                          $16-20             │
│ CloudWatch Logs                      $0.00   (FREE)     │
│ CloudWatch Metrics                   $0.00   (FREE)     │
│ Secrets Manager                      $0.40              │
│ ECR Repository                       $0.00   (FREE)     │
│ Data Transfer                        $0.00   (FREE)     │
│ RDS Backups                          $0.00   (FREE)     │
├──────────────────────────────────────────────────────────┤
│ TOTAL                                $35-45/month  ⭐   │
└──────────────────────────────────────────────────────────┘
```

### Standard MVP Configuration (No Free Tier)

```
┌──────────────────────────────────────────────────────────┐
│ Standard MVP - cliente-core Monthly Cost                │
├──────────────────────────────────────────────────────────┤
│ RDS db.t4g.micro (Single-AZ)         $12-15             │
│ ECS Fargate (2 tasks, 512/1024)      $25-30             │
│ ALB                                  $16-20             │
│ NAT Gateway                          $32-40             │
│ CloudWatch Logs                      $2-5               │
│ CloudWatch Metrics                   $3                 │
│ Secrets Manager                      $0.40              │
│ ECR Repository                       $0.50              │
│ Data Transfer                        $0.45              │
│ RDS Backups                          $2-3               │
├──────────────────────────────────────────────────────────┤
│ TOTAL                                $90-115/month      │
└──────────────────────────────────────────────────────────┘
```

### Savings Summary

**First 12 months:** Save **$50-70/month** = **$600-840/year**

**After month 13:** Cost increases to **$55-70/month** (RDS no longer free)

---

## Deployment Steps

### 1. Check Your Free Tier Eligibility

```bash
# Check account creation date
aws iam get-account-summary --query 'SummaryMap.AccountCreated'

# If created less than 12 months ago, you're eligible!
```

### 2. Copy Free Tier Configuration

```bash
cd terraform
cp terraform.tfvars.freetier terraform.tfvars
```

### 3. Verify Free Tier Settings

Open `terraform.tfvars` and confirm:

```hcl
enable_free_tier            = true   # ✅ Enable Free Tier optimizations
db_instance_class           = "db.t3.micro"  # ✅ Free Tier eligible
db_multi_az                 = false  # ✅ Must be Single-AZ
ecs_desired_count           = 1      # ✅ Minimize Fargate costs
ecs_task_cpu                = 256    # ✅ Minimum CPU
ecs_task_memory             = 512    # ✅ Minimum memory
cloudwatch_retention_days   = 3      # ✅ Reduce log storage
db_backup_retention_period  = 1      # ✅ Reduce backup storage
```

### 4. Deploy Infrastructure

```bash
# Initialize Terraform
terraform init

# Validate configuration
terraform validate

# Plan deployment (review changes)
terraform plan

# Apply infrastructure (~15-20 minutes)
terraform apply
```

### 5. Build and Deploy Application

```bash
# Build Spring Boot application
cd ..
mvn clean package -DskipTests

# Build Docker image
docker build -t cliente-core:latest .

# Push to ECR
ECR_URL=$(cd terraform && terraform output -raw ecr_repository_url)
docker tag cliente-core:latest $ECR_URL:latest
docker push $ECR_URL:latest

# Update ECS service (auto-deploys new image)
aws ecs update-service \
  --cluster cliente-core-dev-cluster \
  --service cliente-core-dev-service \
  --force-new-deployment
```

### 6. Verify Deployment

```bash
# Get ALB DNS name
ALB_DNS=$(cd terraform && terraform output -raw alb_dns_name)

# Test health endpoint
curl http://$ALB_DNS/api/clientes/actuator/health
# Expected: {"status":"UP"}
```

---

## Limitations & Tradeoffs

### Free Tier Configuration Tradeoffs

| Aspect | Free Tier | Standard MVP | Impact |
|--------|-----------|--------------|--------|
| **High Availability** | ❌ Single task | ✅ 2+ tasks | Downtime during deployments |
| **Database HA** | ❌ Single-AZ | ✅ Multi-AZ option | No automatic failover |
| **Performance** | ⚠️ Slower (256 CPU, 512 MB) | ✅ Faster (512 CPU, 1024 MB) | ~30% slower response times |
| **Log Retention** | ⚠️ 3 days | ✅ 7 days | Less debugging history |
| **Backup Retention** | ⚠️ 1 day | ✅ 7 days | Limited recovery window |
| **Cost** | ✅ $35-45/month | ❌ $90-115/month | **60% savings** |

### When Free Tier is Appropriate

✅ **Good for:**
- Development environments
- MVP/proof-of-concept
- Low-traffic applications (<100 requests/hour)
- Learning/testing AWS
- Startups with limited budget

❌ **NOT good for:**
- Production workloads with SLA requirements
- High-traffic applications
- Mission-critical systems
- Applications requiring high availability

### Performance Impact

**Free Tier (256 CPU, 512 MB):**
- Spring Boot startup time: ~30-40 seconds
- API response time: ~200-500ms (simple queries)
- Concurrent requests: ~10-20 (before degradation)
- Database connections: Max 10 (HikariCP pool)

**Standard MVP (512 CPU, 1024 MB):**
- Spring Boot startup time: ~20-30 seconds
- API response time: ~100-300ms (simple queries)
- Concurrent requests: ~50-100 (before degradation)
- Database connections: Max 20

---

## Monitoring Free Tier Usage

### 1. AWS Billing Dashboard

```bash
# Open AWS Billing Console
open https://console.aws.amazon.com/billing/home#/freetier
```

**Check:**
- RDS usage: Should be <750 hours/month
- Data transfer: Should be <100GB/month
- CloudWatch logs: Should be <5GB/month

### 2. Set Up Billing Alarms

```bash
# Create SNS topic for billing alerts
aws sns create-topic --name billing-alerts

# Subscribe to email notifications
aws sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:ACCOUNT_ID:billing-alerts \
  --protocol email \
  --notification-endpoint your-email@example.com

# Create billing alarm ($50 threshold)
aws cloudwatch put-metric-alarm \
  --alarm-name billing-alert-50 \
  --alarm-description "Alert when bill exceeds $50" \
  --metric-name EstimatedCharges \
  --namespace AWS/Billing \
  --statistic Maximum \
  --period 21600 \
  --evaluation-periods 1 \
  --threshold 50 \
  --comparison-operator GreaterThanThreshold \
  --alarm-actions arn:aws:sns:us-east-1:ACCOUNT_ID:billing-alerts
```

### 3. CloudWatch Dashboard

Create a custom dashboard to monitor Free Tier usage:

```bash
# Create dashboard
aws cloudwatch put-dashboard \
  --dashboard-name cliente-core-freetier \
  --dashboard-body file://freetier-dashboard.json
```

**Metrics to monitor:**
- RDS: `DatabaseConnections`, `CPUUtilization`, `FreeStorageSpace`
- ECS: `CPUUtilization`, `MemoryUtilization`
- ALB: `RequestCount`, `ProcessedBytes`
- NAT: `BytesOutToDestination`

---

## Upgrade Path

### When to Upgrade from Free Tier

**Upgrade when you experience:**
- ❌ Application downtime during deployments (need 2+ tasks)
- ❌ Slow response times (>1s) under load
- ❌ Out of memory errors (need more than 512 MB)
- ❌ Database connection pool exhaustion
- ❌ Free Tier expires (month 13)

### Gradual Upgrade Strategy

**Phase 1: Increase ECS Resources (Month 1-3)**
```hcl
ecs_task_cpu    = 512   # +$9/month
ecs_task_memory = 1024  # +$4.50/month
# Cost: +$13.50/month
```

**Phase 2: Add HA (Month 3-6)**
```hcl
ecs_desired_count = 2   # +$15-18/month
ecs_min_capacity  = 2
# Cost: +$15-18/month
```

**Phase 3: Upgrade Database (Month 6-12)**
```hcl
db_instance_class = "db.t4g.micro"  # +$3/month (ARM is cheaper)
db_multi_az       = true            # +$12-15/month
# Cost: +$15-18/month
```

**Phase 4: Production Hardening (Month 12+)**
```hcl
single_nat_gateway         = false  # +$32-40/month
cloudwatch_retention_days  = 30     # +$5/month
db_backup_retention_period = 30     # +$5/month
# Cost: +$42-50/month
```

**Total Production Cost:** $250-350/month

---

## FAQ

### Q: Is Free Tier really free?

**A:** Partially. RDS, CloudWatch, and some ALB usage are free. But Fargate and NAT Gateway are not covered, so you'll still pay $35-45/month.

### Q: What happens after 12 months?

**A:** RDS charges start (~$12-15/month). Total cost becomes $55-70/month. You can continue using the same config or upgrade to Standard MVP.

### Q: Can I use Free Tier for production?

**A:** Technically yes, but **not recommended**. Free Tier lacks high availability (single task, single-AZ RDS). Use for dev/staging only.

### Q: What if I exceed Free Tier limits?

**A:** You'll be charged standard rates for the excess. Example: If you use 800 hours of RDS (vs 750 free), you pay for 50 hours at ~$0.017/hour = $0.85.

### Q: Can I use EC2 instead of Fargate to save more?

**A:** Yes! EC2 t3.micro is free tier (750h/month). But you'll need to:
- Manage EC2 instances (AMI updates, patching)
- Configure Auto Scaling Groups
- Handle deployments manually
- Tradeoff: Complexity vs Cost (~$15/month savings)

### Q: How do I switch from Free Tier to Standard MVP?

**A:**
```bash
# Edit terraform.tfvars
enable_free_tier = false
db_instance_class = "db.t4g.micro"
ecs_desired_count = 2
ecs_task_cpu = 512
ecs_task_memory = 1024

# Apply changes (~10 minutes, zero downtime)
terraform apply
```

### Q: What's the cheapest possible config?

**A:** If cost is the ONLY concern:
- Use EC2 t3.micro (free) instead of Fargate: -$15/month
- Use VPC Endpoints instead of NAT Gateway: -$25/month
- Use Parameter Store instead of Secrets Manager: -$0.40/month
- **Total: ~$5-10/month** (but significantly more complex)

### Q: Will I be notified before Free Tier expires?

**A:** No automatic notification. Set a calendar reminder for month 11 to review your usage and plan the transition.

---

## Resources

**AWS Free Tier Documentation:**
- Overview: https://aws.amazon.com/free/
- RDS: https://aws.amazon.com/rds/free/
- ECS: https://aws.amazon.com/ecs/pricing/ (Fargate NOT free)
- CloudWatch: https://aws.amazon.com/cloudwatch/pricing/

**Cost Calculators:**
- AWS Pricing Calculator: https://calculator.aws/
- cliente-core estimate: https://calculator.aws/#/estimate?id=XXXXX

**Monitoring:**
- Free Tier Usage: https://console.aws.amazon.com/billing/home#/freetier
- Billing Dashboard: https://console.aws.amazon.com/billing/
- Cost Explorer: https://console.aws.amazon.com/cost-management/home

---

**Last updated:** 2025-11-04
**Terraform version:** 1.6+
**AWS Free Tier:** 12 months from account creation
