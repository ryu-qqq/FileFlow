# Adapter-Out: AWS SQS

S3 이벤트를 SQS로부터 수신하고 업로드 세션을 업데이트하는 Outbound Adapter입니다.

## 아키텍처

```
┌─────────────────────────────────────────┐
│         S3 Event Notification            │
│                                          │
│  S3 Upload Complete                      │
│       ↓                                  │
│  SQS Queue (S3 Event Notifications)      │
│       ↓                                  │
│  S3EventListener (Polling)               │
│       ↓                                  │
│  S3UploadEventHandler                    │
│       ↓                                  │
│  UploadSessionPort.save()                │
│       ↓                                  │
│  Session Status: COMPLETED               │
└─────────────────────────────────────────┘
```

## 주요 컴포넌트

### 1. S3EventListener
- **역할**: SQS 큐로부터 메시지를 폴링하고 배치 처리
- **동작**:
  - 5초마다 실행
  - 최대 10개 메시지 배치 수신
  - 비동기 처리 지원
  - 처리 성공 시 메시지 삭제

### 2. S3UploadEventHandler
- **역할**: S3 이벤트 파싱 및 세션 업데이트
- **동작**:
  - S3 이벤트 JSON 파싱
  - S3 key에서 세션 ID 추출 (패턴: `uploads/{sessionId}/{filename}`)
  - 업로드 세션 조회 및 완료 상태로 업데이트

### 3. SqsConfig
- **역할**: SQS 클라이언트 설정
- **기능**:
  - AWS 환경 및 LocalStack 지원
  - 비동기 클라이언트 구성
  - Region 및 Endpoint 설정

## 설정

### application.yml

```yaml
aws:
  sqs:
    region: ap-northeast-2
    endpoint: # LocalStack용, 프로덕션에서는 비워둠
    s3-event-queue-url: https://sqs.ap-northeast-2.amazonaws.com/{account-id}/{queue-name}
    wait-time-seconds: 20
    max-number-of-messages: 10
    visibility-timeout: 30
```

### S3 Bucket Notification 설정

S3 버킷에 이벤트 알림을 설정해야 합니다:

```json
{
  "QueueConfigurations": [
    {
      "QueueArn": "arn:aws:sqs:ap-northeast-2:{account-id}:{queue-name}",
      "Events": ["s3:ObjectCreated:*"],
      "Filter": {
        "Key": {
          "FilterRules": [
            {
              "Name": "prefix",
              "Value": "uploads/"
            }
          ]
        }
      }
    }
  ]
}
```

## S3 Key 형식

업로드된 파일의 S3 key는 다음 형식을 따라야 합니다:

```
uploads/{sessionId}/{filename}
```

예시:
- `uploads/abc123-def456/profile.jpg`
- `uploads/xyz789-ghi012/document.pdf`

## 에러 처리

### 1. 파싱 실패
- **예외**: `S3EventParsingException`
- **처리**: 로그 기록, DLQ로 이동 (SQS 설정)

### 2. 세션 매칭 실패
- **예외**: `SessionMatchingException`
- **원인**:
  - 잘못된 S3 key 형식
  - 존재하지 않는 세션 ID
- **처리**: 로그 기록, DLQ로 이동

### 3. 세션 상태 불일치
- **처리**: 경고 로그, 업데이트 건너뜀

## 모니터링

### 로그
```java
log.info("Received {} messages from SQS", messages.size());
log.info("Successfully processed message: {}", messageId);
log.error("Failed to process message: {}. Error: {}", messageId, error);
```

### 메트릭 (TODO)
- `sqs.messages.received`: 수신한 메시지 수
- `sqs.messages.processed`: 처리된 메시지 수
- `sqs.messages.failed`: 실패한 메시지 수
- `sqs.session.updated`: 업데이트된 세션 수

## 테스트

### 단위 테스트
```bash
./gradlew :adapter:adapter-out-aws-sqs:test
```

### 통합 테스트 (LocalStack)
```bash
./gradlew :adapter:adapter-out-aws-sqs:test --tests "*IntegrationTest"
```

## DLQ (Dead Letter Queue) 설정

재시도 실패 시 메시지를 보관할 DLQ 설정:

```yaml
aws:
  sqs:
    dlq-url: https://sqs.ap-northeast-2.amazonaws.com/{account-id}/{dlq-name}
    max-receive-count: 3
```

## 성능 최적화

1. **배치 처리**: 최대 10개 메시지 동시 처리
2. **비동기 처리**: CompletableFuture 사용
3. **Long Polling**: 20초 대기로 API 호출 최소화
4. **Visibility Timeout**: 30초로 재처리 방지

## 제약 사항

- S3 key 형식 준수 필수
- 세션 ID는 UUID 또는 영숫자 조합
- 메시지 크기 제한: 256KB (SQS 제한)
- 처리 시간 제한: 30초 (Visibility Timeout)

## 참고 자료

- [AWS SQS Developer Guide](https://docs.aws.amazon.com/sqs/)
- [S3 Event Notifications](https://docs.aws.amazon.com/AmazonS3/latest/userguide/NotificationHowTo.html)
- [LocalStack SQS](https://docs.localstack.cloud/user-guide/aws/sqs/)
