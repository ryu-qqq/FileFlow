# Phase 3B: 파일 관계/가시성 기능 구현 가이드

## 📋 Phase 3B 개요
- **목표**: 파일 간 관계(버전/참조/그룹) 및 가시성 관리
- **기간**: 2일 (Day 4-5)
- **태스크 수**: 5개

---

## 🎯 KAN-295: FileRelationship Domain Aggregate 구현

### 작업 내용
파일 간 관계(버전, 참조, 그룹)를 표현하는 도메인 모델을 구현합니다.

### 구현 체크리스트

#### 1. Domain Aggregate 구현
```java
package com.ryuqq.fileflow.domain.file.relationship;

/**
 * 파일 관계 Aggregate Root
 * 파일 간의 버전, 참조, 그룹 관계를 관리
 *
 * @author developer
 * @since 1.0
 */
public class FileRelationship extends AbstractAggregateRoot<FileRelationship> {

    private Long id;
    private String sourceFileAssetId;  // Long FK 대신 String ID
    private String targetFileAssetId;  // Long FK 대신 String ID
    private RelationshipType relationshipType;
    private Long tenantId;             // Long FK
    private Long organizationId;        // Long FK
    private Instant createdAt;
    private Long createdBy;             // 생성자 userId

    // Private 생성자 (NO Lombok!)
    private FileRelationship(String sourceFileAssetId,
                           String targetFileAssetId,
                           RelationshipType relationshipType,
                           Long tenantId,
                           Long organizationId,
                           Long createdBy) {
        validateRelationship(sourceFileAssetId, targetFileAssetId, relationshipType);
        this.sourceFileAssetId = sourceFileAssetId;
        this.targetFileAssetId = targetFileAssetId;
        this.relationshipType = relationshipType;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    // Static Factory Method
    public static FileRelationship create(String sourceFileAssetId,
                                         String targetFileAssetId,
                                         RelationshipType type,
                                         Long tenantId,
                                         Long organizationId,
                                         Long createdBy) {
        return new FileRelationship(
            sourceFileAssetId,
            targetFileAssetId,
            type,
            tenantId,
            organizationId,
            createdBy
        );
    }

    // 도메인 검증 로직
    private static void validateRelationship(String source,
                                            String target,
                                            RelationshipType type) {
        // 순환 참조 방지
        if (source.equals(target)) {
            throw new InvalidRelationshipException(
                "파일은 자기 자신과 관계를 가질 수 없습니다"
            );
        }

        // null 체크
        if (source == null || target == null) {
            throw new InvalidRelationshipException(
                "Source와 Target 파일은 필수입니다"
            );
        }

        // 타입별 추가 검증
        validateByType(type);
    }

    private static void validateByType(RelationshipType type) {
        // 타입별 비즈니스 규칙 검증
        switch (type) {
            case VERSION:
                // 버전 관계는 단방향만 허용
                break;
            case REFERENCE:
                // 참조 관계 규칙
                break;
            case GROUP:
                // 그룹 관계 규칙
                break;
        }
    }

    // Tell, Don't Ask - 비즈니스 로직
    public boolean isVersionRelationship() {
        return this.relationshipType == RelationshipType.VERSION;
    }

    public boolean involvesFile(String fileAssetId) {
        return this.sourceFileAssetId.equals(fileAssetId) ||
               this.targetFileAssetId.equals(fileAssetId);
    }

    public boolean canBeDeleted(Long userId) {
        // 생성자만 삭제 가능
        return this.createdBy.equals(userId);
    }

    // Getter only (NO Setter!)
    public Long getId() {
        return id;
    }

    public String getSourceFileAssetId() {
        return sourceFileAssetId;
    }

    public String getTargetFileAssetId() {
        return targetFileAssetId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
```

#### 2. RelationshipType Enum
```java
package com.ryuqq.fileflow.domain.file.relationship;

/**
 * 파일 관계 타입
 */
public enum RelationshipType {
    /**
     * 버전 관계 - 파일의 이전/다음 버전
     */
    VERSION("version", "파일 버전 관계"),

    /**
     * 참조 관계 - 한 파일이 다른 파일을 참조
     */
    REFERENCE("reference", "파일 참조 관계"),

    /**
     * 그룹 관계 - 관련 파일들의 묶음
     */
    GROUP("group", "파일 그룹 관계");

    private final String code;
    private final String description;

    RelationshipType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RelationshipType fromCode(String code) {
        for (RelationshipType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown relationship type: " + code);
    }
}
```

