# FILE-001: Domain Layer 구현

**Epic**: File Management System (파일 관리 시스템)
**Layer**: Domain Layer
**브랜치**: feature/FILE-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 업로드 도메인 모델을 구현합니다.
- File Aggregate (업로드 완료된 파일)
- UploadSession Aggregate (업로드 세션)
- 11개 Value Objects (FileId, FileName, FileSize 등)
- 3개 Enums (FileStatus, SessionStatus, UploadType)
- 5개 Domain Exceptions

---

## 🎯 요구사항

### A. Value Objects (11개)

#### 1. FileId (UUID v7)
- [ ] `generate()`: UUID v7 생성
- [ ] `uuid()`: String 반환

#### 2. FileName
- [ ] `of(String)`: 파일명 검증 (1-255자)
- [ ] 빈 값 검증
- [ ] 최대 길이 검증

#### 3. FileSize
- [ ] `of(Long)`: 파일 크기 검증 (1 byte ~ 1GB)
- [ ] 최소 크기 검증
- [ ] 최대 크기 검증 (FileSizeExceededException)

#### 4. MimeType
- [ ] `of(String)`: MIME Type 검증
- [ ] 허용 목록: image/jpeg, image/png, image/gif, image/webp, application/pdf, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
- [ ] UnsupportedMimeTypeException

#### 5. TenantId
- [ ] `of(Long)`: TenantId 검증 (1 이상)

#### 6. UploaderId
- [ ] `of(Long)`: UploaderId 검증 (1 이상)

#### 7. FileCategory (서브카테고리)
- [ ] `of(String, UploaderType)`: 카테고리 검증
- [ ] Admin 카테고리: banner, event, excel, notice, default
- [ ] Seller 카테고리: product, review, promotion, default
- [ ] Customer 카테고리: default만 허용
- [ ] `defaultCategory()`: default 반환

#### 8. S3Key
- [ ] `generate(...)`: S3 경로 생성
  - Admin: `uploads/{tenantId}/admin/{uploaderSlug}/{category}/{fileId}_{fileName}`
  - Seller: `uploads/{tenantId}/seller/{uploaderSlug}/{category}/{fileId}_{fileName}`
  - Customer: `uploads/{tenantId}/customer/default/{fileId}_{fileName}`

#### 9. S3Bucket
- [ ] `forTenant(TenantId)`: Bucket 이름 생성 (`fileflow-uploads-{tenantId}`)

#### 10. SessionId (UUID v7)
- [ ] `generate()`: UUID v7 생성
- [ ] `of(String)`: SessionId 검증 (빈 값 금지)

#### 11. PresignedUrl
- [ ] `of(String)`: Presigned URL 검증 (빈 값 금지)

---

### B. Enums (4개)

#### 1. FileStatus
- [ ] `PENDING`: 업로드 대기 중
- [ ] `COMPLETED`: 업로드 완료

#### 2. SessionStatus
- [ ] `INITIATED`: 세션 생성됨
- [ ] `COMPLETED`: 업로드 완료
- [ ] `EXPIRED`: 세션 만료

#### 3. UploadType
- [ ] `SINGLE`: 단일 업로드 (MVP)

#### 4. UploaderType
- [ ] `ADMIN`: 관리자
- [ ] `SELLER`: 입점 셀러
- [ ] `CUSTOMER`: 일반 고객

---

### C. Aggregate Root (2개)

#### 1. File Aggregate
**필드**:
- [ ] `fileId`: FileId (식별자)
- [ ] `fileName`: FileName
- [ ] `fileSize`: FileSize
- [ ] `mimeType`: MimeType
- [ ] `s3Key`: S3Key
- [ ] `s3Bucket`: S3Bucket
- [ ] `uploaderId`: UploaderId
- [ ] `uploaderType`: UploaderType
- [ ] `uploaderSlug`: String (회사 slug)
- [ ] `category`: FileCategory
- [ ] `tenantId`: TenantId
- [ ] `status`: FileStatus
- [ ] `createdAt`: LocalDateTime
- [ ] `updatedAt`: LocalDateTime

