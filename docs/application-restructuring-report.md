# Application 레이어 재구조화 리포트

## 📅 작업 일자
2025-01-18

## 🎯 재구조화 목적
Domain 레이어 Bounded Context 구조와의 일관성 유지

Domain 레이어에서 수립한 Bounded Context 구조(iam, session, file, outbox)를 Application 레이어에도 동일하게 적용하여:
- 도메인 컨텍스트 간 경계 명확화
- 레이어 간 구조 일관성 확보
- UseCase와 Port의 컨텍스트별 응집도 향상
- 향후 DTO, Port, Service 구현을 위한 구조 준비

## 📦 최종 패키지 구조

```
application/
├── src/main/java/com/ryuqq/fileflow/application/
│   ├── session/                    # Session Bounded Context
│   │   ├── dto/
│   │   │   ├── command/            # UploadSession 관련 Command DTO
│   │   │   ├── query/              # UploadSession 조회 Query DTO
│   │   │   └── response/           # UploadSession 응답 DTO
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   ├── command/        # GeneratePresignedUrl, CompleteUpload UseCase
│   │   │   │   └── query/          # Session 조회 UseCase
│   │   │   └── out/
│   │   │       ├── command/        # Session Persistence Port
│   │   │       ├── query/          # Session Query Port
│   │   │       └── external/       # S3, SQS Port
│   │   └── service/                # UseCase 구현
│   │
│   ├── file/                       # File Bounded Context
│   │   ├── dto/
│   │   │   ├── command/            # File 처리 Command DTO
│   │   │   ├── query/              # File 조회 Query DTO
│   │   │   └── response/           # File 응답 DTO
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   ├── command/        # ProcessFile, UploadFromExternalUrl UseCase
│   │   │   │   └── query/          # File 조회 UseCase
│   │   │   └── out/
│   │   │       ├── command/        # File Persistence Port
│   │   │       ├── query/          # File Query Port
│   │   │       └── external/       # Webhook, S3 Port
│   │   └── service/                # UseCase 구현
│   │
│   ├── outbox/                     # Outbox Bounded Context
│   │   ├── dto/
│   │   │   ├── command/            # MessageOutbox Command DTO
│   │   │   ├── query/              # MessageOutbox Query DTO
│   │   │   └── response/           # MessageOutbox 응답 DTO
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   ├── command/        # Outbox 처리 UseCase
│   │   │   │   └── query/          # Outbox 조회 UseCase
│   │   │   └── out/
│   │   │       ├── command/        # Outbox Persistence Port
│   │   │       ├── query/          # Outbox Query Port
│   │   │       └── external/       # Webhook Port
│   │   └── service/                # UseCase 구현
│   │
│   └── common/                     # 공통 DTO (기존 유지)
│       └── dto/
│           └── response/
│               ├── PageResponse    # 페이지네이션 응답
│               └── SliceResponse   # 슬라이스 응답
│
└── src/testFixtures/java/com/ryuqq/fileflow/application/
    ├── session/fixture/            # Session TestFixtures (4개)
    │   ├── GeneratePresignedUrlCommandFixture
    │   ├── PresignedUrlResponseFixture
    │   ├── CompleteUploadCommandFixture
    │   └── UploadFromExternalUrlCommandFixture
    │
    └── file/fixture/               # File TestFixtures (6개)
        ├── ProcessFileCommandFixture
        ├── GetFileQueryFixture
        ├── ListFilesQueryFixture
        ├── FileResponseFixture
        ├── FileDetailResponseFixture
        └── FileSummaryResponseFixture
```

## 📊 이동한 파일 통계

### Session Bounded Context
- **TestFixtures**: 4개
  - GeneratePresignedUrlCommandFixture
  - PresignedUrlResponseFixture
  - CompleteUploadCommandFixture
  - UploadFromExternalUrlCommandFixture
- **총**: 4개 파일

### File Bounded Context
- **TestFixtures**: 6개
  - ProcessFileCommandFixture
  - GetFileQueryFixture
  - ListFilesQueryFixture
  - FileResponseFixture
  - FileDetailResponseFixture
  - FileSummaryResponseFixture
- **총**: 6개 파일

### Outbox Bounded Context
- **TestFixtures**: 0개 (향후 추가 예정)

### 공통 DTO (application.common 유지)
- **Response DTO**: 2개
  - PageResponse (페이지네이션)
  - SliceResponse (슬라이스)
- **총**: 2개 파일 (이동하지 않음)

**전체 이동 파일**: 10개 (Session: 4, File: 6)

## 🔄 의존성 관계

```
        ┌─────────────┐
        │  Session BC │
        │ (UseCase)   │
        └──────┬──────┘
               │ uses
               ↓
        ┌─────────────┐
        │   File BC   │
        │ (UseCase)   │
        └──────┬──────┘
               │
               ↓
        ┌─────────────┐
        │  Outbox BC  │
        │ (Messaging) │
        └─────────────┘
```