#### 3. Domain Exception
```java
package com.ryuqq.fileflow.domain.file.relationship.exception;

public class InvalidRelationshipException extends DomainException {
    public InvalidRelationshipException(String message) {
        super(message);
    }

    public InvalidRelationshipException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class DuplicateRelationshipException extends DomainException {
    public DuplicateRelationshipException(String source, String target,
                                        RelationshipType type) {
        super(String.format(
            "이미 존재하는 관계입니다: %s -> %s (%s)",
            source, target, type
        ));
    }
}
```

#### 4. 단위 테스트
```java
class FileRelationshipTest {

    @Test
    void 정상적인_관계_생성() {
        // Given
        String sourceId = "FILE-001";
        String targetId = "FILE-002";

        // When
        FileRelationship relationship = FileRelationship.create(
            sourceId, targetId,
            RelationshipType.VERSION,
            1L, 1L, 1L
        );

        // Then
        assertThat(relationship.getSourceFileAssetId()).isEqualTo(sourceId);
        assertThat(relationship.getTargetFileAssetId()).isEqualTo(targetId);
        assertThat(relationship.getRelationshipType()).isEqualTo(RelationshipType.VERSION);
    }

    @Test
    void 순환_참조_방지() {
        // Given
        String fileId = "FILE-001";

        // When & Then
        assertThatThrownBy(() ->
            FileRelationship.create(fileId, fileId,
                RelationshipType.VERSION, 1L, 1L, 1L)
        )
        .isInstanceOf(InvalidRelationshipException.class)
        .hasMessageContaining("자기 자신과 관계");
    }
}
```

---

## 🎯 KAN-296: LinkFileRelationshipUseCase 구현

### 작업 내용
파일 간 관계를 생성하는 UseCase를 구현합니다.

### 구현 체크리스트

#### 1. Command 정의
```java
package com.ryuqq.fileflow.application.file.command;

public class LinkFileRelationshipCommand {
    private final String sourceFileAssetId;
    private final String targetFileAssetId;
    private final RelationshipType relationshipType;
    private final Long tenantId;
    private final Long organizationId;
    private final Long userId;

    private LinkFileRelationshipCommand(Builder builder) {
        this.sourceFileAssetId = builder.sourceFileAssetId;
        this.targetFileAssetId = builder.targetFileAssetId;
        this.relationshipType = builder.relationshipType;
        this.tenantId = builder.tenantId;
        this.organizationId = builder.organizationId;
        this.userId = builder.userId;
    }

    // Builder Pattern (NO Lombok!)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sourceFileAssetId;
        private String targetFileAssetId;
        private RelationshipType relationshipType;
        private Long tenantId;
        private Long organizationId;
        private Long userId;

        public Builder sourceFileAssetId(String sourceFileAssetId) {
            this.sourceFileAssetId = sourceFileAssetId;
            return this;
        }

        public Builder targetFileAssetId(String targetFileAssetId) {
            this.targetFileAssetId = targetFileAssetId;
            return this;
        }

        public Builder relationshipType(RelationshipType relationshipType) {
            this.relationshipType = relationshipType;
            return this;
        }

        public Builder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder organizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public LinkFileRelationshipCommand build() {
            return new LinkFileRelationshipCommand(this);
        }
    }

    // Getters
    public String getSourceFileAssetId() {
        return sourceFileAssetId;
    }

    public String getTargetFileAssetId() {
        return targetFileAssetId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getUserId() {
        return userId;
    }
}
```

