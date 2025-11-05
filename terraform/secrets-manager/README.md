# AWS Secrets Manager - cliente-core

This directory contains Terraform configuration for managing database credentials and JWT keys securely using AWS Secrets Manager.

**CRITICAL:** No sensitive data is stored in Git. All credentials are provided via environment variables or a local `terraform.tfvars` file (which is .gitignored).

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Secure Workflow](#secure-workflow)
- [Terraform Commands](#terraform-commands)
- [Integration with Spring Boot](#integration-with-spring-boot)
- [Troubleshooting](#troubleshooting)
- [Security Best Practices](#security-best-practices)

---

## Overview

### What This Does

1. **Creates AWS Secrets Manager secrets** for:
   - PostgreSQL database credentials (host, port, database, username, password)
   - JWT signing key for OAuth2 authentication

2. **Creates IAM role** that allows ECS tasks/EC2 instances to read secrets

3. **Sets up CloudWatch alarms** to monitor excessive secret access (potential security breach)

4. **Integrates with Spring Boot** via `AwsSecretsManagerConfig.java` to fetch credentials at startup

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Terraform                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Creates:                                                  │  │
│  │ 1. Secret: cliente-core/prod/database                    │  │
│  │ 2. Secret: cliente-core/prod/jwt-key                     │  │
│  │ 3. IAM Role: cliente-core-secrets-role-prod              │  │
│  │ 4. CloudWatch Alarms: excessive access monitoring        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    AWS Secrets Manager                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ {                                                         │  │
│  │   "host": "cliente-core-prod.abc123.rds.amazonaws.com",  │  │
│  │   "port": 5432,                                           │  │
│  │   "database": "vanessa_mudanca_clientes",                 │  │
│  │   "username": "app_user",                                 │  │
│  │   "password": "secure-password-32-chars"                  │  │
│  │ }                                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                      │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ AwsSecretsManagerConfig.java                             │  │
│  │ 1. Assumes IAM role at startup                           │  │
│  │ 2. Calls GetSecretValue API                              │  │
│  │ 3. Parses JSON secret                                    │  │
│  │ 4. Configures DataSource bean                            │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

### 1. AWS CLI Installed and Configured

```bash
# Check AWS CLI version (requires v2.x)
aws --version

# Configure AWS credentials (one-time setup)
aws configure

# Test connection
aws sts get-caller-identity
```

### 2. Terraform Installed

```bash
# Check Terraform version (requires >= 1.5.0)
terraform version

# Install if not present (macOS)
brew install terraform
```

### 3. AWS Permissions Required

Your AWS IAM user/role needs these permissions:

- `secretsmanager:CreateSecret`
- `secretsmanager:PutSecretValue`
- `secretsmanager:GetSecretValue`
- `iam:CreateRole`
- `iam:AttachRolePolicy`
- `cloudwatch:PutMetricAlarm`

**Recommended:** Use `AdministratorAccess` for initial setup, then restrict to least privilege.

### 4. PostgreSQL RDS Instance

You need an existing RDS PostgreSQL instance. If you don't have one, create it first:

```bash
# Example: Create RDS instance (not covered in this Terraform)
# Use AWS Console or separate Terraform module
```

---

## Quick Start

### Step 1: Clone Repository and Navigate

```bash
cd /path/to/cliente-core
cd terraform/secrets-manager
```

### Step 2: Create terraform.tfvars

```bash
# Copy example file
cp terraform.tfvars.example terraform.tfvars

# Edit with your actual values
nano terraform.tfvars
```

**CRITICAL:** Never commit `terraform.tfvars` to Git! It's already .gitignored.

### Step 3: Generate Secure JWT Key

```bash
# Generate 256-bit (32 bytes) key
openssl rand -base64 32

# Output example: 5k9J+2v8X/qY7tR4mN6pL3wH1sA0fD9gC8bE5xZ2vU=
# Copy this to jwt_signing_key in terraform.tfvars
```

### Step 4: Fill terraform.tfvars

```hcl
aws_region  = "us-east-1"
environment = "prod"

db_host     = "cliente-core-prod.abc123xyz.us-east-1.rds.amazonaws.com"
db_port     = 5432
db_name     = "vanessa_mudanca_clientes"
db_username = "app_user"
db_password = "REPLACE_WITH_SECURE_PASSWORD_MIN_16_CHARS"

jwt_signing_key = "REPLACE_WITH_OPENSSL_GENERATED_KEY"
```

### Step 5: Initialize Terraform

```bash
terraform init
```

### Step 6: Preview Changes

```bash
terraform plan
```

Review output carefully. You should see:
- 2 secrets to be created
- 1 IAM role to be created
- 1 IAM policy to be created
- 2 CloudWatch resources to be created

### Step 7: Apply Configuration

```bash
terraform apply

# Type 'yes' when prompted
```

### Step 8: Verify Secrets Created

```bash
# List secrets
aws secretsmanager list-secrets

# Get secret value (to verify)
aws secretsmanager get-secret-value \
  --secret-id cliente-core/prod/database \
  --query SecretString \
  --output text | jq .
```

---

## Secure Workflow

### For Development (Local Machine)

**DO NOT use AWS Secrets Manager in dev profile!**

Use `application-dev.yml` with localhost credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vanessa_mudanca_clientes
    username: postgres
    password: senha123  # OK for local dev only!
```

### For Staging/Production (AWS ECS/EC2)

**ALWAYS use AWS Secrets Manager in prod/staging profiles!**

1. **Deploy Terraform** (creates secrets and IAM role)
2. **Attach IAM role** to ECS task definition or EC2 instance
3. **Set environment variables** in ECS task or systemd service:

```bash
export AWS_REGION=us-east-1
export AWS_SECRETS_NAME=cliente-core/prod/database
export SPRING_PROFILES_ACTIVE=prod
```

4. **Start application** - Spring Boot automatically fetches credentials

### Rotating Credentials

**CRITICAL:** When rotating database passwords:

1. **Update RDS password first**:
```bash
aws rds modify-db-instance \
  --db-instance-identifier cliente-core-prod \
  --master-user-password NEW_SECURE_PASSWORD \
  --apply-immediately
```

2. **Update Terraform variable**:
```bash
# Edit terraform.tfvars
db_password = "NEW_SECURE_PASSWORD"
```

3. **Apply Terraform change**:
```bash
terraform apply
```

4. **Restart application** (ECS will fetch new secret automatically)

---

## Terraform Commands

### Common Operations

```bash
# Preview changes
terraform plan

# Apply changes
terraform apply

# Destroy all resources (DANGEROUS!)
terraform destroy

# Show current state
terraform show

# List all resources
terraform state list

# Get specific output
terraform output db_secret_name
terraform output app_role_arn
```

### Troubleshooting Commands

```bash
# Enable detailed logging
export TF_LOG=DEBUG
terraform apply

# Format Terraform files
terraform fmt

# Validate configuration
terraform validate

# Refresh state (sync with AWS)
terraform refresh
```

---

## Integration with Spring Boot

### How It Works

1. **Application starts** with profile `prod` or `staging`
2. **AwsSecretsManagerConfig** activates (disabled in `dev`)
3. **Spring Boot creates** `SecretsManagerClient` bean
4. **Client fetches secret** using IAM role (no hardcoded credentials!)
5. **Secret JSON is parsed** and injected into `DataSource` bean
6. **PostgreSQL connection** is established with fetched credentials

### Configuration Files

**src/main/resources/application.yml** (profile-specific):
```yaml
---
spring:
  config:
    activate:
      on-profile: prod,staging
  cloud:
    aws:
      secretsmanager:
        enabled: true
      region:
        static: ${AWS_REGION:us-east-1}
      credentials:
        instance-profile: true
```

**src/main/java/.../config/AwsSecretsManagerConfig.java**:
- Fetches secret at startup
- Parses JSON
- Configures DataSource bean
- Masks passwords in logs

### Testing Integration

#### Local Testing (Without AWS)

Use `dev` profile - AWS integration is disabled:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Testing (With AWS)

**Prerequisites:**
- Terraform applied successfully
- IAM role attached to instance
- Environment variables set

```bash
export AWS_REGION=us-east-1
export AWS_SECRETS_NAME=cliente-core/prod/database
export SPRING_PROFILES_ACTIVE=prod

mvn spring-boot:run
```

**Expected logs:**
```
INFO  AwsSecretsManagerConfig - Initializing AWS Secrets Manager client in region: us-east-1
INFO  AwsSecretsManagerConfig - Fetching database credentials from AWS Secrets Manager: cliente-core/prod/database
INFO  AwsSecretsManagerConfig - Successfully fetched database credentials from Secrets Manager
INFO  AwsSecretsManagerConfig - Database URL: jdbc:postgresql://cliente-core-prod.abc123.rds.amazonaws.com:5432/***
```

---

## Troubleshooting

### Error: "Secret not found"

**Symptom:**
```
ERROR AwsSecretsManagerConfig - Failed to fetch database credentials from AWS Secrets Manager
ResourceNotFoundException: Secrets Manager can't find the specified secret.
```

**Solution:**
1. Verify secret exists:
```bash
aws secretsmanager list-secrets | grep cliente-core
```

2. Check secret name matches environment variable:
```bash
echo $AWS_SECRETS_NAME
# Should be: cliente-core/prod/database
```

3. Verify AWS region matches:
```bash
echo $AWS_REGION
# Should be: us-east-1 (or your configured region)
```

---

### Error: "Access Denied"

**Symptom:**
```
ERROR AwsSecretsManagerConfig - Failed to fetch database credentials
AccessDeniedException: User is not authorized to perform: secretsmanager:GetSecretValue
```

**Solution:**
1. Verify IAM role is attached to ECS task/EC2 instance
2. Check IAM policy includes `secretsmanager:GetSecretValue` permission
3. Verify resource ARN in policy matches secret ARN:

```bash
# Get secret ARN
aws secretsmanager describe-secret \
  --secret-id cliente-core/prod/database \
  --query ARN \
  --output text

# Compare with IAM policy
aws iam get-role-policy \
  --role-name cliente-core-secrets-role-prod \
  --policy-name SecretsManagerReadPolicy
```

---

### Error: "Connection refused" after fetching secret

**Symptom:**
```
INFO  AwsSecretsManagerConfig - Successfully fetched database credentials
ERROR HikariPool - Exception during pool initialization
org.postgresql.util.PSQLException: Connection refused
```

**Solution:**
1. Verify RDS security group allows inbound traffic from ECS/EC2:
```bash
aws ec2 describe-security-groups \
  --group-ids sg-123456 \
  --query 'SecurityGroups[0].IpPermissions'
```

2. Check database host is accessible from application:
```bash
# From ECS/EC2 instance
telnet cliente-core-prod.abc123.rds.amazonaws.com 5432
```

3. Verify secret contains correct host/port:
```bash
aws secretsmanager get-secret-value \
  --secret-id cliente-core/prod/database \
  --query SecretString \
  --output text | jq .
```

---

### Error: "Invalid JSON in secret"

**Symptom:**
```
ERROR AwsSecretsManagerConfig - Failed to parse secret JSON
com.fasterxml.jackson.core.JsonParseException: Unexpected character
```

**Solution:**
1. Verify secret JSON format:
```bash
aws secretsmanager get-secret-value \
  --secret-id cliente-core/prod/database \
  --query SecretString \
  --output text | jq .
```

2. Expected format:
```json
{
  "host": "cliente-core-prod.abc123.rds.amazonaws.com",
  "port": 5432,
  "database": "vanessa_mudanca_clientes",
  "username": "app_user",
  "password": "secure-password",
  "url": "jdbc:postgresql://host:port/database"
}
```

3. Fix JSON in Terraform `terraform.tfvars` and reapply:
```bash
terraform apply
```

---

## Security Best Practices

### ✅ DO

1. **Use IAM roles** for ECS/EC2 instances (never hardcode AWS keys)
2. **Rotate credentials** every 90 days (set reminder!)
3. **Monitor CloudWatch alarms** for excessive secret access
4. **Use strong passwords** (minimum 16 characters, mix of upper/lower/numbers/symbols)
5. **Enable MFA** on AWS account with Secrets Manager access
6. **Audit access logs** regularly in CloudTrail
7. **Use separate secrets** for dev/staging/prod environments
8. **Encrypt Terraform state** (use S3 backend with encryption)

### ❌ DON'T

1. **Never commit** `terraform.tfvars` or `*.auto.tfvars` to Git
2. **Never hardcode** credentials in `application.yml`
3. **Never share** `terraform.tfstate` files (contain secret IDs)
4. **Never log** full secret values (use `maskUrl()` method)
5. **Never use** production credentials in dev/staging
6. **Never store** credentials in environment variables on your laptop
7. **Never email** or Slack credentials in plain text
8. **Never disable** CloudWatch alarms in production

---

## Cost Estimation

### AWS Secrets Manager Pricing (us-east-1)

- **Secret storage:** $0.40/secret/month
- **API calls:** $0.05 per 10,000 calls

**Example for cliente-core:**
- 2 secrets (database + JWT) = $0.80/month
- ~1,000 API calls/month (startup + health checks) = $0.005/month
- **Total: ~$0.81/month ($9.70/year)**

**Note:** This is negligible compared to security risk of hardcoded credentials!

---

## Additional Resources

- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [Spring Cloud AWS Reference](https://docs.awspring.io/spring-cloud-aws/docs/3.0.0/reference/html/index.html#spring-cloud-aws-secrets-manager)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)

---

**Last Updated:** 2025-11-05
**Version:** 1.0
**Maintainer:** DevOps Team - Va Nessa Mudança
