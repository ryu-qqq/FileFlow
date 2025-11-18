# Session Cleanup Bounded Context

**Bounded Context**: `session/cleanup`
**Dependencies**: `session/single`, `session/multi` (UploadSession Aggregates)
**예상 기간**: 2일
**우선순위**: Level 3 (Level 2 완료 후)

---

## 📋 개요

**목적**: 만료된 업로드 세션과 임시 파일을 주기적으로 정리하여 스토리지 비용을 절감합니다.

**핵심 문제 해결**:
- **스토리지 비용**: 미완료 업로드로 인한 S3 임시 파일 누적 방지
- **DB 성능**: 만료된 세션 데이터로 인한 DB 용량 증가 방지
- **보안**: 민감한 임시 데이터 자동 삭제

**정리 대상**:
- 만료된 UploadSession (5분 경과)
- S3 Incomplete Multipart Upload (24시간 경과)
- INITIATED 상태로 방치된 세션 (1시간 경과)

---

## 🎯 주요 기능

### In Scope
1. **SessionCleanupJob Aggregate** - 정리 작업 생명주기 관리
2. **만료 세션 삭제** - EXPIRED 상태 세션 DB 삭제
3. **S3 Multipart Upload 중단** - 미완료 Multipart Upload Abort
4. **정리 통계** - 정리된 세션 수, S3 절감 용량 추적
5. **스케줄러** - 매일 새벽 3시 실행

### Out of Scope (Future)
- 파일 보관 정책 (Retention Policy)
- 아카이빙 (Glacier 이동)
- 감사 로그 정리

---

## 🏗️ Domain Layer

### Aggregates

#### 1. SessionCleanupJob
**책임**: 세션 정리 작업 생명주기 관리

**주요 메서드**:
```java
public class SessionCleanupJob {
    private JobId jobId;                    // UUID v7
    private CleanupType cleanupType;        // EXPIRED_SESSIONS, INCOMPLETE_MULTIPART
    private JobStatus status;               // PENDING, RUNNING, COMPLETED, FAILED
    private int deletedSessions;            // 삭제된 세션 수
    private long reclaimedBytes;            // 절감된 S3 용량 (bytes)
    private String errorMessage;            // 실패 시 에러 메시지
    private LocalDateTime startedAt;        // 시작 시각
    private LocalDateTime completedAt;      // 완료 시각

    public static SessionCleanupJob create(CleanupType cleanupType, Clock clock);

    public void startCleanup(Clock clock);
    public void recordDeletion(int sessionCount, long bytes);
    public void markAsCompleted(Clock clock);
    public void markAsFailed(String errorMessage, Clock clock);
}
```

### Enums

#### CleanupType
- `EXPIRED_SESSIONS`: 만료된 세션 삭제 (EXPIRED 상태)
- `INCOMPLETE_MULTIPART`: S3 미완료 Multipart Upload Abort
- `STALE_SESSIONS`: 방치된 세션 삭제 (INITIATED 상태 1시간 이상)

#### JobStatus
- `PENDING`: 실행 대기
- `RUNNING`: 실행 중
- `COMPLETED`: 완료
- `FAILED`: 실패

---

## 📦 Application Layer

### Use Cases

#### 1. CleanupExpiredSessionsUseCase (Scheduler)
**책임**: 만료된 세션 정리