#### 2. UseCase 구현
```java
package com.ryuqq.fileflow.application.file.command;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkFileRelationshipUseCase {
    private final FileRelationshipRepository relationshipRepository;
    private final FileAssetQueryPort fileAssetQueryPort;
    private final FileRelationshipAssembler assembler;

    /**
     * 파일 관계 생성
     *
     * @param command 관계 생성 명령
     * @return 생성된 관계 정보
     */
    @Transactional  // Application Layer에서만!
    public FileRelationshipResponse execute(LinkFileRelationshipCommand command) {
        log.info("파일 관계 생성 시작: {} -> {} ({})",
            command.getSourceFileAssetId(),
            command.getTargetFileAssetId(),
            command.getRelationshipType());

        // 1. 파일 존재 여부 확인
        validateFilesExist(command);

        // 2. 중복 관계 체크
        checkDuplicateRelationship(command);

        // 3. 순환 참조 체크 (VERSION 타입의 경우)
        if (command.getRelationshipType() == RelationshipType.VERSION) {
            checkCyclicReference(command);
        }

        // 4. Domain 객체 생성
        FileRelationship relationship = FileRelationship.create(
            command.getSourceFileAssetId(),
            command.getTargetFileAssetId(),
            command.getRelationshipType(),
            command.getTenantId(),
            command.getOrganizationId(),
            command.getUserId()
        );

        // 5. 저장
        FileRelationship saved = relationshipRepository.save(relationship);

        // 6. 이벤트 발행 (필요시)
        publishRelationshipCreatedEvent(saved);

        log.info("파일 관계 생성 완료: relationshipId={}", saved.getId());

        // 7. Response 반환
        return assembler.toResponse(saved);
    }

    private void validateFilesExist(LinkFileRelationshipCommand command) {
        // Source 파일 확인
        fileAssetQueryPort.findByFileAssetId(command.getSourceFileAssetId())
            .orElseThrow(() -> new FileNotFoundException(
                "Source 파일을 찾을 수 없습니다: " + command.getSourceFileAssetId()
            ));

        // Target 파일 확인
        fileAssetQueryPort.findByFileAssetId(command.getTargetFileAssetId())
            .orElseThrow(() -> new FileNotFoundException(
                "Target 파일을 찾을 수 없습니다: " + command.getTargetFileAssetId()
            ));
    }

    private void checkDuplicateRelationship(LinkFileRelationshipCommand command) {
        boolean exists = relationshipRepository.existsBySourceAndTargetAndType(
            command.getSourceFileAssetId(),
            command.getTargetFileAssetId(),
            command.getRelationshipType()
        );

        if (exists) {
            throw new DuplicateRelationshipException(
                command.getSourceFileAssetId(),
                command.getTargetFileAssetId(),
                command.getRelationshipType()
            );
        }
    }

    private void checkCyclicReference(LinkFileRelationshipCommand command) {
        // VERSION 타입의 경우 순환 참조 체크
        Set<String> visited = new HashSet<>();
        checkCyclicReferenceRecursive(
            command.getTargetFileAssetId(),
            command.getSourceFileAssetId(),
            visited
        );
    }

    private void checkCyclicReferenceRecursive(String current,
                                              String target,
                                              Set<String> visited) {
        if (current.equals(target)) {
            throw new CyclicReferenceException(
                "순환 참조가 감지되었습니다"
            );
        }

        if (visited.contains(current)) {
            return;
        }

        visited.add(current);

        // current가 source인 모든 VERSION 관계 조회
        List<FileRelationship> relationships = relationshipRepository
            .findBySourceAndType(current, RelationshipType.VERSION);

        for (FileRelationship rel : relationships) {
            checkCyclicReferenceRecursive(
                rel.getTargetFileAssetId(),
                target,
                visited
            );
        }
    }

    private void publishRelationshipCreatedEvent(FileRelationship relationship) {
        // 도메인 이벤트 발행 (필요시)
        registerEvent(FileRelationshipCreatedEvent.of(
            relationship.getId(),
            relationship.getSourceFileAssetId(),
            relationship.getTargetFileAssetId(),
            relationship.getRelationshipType()
        ));
    }
}
```

#### 3. Response DTO
```java
public class FileRelationshipResponse {
    private Long id;
    private String sourceFileAssetId;
    private String targetFileAssetId;
    private String relationshipType;
    private Instant createdAt;

    // Static Factory Method
    public static FileRelationshipResponse of(FileRelationship domain) {
        FileRelationshipResponse response = new FileRelationshipResponse();
        response.id = domain.getId();
        response.sourceFileAssetId = domain.getSourceFileAssetId();
        response.targetFileAssetId = domain.getTargetFileAssetId();
        response.relationshipType = domain.getRelationshipType().getCode();
        response.createdAt = domain.getCreatedAt();
        return response;
    }

    // Getters (NO Setters for Response!)
    public Long getId() {
        return id;
    }

    public String getSourceFileAssetId() {
        return sourceFileAssetId;
    }

    public String getTargetFileAssetId() {
        return targetFileAssetId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
```

---

## 🎯 KAN-297: UpdateFileVisibilityUseCase 구현

### 작업 내용
파일의 가시성(PUBLIC, PRIVATE, INTERNAL)을 변경하는 UseCase를 구현합니다.

### 구현 체크리스트

