# ========================================
# Terraform Outputs
# cliente-core Infrastructure
# ========================================

# ========================================
# Application Access
# ========================================

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer (use this to access the application)"
  value       = module.alb.alb_dns_name
}

output "application_url" {
  description = "Full application URL"
  value       = "http://${module.alb.alb_dns_name}/api/clientes"
}

output "health_check_url" {
  description = "Health check endpoint URL"
  value       = "http://${module.alb.alb_dns_name}${var.health_check_path}"
}

# ========================================
# Database Connection
# ========================================

output "rds_endpoint" {
  description = "RDS endpoint (host:port)"
  value       = module.rds.db_endpoint
  sensitive   = true
}

output "rds_address" {
  description = "RDS hostname only"
  value       = module.rds.db_address
  sensitive   = true
}

output "rds_port" {
  description = "RDS port number"
  value       = module.rds.db_port
}

output "database_name" {
  description = "Database name"
  value       = var.db_name
}

output "database_secret_arn" {
  description = "ARN of Secrets Manager secret containing database credentials"
  value       = module.security.db_secret_arn
}

# ========================================
# ECS Resources
# ========================================

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs.service_name
}

output "ecs_task_definition_arn" {
  description = "ARN of the ECS task definition"
  value       = module.ecs.task_definition_arn
}

# ========================================
# ECR Repository
# ========================================

output "ecr_repository_url" {
  description = "URL of the ECR repository (use for docker push)"
  value       = module.security.ecr_repository_url
}

output "ecr_repository_name" {
  description = "Name of the ECR repository"
  value       = module.security.ecr_repository_name
}

# ========================================
# Network Configuration
# ========================================

output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (ALB)"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs (ECS tasks)"
  value       = module.vpc.private_subnet_ids
}

output "database_subnet_ids" {
  description = "Database subnet IDs (RDS)"
  value       = module.vpc.database_subnet_ids
}

# ========================================
# Security Groups
# ========================================

output "alb_security_group_id" {
  description = "Security group ID for ALB"
  value       = module.security.alb_security_group_id
}

output "ecs_security_group_id" {
  description = "Security group ID for ECS tasks"
  value       = module.security.ecs_security_group_id
}

output "rds_security_group_id" {
  description = "Security group ID for RDS"
  value       = module.security.rds_security_group_id
}

# ========================================
# CloudWatch Logs
# ========================================

output "cloudwatch_log_group_name" {
  description = "CloudWatch log group name for ECS tasks"
  value       = aws_cloudwatch_log_group.ecs_logs.name
}

# ========================================
# Deployment Instructions
# ========================================

output "next_steps" {
  description = "Next steps for deployment"
  value = <<-EOT

  ========================================
  DEPLOYMENT NEXT STEPS
  ========================================

  1. Build and push Docker image:

     cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
     mvn clean package -DskipTests

     aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${module.security.ecr_repository_url}

     docker build -t cliente-core .
     docker tag cliente-core:latest ${module.security.ecr_repository_url}:latest
     docker push ${module.security.ecr_repository_url}:latest

  2. Access the application:

     Application URL: http://${module.alb.alb_dns_name}/api/clientes
     Health Check:    http://${module.alb.alb_dns_name}${var.health_check_path}

  3. View logs:

     aws logs tail /ecs/${var.project_name}-${var.environment} --follow

  4. Connect to database (via bastion or SSM Session Manager):

     psql -h ${module.rds.db_address} -U ${var.db_username} -d ${var.db_name}

  5. Monitor ECS service:

     aws ecs describe-services --cluster ${module.ecs.cluster_name} --services ${module.ecs.service_name}

  6. Scale ECS service manually:

     aws ecs update-service --cluster ${module.ecs.cluster_name} --service ${module.ecs.service_name} --desired-count 3

  ========================================
  EOT
}