```java
@Component
public class SessionCleanupScheduler {

    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시
    public void cleanupExpiredSessions() {
        // 1. 트랜잭션: CleanupJob 생성
        SessionCleanupJob job = SessionCleanupJob.create(
            CleanupType.EXPIRED_SESSIONS,
            clock
        );
        sessionCleanupJobPersistencePort.save(job);

        try {
            job.startCleanup(clock);
            sessionCleanupJobPersistencePort.update(job);

            // 2. 트랜잭션: 만료된 세션 조회 (배치 100개)
            List<UploadSession> expiredSessions =
                uploadSessionQueryPort.findExpiredSessions(LocalDateTime.now(clock));

            int deletedCount = 0;
            for (UploadSession session : expiredSessions) {
                // 3. 트랜잭션: 세션 삭제
                uploadSessionPersistencePort.delete(session.sessionId());
                deletedCount++;

                // 4. 배치 커밋 (100개마다)
                if (deletedCount % 100 == 0) {
                    job.recordDeletion(deletedCount, 0);
                    sessionCleanupJobPersistencePort.update(job);
                }
            }

            // 5. 트랜잭션: Job 완료
            job.recordDeletion(deletedCount, 0);
            job.markAsCompleted(clock);
            sessionCleanupJobPersistencePort.update(job);

        } catch (Exception e) {
            job.markAsFailed(e.getMessage(), clock);
            sessionCleanupJobPersistencePort.update(job);
        }
    }
}
```

#### 2. CleanupIncompleteMultipartUploadsUseCase (Scheduler)
**책임**: S3 미완료 Multipart Upload 정리

```java
@Component
public class MultipartCleanupScheduler {

    @Scheduled(cron = "0 30 3 * * *")  // 매일 새벽 3시 30분
    public void cleanupIncompleteMultipart() {
        SessionCleanupJob job = SessionCleanupJob.create(
            CleanupType.INCOMPLETE_MULTIPART,
            clock
        );
        sessionCleanupJobPersistencePort.save(job);

        try {
            job.startCleanup(clock);
            sessionCleanupJobPersistencePort.update(job);

            // 1. 트랜잭션 밖: S3에서 미완료 Multipart Upload 목록 조회
            List<MultipartUpload> incompleteUploads =
                s3ClientPort.listIncompleteMultipartUploads(
                    s3Bucket,
                    LocalDateTime.now(clock).minusHours(24)  // 24시간 경과
                );

            int abortedCount = 0;
            long reclaimedBytes = 0;

            for (MultipartUpload upload : incompleteUploads) {
                // 2. 트랜잭션 밖: S3 Abort Multipart Upload
                long uploadSize = s3ClientPort.abortMultipartUpload(
                    s3Bucket,
                    upload.key(),
                    upload.uploadId()
                );

                abortedCount++;
                reclaimedBytes += uploadSize;

                // 3. 트랜잭션: 연관 세션 삭제
                Optional<MultipartUploadSession> session =
                    multipartSessionQueryPort.findByS3UploadId(upload.uploadId());
                session.ifPresent(s ->
                    multipartSessionPersistencePort.delete(s.sessionId())
                );
            }

            // 4. 트랜잭션: Job 완료
            job.recordDeletion(abortedCount, reclaimedBytes);
            job.markAsCompleted(clock);
            sessionCleanupJobPersistencePort.update(job);

        } catch (Exception e) {
            job.markAsFailed(e.getMessage(), clock);
            sessionCleanupJobPersistencePort.update(job);
        }
    }
}
```

#### 3. CleanupStaleSessions (Scheduler)
**책임**: 방치된 세션 정리

