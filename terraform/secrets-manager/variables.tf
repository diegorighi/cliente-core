# ============================================================================
# Terraform Variables - cliente-core Secrets Manager
# ============================================================================
# CRITICAL: Never commit terraform.tfvars or *.auto.tfvars to Git!
# These files contain sensitive values and must be .gitignored

variable "aws_region" {
  description = "AWS region where secrets will be stored"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "secret_recovery_days" {
  description = "Number of days to retain deleted secrets before permanent deletion"
  type        = number
  default     = 7

  validation {
    condition     = var.secret_recovery_days >= 7 && var.secret_recovery_days <= 30
    error_message = "Recovery window must be between 7 and 30 days."
  }
}

# ============================================================================
# Database Configuration Variables
# ============================================================================

variable "db_host" {
  description = "PostgreSQL database host (RDS endpoint or localhost)"
  type        = string
  sensitive   = true

  # Example: cliente-core-dev.abc123.us-east-1.rds.amazonaws.com
}

variable "db_port" {
  description = "PostgreSQL database port"
  type        = number
  default     = 5432

  validation {
    condition     = var.db_port > 0 && var.db_port < 65536
    error_message = "Database port must be between 1 and 65535."
  }
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_name) > 0
    error_message = "Database name cannot be empty."
  }
}

variable "db_username" {
  description = "PostgreSQL database username"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_username) > 0
    error_message = "Database username cannot be empty."
  }
}

variable "db_password" {
  description = "PostgreSQL database password (NEVER commit this!)"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_password) >= 16
    error_message = "Database password must be at least 16 characters for security."
  }
}

# ============================================================================
# JWT Authentication Variables
# ============================================================================

variable "jwt_signing_key" {
  description = "JWT signing key for OAuth2 (NEVER commit this!)"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.jwt_signing_key) >= 32
    error_message = "JWT signing key must be at least 32 characters for security."
  }
}

# ============================================================================
# Monitoring Variables
# ============================================================================

variable "sns_alarm_topic_arn" {
  description = "SNS topic ARN for CloudWatch alarms (optional)"
  type        = string
  default     = ""
}

# ============================================================================
# Tagging Variables
# ============================================================================

variable "additional_tags" {
  description = "Additional tags to apply to all resources"
  type        = map(string)
  default     = {}
}
