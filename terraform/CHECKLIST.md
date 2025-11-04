# Pre-Deployment Checklist

Complete this checklist before deploying to production.

## Prerequisites

### Tools Installed

- [ ] **Terraform** >= 1.6.0
  ```bash
  terraform --version
  ```

- [ ] **AWS CLI** >= 2.0
  ```bash
  aws --version
  ```

- [ ] **Docker** (for building images)
  ```bash
  docker --version
  ```

- [ ] **Maven** 3.8+ (for building Spring Boot)
  ```bash
  mvn --version
  ```

- [ ] **jq** (for JSON parsing in scripts)
  ```bash
  jq --version
  ```

### AWS Configuration

- [ ] **AWS credentials configured**
  ```bash
  aws sts get-caller-identity
  ```

- [ ] **Correct AWS region set**
  ```bash
  aws configure get region
  # Should be: us-east-1 (or your preferred region)
  ```

- [ ] **IAM permissions verified**
  - VPC, Subnet, Route Table, NAT, IGW
  - RDS, DB Subnet Group, Parameter Group
  - ECS, ECR, ALB, Target Group
  - Security Groups, IAM, Secrets Manager
  - CloudWatch Logs and Alarms

### Backend Setup (Optional)

- [ ] **S3 bucket created for Terraform state**
  ```bash
  aws s3 ls s3://vanessa-mudanca-terraform-state
  ```

- [ ] **DynamoDB table created for state locking**
  ```bash
  aws dynamodb describe-table --table-name terraform-state-lock
  ```

- [ ] **Backend configuration uncommented in main.tf**

## Configuration Review

### File Setup

- [ ] **terraform.tfvars created**
  ```bash
  cp terraform.tfvars.example terraform.tfvars
  ```

- [ ] **Environment variable set correctly**
  ```hcl
  environment = "prod" # or "dev", "staging"
  ```

- [ ] **AWS region matches your preference**
  ```hcl
  aws_region = "us-east-1"
  ```

- [ ] **Database credentials reviewed**
  ```hcl
  db_name     = "vanessa_mudanca_clientes"
  db_username = "clientecore_admin"
  # Password auto-generated
  ```

### Resource Sizing

- [ ] **RDS instance class appropriate**
  - MVP: `db.t4g.micro` ($12-15/month)
  - Production: `db.t4g.small` or `db.r6g.large`

- [ ] **ECS task sizing appropriate**
  - MVP: 512 CPU / 1024 MB Memory
  - Production: 1024 CPU / 2048 MB Memory

- [ ] **Auto-scaling limits set**
  - MVP: 2-4 tasks
  - Production: 4-10 tasks

### High Availability

- [ ] **Multi-AZ RDS decision made**
  - MVP: `db_multi_az = false` (cost savings)
  - Production: `db_multi_az = true` (HA)

- [ ] **NAT Gateway strategy decided**
  - MVP: `single_nat_gateway = true` (cost savings)
  - Production: `single_nat_gateway = false` (HA)

### Cost Optimization

- [ ] **Cost estimate reviewed**
  - MVP: $90-115/month
  - Production: $250-350/month

- [ ] **AWS Budget alerts configured** (optional)
  ```bash
  aws budgets describe-budgets --account-id $(aws sts get-caller-identity --query Account --output text)
  ```

## Validation

### Pre-Flight Checks

- [ ] **Run validation script**
  ```bash
  ./validate.sh
  ```

- [ ] **Terraform configuration validated**
  ```bash
  terraform validate
  ```

- [ ] **No syntax errors in .tf files**

- [ ] **Variables file syntax correct**

### Security Review

- [ ] **Security groups follow least privilege**
  - ALB: Only port 80 from internet
  - ECS: Only port 8080 from ALB
  - RDS: Only port 5432 from ECS

- [ ] **No hardcoded credentials in code**

- [ ] **Secrets Manager used for database credentials**

- [ ] **IAM roles use least privilege**

- [ ] **VPC Flow Logs enabled**

## Deployment

### Infrastructure Provisioning

- [ ] **Terraform initialized**
  ```bash
  terraform init
  ```

- [ ] **Terraform plan reviewed**
  ```bash
  terraform plan -out=tfplan
  ```

