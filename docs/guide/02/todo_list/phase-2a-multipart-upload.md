# Phase 2A: Multipart Upload

**진행 상태**: ⏳ 대기 중 (0/10 - 0%)

## 개요

Phase 2A는 대용량 파일(>100MB) 업로드를 위한 Multipart Upload 기능을 구현합니다.
AWS S3 Multipart Upload API를 활용하여 파일을 작은 파트로 나눠 업로드하고, 클라이언트가 직접 S3에 업로드할 수 있도록 Presigned URL을 제공합니다.

**핵심 목표**: 대용량 파일 안정적 업로드, 네트워크 효율성, 재시도 가능

## Multipart Upload 흐름

```
1. Init → providerUploadId(S3 UploadId) 발급
2. 각 파트별 Presigned URL 생성
3. 클라이언트가 직접 S3에 업로드
4. 서버에 업로드 완료 통보 (ETag)
5. Complete → S3가 모든 파트 병합
```

## 태스크 목록

### ⏳ KAN-310: MultipartUpload Aggregate 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: MultipartUpload Aggregate 구현 (상태 머신: INIT → IN_PROGRESS → COMPLETED/ABORTED/FAILED)

**세부 작업**:
- [ ] MultipartUpload Aggregate Root 클래스 생성
- [ ] 상태 머신 구현 (6가지 상태 전환 로직)
- [ ] providerUploadId (S3 UploadId) 관리
- [ ] totalParts, uploadedParts 추적
- [ ] Domain Event 발행 (MultipartInitiated, MultipartCompleted, MultipartFailed)
- [ ] Invariant 검증 (파트 번호 중복 방지, 상태 전환 규칙)

**DoD**:
- [ ] Zero-Tolerance 규칙 준수 (Lombok 금지, Law of Demeter)
- [ ] Javadoc 작성 (@author, @since 포함)
- [ ] Unit Test 작성 (Coverage ≥ 80%)
- [ ] 상태 전환 시나리오 테스트 (happy path + 예외 케이스)

**참고**:
- schema.sql: upload_multipart 테이블 (lines 66-89)
- seed.sql: usn_demo_multi_001 샘플 데이터

**도메인 모델 예시**:
```java
/**
 * MultipartUpload Aggregate Root
 *
 * @author FileFlow Team
 * @since 2025-01-01
 */
public class MultipartUpload {
    private MultipartUploadId id;
    private String sessionId; // FK to UploadSession
    private String providerUploadId; // S3 UploadId
    private int totalParts;
    private int uploadedParts;
    private MultipartUploadStatus status;
    private List<UploadPart> parts;

    // 비즈니스 로직
    public void markPartUploaded(int partNo, String etag, long size) {
        // Invariant 검증
        if (status != MultipartUploadStatus.IN_PROGRESS) {
            throw new InvalidStateException("Cannot mark part uploaded in status: " + status);
        }

        // 파트 업데이트
        UploadPart part = findPart(partNo);
        part.markUploaded(etag, size);
        uploadedParts++;

        // Domain Event 발행
        registerEvent(new PartUploadedEvent(id, partNo, etag));
    }

    public void complete(List<PartETag> partETags) {
        // 모든 파트 업로드 확인
        if (uploadedParts != totalParts) {
            throw new IncompleteUploadException(
                "Uploaded " + uploadedParts + " / " + totalParts
            );
        }

        // 상태 전환
        this.status = MultipartUploadStatus.COMPLETED;

        // Domain Event 발행
        registerEvent(new MultipartCompletedEvent(id, sessionId, partETags));
    }
}
```

---

### ⏳ KAN-311: UploadPart Value Object 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: UploadPart Value Object 구현

**구현 클래스**:
- `UploadPart.java` (Value Object)
- `PartETag.java` (Record - partNo, etag)

**핵심**:
- partNo (1부터 시작, 순차 증가)
- etag (S3가 반환하는 해시 값)
- size (파트 크기, bytes)
- uploadedAt (업로드 완료 시각)

**DoD**:
- [ ] Record 패턴 사용 (PartETag)
- [ ] equals/hashCode 구현 (partNo 기준)
- [ ] Unit Test 작성

---

### ⏳ KAN-312: UploadSession Aggregate 확장

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: UploadSession Aggregate 확장 (Multipart 지원)

**확장 내용**:
- `uploadType` 추가 (DIRECT/MULTIPART/EXTERNAL)
- `multipartUploadId` 필드 추가 (Optional FK)
- `startMultipartUpload()` 메서드
- `completeMultipartUpload()` 메서드

