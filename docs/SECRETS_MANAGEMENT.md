# Secrets Management - cliente-core

**Quick reference guide for developers on how credentials are managed securely.**

---

## TL;DR

- **Local development:** Credentials in `application-dev.yml` (OK for localhost)
- **Production/Staging:** Credentials in AWS Secrets Manager (NEVER in Git!)
- **Terraform:** Infrastructure as code for secrets (in `terraform/secrets-manager/`)
- **Spring Boot:** Auto-fetches credentials at startup using IAM roles

---

## Why Secrets Manager?

### ❌ BEFORE (Hardcoded - Insecure!)

```yaml
# application.yml - NEVER DO THIS IN PRODUCTION!
spring:
  datasource:
    url: jdbc:postgresql://prod-db.aws.com:5432/clientes
    username: admin
    password: SuperSecret123!  # ⚠️ EXPOSED IN GIT!
```

**Problems:**
- Credentials committed to Git (visible to everyone with repo access)
- Can't rotate passwords without code deployment
- Difficult to audit who accessed credentials
- One compromised laptop = entire database exposed

### ✅ AFTER (AWS Secrets Manager - Secure!)

```yaml
# application.yml - Safe to commit
spring:
  cloud:
    aws:
      secretsmanager:
        enabled: true  # Fetches from AWS at runtime
```

**Benefits:**
- ✅ Zero credentials in Git
- ✅ Rotate passwords without code changes
- ✅ CloudTrail audit logs for every access
- ✅ IAM-based access control
- ✅ Encrypted at rest and in transit
- ✅ CloudWatch alarms for security monitoring

---

## How It Works

### Development Environment (Your Laptop)

1. Start application with `dev` profile:
```bash
mvn spring-boot:run  # Uses application-dev.yml
```

2. Spring Boot reads `application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vanessa_mudanca_clientes
    username: postgres
    password: senha123  # OK for local dev only!
```

3. **AWS Secrets Manager is NOT used** (disabled in dev profile)

### Production Environment (AWS ECS/EC2)

1. ECS task starts with environment variables:
```bash
AWS_REGION=us-east-1
AWS_SECRETS_NAME=cliente-core/prod/database
SPRING_PROFILES_ACTIVE=prod
```

2. Spring Boot activates `prod` profile and triggers `AwsSecretsManagerConfig`

3. Config class calls AWS Secrets Manager API:
```java
SecretsManagerClient client = SecretsManagerClient.builder()
    .region(Region.US_EAST_1)
    .credentialsProvider(DefaultCredentialsProvider.create())  // Uses IAM role!
    .build();

GetSecretValueResponse response = client.getSecretValue(request);
String secretJson = response.secretString();
```

4. Secret JSON is parsed and injected into DataSource:
```json
{
  "host": "cliente-core-prod.abc123.rds.amazonaws.com",
  "port": 5432,
  "database": "vanessa_mudanca_clientes",
  "username": "app_user",
  "password": "secure-password-from-secrets-manager"
}
```

5. PostgreSQL connection established with fetched credentials

---

## For Developers: What You Need to Know

### Local Development

**Just run the application normally:**
```bash
mvn spring-boot:run
```

That's it! Application uses localhost credentials from `application-dev.yml`.

### Adding New Secrets

If you need to add a new secret (e.g., API key for external service):

1. **Add to Terraform configuration** (`terraform/secrets-manager/main.tf`):
```hcl
resource "aws_secretsmanager_secret" "external_api_key" {
  name        = "cliente-core/${var.environment}/external-api-key"
  description = "API key for ExternalService integration"
}

resource "aws_secretsmanager_secret_version" "external_api_key" {
  secret_id     = aws_secretsmanager_secret.external_api_key.id
  secret_string = var.external_api_key
}
```

2. **Add variable** (`terraform/secrets-manager/variables.tf`):
```hcl
variable "external_api_key" {
  description = "API key for ExternalService (NEVER commit!)"
  type        = string
  sensitive   = true
}
```

3. **Add to terraform.tfvars** (NOT committed to Git):
```hcl
external_api_key = "actual-api-key-value"
```

4. **Apply Terraform:**
```bash
cd terraform/secrets-manager
terraform apply
```

5. **Fetch in Spring Boot:**
```java
@Component
public class ExternalServiceClient {
    private final String apiKey;

    public ExternalServiceClient(SecretsManagerClient client) {
        GetSecretValueResponse response = client.getSecretValue(
            GetSecretValueRequest.builder()
                .secretId("cliente-core/prod/external-api-key")
                .build()
        );
        this.apiKey = response.secretString();
    }
}
```

### Testing with Real AWS Secrets (Optional)

If you want to test AWS integration locally:

1. **Configure AWS CLI:**
```bash
aws configure
# Enter your AWS Access Key ID
# Enter your AWS Secret Access Key
```

2. **Set environment variables:**
```bash
export AWS_REGION=us-east-1
export AWS_SECRETS_NAME=cliente-core/dev/database  # Use dev secrets!
export SPRING_PROFILES_ACTIVE=staging
```

3. **Run application:**
```bash
mvn spring-boot:run
```

**Note:** Most developers don't need to do this. Only DevOps or when troubleshooting AWS integration.

---

## For DevOps: Deployment Checklist

### First-Time Setup

