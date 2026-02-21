# CloudWatch Alarm + SNS + Slack 연동 가이드

## 개요

FileFlow 프로젝트의 모니터링 알람 파이프라인 구성 가이드.
CloudWatch Alarm → SNS Topic → AWS Chatbot → Slack 채널로 알림이 전달됩니다.

## 아키텍처

```
CloudWatch Alarm
    ↓ (alarm_actions)
SNS Topic (critical / warning / info)
    ↓ (subscription)
AWS Chatbot
    ↓
Slack Channel (#fileflow-alerts)
```

## 3-Tier SNS 토픽 설계

| 토픽 | 네이밍 규칙 | 용도 |
|------|------------|------|
| Critical | `{env}-monitoring-critical` | 즉시 대응 필요 (서비스 다운, 데이터 유실) |
| Warning | `{env}-monitoring-warning` | 주의 필요 (큐 적체, 처리 지연, DLQ) |
| Info | `{env}-monitoring-info` | 참고 (스케일링, 배포 완료) |

## SQS CloudWatch Alarm 종류

| 알람 | 메트릭 | 임계값 (external_download) | 임계값 (file_processing) | 심각도 |
|------|--------|--------------------------|------------------------|--------|
| Message Age | `ApproximateAgeOfOldestMessage` | > 600초 (10분) | > 900초 (15분) | WARNING |
| Queue Depth | `ApproximateNumberOfMessagesVisible` | > 1000 | > 500 | WARNING |
| DLQ Messages | `ApproximateNumberOfMessagesVisible` (DLQ) | > 1 | > 1 | WARNING |

## alarm_actions 연결 패턴

### data source로 SNS 토픽 조회 (권장)

```hcl
# SNS 토픽이 다른 Terraform 모듈에서 생성된 경우
data "aws_sns_topic" "warning" {
  name = "${var.environment}-monitoring-warning"
}

module "my_queue" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/sqs?ref=main"
  # ...
  alarm_actions  = [data.aws_sns_topic.warning.arn]
  alarm_ok_actions = [data.aws_sns_topic.warning.arn]
}
```

### SSM Parameter로 조회 (cross-stack)

```hcl
data "aws_ssm_parameter" "sns_warning_arn" {
  name = "/${var.project_name}/monitoring/sns-warning-arn"
}

module "my_queue" {
  # ...
  alarm_actions = [data.aws_ssm_parameter.sns_warning_arn.value]
}
```

## AWS Chatbot → Slack 설정

### Terraform 변수

```hcl
# terraform/environments/prod/monitoring/terraform.tfvars
enable_chatbot     = true
slack_channel_id   = "C0AGKC5S46M"   # Slack 채널 ID
slack_workspace_id = "T0A8AT1Q9QQ"    # Slack 워크스페이스 ID
```

### Slack 채널 ID 확인 방법

1. Slack 채널 우클릭 → "채널 세부정보 보기"
2. 하단 "채널 ID" 복사 (예: `C0AGKC5S46M`)

### Slack 워크스페이스 ID 확인 방법

1. Slack 워크스페이스 설정 → "About this workspace"
2. Workspace ID 복사 (예: `T0A8AT1Q9QQ`)

## Chatbot 활성화 절차

```bash
cd terraform/environments/prod/monitoring

terraform plan \
  -var="enable_chatbot=true" \
  -var="slack_channel_id=C0AGKC5S46M" \
  -var="slack_workspace_id=T0A8AT1Q9QQ"

terraform apply \
  -var="enable_chatbot=true" \
  -var="slack_channel_id=C0AGKC5S46M" \
  -var="slack_workspace_id=T0A8AT1Q9QQ"
```

## 검증

```bash
# 1. SQS alarm_actions 확인
cd terraform/environments/prod/sqs
terraform plan  # alarm_actions 변경사항 확인

# 2. SNS 토픽 구독 확인
aws sns list-subscriptions-by-topic \
  --topic-arn arn:aws:sns:ap-northeast-2:ACCOUNT_ID:prod-monitoring-warning

# 3. 테스트 알림 발송
aws cloudwatch set-alarm-state \
  --alarm-name "test-alarm" \
  --state-value ALARM \
  --state-reason "Testing SNS notification"
```