**의존성 규칙**:
- **Session → File**: Session이 File DTO 참조 (FileSize, MimeType 등)
- **File → Outbox**: File이 Outbox Port 사용 (이벤트 발행)
- **Application → Domain**: 모든 Application UseCase가 Domain Aggregate 사용
- **역방향 의존성 금지**: Domain은 Application에 의존하지 않음 (헥사고날 아키텍처)

## ✅ 검증 결과

### 빌드 상태
- ⚠️ application:compileTestFixtures: **컴파일 에러** (예상됨)
  - 원인: TestFixtures가 참조하는 DTO 클래스가 아직 구현되지 않음
  - 영향: 100개 컴파일 에러 (Command, Query, Response DTO 미구현)
  - 해결: FILE-002 Plan 실행 시 DTO 구현으로 해결 예정

### 구조 검증
- ✅ Bounded Context 디렉토리 구조 생성: **완료**
- ✅ TestFixture 패키지 분리: **완료**
- ✅ Git 이력 보존 (git mv): **완료**
- ✅ Domain import 경로 업데이트: **완료**

### 패키지 선언 업데이트
```java
// Before
package com.ryuqq.fileflow.application.fixture;

// After (Session Context)
package com.ryuqq.fileflow.application.session.fixture;

// After (File Context)
package com.ryuqq.fileflow.application.file.fixture;
```

### Domain Import 업데이트
```java
// FileDetailResponseFixture.java
// Before
import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;

// After
import com.ryuqq.fileflow.domain.file.aggregate.FileProcessingJob;
```

## 🚀 다음 단계

1. **FILE-002: Application 레이어 구현**
   - Phase 1: DTO 구현 (Command, Query, Response)
   - Phase 2: Port 인터페이스 정의 (Port In/Out)
   - Phase 3: UseCase Service 구현
   - Phase 4: ArchUnit 테스트 추가

2. **MVP Scope (FILE-002)**
   - GeneratePresignedUrlUseCase (Presigned URL 생성)
   - CompleteUploadUseCase (업로드 완료 처리)
   - ProcessFileUseCase (파일 처리 Job 생성)

3. **Persistence 레이어 재구조화 (FILE-003 이후)**
   - Adapter를 Bounded Context별로 재구조화
   - Entity/Mapper를 컨텍스트별로 그룹화

4. **REST API 레이어 재구조화 (FILE-004 이후)**
   - Controller를 Bounded Context별로 재구조화
   - DTO를 컨텍스트별로 그룹화

## 📝 주요 변경사항

### 1. Bounded Context 도입
- 기존: 기술적 레이어(dto, port, service)로 분리
- 변경: 비즈니스 도메인(session, file, outbox)으로 먼저 분리 후 기술 레이어 분리

### 2. TestFixture 컨텍스트별 분리
- Session 관련 Fixture: `application.session.fixture`
- File 관련 Fixture: `application.file.fixture`
- Outbox 관련 Fixture: 향후 추가 예정

### 3. 공통 DTO 유지
- PageResponse, SliceResponse는 `application.common.dto.response`에 유지
- 여러 컨텍스트에서 공통 사용하는 응답 DTO

### 4. Git 이력 보존
- 모든 파일 이동에 `git mv` 사용
- 파일 이력 완전히 보존됨

### 5. 컴파일 에러 허용
- TestFixtures가 참조하는 DTO 미구현으로 컴파일 에러 발생
- 이는 TDD 워크플로우의 일부 (Red Phase)
- FILE-002 Plan 실행 시 순차적으로 해결

## 🔍 컴파일 에러 상세

### 미구현 DTO 클래스 (10개)

**Command DTO** (4개):
- GeneratePresignedUrlCommand
- CompleteUploadCommand
- ProcessFileCommand
- UploadFromExternalUrlCommand

**Query DTO** (2개):
- GetFileQuery
- ListFilesQuery

**Response DTO** (4개):
- PresignedUrlResponse
- FileResponse
- FileDetailResponse
- FileSummaryResponse

**해결 계획**: FILE-002 TDD Cycle에서 순차적으로 구현

## 📌 참고사항

### Git 이력 보존
- 모든 파일 이동에 `git mv` 사용
- 파일 이력 완전히 보존됨

### 커밋 메시지 규칙
- `struct:` prefix 사용 (Structural Changes)
- Kent Beck의 Tidy First 원칙 준수
- 동작 변경 없음, 구조만 개선

### Domain 의존성 업데이트
- FileProcessingJob import 경로 업데이트
- Domain 레이어 Bounded Context 구조 반영

## 📂 커밋 이력

```
4560741 struct: Application 레이어 Bounded Context별 재구조화 (Fixture 분리)
```

---

**작성자**: Claude Code
**검증**: ✅ 구조 생성 완료, DTO 구현 대기 중
**다음 작업**: FILE-002 Application 레이어 구현 (TDD)
