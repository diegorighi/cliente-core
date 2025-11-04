# ========================================
# Input Variables
# cliente-core Infrastructure Configuration
# ========================================

# ========================================
# General Configuration
# ========================================

variable "project_name" {
  description = "Project name used for resource naming and tagging"
  type        = string
  default     = "cliente-core"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "prod"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "aws_region" {
  description = "AWS region for resource deployment"
  type        = string
  default     = "us-east-1"
}

variable "enable_free_tier" {
  description = "Enable AWS Free Tier optimizations (RDS db.t3.micro, reduced tasks, etc). Valid for 12 months from AWS account creation."
  type        = bool
  default     = false
}

# ========================================
# VPC Configuration
# ========================================

variable "vpc_cidr" {
  description = "CIDR block for VPC (10.0.0.0/16 provides 65,536 IPs)"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones for multi-AZ deployment"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnet internet access (required for ECS)"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Use single NAT Gateway for cost optimization (MVP only, not HA)"
  type        = bool
  default     = true
}

# ========================================
# RDS Configuration
# ========================================

variable "db_instance_class" {
  description = "RDS instance class (db.t3.micro for Free Tier, db.t4g.micro for MVP, db.t4g.small for production)"
  type        = string
  default     = "db.t4g.micro"

  validation {
    condition     = can(regex("^db\\.(t3|t4g)\\.(micro|small|medium)$", var.db_instance_class))
    error_message = "Must be a valid RDS instance class (db.t3.micro, db.t4g.micro, db.t4g.small, etc)."
  }
}

variable "db_allocated_storage" {
  description = "Initial allocated storage in GB"
  type        = number
  default     = 20
}

variable "db_max_allocated_storage" {
  description = "Maximum storage for autoscaling in GB"
  type        = number
  default     = 100
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "vanessa_mudanca_clientes"
}

variable "db_username" {
  description = "Master username for database"
  type        = string
  default     = "clientecore_admin"
  sensitive   = true
}

variable "db_backup_retention_period" {
  description = "Backup retention period in days"
  type        = number
  default     = 7
}

variable "db_backup_window" {
  description = "Preferred backup window (UTC)"
  type        = string
  default     = "03:00-04:00"
}

variable "db_maintenance_window" {
  description = "Preferred maintenance window (UTC)"
  type        = string
  default     = "mon:04:00-mon:05:00"
}

variable "db_multi_az" {
  description = "Enable Multi-AZ deployment for high availability (disabled for MVP cost savings)"
  type        = bool
  default     = false
}

# ========================================
# ECS Configuration
# ========================================

variable "ecs_task_cpu" {
  description = "Fargate task CPU units (512 = 0.5 vCPU)"
  type        = number
  default     = 512

  validation {
    condition     = contains([256, 512, 1024, 2048, 4096], var.ecs_task_cpu)
    error_message = "Valid values: 256, 512, 1024, 2048, 4096."
  }
}

variable "ecs_task_memory" {
  description = "Fargate task memory in MB (1024 = 1 GB)"
  type        = number
  default     = 1024

  validation {
    condition     = var.ecs_task_memory >= 512 && var.ecs_task_memory <= 30720
    error_message = "Memory must be between 512 MB and 30720 MB."
  }
}

variable "container_port" {
  description = "Container port (Spring Boot default is 8080)"
  type        = number
  default     = 8080
}

variable "ecs_desired_count" {
  description = "Desired number of ECS tasks (1 for Free Tier, minimum 2 for HA)"
  type        = number
  default     = 2
}

variable "ecs_min_capacity" {
  description = "Minimum number of tasks for auto-scaling (1 for Free Tier, 2+ for HA)"
  type        = number
  default     = 2
}

variable "ecs_max_capacity" {
  description = "Maximum number of tasks for auto-scaling"
  type        = number
  default     = 4
}

variable "autoscaling_target_cpu" {
  description = "Target CPU utilization percentage for auto-scaling"
  type        = number
  default     = 70
}

variable "autoscaling_target_memory" {
  description = "Target memory utilization percentage for auto-scaling"
  type        = number
  default     = 80
}

# ========================================
# ALB Configuration
# ========================================

variable "health_check_path" {
  description = "Health check endpoint path"
  type        = string
  default     = "/api/clientes/actuator/health"
}

variable "health_check_interval" {
  description = "Health check interval in seconds"
  type        = number
  default     = 30
}

variable "health_check_timeout" {
  description = "Health check timeout in seconds"
  type        = number
  default     = 5
}

variable "healthy_threshold" {
  description = "Number of consecutive successful health checks before considering target healthy"
  type        = number
  default     = 2
}

variable "unhealthy_threshold" {
  description = "Number of consecutive failed health checks before considering target unhealthy"
  type        = number
  default     = 3
}

variable "deregistration_delay" {
  description = "Time in seconds for ALB to wait before deregistering target"
  type        = number
  default     = 30
}

# ========================================
# CloudWatch Configuration
# ========================================

variable "cloudwatch_retention_days" {
  description = "CloudWatch logs retention period in days"
  type        = number
  default     = 7

  validation {
    condition     = contains([1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653], var.cloudwatch_retention_days)
    error_message = "Must be a valid CloudWatch retention period."
  }
}

# ========================================
# ECR Configuration
# ========================================

variable "ecr_image_tag_mutability" {
  description = "Image tag mutability setting (MUTABLE or IMMUTABLE)"
  type        = string
  default     = "MUTABLE"

  validation {
    condition     = contains(["MUTABLE", "IMMUTABLE"], var.ecr_image_tag_mutability)
    error_message = "Must be MUTABLE or IMMUTABLE."
  }
}

variable "ecr_scan_on_push" {
  description = "Enable image scanning on push"
  type        = bool
  default     = true
}

variable "ecr_lifecycle_count" {
  description = "Number of images to keep in ECR"
  type        = number
  default     = 5
}
