# Single Presigned URL Upload - Bounded Context

**Bounded Context**: `session/single`
**작성일**: 2025-11-18
**버전**: v1.0 (MVP)
**우선순위**: P0 (최우선)
**예상 기간**: 5일

---

## 📝 개요

단일 Presigned URL 기반 파일 업로드 기능을 제공하는 Bounded Context입니다. 세토프 커머스에서 Admin(관리자), Seller(입점 셀러), Customer(일반 고객)가 S3로 파일을 직접 업로드할 수 있도록 Presigned URL을 발급하고, 업로드 완료 후 File Aggregate을 생성합니다.

### 핵심 가치

- **멱등성 보장**: SessionId를 통한 중복 업로드 방지
- **단순성**: 100MB 미만 파일에 대한 단일 업로드 지원
- **즉시성**: Presigned URL 발급 응답 시간 < 200ms (P95)
- **Zero-Tolerance 준수**: Transaction 경계, Law of Demeter, Long FK 전략 완벽 준수

---

## 🎯 범위 (Scope)

### 포함 범위 (In Scope)

✅ **Domain Layer**:
- 11개 Value Objects (FileId, FileName, FileSize, MimeType, S3Key, S3Bucket, TenantId, UploaderId, FileCategory, SessionId, PresignedUrl)
- UploadSession Aggregate (세션 기반 멱등성)
- File Aggregate (기본 필드만)
- 3개 Enums (FileStatus, SessionStatus, UploaderType)
- 5개 Domain Exceptions

✅ **Application Layer**:
- GeneratePresignedUrlFacade (Orchestration Pattern)
- SessionManager (Transaction 경계 관리)
- CompleteUploadService
- Port 인터페이스 (Command/Query 분리)
- UserContext (JWT에서 추출)

✅ **Persistence Layer**:
- FileJpaEntity, UploadSessionJpaEntity
- Flyway Migration (V1: files, V2: upload_sessions)
- Command/Query Adapter
- S3ClientAdapter (Presigned URL 생성)

✅ **REST API Layer**:
- POST /api/v1/files/presigned-url
- POST /api/v1/files/upload-complete
- Request/Response DTO
- GlobalExceptionHandler

### 제외 범위 (Out of Scope)

❌ **Multipart Upload** (100MB 이상 파일) → `session/multi/` Bounded Context
❌ **Checksum 검증** → `validation/checksum/` Bounded Context
❌ **DownloadSession** (외부 URL 다운로드) → `session/download/` Bounded Context
❌ **FileProcessingJob** (이미지 가공) → `file/processing/` Bounded Context
❌ **CDN URL 생성** → `cdn/url-generation/` Bounded Context
❌ **MessageOutbox** (이벤트 발행) → `messaging/outbox/` Bounded Context
❌ **Visibility** (접근 제어) → `security/visibility/` Bounded Context
❌ **File 만료 관리** → `file/retention/` Bounded Context

---

## 🔗 의존성 (Dependencies)

### 외부 의존성

- **AWS S3**: Presigned URL 생성 (S3ClientPort)
- **MySQL**: File/UploadSession 저장 (JPA + QueryDSL)
- **JWT**: UserContext 추출 (TenantId, UploaderId, UploaderType)

### 내부 의존성

- **Shared VOs**: FileId, SessionId, FileName, FileSize, MimeType, S3Key, S3Bucket, TenantId, UploaderId, FileCategory
- **Shared Exceptions**: DomainException (base class)

### 의존 관계

```
session/single (MVP, Level 1)
  ├─ 외부: AWS S3, MySQL, JWT
  ├─ 공유: Shared VOs, Shared Exceptions
  └─ 의존 없음 (독립 실행 가능)

후속 Bounded Contexts (의존):
  ├─ session/multi → session/single (UploadSession, File)
  ├─ session/download → session/single (File 생성 로직 재사용)
  └─ file/processing → session/single (File Aggregate)
```

---

## 🏗️ 핵심 플로우

### 1. Presigned URL 발급 플로우

```
Client
  ↓
1. POST /api/v1/files/presigned-url
  {sessionId, fileName, fileSize, mimeType, category}
  ↓
2. GeneratePresignedUrlFacade (Orchestration)
  ├─ SessionManager.prepareSession() ← 트랜잭션 안
  │   ├─ 멱등성 체크 (sessionId 중복 조회)
  │   ├─ UploadSession 생성 (INITIATED)
  │   └─ File 메타데이터 생성 (PENDING)
  ├─ 트랜잭션 커밋
  ├─ S3ClientPort.generatePresignedUrl() ← 트랜잭션 밖 (외부 API)
  ├─ 트랜잭션 시작
  └─ SessionManager.completePreparation() ← 트랜잭션 안
      ├─ UploadSession에 presignedUrl 저장
      └─ Status → IN_PROGRESS
  ↓
3. Response
  {sessionId, fileId, presignedUrl, expiresIn=300}
  ↓
Client: S3로 직접 업로드 (PUT request)
```