```java
@Scheduled(cron = "0 0 4 * * *")  // 매일 새벽 4시
public void cleanupStaleSessions() {
    SessionCleanupJob job = SessionCleanupJob.create(
        CleanupType.STALE_SESSIONS,
        clock
    );
    sessionCleanupJobPersistencePort.save(job);

    try {
        job.startCleanup(clock);

        // 1. INITIATED 상태로 1시간 이상 방치된 세션 조회
        LocalDateTime threshold = LocalDateTime.now(clock).minusHours(1);
        List<UploadSession> staleSessions =
            uploadSessionQueryPort.findStaleInitiatedSessions(threshold);

        int deletedCount = 0;
        for (UploadSession session : staleSessions) {
            // 2. 세션 삭제
            uploadSessionPersistencePort.delete(session.sessionId());
            deletedCount++;
        }

        job.recordDeletion(deletedCount, 0);
        job.markAsCompleted(clock);
        sessionCleanupJobPersistencePort.update(job);

    } catch (Exception e) {
        job.markAsFailed(e.getMessage(), clock);
        sessionCleanupJobPersistencePort.update(job);
    }
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V9__create_session_cleanup_jobs_table.sql
```sql
CREATE TABLE session_cleanup_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL UNIQUE,
    cleanup_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted_sessions INT NOT NULL DEFAULT 0,
    reclaimed_bytes BIGINT NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at DATETIME(6),
    completed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_job_id (job_id),
    INDEX idx_cleanup_type_started (cleanup_type, started_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📊 S3 Lifecycle Policy

### S3 Bucket Lifecycle Configuration
```json
{
  "Rules": [
    {
      "Id": "AbortIncompleteMultipartUpload",
      "Status": "Enabled",
      "AbortIncompleteMultipartUpload": {
        "DaysAfterInitiation": 1
      }
    },
    {
      "Id": "DeleteExpiredTempFiles",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "temp/"
      },
      "Expiration": {
        "Days": 1
      }
    }
  ]
}
```

**S3 Lifecycle Policy vs Application Cleanup**:
- S3 Lifecycle: S3 레벨에서 자동 삭제 (24시간 후)
- Application Cleanup: DB 레코드 삭제 + S3 Abort 명시적 호출

---

## 🌐 REST API Layer

### Endpoints

| Method | Path | Description | Status Code |
|--------|------|-------------|-------------|
| GET | /api/v1/admin/cleanup/history | 정리 작업 이력 조회 | 200 OK |
| POST | /api/v1/admin/cleanup/run | 수동 정리 실행 | 202 Accepted |

### Response Example

**GET /api/v1/admin/cleanup/history (200 OK)**:
```json
{
  "jobs": [
    {
      "jobId": "01JDD000-1234-5678-9abc-def012345678",
      "cleanupType": "EXPIRED_SESSIONS",
      "status": "COMPLETED",
      "deletedSessions": 1523,
      "reclaimedBytes": 0,
      "startedAt": "2025-11-18T03:00:00Z",
      "completedAt": "2025-11-18T03:02:15Z"
    },
    {
      "jobId": "01JDD001-1234-5678-9abc-def012345678",
      "cleanupType": "INCOMPLETE_MULTIPART",
      "status": "COMPLETED",
      "deletedSessions": 45,
      "reclaimedBytes": 5368709120,
      "startedAt": "2025-11-18T03:30:00Z",
      "completedAt": "2025-11-18T03:31:30Z"
    }
  ],
  "totalReclaimedBytes": 5368709120,
  "totalReclaimedMB": 5120
}
```

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] 만료된 세션 자동 삭제 (매일 3시)
- [ ] S3 미완료 Multipart Upload Abort (매일 3시 30분)
- [ ] 방치된 세션 삭제 (INITIATED 1시간 초과, 매일 4시)
- [ ] 정리 통계 추적 (삭제 수, 절감 용량)
- [ ] Admin API (정리 이력 조회, 수동 실행)

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test (TestContainers + LocalStack S3)
- [ ] ArchUnit Test 통과

### 성능 요구사항
- [ ] 1,000개 세션 삭제 < 3분
- [ ] S3 Multipart Abort < 5분 (100개 기준)

---

## 🔗 의존성

### Upstream
- `session/single` - UploadSession 정리
- `session/multi` - MultipartUploadSession, UploadPart 정리

### Downstream
- S3 Abort Multipart Upload API
- S3 List Multipart Uploads API

---

## 📊 모니터링 메트릭

### 추적 항목
- 일일 삭제된 세션 수
- 일일 절감된 S3 용량 (GB)
- 정리 작업 실행 시간
- 정리 작업 실패율

### 알림
- 정리 작업 실패 시 Slack 알림
- 비정상적으로 많은 세션 삭제 시 알림 (> 10,000개)

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/cleanup Bounded Context)
