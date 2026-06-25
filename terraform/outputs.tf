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

output "frontend_static_bucket_name" {
  description = "S3 bucket that stores the generated Nuxt frontend files."
  value       = aws_s3_bucket.frontend_static.bucket
}

output "frontend_cloudfront_distribution_id" {
  description = "CloudFront distribution ID for the static frontend."
  value       = aws_cloudfront_distribution.frontend_static.id
}

output "frontend_cloudfront_domain_name" {
  description = "CloudFront domain name for the static frontend."
  value       = aws_cloudfront_distribution.frontend_static.domain_name
}

output "frontend_static_url" {
  description = "HTTPS URL for the static frontend."
  value       = "https://${aws_cloudfront_distribution.frontend_static.domain_name}"
}

output "postgres_endpoint" {
  description = "RDS PostgreSQL endpoint."
  value       = aws_db_instance.postgres.endpoint
}