### 2. 업로드 완료 플로우

```
Client
  ↓
1. POST /api/v1/files/upload-complete
  {sessionId}
  ↓
2. CompleteUploadService
  ├─ UploadSession 조회 (sessionId)
  ├─ 세션 상태 검증 (IN_PROGRESS만 허용)
  ├─ 세션 만료 체크 (expiresAt)
  ├─ File 상태 업데이트 (COMPLETED)
  └─ UploadSession 상태 업데이트 (COMPLETED)
  ↓
3. Response
  {sessionId, fileId, status="COMPLETED"}
```

---

## 🏗️ 비즈니스 구조

### Tenant + UploaderType

```
Tenant 1: Connectly (커넥틀리)
└─ 세토프 커머스
   ├─ Admin: 커넥틀리 관리자 (자사 상품)
   ├─ Seller: 입점 셀러들 (회사별)
   └─ Customer: 일반 고객 (리뷰)
```

### UploaderType Enum

```java
public enum UploaderType {
    ADMIN,      // 커넥틀리 관리자
    SELLER,     // 입점 셀러
    CUSTOMER    // 일반 고객
}
```

### 스토리지 경로 전략 (S3Key VO)

```
Admin:
uploads/1/admin/connectly/{category}/{uuid}_{file}
  - category: banner, event, excel, notice, default

Seller:
uploads/1/seller/{company-slug}/{category}/{uuid}_{file}
  - category: product, review, promotion, default

Customer:
uploads/1/customer/default/{uuid}_{file}
  - category: 없음 (항상 default)
```

**실제 예시**:
```
uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg
uploads/1/seller/samsung-electronics/product/01JD8010_갤럭시.jpg
uploads/1/customer/default/01JD8100_리뷰.jpg
```

---

## 🧱 주요 컴포넌트

### Domain Layer

| 컴포넌트 | 책임 | 파일 경로 |
|---------|------|----------|
| **FileId** | UUID v7 생성 및 검증 | `domain/shared/vo/FileId.java` |
| **SessionId** | UUID v7 멱등키 생성 | `domain/shared/vo/SessionId.java` |
| **FileName** | 파일명 검증 (1-255자) | `domain/shared/vo/FileName.java` |
| **FileSize** | 파일 크기 검증 (1B-1GB) | `domain/shared/vo/FileSize.java` |
| **MimeType** | MIME 타입 화이트리스트 검증 | `domain/shared/vo/MimeType.java` |
| **S3Key** | 스토리지 경로 생성 | `domain/shared/vo/S3Key.java` |
| **S3Bucket** | S3 버킷 네이밍 | `domain/shared/vo/S3Bucket.java` |
| **TenantId** | 테넌트 식별자 (Long FK) | `domain/shared/vo/TenantId.java` |
| **UploaderId** | 업로더 식별자 (Long FK) | `domain/shared/vo/UploaderId.java` |
| **FileCategory** | 카테고리 검증 (UploaderType별) | `domain/shared/vo/FileCategory.java` |
| **PresignedUrl** | Presigned URL 검증 | `domain/shared/vo/PresignedUrl.java` |
| **UploadSession** | 세션 기반 멱등성 관리 | `domain/session/UploadSession.java` |
| **File** | 파일 메타데이터 관리 | `domain/file/File.java` |

### Application Layer

| 컴포넌트 | 책임 | Transaction | 파일 경로 |
|---------|------|-------------|----------|
| **GeneratePresignedUrlFacade** | Orchestration (S3 호출 분리) | ❌ | `application/facade/GeneratePresignedUrlFacade.java` |
| **SessionManager** | Transaction 경계 관리 | ✅ | `application/manager/SessionManager.java` |
| **CompleteUploadService** | 업로드 완료 처리 | ✅ | `application/service/CompleteUploadService.java` |

### Persistence Layer

| 컴포넌트 | 책임 | 파일 경로 |
|---------|------|----------|
| **FileJpaEntity** | File Aggregate → JPA 매핑 | `persistence/mysql/entity/FileJpaEntity.java` |
| **UploadSessionJpaEntity** | UploadSession → JPA 매핑 | `persistence/mysql/entity/UploadSessionJpaEntity.java` |
| **S3ClientAdapter** | AWS S3 SDK 래핑 | `persistence/s3/adapter/S3ClientAdapter.java` |
| **V1__create_files_table.sql** | files 테이블 생성 | `persistence/mysql/migration/` |
| **V2__create_upload_sessions_table.sql** | upload_sessions 테이블 생성 | `persistence/mysql/migration/` |

