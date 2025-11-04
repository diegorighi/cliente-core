# ========================================
# ALB Module Outputs
# ========================================

output "alb_id" {
  description = "ALB ID"
  value       = aws_lb.main.id
}

output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.main.arn
}

output "alb_arn_suffix" {
  description = "ALB ARN suffix (for CloudWatch metrics)"
  value       = aws_lb.main.arn_suffix
}

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = aws_lb.main.dns_name
}

output "alb_zone_id" {
  description = "ALB Route53 zone ID (for future DNS records)"
  value       = aws_lb.main.zone_id
}

output "target_group_arn" {
  description = "Target group ARN"
  value       = aws_lb_target_group.main.arn
}

output "target_group_arn_suffix" {
  description = "Target group ARN suffix (for CloudWatch metrics)"
  value       = aws_lb_target_group.main.arn_suffix
}

output "listener_http_arn" {
  description = "HTTP listener ARN"
  value       = aws_lb_listener.http.arn
}