#### 1. FileVisibility Enum
```java
package com.ryuqq.fileflow.domain.file.visibility;

public enum FileVisibility {
    /**
     * 공개 - 모든 사용자 접근 가능
     */
    PUBLIC("public", "공개"),

    /**
     * 내부 - 조직 내부 사용자만 접근 가능
     */
    INTERNAL("internal", "내부 공개"),

    /**
     * 비공개 - 소유자와 권한 부여된 사용자만 접근 가능
     */
    PRIVATE("private", "비공개");

    private final String code;
    private final String description;

    FileVisibility(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isAccessibleByOrganization() {
        return this == PUBLIC || this == INTERNAL;
    }
}
```

#### 2. UseCase 구현
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateFileVisibilityUseCase {
    private final FileAssetRepository fileAssetRepository;
    private final FilePermissionEvaluator permissionEvaluator;
    private final FileVisibilityHistoryPort historyPort;

    @Transactional
    public FileAssetResponse execute(UpdateFileVisibilityCommand command) {
        log.info("파일 가시성 변경: fileAssetId={}, visibility={}",
            command.getFileAssetId(), command.getNewVisibility());

        // 1. 파일 조회
        FileAsset fileAsset = fileAssetRepository
            .findByFileAssetId(command.getFileAssetId())
            .orElseThrow(() -> new FileNotFoundException(
                command.getFileAssetId()
            ));

        // 2. 권한 체크 (소유자 또는 ADMIN만 가능)
        if (!permissionEvaluator.canChangeVisibility(
            command.getUserId(),
            fileAsset)) {
            throw new InsufficientPermissionException(
                "파일 가시성을 변경할 권한이 없습니다"
            );
        }

        // 3. 이전 가시성 저장 (히스토리)
        FileVisibility previousVisibility = fileAsset.getVisibility();

        // 4. 가시성 변경
        fileAsset.updateVisibility(command.getNewVisibility());

        // 5. 저장
        FileAsset updated = fileAssetRepository.save(fileAsset);

        // 6. 히스토리 기록
        recordVisibilityChange(
            fileAsset.getFileAssetId(),
            previousVisibility,
            command.getNewVisibility(),
            command.getUserId(),
            command.getReason()
        );

        // 7. 이벤트 발행
        publishVisibilityChangedEvent(updated, previousVisibility);

        log.info("파일 가시성 변경 완료: {} -> {}",
            previousVisibility, command.getNewVisibility());

        return FileAssetResponse.of(updated);
    }

    private void recordVisibilityChange(String fileAssetId,
                                       FileVisibility from,
                                       FileVisibility to,
                                       Long changedBy,
                                       String reason) {
        VisibilityChangeHistory history = VisibilityChangeHistory.of(
            fileAssetId,
            from,
            to,
            changedBy,
            reason,
            Instant.now()
        );

        historyPort.save(history);
    }

    private void publishVisibilityChangedEvent(FileAsset fileAsset,
                                              FileVisibility previousVisibility) {
        FileVisibilityChangedEvent event = FileVisibilityChangedEvent.of(
            fileAsset.getFileAssetId(),
            previousVisibility,
            fileAsset.getVisibility(),
            Instant.now()
        );

        // 이벤트 발행 (캐시 무효화 등 처리)
        eventPublisher.publish(event);
    }
}
```

#### 3. FileAsset 도메인 수정
```java
public class FileAsset extends AbstractAggregateRoot<FileAsset> {
    // 기존 필드들...
    private FileVisibility visibility;

    /**
     * 가시성 변경
     * Tell, Don't Ask 패턴 적용
     */
    public void updateVisibility(FileVisibility newVisibility) {
        if (this.visibility == newVisibility) {
            return;  // 변경 없음
        }

        validateVisibilityChange(newVisibility);
        this.visibility = newVisibility;
        this.updatedAt = Instant.now();
    }

    private void validateVisibilityChange(FileVisibility newVisibility) {
        // PUBLIC -> PRIVATE는 주의 필요
        if (this.visibility == FileVisibility.PUBLIC &&
            newVisibility == FileVisibility.PRIVATE) {
            // 외부 링크 존재 여부 체크 등
            log.warn("PUBLIC에서 PRIVATE로 변경: fileAssetId={}",
                this.fileAssetId);
        }
    }

