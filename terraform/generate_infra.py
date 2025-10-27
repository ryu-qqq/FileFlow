#!/usr/bin/env python3
"""
FileFlow Infrastructure Generator
infra-wizard를 사용하여 Terraform 코드를 생성합니다.
"""

from infra_wizard.core.generator import CodeGenerator
from pathlib import Path

def generate_elasticache_redis():
    """ElastiCache Redis 인프라 생성"""
    params = {
        "service_name": "fileflow",
        "environment": "prod",
        "aws_region": "ap-northeast-2",
        "node_type": "cache.t3.small",
        "num_cache_nodes": 1
    }

    components = {
        "automatic_failover": False,
        "snapshot_retention": 7
    }

    generator = CodeGenerator()
    output_dir = Path("elasticache-redis")

    print(f"🔨 Generating ElastiCache Redis infrastructure...")
    result = generator.generate(
        template="elasticache-redis",
        service_name="fileflow",
        output_dir=output_dir,
        params=params,
        components=components
    )

    if result.success:
        print(f"✅ ElastiCache Redis generated at: {output_dir}")
    else:
        print(f"❌ Failed: {result.error}")

def generate_s3_bucket():
    """S3 Bucket 인프라 생성"""
    params = {
        "service_name": "fileflow",
        "environment": "prod",
        "aws_region": "ap-northeast-2"
    }

    components = {
        "versioning": True,
        "lifecycle_enabled": True,
        "public_access": False
    }

    generator = CodeGenerator()
    output_dir = Path("s3-bucket")

    print(f"🔨 Generating S3 Bucket infrastructure...")
    result = generator.generate(
        template="s3-bucket",
        service_name="fileflow",
        output_dir=output_dir,
        params=params,
        components=components
    )

    if result.success:
        print(f"✅ S3 Bucket generated at: {output_dir}")
    else:
        print(f"❌ Failed: {result.error}")

def generate_sqs_queue():
    """SQS Queue 인프라 생성"""
    params = {
        "service_name": "fileflow",
        "environment": "prod",
        "aws_region": "ap-northeast-2",
        "message_retention": 345600  # 4 days in seconds
    }

    components = {
        "fifo_queue": False,
        "dead_letter_queue": True,
        "encryption": True
    }

    generator = CodeGenerator()
    output_dir = Path("sqs-queue")

    print(f"🔨 Generating SQS Queue infrastructure...")
    result = generator.generate(
        template="sqs-queue",
        service_name="fileflow",
        output_dir=output_dir,
        params=params,
        components=components
    )

    if result.success:
        print(f"✅ SQS Queue generated at: {output_dir}")
    else:
        print(f"❌ Failed: {result.error}")

def generate_ecs_service():
    """ECS Service 인프라 생성"""
    params = {
        "service_name": "fileflow",
        "environment": "prod",
        "aws_region": "ap-northeast-2",
        "cpu": 1024,
        "memory": 2048,
        "desired_count": 1,
        "container_port": 8080
    }

    components = {
        "load_balancer": True,
        "auto_scaling": True,
        "database": "shared-rds",  # Shared RDS 사용
        "cache": "none"  # 별도 Redis 사용
    }

    generator = CodeGenerator()
    output_dir = Path("ecs-service")

    print(f"🔨 Generating ECS Service infrastructure...")
    result = generator.generate(
        template="ecs-service",
        service_name="fileflow",
        output_dir=output_dir,
        params=params,
        components=components
    )

    if result.success:
        print(f"✅ ECS Service generated at: {output_dir}")
    else:
        print(f"❌ Failed: {result.error}")

if __name__ == "__main__":
    print("🧙 FileFlow Infrastructure Generator")
    print("=" * 50)

    generate_elasticache_redis()
    print()
    generate_s3_bucket()
    print()
    generate_sqs_queue()
    print()
    generate_ecs_service()

    print()
    print("=" * 50)
    print("✅ All infrastructure generated successfully!")
    print()
    print("📝 Next steps:")
    print("1. Review generated Terraform code")
    print("2. Initialize Terraform: terraform init")
    print("3. Plan changes: terraform plan")
    print("4. Apply (via Atlantis PR)")
