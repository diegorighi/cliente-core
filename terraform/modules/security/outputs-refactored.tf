# ========================================
# Security Module Outputs (Refactored)
# ========================================
# ECR outputs removed (now in shared infrastructure)
# ========================================

# ========================================
# Security Groups
# ========================================

output "alb_security_group_id" {
  description = "Security group ID for ALB"
  value       = aws_security_group.alb.id
}

output "ecs_security_group_id" {
  description = "Security group ID for ECS tasks"
  value       = aws_security_group.ecs.id
}

output "rds_security_group_id" {
  description = "Security group ID for RDS"
  value       = aws_security_group.rds.id
}

# ========================================
# Secrets Manager
# ========================================

output "db_secret_arn" {
  description = "ARN of database credentials secret"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "db_password" {
  description = "Auto-generated database password (sensitive)"
  value       = random_password.db_password.result
  sensitive   = true
}

# ========================================
# IAM Roles
# ========================================

output "ecs_task_execution_role_arn" {
  description = "ARN of ECS task execution role"
  value       = aws_iam_role.ecs_task_execution.arn
}

output "ecs_task_role_arn" {
  description = "ARN of ECS task role"
  value       = aws_iam_role.ecs_task.arn
}