1. **Create RDS database** (if not exists):
```bash
# Use AWS Console or Terraform module
# Generate strong password (min 16 chars)
openssl rand -base64 24
```

2. **Navigate to Terraform directory:**
```bash
cd terraform/secrets-manager
```

3. **Create terraform.tfvars:**
```bash
cp terraform.tfvars.example terraform.tfvars
nano terraform.tfvars
```

4. **Fill in all values:**
```hcl
aws_region  = "us-east-1"
environment = "prod"

db_host     = "cliente-core-prod.abc123xyz.us-east-1.rds.amazonaws.com"
db_port     = 5432
db_name     = "vanessa_mudanca_clientes"
db_username = "app_user"
db_password = "REPLACE_WITH_SECURE_PASSWORD"

jwt_signing_key = "$(openssl rand -base64 32)"
```

5. **Initialize and apply:**
```bash
terraform init
terraform plan
terraform apply
```

6. **Verify secrets created:**
```bash
aws secretsmanager list-secrets | grep cliente-core
```

7. **Get IAM role ARN:**
```bash
terraform output app_role_arn
# Example: arn:aws:iam::123456789012:role/cliente-core-secrets-role-prod
```

8. **Attach IAM role to ECS task definition:**
```json
{
  "taskRoleArn": "arn:aws:iam::123456789012:role/cliente-core-secrets-role-prod",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "cliente-core",
      "environment": [
        {"name": "AWS_REGION", "value": "us-east-1"},
        {"name": "AWS_SECRETS_NAME", "value": "cliente-core/prod/database"},
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"}
      ]
    }
  ]
}
```

9. **Deploy application to ECS**

10. **Verify application logs:**
```bash
aws logs tail /aws/ecs/cliente-core-prod --follow

# Expected:
# INFO AwsSecretsManagerConfig - Successfully fetched database credentials
# INFO HikariPool - HikariPool-1 - Start completed.
```

### Password Rotation

**Every 90 days (set calendar reminder!):**

1. **Generate new password:**
```bash
openssl rand -base64 24
```

2. **Update RDS password:**
```bash
aws rds modify-db-instance \
  --db-instance-identifier cliente-core-prod \
  --master-user-password NEW_PASSWORD \
  --apply-immediately
```

3. **Update terraform.tfvars:**
```bash
nano terraform.tfvars
# Change db_password = "NEW_PASSWORD"
```

4. **Apply Terraform:**
```bash
terraform apply
```

5. **Restart ECS service** (picks up new secret automatically):
```bash
aws ecs update-service \
  --cluster cliente-core-prod-cluster \
  --service cliente-core-prod-service \
  --force-new-deployment
```

6. **Monitor logs for successful startup**

---

## Security Checklist

### ✅ Before Committing Code

- [ ] No passwords in `application.yml` or `application-prod.yml`
- [ ] No API keys in Java source files
- [ ] `terraform.tfvars` is .gitignored (verify with `git status`)
- [ ] No `terraform.tfstate` files in Git
- [ ] Secrets only referenced by name, never by value

### ✅ Before Production Deployment

- [ ] Secrets created in AWS Secrets Manager
- [ ] IAM role attached to ECS task with correct permissions
- [ ] CloudWatch alarms configured for excessive access
- [ ] Database password is strong (min 16 chars, alphanumeric + symbols)
- [ ] JWT signing key is cryptographically random (use `openssl rand`)
- [ ] Security group allows ECS → RDS traffic only
- [ ] RDS is NOT publicly accessible

### ✅ Regular Maintenance

- [ ] Rotate database password every 90 days
- [ ] Review CloudTrail logs monthly for unauthorized access
- [ ] Review CloudWatch alarms weekly
- [ ] Update Terraform providers quarterly (`terraform init -upgrade`)
- [ ] Audit IAM roles/policies quarterly

---

## Troubleshooting

### "Application won't start - connection refused"

**Check:**
1. Profile is set correctly: `echo $SPRING_PROFILES_ACTIVE`
2. AWS region matches: `echo $AWS_REGION`
3. Secret exists: `aws secretsmanager get-secret-value --secret-id cliente-core/prod/database`
4. IAM role is attached to ECS task
5. Security group allows ECS → RDS traffic

### "Access Denied to Secrets Manager"

**Check:**
1. IAM role has `secretsmanager:GetSecretValue` permission
2. Resource ARN in policy matches secret ARN
3. ECS task is using correct task role (not execution role!)

### "Secret not found"

**Check:**
1. Secret name matches environment variable: `echo $AWS_SECRETS_NAME`
2. Secret exists in correct region: `aws secretsmanager list-secrets --region us-east-1`
3. Terraform applied successfully: `terraform state list`

---

## Additional Documentation

- **Full Terraform documentation:** [`terraform/secrets-manager/README.md`](../terraform/secrets-manager/README.md)
- **Spring Boot integration:** [`src/main/java/.../config/AwsSecretsManagerConfig.java`](../src/main/java/br/com/vanessa_mudanca/cliente_core/config/AwsSecretsManagerConfig.java)
- **AWS Secrets Manager pricing:** [aws.amazon.com/secrets-manager/pricing](https://aws.amazon.com/secrets-manager/pricing/)

---

**Last Updated:** 2025-11-05
**Questions?** Ask in #devops Slack channel or open issue in GitHub
