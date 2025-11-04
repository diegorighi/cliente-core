# ========================================
# Main Terraform Configuration
# cliente-core Spring Boot Microservice
# ========================================

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Remote state backend (S3 + DynamoDB locking)
  # Uncomment after creating the S3 bucket and DynamoDB table
  # backend "s3" {
  #   bucket         = "vanessa-mudanca-terraform-state"
  #   key            = "cliente-core/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-state-lock"
  # }
}

# ========================================
# Provider Configuration
# ========================================

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Service     = "cliente-core"
      CostCenter  = "Engineering"
    }
  }
}

# ========================================
# VPC Module
# Network foundation with public, private, and database subnets
# ========================================

module "vpc" {
  source = "./modules/vpc"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
  enable_nat_gateway = var.enable_nat_gateway
  single_nat_gateway = var.single_nat_gateway # Cost optimization for MVP
}

# ========================================
# Security Module
# Security groups, IAM roles, and Secrets Manager
# ========================================

module "security" {
  source = "./modules/security"

  project_name       = var.project_name
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id

  # Database credentials for Secrets Manager
  db_username        = var.db_username
  db_name            = var.db_name

  # Dependencies
  depends_on = [module.vpc]
}

# ========================================
# RDS Module
# PostgreSQL 16 database with optimized configuration
# ========================================

module "rds" {
  source = "./modules/rds"

  project_name          = var.project_name
  environment           = var.environment

  # Instance configuration
  instance_class        = var.db_instance_class
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_max_allocated_storage

  # Database configuration
  db_name               = var.db_name
  db_username           = var.db_username
  db_password           = module.security.db_password

  # Network configuration
  db_subnet_ids         = module.vpc.database_subnet_ids
  security_group_ids    = [module.security.rds_security_group_id]

  # Backup configuration
  backup_retention_period = var.db_backup_retention_period
  backup_window           = var.db_backup_window
  maintenance_window      = var.db_maintenance_window

  # High availability (disabled for MVP cost optimization)
  multi_az              = var.db_multi_az

  # Dependencies
  depends_on = [module.vpc, module.security]
}

# ========================================
# ALB Module
# Application Load Balancer for HTTP traffic
# ========================================

module "alb" {
  source = "./modules/alb"

  project_name           = var.project_name
  environment            = var.environment
  vpc_id                 = module.vpc.vpc_id
  public_subnet_ids      = module.vpc.public_subnet_ids
  alb_security_group_id  = module.security.alb_security_group_id

  # Health check configuration
  health_check_path      = var.health_check_path
  health_check_interval  = var.health_check_interval
  health_check_timeout   = var.health_check_timeout
  healthy_threshold      = var.healthy_threshold
  unhealthy_threshold    = var.unhealthy_threshold

  # Dependencies
  depends_on = [module.vpc, module.security]
}

# ========================================
# ECS Module
# Fargate cluster and service for Spring Boot application
# ========================================

module "ecs" {
  source = "./modules/ecs"

  project_name       = var.project_name
  environment        = var.environment
  aws_region         = var.aws_region

  # Task configuration
  task_cpu           = var.ecs_task_cpu
  task_memory        = var.ecs_task_memory
  container_port     = var.container_port

  # Service configuration
  desired_count      = var.ecs_desired_count
  min_capacity       = var.ecs_min_capacity
  max_capacity       = var.ecs_max_capacity

  # Network configuration
  private_subnet_ids = module.vpc.private_subnet_ids
  ecs_security_group_id = module.security.ecs_security_group_id

  # Load balancer integration
  target_group_arn   = module.alb.target_group_arn

  # IAM roles
  task_execution_role_arn = module.security.ecs_task_execution_role_arn
  task_role_arn          = module.security.ecs_task_role_arn

  # Database secrets
  db_secret_arn      = module.security.db_secret_arn
  db_host            = module.rds.db_endpoint
  db_port            = module.rds.db_port
  db_name            = var.db_name

  # ECR repository
  ecr_repository_url = module.security.ecr_repository_url

  # Dependencies
  depends_on = [module.vpc, module.security, module.rds, module.alb]
}

# ========================================
# CloudWatch Log Groups
# Centralized logging
# ========================================

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/${var.project_name}-${var.environment}"
  retention_in_days = var.cloudwatch_retention_days

  tags = {
    Name = "${var.project_name}-${var.environment}-ecs-logs"
  }
}
