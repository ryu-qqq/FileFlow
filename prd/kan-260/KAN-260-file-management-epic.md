# KAN-260: 파일 관리 시스템 에픽 - 전체 구현 가이드

## 📋 에픽 개요

**제목**: 파일 관리 시스템
**타입**: Epic
**상태**: 해야 할 일
**총 태스크**: 20개

## 🏗️ 구현 단계별 구조

### Phase 3A: 핵심 파일 관리 (7개 태스크)
- 파일 생성/조회/수정/삭제 기본 기능
- 업로드 완료 후 FileAsset 연계

### Phase 3B: 파일 관계/가시성 (5개 태스크)
- 파일 간 버전/참조/그룹 관계
- 파일 가시성 및 권한 관리

### Phase 3C: 파일 라이프사이클 (5개 태스크)
- 파일 만료 및 삭제 정책
- 접근 로그 기록

### 추가: 테스트 및 검증 (3개 태스크)
- 통합 테스트
- ArchUnit 아키텍처 검증

## 🚨 프로젝트 Zero-Tolerance 규칙 (필수 준수)

### 1. NO Lombok
```java
// ❌ 절대 금지
@Data, @Getter, @Setter, @Builder

// ✅ 수동 작성
public String getFileAssetId() {
    return this.fileAssetId;
}
```

### 2. Law of Demeter (Tell, Don't Ask)
```java
// ❌ 금지
fileAsset.getMetadata().getSize()

// ✅ 올바른 방법
fileAsset.getFileSize()
```

### 3. Long FK 전략 (JPA 관계 금지)
```java
// ❌ 금지
@ManyToOne
private Organization organization;

// ✅ 올바른 방법
private Long organizationId;
```

### 4. Transaction 경계
```java
// ❌ 금지
@Transactional
public void uploadAndNotify() {
    // DB 저장
    // 외부 API 호출 <- Transaction 내에서 금지!
}

// ✅ 올바른 방법
@Transactional
public void save() { }  // DB만

public void notify() { }  // 외부 API는 별도
```

## 🔧 프로젝트 구조

```
fileflow/
├── domain/                     # Domain Layer (비즈니스 로직)
│   └── src/main/java/com/ryuqq/fileflow/domain/
│       └── file/
│           ├── asset/         # FileAsset Aggregate
│           ├── relationship/  # FileRelationship
│           └── lifecycle/     # FileLifecycle
│
├── application/                # Application Layer (UseCase)
│   └── src/main/java/com/ryuqq/fileflow/application/
│       └── file/
│           ├── command/       # 생성/수정/삭제
│           └── query/         # 조회
│
├── adapter-in/                 # REST API Layer
│   └── rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/
│       └── file/
│           ├── controller/    # REST Controller
│           └── dto/           # Request/Response DTO
│
└── adapter-out/               # Persistence Layer
    └── persistence/src/main/java/com/ryuqq/fileflow/adapter/persistence/
        └── file/
            ├── entity/        # JPA Entity
            └── repository/    # Repository
```

## 📝 공통 작업 패턴

### 1. Domain Aggregate 구현 패턴
```java
public class FileAsset extends AbstractAggregateRoot<FileAsset> {
    // 1. 필드 선언 (final 선호)
    private final Long id;
    private final String fileAssetId;
    private Long uploadSessionId;  // Long FK

    // 2. Private 생성자
    private FileAsset(String fileAssetId, Long uploadSessionId) {
        this.fileAssetId = fileAssetId;
        this.uploadSessionId = uploadSessionId;
    }

    // 3. Static Factory Method
    public static FileAsset create(String fileAssetId, Long uploadSessionId) {
        validateFileAssetId(fileAssetId);
        return new FileAsset(fileAssetId, uploadSessionId);
    }

    // 4. 도메인 로직 (Tell, Don't Ask)
    public void markAsDeleted(Instant deletedAt) {
        this.status = FileStatus.DELETED;
        this.deletedAt = deletedAt;
        registerEvent(FileDeletedEvent.of(this.fileAssetId, deletedAt));
    }

    // 5. Getter만 (Setter 금지)
    public String getFileAssetId() {
        return this.fileAssetId;
    }
}
```

### 2. UseCase 구현 패턴
```java
@Component
@RequiredArgsConstructor  // 생성자 주입
public class CreateFileAssetUseCase {
    private final SaveFileAssetPort savePort;
    private final FileAssetAssembler assembler;

    @Transactional  // Application Layer에서만!
    public FileAssetResponse execute(CreateFileAssetCommand command) {
        // 1. Domain 객체 생성
        FileAsset fileAsset = FileAsset.create(
            command.getFileAssetId(),
            command.getUploadSessionId()
        );

        // 2. 저장
        FileAsset saved = savePort.save(fileAsset);

        // 3. Response 변환
        return assembler.toResponse(saved);
    }
}
```

### 3. REST Controller 구현 패턴
```java
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final CreateFileAssetUseCase createUseCase;
    private final FileApiMapper mapper;

    @PostMapping
    public ResponseEntity<FileAssetResponse> create(
        @Valid @RequestBody CreateFileRequest request
    ) {
        CreateFileAssetCommand command = mapper.toCommand(request);
        FileAssetResponse response = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### 4. JPA Entity 구현 패턴
```java
@Entity
@Table(name = "file_assets")
public class FileAssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_asset_id", nullable = false, unique = true)
    private String fileAssetId;

    @Column(name = "upload_session_id")  // Long FK, 관계 어노테이션 없음!
    private Long uploadSessionId;

    // NO Lombok - 수동 Getter/Setter
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
```

## 🎯 각 태스크별 체크리스트

### 모든 태스크 공통 체크리스트

#### 시작 전
- [ ] Jira 태스크 상태를 "진행 중"으로 변경
- [ ] 해당 기능의 요구사항 문서 확인
- [ ] 기존 코드 구조 파악

#### 코딩 중
- [ ] **NO Lombok** - 어노테이션 사용 금지
- [ ] **Law of Demeter** - Getter 체이닝 금지
- [ ] **Long FK** - JPA 관계 어노테이션 금지
- [ ] **Transaction 경계** - Application Layer에서만
- [ ] **Static Factory Method** 사용
- [ ] **Javadoc** 작성 (@author, @since 포함)

#### 완료 후
- [ ] 단위 테스트 작성 및 통과
- [ ] 통합 테스트 작성 및 통과
- [ ] 코드 리뷰 요청
- [ ] Jira 태스크 "완료"로 변경

## 📂 다음 단계

각 태스크별 상세 구현 가이드는 개별 문서를 참조하세요:
- Phase 3A: `prd/KAN-260-phase-3a-tasks.md`
- Phase 3B: `prd/KAN-260-phase-3b-tasks.md`
- Phase 3C: `prd/KAN-260-phase-3c-tasks.md`