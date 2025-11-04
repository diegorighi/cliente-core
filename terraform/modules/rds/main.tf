# ========================================
# RDS Module - PostgreSQL Database
# ========================================
# PostgreSQL 16 optimized for Spring Boot + Liquibase
# ========================================

# ========================================
# DB Subnet Group
# ========================================

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = var.db_subnet_ids

  tags = {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  }
}

# ========================================
# DB Parameter Group
# Optimized for Spring Boot + HikariCP + Liquibase
# ========================================

resource "aws_db_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-pg16-params"
  family = "postgres16"

  # Connection settings (HikariCP max 10 connections)
  parameter {
    name  = "max_connections"
    value = "100" # Allow headroom for admin connections
  }

  # Memory settings for db.t4g.micro (1 GB RAM)
  parameter {
    name  = "shared_buffers"
    value = "{DBInstanceClassMemory/32768}" # ~32 MB (conservative for micro)
  }

  parameter {
    name  = "effective_cache_size"
    value = "{DBInstanceClassMemory/16384}" # ~64 MB
  }

  parameter {
    name  = "work_mem"
    value = "4096" # 4 MB per operation
  }

  parameter {
    name  = "maintenance_work_mem"
    value = "65536" # 64 MB for VACUUM, CREATE INDEX
  }

  # Query planner settings
  parameter {
    name  = "random_page_cost"
    value = "1.1" # SSD storage (gp3)
  }

  parameter {
    name  = "effective_io_concurrency"
    value = "200" # SSD optimization
  }

  # WAL settings for performance
  parameter {
    name  = "wal_buffers"
    value = "2048" # 2 MB (16 MB shared_buffers / 8)
  }

  parameter {
    name  = "checkpoint_completion_target"
    value = "0.9"
  }

  # Logging for troubleshooting
  parameter {
    name  = "log_statement"
    value = "none" # Don't log statements (use app-level logging)
  }

  parameter {
    name  = "log_min_duration_statement"
    value = "1000" # Log queries taking > 1 second
  }

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  # Liquibase optimization
  parameter {
    name  = "lock_timeout"
    value = "30000" # 30 seconds (Liquibase changesets)
  }

  parameter {
    name  = "statement_timeout"
    value = "60000" # 60 seconds (long-running migrations)
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-pg16-params"
  }
}

# ========================================
# RDS Instance
# ========================================

resource "aws_db_instance" "main" {
  identifier = "${var.project_name}-${var.environment}-db"

  # Engine configuration
  engine               = "postgres"
  engine_version       = "16.6"
  instance_class       = var.instance_class
  parameter_group_name = aws_db_parameter_group.main.name

  # Storage configuration
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true
  iops                  = 3000  # gp3 baseline
  storage_throughput    = 125   # gp3 baseline (MB/s)

  # Database configuration
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 5432

  # Network configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = var.security_group_ids
  publicly_accessible    = false

  # High availability (disabled for MVP)
  multi_az = var.multi_az

  # Backup configuration
  backup_retention_period = var.backup_retention_period
  backup_window           = var.backup_window
  maintenance_window      = var.maintenance_window
  skip_final_snapshot     = false
  final_snapshot_identifier = "${var.project_name}-${var.environment}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  # Monitoring
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  monitoring_interval             = 60 # Enhanced monitoring every 60 seconds
  monitoring_role_arn            = aws_iam_role.rds_monitoring.arn

  # Performance Insights (free tier for 7 days retention)
  performance_insights_enabled    = true
  performance_insights_retention_period = 7

  # Upgrade settings
  auto_minor_version_upgrade = true
  allow_major_version_upgrade = false

  # Deletion protection (enable for production)
  deletion_protection = var.environment == "prod" ? true : false

  # Apply changes immediately for non-prod
  apply_immediately = var.environment != "prod"

  tags = {
    Name = "${var.project_name}-${var.environment}-db"
  }

  lifecycle {
    ignore_changes = [
      final_snapshot_identifier # Prevent changes on every apply
    ]
  }
}

# ========================================
# IAM Role for Enhanced Monitoring
# ========================================

resource "aws_iam_role" "rds_monitoring" {
  name = "${var.project_name}-${var.environment}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-rds-monitoring-role"
  }
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  role       = aws_iam_role.rds_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

# ========================================
# CloudWatch Alarms
# ========================================

resource "aws_cloudwatch_metric_alarm" "database_cpu" {
  alarm_name          = "${var.project_name}-${var.environment}-db-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = "300" # 5 minutes
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "Database CPU utilization is too high"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.id
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-cpu-alarm"
  }
}

resource "aws_cloudwatch_metric_alarm" "database_memory" {
  alarm_name          = "${var.project_name}-${var.environment}-db-memory-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "FreeableMemory"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "104857600" # 100 MB
  alarm_description   = "Database freeable memory is too low"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.id
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-memory-alarm"
  }
}

resource "aws_cloudwatch_metric_alarm" "database_storage" {
  alarm_name          = "${var.project_name}-${var.environment}-db-storage-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "2147483648" # 2 GB
  alarm_description   = "Database free storage is too low"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.id
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-storage-alarm"
  }
}

resource "aws_cloudwatch_metric_alarm" "database_connections" {
  alarm_name          = "${var.project_name}-${var.environment}-db-connections-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80" # 80% of max_connections
  alarm_description   = "Database connections are too high"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.id
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-connections-alarm"
  }
}
