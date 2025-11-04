# Quick Deployment Guide - cliente-core

This is a streamlined guide for deploying the cliente-core infrastructure. For detailed documentation, see [README.md](README.md).

## Prerequisites Checklist

- [ ] AWS CLI installed and configured (`aws --version`)
- [ ] Terraform >= 1.6.0 installed (`terraform --version`)
- [ ] Docker installed (`docker --version`)
- [ ] AWS credentials configured (`aws sts get-caller-identity`)
- [ ] Maven installed (`mvn --version`)

## 5-Minute Quick Start

### 1. Initialize Terraform

```bash
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core/terraform
terraform init
```

### 2. Create Configuration

```bash
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars if needed (or use defaults)
```

### 3. Deploy Infrastructure

```bash
# Preview changes
terraform plan

# Deploy (takes ~15-20 minutes)
terraform apply -auto-approve
```

### 4. Build and Push Application

```bash
# Get ECR URL
ECR_URL=$(terraform output -raw ecr_repository_url)

# Authenticate Docker
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URL

# Build application
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
mvn clean package -DskipTests

# Build and push Docker image
docker build -t cliente-core:latest .
docker tag cliente-core:latest $ECR_URL:latest
docker push $ECR_URL:latest
```

### 5. Force ECS Deployment

```bash
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment \
  --region us-east-1
```

### 6. Verify Deployment

```bash
# Wait 2-3 minutes for tasks to start
sleep 180

# Get ALB DNS
cd terraform
ALB_DNS=$(terraform output -raw alb_dns_name)

# Test health endpoint
curl http://$ALB_DNS/api/clientes/actuator/health

# Expected: {"status":"UP"}
```

## What Gets Deployed

| Resource | Configuration | Cost/Month |
|----------|--------------|------------|
| VPC | 10.0.0.0/16, 2 AZs | Free |
| NAT Gateway | 1 instance (cost optimization) | $32-40 |
| RDS PostgreSQL | db.t4g.micro, 20 GB gp3 | $12-15 |
| ECS Fargate | 2 tasks × 512 CPU × 1024 MB | $25-30 |
| ALB | Application Load Balancer | $16-20 |
| CloudWatch | Logs + Alarms | $2-5 |
| **Total** | **MVP Configuration** | **$90-115** |

## Common Post-Deployment Tasks

### View Logs

```bash
aws logs tail /ecs/cliente-core-prod --follow
```

### Scale Service

```bash
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --desired-count 4 \
  --region us-east-1
```

### Update Application

```bash
# Build new image
mvn clean package -DskipTests
docker build -t cliente-core:latest .

# Push to ECR
ECR_URL=$(cd terraform && terraform output -raw ecr_repository_url)
docker tag cliente-core:latest $ECR_URL:latest
docker push $ECR_URL:latest

# Force new deployment
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment
```

### Connect to Database

```bash
# Get database endpoint
cd terraform
DB_HOST=$(terraform output -raw rds_address)

# Get credentials from Secrets Manager
aws secretsmanager get-secret-value \
  --secret-id cliente-core-prod-db-credentials \
  --query SecretString \
  --output text | jq -r '.username, .password'

# Connect via psql (requires bastion or VPN)
psql -h $DB_HOST -U clientecore_admin -d vanessa_mudanca_clientes
```

## Troubleshooting

### Tasks Keep Restarting

```bash
# Check logs for errors
aws logs tail /ecs/cliente-core-prod --filter-pattern "ERROR"

# Check task status
aws ecs describe-tasks \
  --cluster cliente-core-prod-cluster \
  --tasks $(aws ecs list-tasks \
    --cluster cliente-core-prod-cluster \
    --service-name cliente-core-prod-service \
    --query 'taskArns[0]' --output text)
```

### Health Check Fails

```bash
# Verify health endpoint locally
docker run -p 8080:8080 cliente-core:latest

# Test from another terminal
curl http://localhost:8080/api/clientes/actuator/health
```

### Database Connection Issues

```bash
# Verify security group allows ECS → RDS on port 5432
aws ec2 describe-security-groups \
  --group-ids $(cd terraform && terraform output -raw rds_security_group_id) | \
  jq '.SecurityGroups[].IpPermissions'
```

## Cleanup (Destroy Infrastructure)

```bash
# WARNING: This destroys ALL resources!
cd terraform
terraform destroy -auto-approve
```

**Note:** RDS will create a final snapshot before deletion.

## Next Steps

1. **Add HTTPS:** Configure ACM certificate and HTTPS listener
2. **Custom Domain:** Set up Route53 DNS
3. **Monitoring:** Create CloudWatch dashboard
4. **Alerts:** Configure SNS notifications for alarms
5. **Backups:** Test RDS snapshot restoration
6. **Security:** Enable WAF rules and rate limiting

## Support

- **Full Documentation:** [README.md](README.md)
- **Terraform Docs:** [Registry](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- **AWS ECS Guide:** [Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/)

---

**Last updated:** 2025-11-04
