# FILE-002: Application Layer 구현

**Epic**: File Management System (파일 관리 시스템)
**Layer**: Application Layer
**브랜치**: feature/FILE-002-application
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 업로드 비즈니스 로직을 구현합니다.
- Presigned URL 발급 UseCase
- 업로드 완료 처리 UseCase
- Port 인터페이스 (In/Out)
- UserContext (JWT 기반)

---

## 🎯 요구사항

### A. Command DTOs (2개)

#### 1. GeneratePresignedUrlCommand
- [ ] `sessionId`: SessionId
- [ ] `fileName`: FileName
- [ ] `fileSize`: FileSize
- [ ] `mimeType`: MimeType
- [ ] `category`: FileCategory (Nullable)

#### 2. CompleteUploadCommand
- [ ] `sessionId`: SessionId

---

### B. Response DTOs (2개)

#### 1. PresignedUrlResponse
- [ ] `sessionId`: String
- [ ] `fileId`: String
- [ ] `presignedUrl`: String
- [ ] `expiresIn`: int (초 단위, 300초)
- [ ] `uploadType`: String ("SINGLE")

#### 2. FileResponse
- [ ] `sessionId`: String
- [ ] `fileId`: String
- [ ] `fileName`: String
- [ ] `fileSize`: Long
- [ ] `mimeType`: String
- [ ] `status`: String
- [ ] `s3Key`: String
- [ ] `s3Bucket`: String
- [ ] `createdAt`: LocalDateTime

---

### C. Port In (UseCase) (2개)

#### 1. GeneratePresignedUrlUseCase
- [ ] `execute(GeneratePresignedUrlCommand)`: PresignedUrlResponse
- [ ] 멱등성 보장 (동일 sessionId 재요청 시 기존 URL 반환)

#### 2. CompleteUploadUseCase
- [ ] `execute(CompleteUploadCommand)`: FileResponse
- [ ] 세션 상태 검증 (만료, 중복 완료)

---

### D. Port Out - Command (2개)

#### 1. FilePersistencePort
- [ ] `save(File)`: File

#### 2. UploadSessionPersistencePort
- [ ] `save(UploadSession)`: UploadSession
- [ ] `update(UploadSession)`: UploadSession

---

### E. Port Out - Query (1개)

#### UploadSessionQueryPort
- [ ] `findBySessionId(SessionId)`: Optional<UploadSession>

---

### F. Port Out - External (1개)

#### S3ClientPort
- [ ] `generatePresignedPutUrl(S3Bucket, S3Key, MimeType, Duration)`: PresignedUrl

---

### G. UserContext (1개)

#### UserContext (JWT 기반)
- [ ] `tenantId`: TenantId
- [ ] `uploaderId`: UploaderId
- [ ] `uploaderType`: UploaderType
- [ ] `uploaderSlug`: String (회사 slug)

**추출 위치**: `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`

---

### H. UseCases Implementation (2개)

#### 1. GeneratePresignedUrlService

**구현 로직**:
1. [ ] SecurityContext에서 UserContext 추출
2. [ ] 멱등성 확인 (기존 sessionId 존재 시 기존 URL 반환)
3. [ ] FileId 생성 (UUID v7)
4. [ ] FileCategory 처리:
   - Customer: 항상 `default`
   - Admin/Seller: 요청 category 또는 `default`
5. [ ] S3Key 생성 (경로 전략 적용)
6. [ ] S3Bucket 생성
7. [ ] Presigned URL 생성 (S3ClientPort)
8. [ ] UploadSession 생성 및 저장
9. [ ] PresignedUrlResponse 반환

**Transaction 규칙**:
- [ ] `@Transactional` 필수
- [ ] 외부 API 호출 (S3) 있음 → **Transaction 경계 주의**

#### 2. CompleteUploadService

**구현 로직**:
1. [ ] SecurityContext에서 UserContext 추출
2. [ ] UploadSession 조회 (SessionNotFoundException)
3. [ ] 세션 상태 검증:
   - `ensureNotExpired()`: SessionExpiredException
   - `ensureNotCompleted()`: SessionAlreadyCompletedException
4. [ ] FileId 생성
5. [ ] FileCategory 처리 (세션 생성 시와 동일)
6. [ ] S3Key 재생성
7. [ ] S3Bucket 생성
8. [ ] File Aggregate 생성 (`createFromSession`)
9. [ ] File 저장
10. [ ] UploadSession 완료 처리 (`markAsCompleted`)
11. [ ] FileResponse 반환

**Transaction 규칙**:
- [ ] `@Transactional` 필수
- [ ] 외부 API 호출 없음 → Transaction 내 안전

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지**: Plain Java만 사용
- [ ] **Law of Demeter 준수**: DTO Flat 구조
- [ ] **Transaction 경계 엄격히 준수**:
  - ❌ `@Transactional` 내부에서 외부 API 호출 금지
  - ✅ S3 호출 후 Transaction 시작 (GeneratePresignedUrlService는 주의)
- [ ] **Port 분리**: Command/Query 명확히 분리

### Application Layer 규칙
- [ ] **UseCase 단일 책임**: 1개 UseCase = 1개 비즈니스 기능
- [ ] **DTO → Domain 변환**: Controller에서 DTO 받고 Command로 변환
- [ ] **Domain → DTO 변환**: UseCase에서 Response DTO 반환
- [ ] **Port 의존성**: 구현체가 아닌 인터페이스에만 의존

### 테스트 규칙
- [ ] **ArchUnit 테스트 필수**:
  - UseCase: interface + `@Component` 구현체
  - Port: interface만
  - Command DTO: Record
  - Response DTO: Record
- [ ] **TestFixture 사용**: Aggregate, VO 생성 시
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] 2개 Command DTOs 구현 완료
- [ ] 2개 Response DTOs 구현 완료
- [ ] 2개 Port In (UseCase) 인터페이스 정의
- [ ] 2개 Port Out (Command) 인터페이스 정의
- [ ] 1개 Port Out (Query) 인터페이스 정의
- [ ] 1개 Port Out (External) 인터페이스 정의
- [ ] 1개 UserContext 구현
- [ ] 2개 UseCases 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] ArchUnit 테스트 통과
  - `ApplicationLayerDependencyRules`
  - `UseCaseNamingRules`
  - `PortNamingRules`
  - `DtoRecordRules`
- [ ] Transaction 경계 검증 완료 (pre-commit hook)
- [ ] Zero-Tolerance 규칙 100% 준수
- [ ] 테스트 커버리지 > 80%
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mvp/file-upload-mvp.md
- **Domain Layer**: docs/prd/tasks/FILE-001.md
- **Plan**: docs/prd/plans/FILE-002-application-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: docs/coding_convention/03-application-layer/application-guide.md

---

## 📚 참고 규칙

- `docs/coding_convention/03-application-layer/port/in/command/guide.md` (Command UseCase)
- `docs/coding_convention/03-application-layer/port/out/command/guide.md` (Persistence Port)
- `docs/coding_convention/03-application-layer/dto/command/command-dto-guide.md` (Command DTO)
- `docs/coding_convention/03-application-layer/dto/response/response-dto-guide.md` (Response DTO)
- `docs/coding_convention/03-application-layer/manager/transaction-manager-guide.md` (Transaction 규칙)