    /**
     * 사용자가 파일에 접근 가능한지 확인
     */
    public boolean isAccessibleBy(Long userId, Long userOrganizationId) {
        // PUBLIC은 모두 접근 가능
        if (visibility == FileVisibility.PUBLIC) {
            return true;
        }

        // INTERNAL은 같은 조직만
        if (visibility == FileVisibility.INTERNAL) {
            return this.organizationId.equals(userOrganizationId);
        }

        // PRIVATE은 소유자만
        return this.createdBy.equals(userId);
    }
}
```

---

## 🎯 KAN-298: QueryFilesByRelationshipUseCase 구현

### 작업 내용
관계를 기준으로 파일을 조회하는 UseCase를 구현합니다.

### 구현 체크리스트

#### 1. Query UseCase 구현
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class QueryFilesByRelationshipUseCase {
    private final FileRelationshipQueryPort relationshipQueryPort;
    private final FileAssetQueryPort fileAssetQueryPort;
    private final FileAssembler assembler;

    /**
     * 특정 파일과 관계를 가진 모든 파일 조회
     */
    public FileRelationshipGraphResponse execute(
        QueryFilesByRelationshipCommand command) {

        log.info("파일 관계 조회: fileAssetId={}, type={}",
            command.getFileAssetId(), command.getRelationshipType());

        // 1. 기준 파일 조회
        FileAsset baseFile = fileAssetQueryPort
            .findByFileAssetId(command.getFileAssetId())
            .orElseThrow(() -> new FileNotFoundException(
                command.getFileAssetId()
            ));

        // 2. 관계 조회 (source로 사용된 경우)
        List<FileRelationship> outgoingRelations =
            relationshipQueryPort.findBySource(
                command.getFileAssetId(),
                command.getRelationshipType()
            );

        // 3. 관계 조회 (target으로 사용된 경우)
        List<FileRelationship> incomingRelations =
            relationshipQueryPort.findByTarget(
                command.getFileAssetId(),
                command.getRelationshipType()
            );

        // 4. 관련 파일들 조회
        Set<String> relatedFileIds = new HashSet<>();
        outgoingRelations.forEach(r ->
            relatedFileIds.add(r.getTargetFileAssetId())
        );
        incomingRelations.forEach(r ->
            relatedFileIds.add(r.getSourceFileAssetId())
        );

        List<FileAsset> relatedFiles = fileAssetQueryPort
            .findByFileAssetIds(new ArrayList<>(relatedFileIds));

        // 5. Response 구성
        return buildGraphResponse(
            baseFile,
            outgoingRelations,
            incomingRelations,
            relatedFiles
        );
    }

    /**
     * 파일의 버전 히스토리 조회
     */
    public FileVersionHistoryResponse getVersionHistory(String fileAssetId) {
        // 버전 체인 추적
        List<FileAsset> versionChain = new ArrayList<>();

        // 최초 버전 찾기
        String currentId = findFirstVersion(fileAssetId);

        // 버전 체인 구성
        while (currentId != null) {
            FileAsset file = fileAssetQueryPort
                .findByFileAssetId(currentId)
                .orElse(null);

            if (file != null) {
                versionChain.add(file);
            }

            // 다음 버전 찾기
            currentId = findNextVersion(currentId);
        }

        return FileVersionHistoryResponse.of(versionChain);
    }

    private String findFirstVersion(String fileAssetId) {
        String currentId = fileAssetId;

        while (true) {
            // 이전 버전 찾기 (현재 파일이 target인 VERSION 관계)
            Optional<FileRelationship> previous = relationshipQueryPort
                .findByTargetAndType(currentId, RelationshipType.VERSION)
                .stream()
                .findFirst();

            if (previous.isEmpty()) {
                return currentId;  // 최초 버전
            }

            currentId = previous.get().getSourceFileAssetId();
        }
    }

    private String findNextVersion(String currentId) {
        return relationshipQueryPort
            .findBySourceAndType(currentId, RelationshipType.VERSION)
            .stream()
            .findFirst()
            .map(FileRelationship::getTargetFileAssetId)
            .orElse(null);
    }

    private FileRelationshipGraphResponse buildGraphResponse(
        FileAsset baseFile,
        List<FileRelationship> outgoing,
        List<FileRelationship> incoming,
        List<FileAsset> relatedFiles) {

        FileRelationshipGraphResponse response =
            new FileRelationshipGraphResponse();

        response.setBaseFile(assembler.toResponse(baseFile));

        // 노드 구성
        List<FileNodeResponse> nodes = relatedFiles.stream()
            .map(file -> FileNodeResponse.of(
                file.getFileAssetId(),
                file.getFileName(),
                file.getMimeType()
            ))
            .collect(Collectors.toList());
        response.setNodes(nodes);

        // 엣지 구성
        List<FileEdgeResponse> edges = new ArrayList<>();

        outgoing.forEach(rel -> edges.add(
            FileEdgeResponse.of(
                rel.getSourceFileAssetId(),
                rel.getTargetFileAssetId(),
                rel.getRelationshipType().getCode(),
                "outgoing"
            )
        ));

        incoming.forEach(rel -> edges.add(
            FileEdgeResponse.of(
                rel.getSourceFileAssetId(),
                rel.getTargetFileAssetId(),
                rel.getRelationshipType().getCode(),
                "incoming"
            )
        ));

        response.setEdges(edges);

        return response;
    }
}
```

