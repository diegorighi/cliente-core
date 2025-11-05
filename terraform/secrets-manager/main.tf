# ============================================================================
# AWS Secrets Manager - cliente-core Database Credentials
# ============================================================================
# CRITICAL: No sensitive data hardcoded here!
# Secrets values are provided via environment variables or terraform.tfvars

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Optional: Store Terraform state in S3 (recommended for production)
  # backend "s3" {
  #   bucket         = "vanessa-mudanca-terraform-state"
  #   key            = "cliente-core/secrets-manager/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-state-lock"
  # }
}

provider "aws" {
  region = var.aws_region

  # Tags applied to all resources
  default_tags {
    tags = {
      Project     = "Va Nessa Mudança"
      Service     = "cliente-core"
      ManagedBy   = "Terraform"
      Environment = var.environment
    }
  }
}

# ============================================================================
# Secret: Database Credentials
# ============================================================================

resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "cliente-core/${var.environment}/database"
  description = "PostgreSQL database credentials for cliente-core (${var.environment})"

  # Recovery window before permanent deletion (7-30 days)
  recovery_window_in_days = var.secret_recovery_days

  tags = {
    Name        = "cliente-core-db-${var.environment}"
    SecretType  = "Database"
    Rotation    = "Manual" # Change to "Automatic" when rotation is implemented
  }
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id

  # CRITICAL: Values come from variables, NOT hardcoded!
  secret_string = jsonencode({
    host     = var.db_host
    port     = var.db_port
    database = var.db_name
    username = var.db_username
    password = var.db_password
    url      = "jdbc:postgresql://${var.db_host}:${var.db_port}/${var.db_name}"
  })
}

# ============================================================================
# Secret: JWT Signing Key (for future OAuth2 implementation)
# ============================================================================

resource "aws_secretsmanager_secret" "jwt_key" {
  name        = "cliente-core/${var.environment}/jwt-key"
  description = "JWT signing key for OAuth2 authentication (${var.environment})"

  recovery_window_in_days = var.secret_recovery_days

  tags = {
    Name       = "cliente-core-jwt-${var.environment}"
    SecretType = "Authentication"
  }
}

resource "aws_secretsmanager_secret_version" "jwt_key" {
  secret_id = aws_secretsmanager_secret.jwt_key.id

  secret_string = jsonencode({
    signing_key = var.jwt_signing_key
    algorithm   = "HS256"
    expiration  = "86400" # 24 hours in seconds
  })
}

# ============================================================================
# IAM Role: Allow ECS/EC2 to read secrets
# ============================================================================

resource "aws_iam_role" "app_secrets_role" {
  name        = "cliente-core-secrets-role-${var.environment}"
  description = "IAM role for cliente-core to read secrets from AWS Secrets Manager"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = [
            "ecs-tasks.amazonaws.com",
            "ec2.amazonaws.com"
          ]
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy" "secrets_read_policy" {
  name = "SecretsManagerReadPolicy"
  role = aws_iam_role.app_secrets_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = [
          aws_secretsmanager_secret.db_credentials.arn,
          aws_secretsmanager_secret.jwt_key.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:DescribeKey"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "kms:ViaService" = "secretsmanager.${var.aws_region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

# ============================================================================
# CloudWatch Alarms: Monitor secret access
# ============================================================================

resource "aws_cloudwatch_log_metric_filter" "secrets_access" {
  name           = "cliente-core-secrets-access-${var.environment}"
  log_group_name = "/aws/ecs/cliente-core-${var.environment}" # Adjust to your log group

  pattern = "[eventName = GetSecretValue]"

  metric_transformation {
    name      = "SecretsAccessCount"
    namespace = "ClienteCore/Secrets"
    value     = "1"
  }
}

resource "aws_cloudwatch_metric_alarm" "excessive_secrets_access" {
  alarm_name          = "cliente-core-excessive-secrets-access-${var.environment}"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "SecretsAccessCount"
  namespace           = "ClienteCore/Secrets"
  period              = "300" # 5 minutes
  statistic           = "Sum"
  threshold           = "100"
  alarm_description   = "Alert when secrets are accessed more than 100 times in 5 minutes"
  treat_missing_data  = "notBreaching"

  alarm_actions = [var.sns_alarm_topic_arn]
}

# ============================================================================
# Outputs: ARNs and names for application configuration
# ============================================================================

output "db_secret_arn" {
  description = "ARN of the database credentials secret"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "db_secret_name" {
  description = "Name of the database credentials secret (use in Spring Boot)"
  value       = aws_secretsmanager_secret.db_credentials.name
}

output "jwt_secret_arn" {
  description = "ARN of the JWT signing key secret"
  value       = aws_secretsmanager_secret.jwt_key.arn
}

output "jwt_secret_name" {
  description = "Name of the JWT signing key secret"
  value       = aws_secretsmanager_secret.jwt_key.name
}

output "app_role_arn" {
  description = "ARN of the IAM role for the application to assume"
  value       = aws_iam_role.app_secrets_role.arn
}

output "app_role_name" {
  description = "Name of the IAM role"
  value       = aws_iam_role.app_secrets_role.name
}
