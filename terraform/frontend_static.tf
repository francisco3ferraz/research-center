data "aws_caller_identity" "current" {}

locals {
  frontend_static_bucket_name = coalesce(
    var.frontend_static_bucket_name,
    "${local.name_prefix}-${data.aws_caller_identity.current.account_id}-frontend"
  )
}

resource "aws_s3_bucket" "frontend_static" {
  bucket = local.frontend_static_bucket_name

  tags = {
    Name = local.frontend_static_bucket_name
    Role = "frontend-static"
  }
}

resource "aws_s3_bucket_public_access_block" "frontend_static" {
  bucket = aws_s3_bucket.frontend_static.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "frontend_static" {
  bucket = aws_s3_bucket.frontend_static.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "frontend_static" {
  bucket = aws_s3_bucket.frontend_static.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_cloudfront_origin_access_control" "frontend_static" {
  name                              = "${local.name_prefix}-frontend-oac"
  description                       = "Allow CloudFront to read the private frontend S3 bucket."
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "frontend_static" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "${local.name_prefix} static frontend"
  default_root_object = "index.html"
  price_class         = var.frontend_cloudfront_price_class
  aliases             = var.frontend_domain_aliases

  origin {
    domain_name              = aws_s3_bucket.frontend_static.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.frontend_static.id
    origin_id                = "s3-${aws_s3_bucket.frontend_static.id}"
  }

  origin {
    domain_name = aws_instance.backend.public_dns
    origin_id   = "backend-${aws_instance.backend.id}"

    custom_origin_config {
      http_port              = 8080
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  default_cache_behavior {
    target_origin_id       = "s3-${aws_s3_bucket.frontend_static.id}"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    cache_policy_id        = "658327ea-f89d-4fab-a63d-7e88639e58f6"
    compress               = true
  }

  ordered_cache_behavior {
    path_pattern             = "/research-center/api/*"
    target_origin_id         = "backend-${aws_instance.backend.id}"
    viewer_protocol_policy   = "redirect-to-https"
    allowed_methods          = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods           = ["GET", "HEAD"]
    cache_policy_id          = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad"
    origin_request_policy_id = "b689b0a8-53d0-40ab-baf2-68738e2966ac"
    compress                 = true
  }

  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/index.html"
  }

  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/index.html"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn            = var.frontend_cloudfront_certificate_arn
    cloudfront_default_certificate = var.frontend_cloudfront_certificate_arn == null
    minimum_protocol_version       = var.frontend_cloudfront_certificate_arn == null ? "TLSv1" : "TLSv1.2_2021"
    ssl_support_method             = var.frontend_cloudfront_certificate_arn == null ? null : "sni-only"
  }

  tags = {
    Name = "${local.name_prefix}-frontend-cdn"
    Role = "frontend-static"
  }
}

resource "aws_s3_bucket_policy" "frontend_static" {
  bucket = aws_s3_bucket.frontend_static.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontRead"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.frontend_static.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.frontend_static.arn
          }
        }
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.frontend_static]
}
