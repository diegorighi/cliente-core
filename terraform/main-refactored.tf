# ========================================
# cliente-core Terraform Configuration
# ========================================
# Microservice-specific infrastructure
# Consumes shared infrastructure via remote state
# Managed by: Development Team
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
  backend "s3" {
    bucket         = "vanessa-mudanca-terraform-state"
    key            = "cliente-core/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
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
      Team        = "Development"
      CostCenter  = "Engineering"
    }
  }
}

# ========================================
# Remote State Data Source (Shared Infrastructure)
# ========================================

data "terraform_remote_state" "shared" {
  backend = "s3"

  config = {
    bucket = "vanessa-mudanca-terraform-state"
    key    = "infra-shared/terraform.tfstate"
    region = "us-east-1"
  }
}

# ========================================
# Security Module (Microservice-Specific)
# ========================================
# Creates MS-specific security groups, IAM roles, and secrets
# ========================================

module "security" {
  source = "./modules/security"

  project_name = var.project_name
  environment  = var.environment

  # CONSUME shared VPC
  vpc_id       = data.terraform_remote_state.shared.outputs.vpc_id

  # Database credentials for Secrets Manager
  db_username  = var.db_username
  db_name      = var.db_name

  # CONSUME shared KMS key
  kms_key_arn  = data.terraform_remote_state.shared.outputs.kms_key_arn
}

# ========================================
# RDS Module (Microservice-Specific)
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

  # CONSUME shared infrastructure
  db_subnet_ids         = data.terraform_remote_state.shared.outputs.database_subnet_ids
  kms_key_arn           = data.terraform_remote_state.shared.outputs.kms_key_arn

  # MS-specific security group
  security_group_ids    = [module.security.rds_security_group_id]

  # Backup configuration
  backup_retention_period = var.db_backup_retention_period
  backup_window           = var.db_backup_window
  maintenance_window      = var.db_maintenance_window

  # High availability
  multi_az              = var.db_multi_az

  depends_on = [module.security]
}

# ========================================
# ALB Module (Microservice-Specific)
# ========================================

module "alb" {
  source = "./modules/alb"

  project_name           = var.project_name
  environment            = var.environment

  # CONSUME shared VPC and subnets
  vpc_id                 = data.terraform_remote_state.shared.outputs.vpc_id
  public_subnet_ids      = data.terraform_remote_state.shared.outputs.public_subnet_ids

  # MS-specific security group
  alb_security_group_id  = module.security.alb_security_group_id

  # Health check configuration
  health_check_path      = var.health_check_path
  health_check_interval  = var.health_check_interval
  health_check_timeout   = var.health_check_timeout
  healthy_threshold      = var.healthy_threshold
  unhealthy_threshold    = var.unhealthy_threshold

  depends_on = [module.security]
}

# ========================================
# ECS Module (Microservice-Specific)
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

  # CONSUME shared infrastructure
  private_subnet_ids = data.terraform_remote_state.shared.outputs.private_subnet_ids
  ecr_repository_url = data.terraform_remote_state.shared.outputs.ecr_repositories["cliente-core"]

  # MS-specific resources
  ecs_security_group_id = module.security.ecs_security_group_id
  target_group_arn      = module.alb.target_group_arn

  # IAM roles (MS-specific)
  task_execution_role_arn = module.security.ecs_task_execution_role_arn
  task_role_arn          = module.security.ecs_task_role_arn

  # Database connection (from RDS module)
  db_secret_arn      = module.security.db_secret_arn
  db_host            = module.rds.db_endpoint
  db_port            = module.rds.db_port
  db_name            = var.db_name

  depends_on = [module.security, module.rds, module.alb]
}

# ========================================
# CloudWatch Log Groups (Microservice-Specific)
# ========================================

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/${var.project_name}-${var.environment}"
  retention_in_days = var.cloudwatch_retention_days
  kms_key_id        = data.terraform_remote_state.shared.outputs.kms_key_arn

  tags = {
    Name = "${var.project_name}-${var.environment}-ecs-logs"
  }
}
