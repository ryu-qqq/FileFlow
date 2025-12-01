# Bastion Host 접근 가이드

fileflow 프로젝트에서 로컬 개발 시 AWS 리소스(RDS, ElastiCache 등)에 접근하기 위한 Bastion Host 사용 방법입니다.

## 📋 개요

- **관리 위치**: Infrastructure 레포 (`terraform/environments/prod/network`)
- **접근 방식**: AWS Systems Manager Session Manager
- **공유 패턴**: SSM Parameter Store를 통한 참조
- **보안**: SSH 키 불필요, IAM 기반 인증

## 🔧 사전 준비

### 1. Infrastructure 레포에서 Bastion 활성화

```bash
cd /path/to/infrastructure/terraform/environments/prod/network

# terraform.tfvars 또는 variables 설정
enable_bastion = true
```

### 2. Terraform 적용

```bash
terraform init
terraform plan
terraform apply
```

생성되는 리소스:
- EC2 Instance (t3.nano, private subnet)
- VPC Endpoints (SSM, SSM Messages, EC2 Messages, Logs)
- Security Groups
- IAM Role & Instance Profile
- **SSM Parameters** (자동 생성):
  - `/shared/bastion/instance-id`
  - `/shared/bastion/security-group-id`
  - `/shared/bastion/private-ip`

## 🚀 사용 방법

### 1. Instance ID 확인

#### 방법 A: Terraform Output
```bash
cd /path/to/infrastructure/terraform/environments/prod/network
terraform output bastion_instance_id
```

#### 방법 B: AWS CLI
```bash
aws ssm get-parameter --name "/shared/bastion/instance-id" --query 'Parameter.Value' --output text
```

#### 방법 C: fileflow terraform (추천)
```bash
cd /path/to/fileflow/terraform
terraform output bastion_instance_id
terraform output bastion_connection_command
```

### 2. SSM Session 시작

```bash
# Instance ID를 직접 입력
aws ssm start-session --target i-xxxxxxxxx --region ap-northeast-2

# 또는 자동으로 가져오기
aws ssm start-session --target $(aws ssm get-parameter --name "/shared/bastion/instance-id" --query 'Parameter.Value' --output text) --region ap-northeast-2
```

### 3. Bastion에서 AWS 리소스 접근

Bastion에 접속한 후:

```bash
# RDS 연결 테스트
mysql -h fileflow-db.xxxxx.ap-northeast-2.rds.amazonaws.com -u admin -p

# ElastiCache 연결 테스트
redis-cli -h fileflow-cache.xxxxx.cache.amazonaws.com -p 6379

# PostgreSQL 연결
psql -h fileflow-db.xxxxx.ap-northeast-2.rds.amazonaws.com -U admin -d fileflow
```

## 🔒 보안 그룹 규칙 추가 (필요 시)

특정 리소스에 Bastion에서만 접근하도록 하려면:

### RDS 보안 그룹에 규칙 추가

```hcl
# fileflow/terraform/shared-rds.tf 또는 별도 파일

resource "aws_security_group_rule" "rds_from_bastion" {
  type                     = "ingress"
  from_port                = 3306  # MySQL (PostgreSQL은 5432)
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = local.bastion_security_group_id
  security_group_id        = aws_security_group.rds.id
  description              = "Allow MySQL access from Bastion"
}
```

### ElastiCache 보안 그룹에 규칙 추가

```hcl
resource "aws_security_group_rule" "elasticache_from_bastion" {
  type                     = "ingress"
  from_port                = 6379  # Redis
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = local.bastion_security_group_id
  security_group_id        = aws_security_group.elasticache.id
  description              = "Allow Redis access from Bastion"
}
```

## 🛠️ 로컬 포트 포워딩 (선택 사항)

로컬 머신에서 직접 RDS/ElastiCache에 연결하려면:

### RDS 포트 포워딩

```bash
# 터미널 1: SSM Session에서 포트 포워딩
aws ssm start-session \
  --target $(aws ssm get-parameter --name "/shared/bastion/instance-id" --query 'Parameter.Value' --output text) \
  --region ap-northeast-2 \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{
    "portNumber": ["3306"],
    "localPortNumber": ["13306"],
    "host": ["fileflow-db.xxxxx.ap-northeast-2.rds.amazonaws.com"]
  }'

# 터미널 2: 로컬에서 연결
mysql -h 127.0.0.1 -P 13306 -u admin -p
```

### ElastiCache 포트 포워딩

```bash
aws ssm start-session \
  --target $(aws ssm get-parameter --name "/shared/bastion/instance-id" --query 'Parameter.Value' --output text) \
  --region ap-northeast-2 \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{
    "portNumber": ["6379"],
    "localPortNumber": ["16379"],
    "host": ["fileflow-cache.xxxxx.cache.amazonaws.com"]
  }'

# 로컬에서 연결
redis-cli -h 127.0.0.1 -p 16379
```

## 💰 비용

- **EC2 (t3.nano)**: ~$3.80/월
- **VPC Endpoints (4개)**: ~$29.20/월
- **CloudWatch Logs**: ~$1-5/월
- **데이터 전송**: 사용량에 따라

**총 예상**: ~$35-40/월

## 🔄 Bastion 중단/재시작

### 비용 절감을 위한 중단

```bash
# Infrastructure 레포에서
cd /path/to/infrastructure/terraform/environments/prod/network

# enable_bastion = false로 변경
terraform apply
```

### 재시작

```bash
# enable_bastion = true로 변경
terraform apply
```

## 📝 참고사항

1. **SSH 키 불필요**: SSM Session Manager는 IAM 인증 사용
2. **세션 로깅**: 모든 세션이 CloudWatch Logs에 기록됨 (`/aws/ssm/bastion/prod`)
3. **보안**: Bastion은 private subnet에 배치되어 public IP 없음
4. **공유 리소스**: 여러 프로젝트(fileflow, crawler 등)에서 공유 가능

## 🆘 트러블슈팅

### "TargetNotConnected" 에러

```bash
# SSM Agent 상태 확인
aws ssm describe-instance-information --filters "Key=InstanceIds,Values=i-xxxxxxxxx"

# EC2 인스턴스 상태 확인
aws ec2 describe-instances --instance-ids i-xxxxxxxxx --query 'Reservations[0].Instances[0].State.Name'
```

### IAM 권한 부족

필요한 IAM 권한:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ssm:StartSession"
      ],
      "Resource": [
        "arn:aws:ec2:ap-northeast-2:*:instance/*",
        "arn:aws:ssm:*:*:document/AWS-StartPortForwardingSession"
      ]
    }
  ]
}
```

## 📚 관련 문서

- [Infrastructure bastion-ssm 모듈](https://github.com/your-org/infrastructure/tree/main/terraform/modules/bastion-ssm)
- [AWS Systems Manager Session Manager](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager.html)
