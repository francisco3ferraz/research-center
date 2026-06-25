variable "aws_region" {
  description = "AWS region where infrastructure is provisioned."
  type        = string
  default     = "eu-west-1"
}

variable "project_name" {
  description = "Project name used for resource naming and tags."
  type        = string
  default     = "research-center"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "CIDR block for the production VPC."
  type        = string
  default     = "10.40.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets, intended for internet-facing web/client resources and the app ingress tier."
  type        = list(string)
  default     = ["10.40.0.0/24", "10.40.1.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private database subnets."
  type        = list(string)
  default     = ["10.40.10.0/24", "10.40.11.0/24"]
}

variable "app_instance_type" {
  description = "EC2 instance type for the WildFly backend server."
  type        = string
  default     = "t3.micro"
}

variable "app_key_name" {
  description = "Optional EC2 key pair name for SSH access. Leave null to disable key pair login."
  type        = string
  default     = null
}

variable "ssh_allowed_cidrs" {
  description = "CIDR blocks allowed to SSH to the application server. Keep empty unless break-glass SSH is required."
  type        = list(string)
  default     = []
}

variable "backend_allowed_cidrs" {
  description = "CIDR blocks allowed to reach the WildFly HTTP backend on port 8080."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "db_name" {
  description = "Initial PostgreSQL database name."
  type        = string
  default     = "research_center"
}

variable "db_username" {
  description = "PostgreSQL master username."
  type        = string
  default     = "research_admin"
}

variable "db_password" {
  description = "PostgreSQL master password. Provide through TF_VAR_db_password or a secure secrets workflow."
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS PostgreSQL instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage_gb" {
  description = "Allocated RDS storage in GiB."
  type        = number
  default     = 20
}
