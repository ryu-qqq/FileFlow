# ============================================================================
# Application Load Balancer for fileflow
# ============================================================================

# Security Group for ALB
resource "aws_security_group" "fileflow_alb" {
  name_prefix = "${local.name_prefix}-alb-"
  description = "Security group for fileflow ALB"
  vpc_id      = local.vpc_id

  # Ingress: HTTPS from anywhere
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTPS from internet"
  }

  # Ingress: HTTP from anywhere (redirect to HTTPS)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTP from internet (redirect to HTTPS)"
  }

  # Egress: Allow to ECS tasks
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "sg-${local.name_prefix}-alb"
      Component = "load-balancer"
    }
  )

  lifecycle {
    create_before_destroy = true
  }
}

# Application Load Balancer
module "fileflow_alb" {
  source = "../modules/alb"

  # Required variables
  name       = "${local.name_prefix}-alb"
  subnet_ids = local.public_subnet_ids
  vpc_id     = local.vpc_id

  # Optional configuration
  internal           = false
  security_group_ids = [aws_security_group.fileflow_alb.id]

  # Target Group
  target_groups = {
    fileflow = {
      port        = local.container_port
      protocol    = "HTTP"
      target_type = "ip"

      health_check = {
        enabled             = true
        healthy_threshold   = 2
        unhealthy_threshold = 3
        timeout             = 5
        interval            = 30
        path                = "/health"
        matcher             = "200-299"
      }
    }
  }

  # HTTP Listener (forward to Target Group - HTTPS disabled until certificate provided)
  http_listeners = {
    default = {
      port     = 80
      protocol = "HTTP"

      default_action = {
        type             = "forward"
        target_group_key = "fileflow"
      }
    }
  }

  # HTTPS Listener (commented out until certificate is provided)
  # https_listeners = {
  #   default = {
  #     port            = 443
  #     protocol        = "HTTPS"
  #     certificate_arn = "arn:aws:acm:ap-northeast-2:ACCOUNT_ID:certificate/CERT_ID"
  #     ssl_policy      = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  #
  #     default_action = {
  #       type             = "forward"
  #       target_group_key = "fileflow"
  #     }
  #   }
  # }

  enable_deletion_protection = false

  common_tags = merge(
    local.required_tags,
    {
      Name      = "alb-${local.name_prefix}"
      Component = "load-balancer"
    }
  )
}

# Export ALB DNS name to SSM for other services
resource "aws_ssm_parameter" "alb_dns_name" {
  name        = "/services/${local.service_name}/alb-dns-name"
  description = "ALB DNS name for ${local.service_name}"
  type        = "String"
  value       = module.fileflow_alb.alb_dns_name

  tags = local.required_tags
}