#### 2. Response DTOs
```java
public class FileRelationshipGraphResponse {
    private FileAssetResponse baseFile;
    private List<FileNodeResponse> nodes;
    private List<FileEdgeResponse> edges;

    // Getters and Setters
}

public class FileNodeResponse {
    private String fileAssetId;
    private String fileName;
    private String mimeType;

    public static FileNodeResponse of(String fileAssetId,
                                     String fileName,
                                     String mimeType) {
        FileNodeResponse node = new FileNodeResponse();
        node.fileAssetId = fileAssetId;
        node.fileName = fileName;
        node.mimeType = mimeType;
        return node;
    }

    // Getters
}

public class FileEdgeResponse {
    private String sourceId;
    private String targetId;
    private String relationshipType;
    private String direction;

    public static FileEdgeResponse of(String sourceId,
                                     String targetId,
                                     String type,
                                     String direction) {
        FileEdgeResponse edge = new FileEdgeResponse();
        edge.sourceId = sourceId;
        edge.targetId = targetId;
        edge.relationshipType = type;
        edge.direction = direction;
        return edge;
    }

    // Getters
}
```

---

## 🎯 KAN-299: FileRelationship Persistence Adapter 구현

### 작업 내용
FileRelationship의 영속성 계층을 구현합니다.

### 구현 체크리스트

#### 1. JPA Entity 구현
```java
package com.ryuqq.fileflow.adapter.persistence.file;

@Entity
@Table(name = "file_relationships",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_file_relationship",
            columnNames = {"source_file_asset_id", "target_file_asset_id", "relationship_type"}
        )
    }
)
public class FileRelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_file_asset_id", nullable = false, length = 100)
    private String sourceFileAssetId;  // NO FK!

    @Column(name = "target_file_asset_id", nullable = false, length = 100)
    private String targetFileAssetId;  // NO FK!

    @Column(name = "relationship_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;  // NO FK!

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;  // NO FK!

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // NO Lombok - 수동 작성
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceFileAssetId() {
        return sourceFileAssetId;
    }

    public void setSourceFileAssetId(String sourceFileAssetId) {
        this.sourceFileAssetId = sourceFileAssetId;
    }

    public String getTargetFileAssetId() {
        return targetFileAssetId;
    }

    public void setTargetFileAssetId(String targetFileAssetId) {
        this.targetFileAssetId = targetFileAssetId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
```

#### 2. Repository 구현
```java
@Repository
public interface FileRelationshipJpaRepository extends
    JpaRepository<FileRelationshipEntity, Long> {

    // 중복 체크
    boolean existsBySourceFileAssetIdAndTargetFileAssetIdAndRelationshipType(
        String source, String target, RelationshipType type
    );

    // Source 기준 조회
    List<FileRelationshipEntity> findBySourceFileAssetIdAndRelationshipType(
        String source, RelationshipType type
    );

    // Target 기준 조회
    List<FileRelationshipEntity> findByTargetFileAssetIdAndRelationshipType(
        String target, RelationshipType type
    );

    // 파일과 관련된 모든 관계 조회
    @Query("SELECT r FROM FileRelationshipEntity r " +
           "WHERE r.sourceFileAssetId = :fileId OR r.targetFileAssetId = :fileId")
    List<FileRelationshipEntity> findAllRelatedToFile(@Param("fileId") String fileId);

    // 조직별 관계 조회
    Page<FileRelationshipEntity> findByOrganizationId(
        Long organizationId, Pageable pageable
    );
}
```

#### 3. Adapter 구현
```java
@Component
@RequiredArgsConstructor
public class FileRelationshipPersistenceAdapter implements
    FileRelationshipRepository, FileRelationshipQueryPort {

    private final FileRelationshipJpaRepository jpaRepository;
    private final FileRelationshipMapper mapper;

    @Override
    public FileRelationship save(FileRelationship relationship) {
        FileRelationshipEntity entity = mapper.toEntity(relationship);
        FileRelationshipEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsBySourceAndTargetAndType(String source,
                                                 String target,
                                                 RelationshipType type) {
        return jpaRepository
            .existsBySourceFileAssetIdAndTargetFileAssetIdAndRelationshipType(
                source, target, type
            );
    }

    @Override
    public List<FileRelationship> findBySourceAndType(String source,
                                                     RelationshipType type) {
        List<FileRelationshipEntity> entities = jpaRepository
            .findBySourceFileAssetIdAndRelationshipType(source, type);
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<FileRelationship> findByTargetAndType(String target,
                                                     RelationshipType type) {
        List<FileRelationshipEntity> entities = jpaRepository
            .findByTargetFileAssetIdAndRelationshipType(target, type);
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(FileRelationship relationship) {
        jpaRepository.deleteById(relationship.getId());
    }
}
```

