# Plan: FileFlow 프로젝트 전면 재구축

> 생성일: 2026-02-07
> 전략: Clean Slate (기존 코드 전면 삭제 후 재작성)
> 보존: Terraform prod/stage, Gradle 설정, CI/CD, Claude 설정

---

## 1. 현재 프로젝트 분석

### 프로젝트 개요
- Spring Boot 3.5.x + Java 21 + Hexagonal Multi-Module
- 도메인 3개: Session, Asset, Download + IAM(VO) + Common
- 소스 ~323개, 테스트 ~223개

### 보존 대상
- `terraform/environments/prod/` (운영 인프라)
- `terraform/environments/stage/` (스테이징 인프라)
- `terraform/shared-*.tf` (공유 인프라)
- `build.gradle`, `settings.gradle`, `gradle/` (빌드 구조)
- `.claude/`, `.github/` (Claude Code + CI/CD)

---

## 2. 구현 순서 원칙

Domain -> Application -> Adapter-Out -> Adapter-In -> Bootstrap

---

## 3. Task 분해 (53개 Task, 14 Phase)

### Phase 0: 기존 코드 정리
- T-000: 기존 Java 소스 전체 삭제 + 보존 확인

### Phase 1: Domain Common (4 Task)
- T-101: common/exception (ErrorCode, DomainException)
- T-102: common/event (DomainEvent)
- T-103: common/vo (CacheKey, LockKey, PageRequest 등 공통 VO 10-15개)
- T-104: iam/vo (UserContext, UserId, UserRole 등 5-7개)

### Phase 2: Domain Session (5 Task)
- T-201: session/aggregate (UploadSession, Single/Multipart, CompletedPart)
- T-202: session/vo (SessionStatus, UploadSessionId, S3Key 등 15-18개)
- T-203: session/exception (SessionErrorCode + 7개 Exception)
- T-204: session/event (FileUploadCompletedEvent)
- T-205: session/service (S3PathResolver)

### Phase 3: Domain Asset (4 Task)
- T-301: asset/aggregate (FileAsset, StatusHistory, ProcessedFileAsset, Outbox)
- T-302: asset/vo (FileAssetId, FileAssetStatus, FileCategory 등 12-15개)
- T-303: asset/exception + event
- T-304: asset/service (Creation, Update, ImageProcessingPolicy)

### Phase 4: Domain Download (3 Task)
- T-401: download/aggregate (ExternalDownload, Outbox, WebhookOutbox)
- T-402: download/vo (ExternalDownloadId, Status, SourceUrl 등 8-10개)
- T-403: download/exception + event

### Phase 5: App Common + Session (4 Task)
- T-501: common/port (CachePort, LockPort, IdGeneratorPort) + component
- T-502: session/port/in (UseCase 9개)
- T-503: session/port/out (Persistence + Client 6개)
- T-504: session/facade + factory + manager

### Phase 6: App Asset (5 Task)
- T-601: asset/port/in (UseCase 13개)
- T-602: asset/port/out (Persistence + Client 8개)
- T-603: asset/dto (Command, Query, Message)
- T-604: asset/facade + coordinator + manager + publisher
- T-605: asset/component (ImageDownloader, Extractor, Uploader)

### Phase 7: App Download (3 Task)
- T-701: download/port (UseCase 7 + Out Port 10개)
- T-702: download/dto + factory
- T-703: download/facade + manager

### Phase 8: Adapter-Out MySQL (4 Task)
- T-801: common (BaseAuditEntity, Config)
- T-802: session (Entity + Mapper + Repository + Adapter)
- T-803: asset (Entity + Mapper + Repository + Adapter + Outbox)
- T-804: download (Entity + Mapper + Repository + Adapter)

### Phase 9: Adapter-Out Others (5 Task)
- T-901: persistence-redis (Config + Cache + Lock + Session)
- T-902: aws-s3 (Config + 3 Adapter)
- T-903: aws-sqs (Config + 2 Adapter)
- T-904: http-client (Config + Download + Webhook)
- T-905: image-processor (Scrimage)

### Phase 10: Adapter-In REST API (6 Task)
- T-1001: common (ApiResponse, ExceptionHandler, ErrorMapper)
- T-1002: auth (Security, Filter, Context, Handler)
- T-1003: config (Filter, Jackson, OpenApi, WebMvc)
- T-1004: session controller + dto + mapper
- T-1005: asset controller + dto + mapper
- T-1006: download controller + dto + mapper

### Phase 11: Adapter-In Listener (2 Task)
- T-1101: sqs-listener (Config + Listener + Metrics)
- T-1102: redis-listener (Config + SessionExpiration)

### Phase 12: Bootstrap (2 Task)
- T-1201: bootstrap-web-api
- T-1202: bootstrap-scheduler + workers

### Phase 13: SDK (1 Task)
- T-1301: SDK Core + Spring Boot Starter

### Phase 14: ArchUnit + E2E (4 Task)
- T-1401: domain ArchUnit
- T-1402: adapter-out ArchUnit
- T-1403: adapter-in ArchUnit
- T-1404: Integration Test (E2E)

---

## 4. 의존성 그래프

```
Phase 0 -> Phase 1 -> Phase 2,3,4 (병렬)
              |
              v
         Phase 5,6,7 (각 도메인별)
              |
              v
         Phase 8,9 (병렬) -> Phase 10,11 (병렬)
              |
              v
         Phase 12 -> Phase 14
         Phase 13 (독립, Phase 1 이후 언제든)
```

---

## 5. 진행 상태

- [x] Phase 0: 기존 코드 정리 (884 Java files deleted, module scaffolding created)
- [ ] Phase 1: Domain Common
- [ ] Phase 2: Domain Session
- [ ] Phase 3: Domain Asset
- [ ] Phase 4: Domain Download
- [ ] Phase 5: App Common + Session
- [ ] Phase 6: App Asset
- [ ] Phase 7: App Download
- [ ] Phase 8: Adapter-Out MySQL
- [ ] Phase 9: Adapter-Out Others
- [ ] Phase 10: Adapter-In REST API
- [ ] Phase 11: Adapter-In Listener
- [ ] Phase 12: Bootstrap
- [ ] Phase 13: SDK
- [ ] Phase 14: ArchUnit + E2E
