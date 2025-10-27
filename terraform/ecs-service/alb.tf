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
  source = "../../modules/alb"

  # ALB Configuration
  name               = "${local.name_prefix}-alb"
  internal           = false
  load_balancer_type = "application"

  # Network Configuration
  vpc_id             = local.vpc_id
  public_subnet_ids  = local.public_subnet_ids
  security_group_ids = [aws_security_group.fileflow_alb.id]

  # Target Group Configuration
  target_group_name = "${local.name_prefix}-tg"
  target_group_port = local.container_port
  target_group_protocol = "HTTP"
  target_type = "ip"

  # Health Check Configuration
  health_check = {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = ""
    matcher             = "200-299"
  }

  # SSL Certificate (if provided)
  # NOTE: SSL certificate ARN should be provided for production
  # Get from: aws acm list-certificates --region ap-northeast-2
  certificate_arn = null

  # Access Logs (optional)
  enable_deletion_protection = true

  tags = merge(
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
  value       = module.fileflow_alb.dns_name

  tags = local.required_tags
}
