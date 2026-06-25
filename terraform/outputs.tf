output "vpc_id" {
  description = "ID of the production VPC."
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs for internet-facing web/client resources."
  value       = aws_subnet.public[*].id
}

output "private_database_subnet_ids" {
  description = "Private subnet IDs for RDS PostgreSQL."
  value       = aws_subnet.private[*].id
}

output "backend_instance_id" {
  description = "EC2 instance ID for the WildFly backend server."
  value       = aws_instance.backend.id
}

output "backend_public_dns" {
  description = "Public DNS name for the WildFly backend server."
  value       = aws_instance.backend.public_dns
}

output "frontend_load_balancer_dns" {
  description = "Public DNS name for the frontend application load balancer."
  value       = aws_lb.frontend.dns_name
}

output "frontend_target_group_arn" {
  description = "Target group ARN for frontend web app instances."
  value       = aws_lb_target_group.frontend.arn
}

output "postgres_endpoint" {
  description = "RDS PostgreSQL endpoint."
  value       = aws_db_instance.postgres.endpoint
}