**DoD**:
- [ ] Long FK 전략 유지 (multipartUploadId: Long)
- [ ] 상태 전환 로직 확장
- [ ] Unit Test 작성

---

### ⏳ KAN-313: MultipartUploadJpaAdapter 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: MultipartUpload JPA Persistence Adapter 구현

**구현 클래스**:
- `MultipartUploadJpaEntity.java`
- `UploadPartJpaEntity.java`
- `MultipartUploadJpaRepository`
- `MultipartUploadRepositoryAdapter`
- `MultipartUploadMapper` (Domain ↔ Entity)

**DoD**:
- [ ] MultipartUploadRepositoryPort 구현
- [ ] @OneToMany(cascade = ALL) 사용 (parts)
- [ ] findBySessionId() 쿼리 메서드
- [ ] Integration Test (TestContainers)

---

### ⏳ KAN-314: UploadSessionJpaAdapter 확장

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: UploadSessionJpaAdapter 확장 (Multipart 관계 추가)

**확장 내용**:
- `UploadSessionJpaEntity`에 uploadType 컬럼 추가
- `multipartUploadId` 컬럼 추가 (Long FK)
- Mapper 확장

**DoD**:
- [ ] 기존 테스트 통과
- [ ] Multipart 관계 매핑 테스트

---

### ⏳ KAN-315: InitMultipartUploadUseCase 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: InitMultipartUploadUseCase 구현 (S3 UploadId 생성)

**세부 작업**:
- [ ] InitMultipartUploadCommand 생성
  - sessionId (String, "usn_xxx")
  - totalParts (int, 예상 파트 수)

- [ ] InitMultipartUploadUseCase 구현
  - UploadSession 조회 및 상태 검증 (INIT or IN_PROGRESS만 허용)
  - S3 MultipartUpload 시작 (AWS SDK)
  - MultipartUpload Aggregate 생성 (providerUploadId 저장)
  - Repository save()

- [ ] InitMultipartUploadResponse DTO
  - providerUploadId (S3 UploadId)
  - totalParts
  - status

**트랜잭션 경계**:
- [ ] @Transactional - S3 호출 제외 (S3 API는 트랜잭션 밖)
- [ ] 보상 트랜잭션: S3 AbortMultipartUpload (실패 시)

**DoD**:
- [ ] Unit Test (Aggregate 생성 로직)
- [ ] Integration Test (S3 Mock 사용)
- [ ] Exception 처리 (세션 없음, 상태 충돌, S3 오류)

**참고**:
- 02-upload-management-develop-guide.md: Section 3 (lines 77-104)

**트랜잭션 처리 예시**:
```java
@Service
public class InitMultipartUploadUseCase {
    private final UploadSessionRepository sessionRepo;
    private final MultipartUploadRepository multipartRepo;
    private final S3StoragePort s3Port; // Adapter

    @Transactional
    public InitMultipartUploadResponse execute(InitMultipartUploadCommand command) {
        // 1. 세션 조회 및 검증 (DB 트랜잭션 내)
        UploadSession session = sessionRepo.findById(command.sessionId())
            .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        if (!session.canStartMultipart()) {
            throw new InvalidSessionStateException(session.getStatus());
        }

        // 2. S3 Multipart 시작 (트랜잭션 밖 - 별도 try-catch)
        String providerUploadId;
        try {
            providerUploadId = s3Port.initiateMultipartUpload(
                session.getBucketName(),
                session.getObjectKey()
            );
        } catch (S3Exception e) {
            throw new StorageException("Failed to initiate multipart upload", e);
        }

        // 3. Aggregate 생성 및 저장 (DB 트랜잭션 내)
        MultipartUpload multipart = new MultipartUpload(
            session.getId(),
            providerUploadId,
            command.totalParts()
        );
        multipartRepo.save(multipart);

        // 4. 세션 업데이트
        session.startMultipartUpload(multipart.getId());
        sessionRepo.save(session);

        return new InitMultipartUploadResponse(
            providerUploadId,
            command.totalParts(),
            MultipartUploadStatus.IN_PROGRESS
        );
    }
}
```

---

### ⏳ KAN-316: GeneratePartPresignedUrlUseCase 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: GeneratePartPresignedUrlUseCase 구현

**핵심 로직**:
- Multipart 조회 및 상태 검증
- S3 UploadPart Presigned URL 생성
- URL 만료 시간: 15분

