# ========================================
# Security Module Variables (Refactored)
# ========================================

variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID (from shared infrastructure)"
  type        = string
}

variable "db_username" {
  description = "Master username for database"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Database name"
  type        = string
}

variable "kms_key_arn" {
  description = "KMS key ARN for encryption (from shared infrastructure)"
  type        = string
}