- [ ] **Resource counts verified**
  - Expected: ~60-70 resources
  - Check for unexpected deletions (should be 0 on first deploy)

- [ ] **Cost estimates reviewed in plan output**

- [ ] **Infrastructure deployed**
  ```bash
  terraform apply tfplan
  ```

- [ ] **Deployment completed successfully**
  - Check for errors in output
  - RDS creation takes ~10-15 minutes
  - Total time: 15-20 minutes

### Application Deployment

- [ ] **ECR repository URL obtained**
  ```bash
  terraform output ecr_repository_url
  ```

- [ ] **Docker authenticated to ECR**
  ```bash
  aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin $(terraform output -raw ecr_repository_url)
  ```

- [ ] **Spring Boot application built**
  ```bash
  cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
  mvn clean package -DskipTests
  ```

- [ ] **Docker image built**
  ```bash
  docker build -t cliente-core:latest .
  ```

- [ ] **Image tagged for ECR**
  ```bash
  docker tag cliente-core:latest $(terraform output -raw ecr_repository_url):latest
  ```

- [ ] **Image pushed to ECR**
  ```bash
  docker push $(terraform output -raw ecr_repository_url):latest
  ```

- [ ] **ECS service deployment forced**
  ```bash
  aws ecs update-service \
    --cluster cliente-core-prod-cluster \
    --service cliente-core-prod-service \
    --force-new-deployment
  ```

## Verification

### Service Health

- [ ] **ECS tasks running**
  ```bash
  aws ecs describe-services \
    --cluster cliente-core-prod-cluster \
    --services cliente-core-prod-service \
    --query 'services[0].runningCount'
  # Should be: 2 (or your desired count)
  ```

- [ ] **All tasks healthy**
  ```bash
  aws elbv2 describe-target-health \
    --target-group-arn $(terraform output -raw target_group_arn)
  # All targets should be "healthy"
  ```

- [ ] **No error logs in CloudWatch**
  ```bash
  aws logs tail /ecs/cliente-core-prod --since 10m --filter-pattern "ERROR"
  # Should be empty or minimal errors
  ```

### Application Endpoints

- [ ] **ALB DNS name obtained**
  ```bash
  terraform output alb_dns_name
  ```

- [ ] **Health endpoint responds 200 OK**
  ```bash
  curl -I http://$(terraform output -raw alb_dns_name)/api/clientes/actuator/health
  # HTTP/1.1 200 OK
  ```

- [ ] **Health check returns UP status**
  ```bash
  curl http://$(terraform output -raw alb_dns_name)/api/clientes/actuator/health
  # {"status":"UP"}
  ```

- [ ] **Application logs show successful startup**
  ```bash
  aws logs tail /ecs/cliente-core-prod --since 5m | grep "Started ClienteCoreApplication"
  ```

### Database Connectivity

- [ ] **Liquibase migrations executed successfully**
  ```bash
  aws logs tail /ecs/cliente-core-prod --since 10m | grep "liquibase"
  # Should show successful migration
  ```

- [ ] **Database connection pool healthy**
  ```bash
  # Check for connection errors in logs
  aws logs tail /ecs/cliente-core-prod --since 10m --filter-pattern "Connection"
  ```

- [ ] **RDS instance available**
  ```bash
  aws rds describe-db-instances \
    --db-instance-identifier cliente-core-prod-db \
    --query 'DBInstances[0].DBInstanceStatus'
  # Should be: "available"
  ```

### Monitoring

- [ ] **CloudWatch alarms created**
  ```bash
  aws cloudwatch describe-alarms --alarm-name-prefix cliente-core-prod
  # Should show 12 alarms
  ```

- [ ] **No alarms in ALARM state**
  ```bash
  aws cloudwatch describe-alarms --state-value ALARM
  # Should be empty
  ```

- [ ] **Container Insights enabled**
  ```bash
  aws ecs describe-clusters --clusters cliente-core-prod-cluster \
    --query 'clusters[0].settings'
  # Should show containerInsights: enabled
  ```

- [ ] **Application metrics visible in CloudWatch**
  - Check ECS CPU/Memory metrics
  - Check ALB request count
  - Check RDS connections

## Post-Deployment

### Documentation

- [ ] **Outputs saved**
  ```bash
  terraform output > outputs.txt
  ```