#### 4. Mapper 구현
```java
@Component
public class FileRelationshipMapper {

    public FileRelationshipEntity toEntity(FileRelationship domain) {
        FileRelationshipEntity entity = new FileRelationshipEntity();
        entity.setId(domain.getId());
        entity.setSourceFileAssetId(domain.getSourceFileAssetId());
        entity.setTargetFileAssetId(domain.getTargetFileAssetId());
        entity.setRelationshipType(domain.getRelationshipType());
        entity.setTenantId(domain.getTenantId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public FileRelationship toDomain(FileRelationshipEntity entity) {
        // Reflection을 사용하여 private 필드 설정
        // 또는 Protected 생성자 사용
        return FileRelationship.reconstitute(
            entity.getId(),
            entity.getSourceFileAssetId(),
            entity.getTargetFileAssetId(),
            entity.getRelationshipType(),
            entity.getTenantId(),
            entity.getOrganizationId(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }
}
```

---

## 🎯 KAN-300: FileRelationship/Visibility REST Controller 구현

### 작업 내용
파일 관계 및 가시성 관련 REST API를 구현합니다.

### 구현 체크리스트

#### 1. Controller 구현
```java
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Relationship & Visibility",
     description = "파일 관계 및 가시성 관리 API")
public class FileRelationshipController {

    private final LinkFileRelationshipUseCase linkUseCase;
    private final QueryFilesByRelationshipUseCase queryUseCase;
    private final UpdateFileVisibilityUseCase visibilityUseCase;
    private final FileRelationshipApiMapper mapper;

    // === 관계 관리 ===

    @PostMapping("/{sourceId}/relationships")
    @Operation(summary = "파일 관계 생성",
              description = "두 파일 간의 관계를 생성합니다")
    public ResponseEntity<FileRelationshipResponse> createRelationship(
        @PathVariable String sourceId,
        @Valid @RequestBody CreateRelationshipRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestHeader("X-Organization-Id") Long organizationId,
        @RequestHeader("X-User-Id") Long userId
    ) {
        LinkFileRelationshipCommand command = mapper.toCommand(
            sourceId,
            request,
            tenantId,
            organizationId,
            userId
        );

        FileRelationshipResponse response = linkUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}/relationships")
    @Operation(summary = "파일 관계 조회",
              description = "파일과 연결된 모든 관계를 조회합니다")
    public ResponseEntity<FileRelationshipGraphResponse> getRelationships(
        @PathVariable String fileId,
        @RequestParam(required = false) RelationshipType type
    ) {
        QueryFilesByRelationshipCommand command =
            new QueryFilesByRelationshipCommand(fileId, type);

        FileRelationshipGraphResponse response =
            queryUseCase.execute(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/versions")
    @Operation(summary = "파일 버전 히스토리 조회",
              description = "파일의 모든 버전을 조회합니다")
    public ResponseEntity<FileVersionHistoryResponse> getVersionHistory(
        @PathVariable String fileId
    ) {
        FileVersionHistoryResponse response =
            queryUseCase.getVersionHistory(fileId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/relationships/{relationshipId}")
    @Operation(summary = "파일 관계 삭제")
    public ResponseEntity<Void> deleteRelationship(
        @PathVariable Long relationshipId,
        @RequestHeader("X-User-Id") Long userId
    ) {
        // 관계 삭제 로직
        return ResponseEntity.noContent().build();
    }

    // === 가시성 관리 ===

    @PutMapping("/{fileId}/visibility")
    @Operation(summary = "파일 가시성 변경",
              description = "파일의 공개 범위를 변경합니다")
    public ResponseEntity<FileAssetResponse> updateVisibility(
        @PathVariable String fileId,
        @Valid @RequestBody UpdateVisibilityRequest request,
        @RequestHeader("X-User-Id") Long userId
    ) {
        UpdateFileVisibilityCommand command =
            UpdateFileVisibilityCommand.builder()
                .fileAssetId(fileId)
                .newVisibility(request.getVisibility())
                .userId(userId)
                .reason(request.getReason())
                .build();

        FileAssetResponse response = visibilityUseCase.execute(command);

        return ResponseEntity.ok(response);
    }
}
```

