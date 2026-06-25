data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-kernel-*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_iam_role" "app" {
  name = "${local.name_prefix}-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${local.name_prefix}-app-role"
  }
}

resource "aws_iam_instance_profile" "app" {
  name = "${local.name_prefix}-app-profile"
  role = aws_iam_role.app.name
}

resource "aws_instance" "backend" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.app_instance_type
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.app.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.app.name
  key_name                    = var.app_key_name

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  root_block_device {
    encrypted   = true
    volume_size = 20
    volume_type = "gp3"
  }

  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/deploy.sh", {
    datasource_jndi         = "java:/ResearchCenterDS"
    datasource_name         = "ResearchCenterDS"
    db_host                 = aws_db_instance.postgres.address
    db_name                 = var.db_name
    db_password             = var.db_password
    db_port                 = aws_db_instance.postgres.port
    db_username             = var.db_username
    docker_buildx_version   = var.docker_buildx_version
    docker_compose_version  = var.docker_compose_version
    postgres_driver_version = var.postgres_driver_version
    project_name            = var.project_name
    repository_ref          = var.repository_ref
    repository_url          = var.repository_url
    wildfly_admin_password  = var.wildfly_admin_password
  })

  tags = {
    Name = "${local.name_prefix}-wildfly-backend"
    Role = "backend"
  }
}
