resource "aws_security_group" "frontend" {
  name        = "${local.name_prefix}-frontend-sg"
  description = "Allow public web traffic for the client web application tier."
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "${local.name_prefix}-frontend-sg"
  }
}

resource "aws_vpc_security_group_ingress_rule" "frontend_http" {
  security_group_id = aws_security_group.frontend.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  ip_protocol       = "tcp"
  to_port           = 80
  description       = "Public HTTP access"
}

resource "aws_vpc_security_group_ingress_rule" "frontend_https" {
  security_group_id = aws_security_group.frontend.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  ip_protocol       = "tcp"
  to_port           = 443
  description       = "Public HTTPS access"
}

resource "aws_vpc_security_group_egress_rule" "frontend_all_egress" {
  security_group_id = aws_security_group.frontend.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
  description       = "Allow outbound traffic"
}

resource "aws_security_group" "app" {
  name        = "${local.name_prefix}-app-sg"
  description = "Allow controlled access to the WildFly backend server."
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "${local.name_prefix}-app-sg"
  }
}

resource "aws_vpc_security_group_ingress_rule" "app_backend" {
  for_each = toset(var.backend_allowed_cidrs)

  security_group_id = aws_security_group.app.id
  cidr_ipv4         = each.value
  from_port         = 8080
  ip_protocol       = "tcp"
  to_port           = 8080
  description       = "WildFly backend HTTP access"
}

resource "aws_vpc_security_group_ingress_rule" "app_ssh" {
  for_each = toset(var.ssh_allowed_cidrs)

  security_group_id = aws_security_group.app.id
  cidr_ipv4         = each.value
  from_port         = 22
  ip_protocol       = "tcp"
  to_port           = 22
  description       = "Restricted SSH access"
}

resource "aws_vpc_security_group_egress_rule" "app_all_egress" {
  security_group_id = aws_security_group.app.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
  description       = "Allow outbound traffic for package downloads and external services"
}

resource "aws_security_group" "database" {
  name        = "${local.name_prefix}-database-sg"
  description = "Allow PostgreSQL traffic only from the application server security group."
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "${local.name_prefix}-database-sg"
  }
}

resource "aws_vpc_security_group_ingress_rule" "database_postgres_from_app" {
  security_group_id            = aws_security_group.database.id
  referenced_security_group_id = aws_security_group.app.id
  from_port                    = 5432
  ip_protocol                  = "tcp"
  to_port                      = 5432
  description                  = "PostgreSQL from WildFly application server only"
}

resource "aws_vpc_security_group_egress_rule" "database_all_egress" {
  security_group_id = aws_security_group.database.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
  description       = "Allow outbound traffic for managed database maintenance"
}