**메서드**:
- [ ] `createFromSession(...)`: UploadSession 완료 후 File 생성
  - 상태: `COMPLETED`
  - Clock 사용
- [ ] Getter 메서드 (Plain Java)

#### 2. UploadSession Aggregate
**필드**:
- [ ] `sessionId`: SessionId (식별자)
- [ ] `tenantId`: TenantId
- [ ] `fileName`: FileName
- [ ] `fileSize`: FileSize
- [ ] `mimeType`: MimeType
- [ ] `uploadType`: UploadType
- [ ] `presignedUrl`: PresignedUrl
- [ ] `expiresAt`: LocalDateTime (5분)
- [ ] `status`: SessionStatus
- [ ] `createdAt`: LocalDateTime
- [ ] `updatedAt`: LocalDateTime

**메서드**:
- [ ] `initiate(...)`: 세션 초기화
  - 상태: `INITIATED`
  - 만료 시간: 5분 후
  - Clock 사용
- [ ] `ensureNotExpired(Clock)`: 세션 만료 확인 → SessionExpiredException
- [ ] `ensureNotCompleted()`: 중복 완료 방지 → SessionAlreadyCompletedException
- [ ] `markAsCompleted(Clock)`: 세션 완료 처리
  - 상태: `INITIATED` → `COMPLETED`
  - InvalidSessionStatusException
- [ ] Getter 메서드 (Plain Java)

---

### D. Domain Exceptions (5개)

#### 1. SessionExpiredException
- [ ] SessionId 포함 메시지

#### 2. SessionAlreadyCompletedException
- [ ] SessionId 포함 메시지

#### 3. InvalidSessionStatusException
- [ ] SessionId, 현재 상태, 예상 상태 포함 메시지

#### 4. FileSizeExceededException
- [ ] 실제 크기, 최대 크기 포함 메시지

#### 5. UnsupportedMimeTypeException
- [ ] MIME Type 포함 메시지

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지**: Plain Java만 사용
- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
- [ ] **Tell, Don't Ask**: 도메인 메서드로 상태 전환 캡슐화
- [ ] **Private Constructor**: 생성자는 private, 정적 팩토리 메서드 사용

### Domain Layer 규칙
- [ ] **Aggregate 경계 명확화**: File ≠ UploadSession (별도 Aggregate)
- [ ] **VO 불변성**: Record 사용 또는 final 필드
- [ ] **비즈니스 규칙 캡슐화**: VO에서 검증, Aggregate에서 상태 전환
- [ ] **Clock 의존성 주입**: LocalDateTime.now() 금지

### 테스트 규칙
- [ ] **ArchUnit 테스트 필수**
  - Aggregate: private 생성자, 정적 팩토리 메서드
  - VO: Record 또는 final 필드
  - Exception: DomainException 상속
- [ ] **TestFixture 사용**: Aggregate 생성 시
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] 11개 Value Objects 구현 완료
- [ ] 4개 Enums 구현 완료
- [ ] 2개 Aggregate 구현 완료
- [ ] 5개 Domain Exceptions 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] ArchUnit 테스트 통과
  - `AggregateRootArchTest`
  - `VOArchTest`
  - `ExceptionArchTest`
- [ ] Zero-Tolerance 규칙 100% 준수
- [ ] 테스트 커버리지 > 80%
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mvp/file-upload-mvp.md
- **Plan**: docs/prd/plans/FILE-001-domain-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: docs/coding_convention/02-domain-layer/domain-guide.md

---

## 📚 참고 규칙

- `docs/coding_convention/02-domain-layer/aggregate/guide.md` (Aggregate Root 패턴)
- `docs/coding_convention/02-domain-layer/vo/guide.md` (Value Object 패턴)
- `docs/coding_convention/02-domain-layer/exception/guide.md` (Domain Exception 패턴)