**DoD**:
- [ ] Presigned URL 생성 테스트
- [ ] 권한 검증 (세션 소유자만)
- [ ] P95 < 100ms

---

### ⏳ KAN-317: MarkPartUploadedUseCase 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: MarkPartUploadedUseCase 구현

**핵심 로직**:
- 파트 업로드 완료 마킹
- ETag, size 저장
- uploadedParts 증가

**DoD**:
- [ ] 중복 마킹 방지
- [ ] Unit Test 작성

---

### ⏳ KAN-318: CompleteMultipartUploadUseCase 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: CompleteMultipartUploadUseCase 구현

**핵심 로직**:
- 모든 파트 업로드 확인
- S3 CompleteMultipartUpload API 호출
- UploadSession 상태 → COMPLETED

**트랜잭션 주의**:
- S3 API 호출은 트랜잭션 밖
- 실패 시 재시도 가능하도록 idempotent 구현

**DoD**:
- [ ] 완전성 검증 (모든 파트 업로드됨)
- [ ] S3 Complete API 호출
- [ ] Integration Test

---

### ⏳ KAN-319: UploadController 확장 (Multipart 엔드포인트 4개)

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: UploadController 확장 (4개 multipart 엔드포인트 추가)

**API 엔드포인트**:

#### 1. POST /uploads/sessions/{sessionId}:multipart-init
- Request: `InitMultipartUploadRequest` (totalParts)
- Response: 200 OK + `InitMultipartUploadResponse` (providerUploadId)
- 권한: file.upload (세션 소유자 SELF)

#### 2. POST /uploads/sessions/{sessionId}:part
- Request: `GeneratePartPresignedUrlRequest` (partNo)
- Response: 200 OK + `GeneratePartPresignedUrlResponse` (url, expiresAt)
- 권한: 세션 소유자 SELF

#### 3. PUT /uploads/sessions/{sessionId}:part
- Request: `MarkPartUploadedRequest` (partNo, etag, size)
- Response: 204 No Content
- 권한: 세션 소유자 SELF

#### 4. POST /uploads/sessions/{sessionId}:complete
- Request: `CompleteMultipartUploadRequest` (parts: List<PartETag>)
- Response: 200 OK + `CompleteMultipartUploadResponse` (completed)
- 권한: 세션 소유자 SELF

**기술 제약사항**:
- [ ] @RestController + @RequestMapping("/api/v1/uploads")
- [ ] DTO → Command 변환 (Assembler 패턴)
- [ ] IAM 권한 검증 (AspectJ 또는 Interceptor)
- [ ] RFC7807 Problem JSON 오류 응답

**DoD**:
- [ ] OpenAPI 3.0 스펙 작성 (Swagger annotations)
- [ ] Controller 통합 테스트 (MockMvc)
- [ ] 권한 거부 시나리오 테스트 (403 Forbidden)
- [ ] 상태 충돌 시나리오 테스트 (409 Conflict)

**참고**:
- 02-upload-management-develop-guide.md: API 계약 (lines 77-162)
- docs/coding_convention/01-adapter-rest-api-layer/

**API 스펙 예시**:
```yaml
POST /api/v1/uploads/sessions/{sessionId}:multipart-init

Request:
{
  "totalParts": 10
}

Response (200 OK):
{
  "providerUploadId": "VXBsb2FkIElEIGZvciBjb250ZXh0Cg",
  "totalParts": 10,
  "status": "IN_PROGRESS"
}

Error (409 Conflict):
{
  "type": "https://api.fileflow.com/problems/invalid-session-state",
  "title": "Invalid Session State",
  "status": 409,
  "detail": "Session is in COMPLETED state, cannot start multipart upload"
}
```

---

## 📊 Phase 2A 요약

### 아키텍처 구성
```
Domain Layer:
- MultipartUpload (Aggregate Root)
- UploadPart (Value Object)
- UploadSession (확장)

Application Layer:
- InitMultipartUploadUseCase
- GeneratePartPresignedUrlUseCase
- MarkPartUploadedUseCase
- CompleteMultipartUploadUseCase

Adapter Layer:
- MultipartUploadJpaAdapter (Persistence)
- UploadSessionJpaAdapter (확장)
- UploadController (REST API 확장)
- S3StorageAdapter (AWS SDK)
```

### 성능 목표
- Presigned URL 생성 P95 < 100ms
- Init Multipart P95 < 200ms (S3 API 포함)
- Complete Multipart P95 < 500ms (S3 API 포함)

### 다음 단계
Phase 2A 완료 후 Phase 2B (External Download)로 진행