### REST API Layer

| 컴포넌트 | 책임 | 파일 경로 |
|---------|------|----------|
| **FileApiController** | HTTP 요청/응답 처리 | `rest-api/controller/FileApiController.java` |
| **GeneratePresignedUrlRequest** | 요청 DTO | `rest-api/dto/request/GeneratePresignedUrlRequest.java` |
| **PresignedUrlResponse** | 응답 DTO | `rest-api/dto/response/PresignedUrlResponse.java` |
| **GlobalExceptionHandler** | Domain Exception → HTTP Status | `rest-api/error/GlobalExceptionHandler.java` |

---

## ✅ 완료 조건 (Definition of Done)

### 기능 요구사항

- [ ] Presigned URL 발급 API 구현 완료
- [ ] 업로드 완료 API 구현 완료
- [ ] 멱등성 보장 (동일 sessionId로 중복 발급 방지)
- [ ] 세션 만료 체크 (5분 초과 시 에러)
- [ ] UploaderType별 스토리지 경로 생성 (Admin/Seller/Customer)

### 품질 요구사항

- [ ] 모든 Unit Test 통과 (Domain, Application)
- [ ] 모든 Integration Test 통과 (Persistence, REST API)
- [ ] E2E Test 통과 (Presigned URL 발급 → S3 업로드 → 완료 처리)
- [ ] ArchUnit Test 통과 (레이어 의존성, 네이밍 규칙)
- [ ] Zero-Tolerance 규칙 100% 준수

### Zero-Tolerance 체크리스트

- [ ] ✅ Lombok 금지 (Plain Java 또는 Record 사용)
- [ ] ✅ Law of Demeter 준수 (Getter 체이닝 금지)
- [ ] ✅ Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] ✅ Transaction 경계 (외부 API 호출은 트랜잭션 밖)
- [ ] ✅ Spring 프록시 제약사항 (Private/Final/내부 호출 금지)
- [ ] ✅ Orchestration Pattern (Facade + Manager)
- [ ] ✅ Javadoc 필수 (public 메서드)
- [ ] ✅ Scope 준수 (MVP 범위 초과 금지)

### 성능 요구사항

- [ ] Presigned URL 발급 응답 시간 < 200ms (P95)
- [ ] 업로드 완료 처리 시간 < 100ms (P95)
- [ ] DB Connection Pool 효율성 (외부 API 호출 시 Connection 미점유)

---

## 📚 관련 문서

- **상세 설계**:
  - [domain.md](./domain.md) - Domain Layer 상세 설계
  - [application.md](./application.md) - Application Layer 상세 설계
  - [persistence.md](./persistence.md) - Persistence Layer 상세 설계
  - [rest-api.md](./rest-api.md) - REST API Layer 상세 설계

- **구현 계획**:
  - [TASK-001.md](./TASK-001.md) - 20 TDD Cycles
  - [plan.md](./plan.md) - 5-Day Development Plan

- **코딩 규칙**:
  - [Domain Layer 규칙](../../../coding_convention/02-domain-layer/)
  - [Application Layer 규칙](../../../coding_convention/03-application-layer/)
  - [Persistence Layer 규칙](../../../coding_convention/04-persistence-layer/)
  - [REST API Layer 규칙](../../../coding_convention/01-adapter-in-layer/rest-api/)

---

## 🔄 다음 단계

### 개발 순서

1. **Domain Layer** (2일):
   - 11개 Value Objects 구현
   - UploadSession Aggregate 구현
   - File Aggregate 구현
   - Domain Exceptions 구현

2. **Application Layer** (1.5일):
   - GeneratePresignedUrlFacade 구현 (Orchestration Pattern)
   - SessionManager 구현 (Transaction 경계)
   - CompleteUploadService 구현

3. **Persistence Layer** (1일):
   - FileJpaEntity, UploadSessionJpaEntity 구현
   - Flyway Migration 작성
   - S3ClientAdapter 구현

4. **REST API Layer** (0.5일):
   - FileApiController 구현
   - Request/Response DTO 구현
   - GlobalExceptionHandler 구현

### 후속 Bounded Contexts

- **Level 2**: `messaging/outbox`, `session/multi`, `session/download`
- **Level 3**: `file/processing`, `validation/checksum`, `session/cleanup`
- **Level 4**: `file/retention`, `security/visibility`

---

**변경 이력**:
- 2025-11-18: 초안 작성 (session/single Bounded Context 문서 분리)
