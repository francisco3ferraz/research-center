resource "aws_lb" "frontend" {
  name               = "${local.short_name_prefix}-fe-alb"
  load_balancer_type = "application"
  internal           = false
  security_groups    = [aws_security_group.frontend.id]
  subnets            = aws_subnet.public[*].id

  enable_deletion_protection = true
  drop_invalid_header_fields = true

  tags = {
    Name = "${local.name_prefix}-frontend-alb"
    Role = "frontend"
  }
}

resource "aws_lb_target_group" "frontend" {
  name        = "${local.short_name_prefix}-fe-tg"
  port        = var.frontend_target_port
  protocol    = "HTTP"
  target_type = "instance"
  vpc_id      = aws_vpc.main.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200-399"
    path                = "/"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "${local.name_prefix}-frontend-tg"
    Role = "frontend"
  }
}

resource "aws_lb_target_group_attachment" "frontend" {
  for_each = var.frontend_target_instance_ids

  target_group_arn = aws_lb_target_group.frontend.arn
  target_id        = each.value
  port             = var.frontend_target_port
}

resource "aws_lb_listener" "frontend_http_forward" {
  count = var.frontend_certificate_arn == null ? 1 : 0

  load_balancer_arn = aws_lb.frontend.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
}

resource "aws_lb_listener" "frontend_http_redirect" {
  count = var.frontend_certificate_arn == null ? 0 : 1

  load_balancer_arn = aws_lb.frontend.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "frontend_https" {
  count = var.frontend_certificate_arn == null ? 0 : 1

  load_balancer_arn = aws_lb.frontend.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = var.frontend_certificate_arn
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
}