- [ ] **Architecture diagram updated** (if needed)

- [ ] **Runbook created** (incident response procedures)

- [ ] **Team notified of deployment**

### Security Hardening

- [ ] **Change default database password** (optional)
  ```bash
  aws rds modify-db-instance \
    --db-instance-identifier cliente-core-prod-db \
    --master-user-password <new-password> \
    --apply-immediately
  ```

- [ ] **Enable CloudTrail** (if not already enabled)

- [ ] **Review security groups** (no unexpected rules)

- [ ] **Enable MFA for AWS account** (if not enabled)

### Monitoring Setup

- [ ] **CloudWatch dashboard created** (optional)

- [ ] **SNS topic created for alarm notifications** (recommended)
  ```bash
  aws sns create-topic --name cliente-core-alerts
  ```

- [ ] **Email subscriptions added to SNS** (recommended)

- [ ] **PagerDuty/Slack integration configured** (optional)

### Backup Verification

- [ ] **RDS automated backups enabled**
  ```bash
  aws rds describe-db-instances \
    --db-instance-identifier cliente-core-prod-db \
    --query 'DBInstances[0].BackupRetentionPeriod'
  # Should be: 7
  ```

- [ ] **Latest RDS snapshot exists**
  ```bash
  aws rds describe-db-snapshots \
    --db-instance-identifier cliente-core-prod-db \
    --query 'DBSnapshots[0].SnapshotCreateTime'
  ```

- [ ] **Backup restoration tested** (recommended)

### Performance Testing

- [ ] **Load testing performed** (recommended)
  - Use Apache Bench, JMeter, or k6
  - Test with expected production load
  - Verify auto-scaling triggers

- [ ] **Performance baseline established**
  - Document P50, P95, P99 latencies
  - Document throughput (requests/second)

- [ ] **Database query performance verified**
  - Check slow query logs
  - Review query plans for N+1 queries

## Production Enhancements (Future)

### HTTPS/TLS

- [ ] **ACM certificate requested**
- [ ] **DNS validation completed**
- [ ] **HTTPS listener added to ALB**
- [ ] **HTTP → HTTPS redirect configured**

### Custom Domain

- [ ] **Route53 hosted zone created**
- [ ] **A record pointing to ALB**
- [ ] **DNS propagation verified**

### Security

- [ ] **WAF rules configured**
- [ ] **AWS Shield Standard enabled**
- [ ] **Secrets rotation enabled**
- [ ] **OAuth2/JWT authentication implemented**

### Scalability

- [ ] **Multi-AZ RDS enabled**
- [ ] **Dual NAT Gateways deployed**
- [ ] **Read replicas added** (if needed)
- [ ] **CloudFront CDN configured** (optional)

### Cost Optimization

- [ ] **Reserved Instances purchased** (RDS)
- [ ] **Savings Plans activated** (Fargate)
- [ ] **VPC Endpoints configured** (ECR, Secrets Manager)
- [ ] **CloudWatch Logs compression enabled**

## Rollback Plan

### If deployment fails:

1. **Check ECS service events**
   ```bash
   aws ecs describe-services \
     --cluster cliente-core-prod-cluster \
     --services cliente-core-prod-service \
     --query 'services[0].events[0:10]'
   ```

2. **Review CloudWatch Logs**
   ```bash
   aws logs tail /ecs/cliente-core-prod --follow
   ```

3. **Rollback to previous task definition**
   ```bash
   aws ecs update-service \
     --cluster cliente-core-prod-cluster \
     --service cliente-core-prod-service \
     --task-definition cliente-core-prod:<previous-revision>
   ```

4. **Destroy infrastructure if needed**
   ```bash
   terraform destroy
   ```

5. **Restore from backup if database corrupted**
   ```bash
   aws rds restore-db-instance-from-db-snapshot \
     --db-instance-identifier cliente-core-prod-db-restored \
     --db-snapshot-identifier <snapshot-id>
   ```

## Sign-Off

- [ ] **Infrastructure Lead:** _______________
- [ ] **Security Lead:** _______________
- [ ] **Operations Lead:** _______________
- [ ] **Product Owner:** _______________

**Deployment Date:** _______________
**Deployment Time:** _______________
**Deployed By:** _______________

---

**Last updated:** 2025-11-04