#### 2. Request DTOs
```java
public class CreateRelationshipRequest {

    @NotBlank(message = "Target 파일 ID는 필수입니다")
    private String targetFileAssetId;

    @NotNull(message = "관계 타입은 필수입니다")
    private RelationshipType relationshipType;

    // NO Lombok!
    public String getTargetFileAssetId() {
        return targetFileAssetId;
    }

    public void setTargetFileAssetId(String targetFileAssetId) {
        this.targetFileAssetId = targetFileAssetId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }
}

public class UpdateVisibilityRequest {

    @NotNull(message = "가시성은 필수입니다")
    private FileVisibility visibility;

    private String reason;  // 변경 사유 (선택)

    public FileVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(FileVisibility visibility) {
        this.visibility = visibility;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
```

---

## 🎯 KAN-301: Phase 3B 통합 테스트 작성

### 작업 내용
Phase 3B에서 구현한 기능들의 통합 테스트를 작성합니다.

### 구현 체크리스트

#### 1. FileRelationship 통합 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class FileRelationshipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileRelationshipRepository repository;

    @Test
    void 파일_관계_생성_및_조회_테스트() throws Exception {
        // Given - 파일 준비
        // ... 파일 생성 로직

        CreateRelationshipRequest request = new CreateRelationshipRequest();
        request.setTargetFileAssetId("FILE-002");
        request.setRelationshipType(RelationshipType.VERSION);

        // When - 관계 생성
        mockMvc.perform(post("/api/v1/files/FILE-001/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Tenant-Id", "1")
                .header("X-Organization-Id", "1")
                .header("X-User-Id", "1"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sourceFileAssetId").value("FILE-001"))
            .andExpect(jsonPath("$.targetFileAssetId").value("FILE-002"));

        // Then - DB 검증
        assertTrue(repository.existsBySourceAndTargetAndType(
            "FILE-001", "FILE-002", RelationshipType.VERSION
        ));
    }

    @Test
    void 순환_참조_방지_테스트() throws Exception {
        // Given
        CreateRelationshipRequest request = new CreateRelationshipRequest();
        request.setTargetFileAssetId("FILE-001");  // 자기 자신
        request.setRelationshipType(RelationshipType.VERSION);

        // When & Then
        mockMvc.perform(post("/api/v1/files/FILE-001/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Tenant-Id", "1")
                .header("X-Organization-Id", "1")
                .header("X-User-Id", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_RELATIONSHIP"));
    }
}
```

#### 2. FileVisibility 통합 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
class FileVisibilityIntegrationTest {

    @Test
    void 가시성_변경_테스트() throws Exception {
        // Given
        UpdateVisibilityRequest request = new UpdateVisibilityRequest();
        request.setVisibility(FileVisibility.PRIVATE);
        request.setReason("보안상 비공개 처리");

        // When
        mockMvc.perform(put("/api/v1/files/FILE-001/visibility")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Id", "1"))  // 소유자
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.visibility").value("PRIVATE"));

        // Then - 히스토리 확인
        List<VisibilityChangeHistory> history =
            historyRepository.findByFileAssetId("FILE-001");
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getToVisibility())
            .isEqualTo(FileVisibility.PRIVATE);
    }

    @Test
    void 권한_없는_사용자_가시성_변경_차단() throws Exception {
        // Given
        UpdateVisibilityRequest request = new UpdateVisibilityRequest();
        request.setVisibility(FileVisibility.PRIVATE);

        // When & Then
        mockMvc.perform(put("/api/v1/files/FILE-001/visibility")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Id", "999"))  // 권한 없는 사용자
            .andExpect(status().isForbidden());
    }
}
```

---

## 📝 Phase 3B 체크리스트 총정리

### 개발 전
- [ ] Phase 3A 완료 확인
- [ ] 스키마 변경사항 확인
- [ ] IAM 연동 방식 확인

### 개발 중
- [ ] **NO Lombok** 모든 코드
- [ ] **Law of Demeter** 준수
- [ ] **Long FK** 전략 (관계 어노테이션 금지)
- [ ] **Transaction** Application Layer만
- [ ] 순환 참조 방지 로직
- [ ] 권한 체크 구현

### 개발 후
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] API 문서 업데이트
- [ ] 코드 리뷰

## 다음 단계
Phase 3C 태스크는 `prd/KAN-260-phase-3c-tasks.md` 참조