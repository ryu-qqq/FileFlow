# Plan: FileFlow 프로젝트 전면 재구축

> 생성일: 2026-02-07
> 최종 수정: 2026-02-07
> 전략: Clean Slate (기존 코드 전면 삭제 후 재작성)
> 보존: Terraform prod/stage, Gradle 설정, CI/CD, Claude 설정

---

## 1. 현재 프로젝트 분석

### 프로젝트 개요
- Spring Boot 3.5.x + Java 21 + Hexagonal Multi-Module
- 도메인 5개: Session, Asset, Download, Transform + Common
- 서비스 성격: 내부 전용 파일 관리 + 이미지 변환 서버
- 인증: Service Token 기반 (X-Service-Name + X-Service-Token)

### 보존 대상
- `terraform/environments/prod/` (운영 인프라)
- `terraform/environments/stage/` (스테이징 인프라)
- `build.gradle`, `settings.gradle`, `gradle/` (빌드 구조)
- `.claude/`, `.github/` (Claude Code + CI/CD)

### 핵심 설계 결정
- **Asset은 업로드 완료 후에만 생성** (존재 = S3에 파일 있음 보장)
- **Session과 Asset 분리**: Session = 업로드 프로세스, Asset = 파일 레코드
- **SingleUploadSession / MultipartUploadSession 분리**: 라이프사이클/필드 차이
- **AssetMetadata 별도 Aggregate**: 비동기 메타데이터 추출, 별도 DB 테이블
- **Transform은 이미지 전용**: 도메인 레벨에서 image/* 검증
- **모든 Aggregate에 updatedAt**: 변경 이력 추적
- **Common 패키지는 MarketPlace(setof-commerce)와 동일 구조 유지**

---

## 2. 구현 순서 원칙

Domain → Application → Adapter-Out → Adapter-In → Bootstrap

---

## 3. 도메인 구조

```
domain/
├── common/
│   ├── exception/   ErrorCode, DomainException
│   ├── event/       DomainEvent (+ eventType())
│   └── vo/          AccessType, StorageInfo, CacheKey, LockKey,
│                    PageRequest, CursorPageRequest, DateRange,
│                    SortKey, SortDirection
├── session/
│   ├── aggregate/   SingleUploadSession, MultipartUploadSession, CompletedPart
│   ├── id/          SingleUploadSessionId, MultipartUploadSessionId
│   ├── vo/          SingleSessionStatus, MultipartSessionStatus, UploadTarget
│   ├── event/       UploadCompletedEvent
│   ├── exception/   SessionErrorCode, SessionException
│   └── service/     S3PathResolver
├── asset/
│   ├── aggregate/   Asset, AssetMetadata
│   ├── id/          AssetId, AssetMetadataId
│   ├── vo/          FileInfo, FileType, AssetOrigin
│   ├── event/       AssetCreatedEvent
│   └── exception/   AssetErrorCode, AssetException
├── download/
│   ├── aggregate/   DownloadTask
│   ├── id/          DownloadTaskId
│   ├── vo/          DownloadTaskStatus
│   ├── event/       DownloadCompletedEvent
│   └── exception/   DownloadErrorCode, DownloadException
└── transform/
    ├── aggregate/   TransformRequest
    ├── id/          TransformRequestId
    ├── vo/          TransformType, TransformStatus, TransformParams
    ├── event/       TransformCompletedEvent
    └── exception/   TransformErrorCode, TransformException
```

---

## 4. Task 분해 (15 Phase)

### Phase 0: 기존 코드 정리
- T-000: 기존 Java 소스 전체 삭제 + 보존 확인

### Phase 1: Domain Common
- T-101: common/exception (ErrorCode, DomainException — MarketPlace 동일)
- T-102: common/event (DomainEvent + eventType())
- T-103: common/vo (AccessType, StorageInfo, CacheKey, LockKey, PageRequest, CursorPageRequest, DateRange, SortKey, SortDirection)

### Phase 2: Domain Session
- T-201: session/aggregate (SingleUploadSession, MultipartUploadSession, CompletedPart)
- T-202: session/id + vo (SessionId, SingleSessionStatus, MultipartSessionStatus, UploadTarget)
- T-203: session/exception (SessionErrorCode, SessionException)
- T-204: session/event (UploadCompletedEvent)
- T-205: session/service (S3PathResolver)

### Phase 3: Domain Asset
- T-301: asset/aggregate (Asset, AssetMetadata)
- T-302: asset/id + vo (AssetId, AssetMetadataId, FileInfo, FileType, AssetOrigin)
- T-303: asset/exception + event (AssetErrorCode, AssetException, AssetCreatedEvent)

### Phase 4: Domain Download
- T-401: download/aggregate (DownloadTask)
- T-402: download/id + vo (DownloadTaskId, DownloadTaskStatus)
- T-403: download/exception + event (DownloadErrorCode, DownloadException, DownloadCompletedEvent)

### Phase 4.5: Domain Transform
- T-451: transform/aggregate (TransformRequest — 이미지 전용 비즈니스 룰)
- T-452: transform/id + vo (TransformRequestId, TransformType, TransformStatus, TransformParams)
- T-453: transform/exception + event (TransformErrorCode, TransformException, TransformCompletedEvent)

### Phase 5: App Common + Session (4 Task)
- T-501: common/port (CachePort, DistributedLockPort, IdGeneratorPort) + component (ClockHolder)
- T-502: session/port/in — UseCase 인터페이스
  - CreateSingleUploadSessionUseCase
  - CompleteSingleUploadSessionUseCase
  - GetSingleUploadSessionUseCase
  - ExpireSingleUploadSessionUseCase
  - CreateMultipartUploadSessionUseCase
  - AddCompletedPartUseCase
  - CompleteMultipartUploadSessionUseCase
  - AbortMultipartUploadSessionUseCase
  - GetMultipartUploadSessionUseCase
- T-503: session/port/out — Persistence + Client Port
  - SingleUploadSessionPersistencePort
  - MultipartUploadSessionPersistencePort
  - S3PresignedUrlPort
  - S3MultipartPort
- T-504: session/facade + factory + manager

### Phase 6: App Asset (4 Task)
- T-601: asset/port/in — UseCase 인터페이스
  - CreateAssetUseCase
  - GetAssetUseCase
  - DeleteAssetUseCase
  - CreateAssetMetadataUseCase
  - GetAssetMetadataUseCase
  - UpdateAssetMetadataUseCase
- T-602: asset/port/out — Persistence Port
  - AssetPersistencePort
  - AssetMetadataPersistencePort
  - MetadataExtractorPort
- T-603: asset/dto (Command, Query)
- T-604: asset/facade + factory + manager

### Phase 7: App Download (3 Task)
- T-701: download/port/in — UseCase 인터페이스
  - CreateDownloadTaskUseCase
  - StartDownloadUseCase
  - CompleteDownloadUseCase
  - FailDownloadUseCase
  - GetDownloadTaskUseCase
- T-702: download/port/out + dto
  - DownloadTaskPersistencePort
  - FileDownloaderPort
  - WebhookClientPort
- T-703: download/facade + manager

### Phase 7.5: App Transform (3 Task)
- T-751: transform/port/in — UseCase 인터페이스
  - CreateTransformRequestUseCase
  - StartTransformUseCase
  - CompleteTransformUseCase
  - FailTransformUseCase
  - GetTransformRequestUseCase
- T-752: transform/port/out + dto
  - TransformRequestPersistencePort
  - ImageTransformPort
- T-753: transform/facade + manager

### Phase 8: Adapter-Out MySQL (5 Task)
- T-801: common (BaseAuditEntity, Config)
- T-802: session (Entity + Mapper + Repository + Adapter)
- T-803: asset (AssetEntity + AssetMetadataEntity + Mapper + Repository + Adapter)
- T-804: download (Entity + Mapper + Repository + Adapter)
- T-805: transform (Entity + Mapper + Repository + Adapter)

### Phase 9: Adapter-Out Others (5 Task)
- T-901: persistence-redis (Config + Cache + Lock + SessionExpiration)
- T-902: aws-s3 (Config + PresignedUrl + Multipart + Storage Adapter)
- T-903: aws-sqs (Config + EventPublisher + TaskQueue Adapter)
- T-904: http-client (Config + FileDownloader + WebhookClient)
- T-905: image-processor (Scrimage — Resize, Convert, Compress, Thumbnail)

### Phase 10: Adapter-In REST API (7 Task)
- T-1001: common (ApiResponse, PageApiResponse, SliceApiResponse, ExceptionHandler, ErrorMapper)
- T-1002: auth (Security, ServiceTokenFilter, ServiceContext)
- T-1003: config (Filter, Jackson, OpenApi, WebMvc)
- T-1004: session controller + dto + mapper
- T-1005: asset controller + dto + mapper
- T-1006: download controller + dto + mapper
- T-1007: transform controller + dto + mapper

### Phase 11: Adapter-In Listener (2 Task)
- T-1101: sqs-listener (Config + DownloadListener + TransformListener)
- T-1102: redis-listener (Config + SessionExpirationListener)

### Phase 12: Bootstrap (2 Task)
- T-1201: bootstrap-web-api (main, application.yml, profile 설정)
- T-1202: bootstrap-worker (download-worker, transform-worker)

### Phase 13: SDK (1 Task)
- T-1301: SDK Core + Spring Boot Starter

### Phase 14: ArchUnit + E2E (4 Task)
- T-1401: domain ArchUnit (도메인 격리, 외부 import 금지)
- T-1402: adapter-out ArchUnit (어댑터 격리)
- T-1403: adapter-in ArchUnit (컨트롤러 격리)
- T-1404: Integration Test (E2E)

---

## 5. 의존성 그래프

```
Phase 0 → Phase 1 → Phase 2, 3, 4, 4.5 (병렬)
                          |
                          v
                     Phase 5, 6, 7, 7.5 (각 도메인별)
                          |
                          v
                     Phase 8, 9 (병렬) → Phase 10, 11 (병렬)
                          |
                          v
                     Phase 12 → Phase 14
                     Phase 13 (독립, Phase 1 이후 언제든)
```

---

## 6. 진행 상태

- [x] Phase 0: 기존 코드 정리 (884 Java files deleted)
- [x] Phase 1: Domain Common
  - ErrorCode, DomainException (MarketPlace 동일: protected 생성자, Map args, code()/httpStatus())
  - DomainEvent (+ eventType() default method)
  - AccessType, StorageInfo, CacheKey, LockKey, PageRequest, CursorPageRequest, DateRange, SortKey, SortDirection
- [x] Phase 2: Domain Session
  - SingleUploadSession, MultipartUploadSession, CompletedPart (updatedAt 포함)
  - UploadTarget, SingleSessionStatus, MultipartSessionStatus
  - S3PathResolver, UploadCompletedEvent
  - SessionErrorCode, SessionException (Map args 생성자)
- [x] Phase 3: Domain Asset
  - Asset (updatedAt, isImage() 포함, FileTypeMetadata 제거)
  - AssetMetadata (별도 Aggregate, 비동기 메타데이터)
  - FileInfo, FileType, AssetOrigin, AssetId, AssetMetadataId
  - AssetCreatedEvent, AssetErrorCode, AssetException
- [x] Phase 4: Domain Download
  - DownloadTask (updatedAt 포함)
  - DownloadTaskStatus, DownloadTaskId
  - DownloadCompletedEvent, DownloadErrorCode, DownloadException
- [x] Phase 4.5: Domain Transform
  - TransformRequest (이미지 전용 비즈니스 룰, 타입별 파라미터 검증)
  - TransformType (RESIZE, CONVERT, COMPRESS, THUMBNAIL)
  - TransformStatus, TransformParams, TransformRequestId
  - TransformCompletedEvent, TransformErrorCode, TransformException
- [x] Phase 5: App Common + Session (46 Java files)
  - Common: ClockHolder, IdGeneratorPort, EventPublisherPort, CachePort, DistributedLockPort, PageResponse, SliceResponse
  - Session DTOs: 6 Command + 2 Response records
  - Session Ports In: 10 UseCase 인터페이스 (8 Command + 2 Query)
  - Session Ports Out: 2 PersistencePort + 2 QueryPort + 3 ClientPort (S3Presigned, S3Multipart, SessionExpiration)
  - Session Impl: SessionAssembler, SessionCommandFactory, SessionPersistenceManager, SessionReadManager
  - Session Services: 8 Command + 2 Query Service 구현체
- [ ] **Phase 6: App Asset** ← 다음
- [ ] Phase 7: App Download
- [ ] Phase 7.5: App Transform
- [ ] Phase 8: Adapter-Out MySQL
- [ ] Phase 9: Adapter-Out Others
- [ ] Phase 10: Adapter-In REST API
- [ ] Phase 11: Adapter-In Listener
- [ ] Phase 12: Bootstrap
- [ ] Phase 13: SDK
- [ ] Phase 14: ArchUnit + E2E
