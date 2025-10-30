# Phase 3C: 파일 라이프사이클 기능 구현 가이드

## 📋 Phase 3C 개요
- **목표**: 파일 만료, 삭제 정책 및 접근 로그 관리
- **기간**: 3일 (Day 6-8)
- **태스크 수**: 8개 (라이프사이클 5개 + 테스트 3개)

---

## 🎯 KAN-302: FileAccessLog Entity 구현

### 작업 내용
파일 접근 로그를 기록하는 Entity를 구현합니다.

### 구현 체크리스트

#### 1. Domain Entity 구현
```java
package com.ryuqq.fileflow.domain.file.log;

/**
 * 파일 접근 로그 Entity
 * 모든 파일 접근을 추적하고 감사(Audit) 목적으로 사용
 *
 * @author developer
 * @since 1.0
 */
public class FileAccessLog {

    private Long id;
    private String fileAssetId;     // 접근한 파일 ID
    private Long userId;            // 접근한 사용자 ID
    private AccessType accessType;  // 접근 유형
    private String ipAddress;       // 접근 IP
    private String userAgent;       // 브라우저/클라이언트 정보
    private Long tenantId;          // Long FK
    private Long organizationId;    // Long FK
    private Instant accessedAt;     // 접근 시간
    private Long responseTime;      // 응답 시간 (ms)
    private Integer statusCode;     // HTTP 상태 코드
    private String errorMessage;    // 에러 메시지 (실패 시)

    // Private 생성자 (NO Lombok!)
    private FileAccessLog(String fileAssetId,
                         Long userId,
                         AccessType accessType,
                         String ipAddress,
                         String userAgent,
                         Long tenantId,
                         Long organizationId) {
        this.fileAssetId = fileAssetId;
        this.userId = userId;
        this.accessType = accessType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.accessedAt = Instant.now();
    }

    // Static Factory Method
    public static FileAccessLog create(String fileAssetId,
                                      Long userId,
                                      AccessType accessType,
                                      String ipAddress,
                                      String userAgent,
                                      Long tenantId,
                                      Long organizationId) {
        validateInput(fileAssetId, userId, accessType);
        return new FileAccessLog(
            fileAssetId,
            userId,
            accessType,
            sanitizeIpAddress(ipAddress),
            truncateUserAgent(userAgent),
            tenantId,
            organizationId
        );
    }

    private static void validateInput(String fileAssetId,
                                     Long userId,
                                     AccessType accessType) {
        if (fileAssetId == null || fileAssetId.trim().isEmpty()) {
            throw new IllegalArgumentException("FileAssetId는 필수입니다");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId는 필수입니다");
        }
        if (accessType == null) {
            throw new IllegalArgumentException("AccessType은 필수입니다");
        }
    }

    private static String sanitizeIpAddress(String ipAddress) {
        // IP 주소 정규화
        if (ipAddress == null) {
            return "unknown";
        }
        // X-Forwarded-For 처리
        if (ipAddress.contains(",")) {
            return ipAddress.split(",")[0].trim();
        }
        return ipAddress.trim();
    }

    private static String truncateUserAgent(String userAgent) {
        // User-Agent 길이 제한 (DB 컬럼 크기)
        if (userAgent == null) {
            return "unknown";
        }
        if (userAgent.length() > 500) {
            return userAgent.substring(0, 497) + "...";
        }
        return userAgent;
    }

    // 비즈니스 로직
    public void markSuccess(Integer statusCode, Long responseTime) {
        this.statusCode = statusCode;
        this.responseTime = responseTime;
    }

    public void markFailure(Integer statusCode,
                           Long responseTime,
                           String errorMessage) {
        this.statusCode = statusCode;
        this.responseTime = responseTime;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode != null &&
               statusCode >= 200 &&
               statusCode < 300;
    }

    public boolean isUnauthorized() {
        return statusCode != null &&
               (statusCode == 401 || statusCode == 403);
    }

    // Getters only
    public Long getId() {
        return id;
    }

    public String getFileAssetId() {
        return fileAssetId;
    }

    public Long getUserId() {
        return userId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
```

#### 2. AccessType Enum
```java
package com.ryuqq.fileflow.domain.file.log;

public enum AccessType {
    /**
     * 파일 조회
     */
    VIEW("view", "파일 조회"),

    /**
     * 파일 다운로드
     */
    DOWNLOAD("download", "파일 다운로드"),

    /**
     * 파일 미리보기
     */
    PREVIEW("preview", "파일 미리보기"),

    /**
     * 파일 메타데이터 조회
     */
    METADATA("metadata", "메타데이터 조회"),

    /**
     * 썸네일 조회
     */
    THUMBNAIL("thumbnail", "썸네일 조회");

    private final String code;
    private final String description;

    AccessType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDownload() {
        return this == DOWNLOAD;
    }

    public boolean isReadOnly() {
        return this != DOWNLOAD;
    }
}
```

#### 3. JPA Entity 구현
```java
@Entity
@Table(name = "file_access_logs",
    indexes = {
        @Index(name = "idx_file_access_file_id", columnList = "file_asset_id"),
        @Index(name = "idx_file_access_user_id", columnList = "user_id"),
        @Index(name = "idx_file_access_accessed_at", columnList = "accessed_at"),
        @Index(name = "idx_file_access_tenant_org",
               columnList = "tenant_id, organization_id")
    }
)
public class FileAccessLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_asset_id", nullable = false, length = 100)
    private String fileAssetId;  // NO FK!

    @Column(name = "user_id", nullable = false)
    private Long userId;  // NO FK!

    @Column(name = "access_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    @Column(name = "response_time")
    private Long responseTime;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // NO Lombok - 수동 Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ... 나머지 getter/setter
}
```

---

## 🎯 KAN-303: SoftDeleteFileAssetUseCase 구현

### 작업 내용
파일의 논리적 삭제(Soft Delete)를 처리하는 UseCase를 구현합니다.

### 구현 체크리스트

#### 1. UseCase 구현
```java
package com.ryuqq.fileflow.application.file.command;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftDeleteFileAssetUseCase {
    private final FileAssetRepository fileAssetRepository;
    private final FileRelationshipQueryPort relationshipQueryPort;
    private final FilePermissionEvaluator permissionEvaluator;
    private final FileDeleteEventPublisher eventPublisher;

    /**
     * 파일 논리 삭제
     * 실제 파일은 삭제하지 않고 상태만 변경
     */
    @Transactional
    public void execute(SoftDeleteFileAssetCommand command) {
        log.info("파일 논리 삭제 시작: fileAssetId={}, userId={}",
            command.getFileAssetId(), command.getUserId());

        // 1. 파일 조회
        FileAsset fileAsset = fileAssetRepository
            .findByFileAssetId(command.getFileAssetId())
            .orElseThrow(() -> new FileNotFoundException(
                command.getFileAssetId()
            ));

        // 2. 이미 삭제된 파일 체크
        if (fileAsset.isDeleted()) {
            log.warn("이미 삭제된 파일: {}", command.getFileAssetId());
            return;
        }

        // 3. 권한 체크 (소유자 또는 ADMIN)
        if (!permissionEvaluator.canDelete(command.getUserId(), fileAsset)) {
            throw new InsufficientPermissionException(
                "파일을 삭제할 권한이 없습니다"
            );
        }

        // 4. 관련 파일 체크 (참조되는 파일은 삭제 불가)
        checkRelatedFiles(fileAsset.getFileAssetId());

        // 5. 논리 삭제 수행
        fileAsset.markAsDeleted(
            command.getUserId(),
            command.getReason()
        );

        // 6. 저장
        FileAsset deleted = fileAssetRepository.save(fileAsset);

        // 7. 관련 관계 정리
        cleanupRelationships(deleted.getFileAssetId());

        // 8. 이벤트 발행
        publishFileDeletedEvent(deleted, command.getUserId());

        log.info("파일 논리 삭제 완료: fileAssetId={}",
            command.getFileAssetId());
    }

    private void checkRelatedFiles(String fileAssetId) {
        // 이 파일을 참조하는 다른 파일이 있는지 체크
        List<FileRelationship> references = relationshipQueryPort
            .findByTargetAndType(fileAssetId, RelationshipType.REFERENCE);

        if (!references.isEmpty()) {
            throw new FileInUseException(
                String.format("파일이 %d개의 다른 파일에서 참조되고 있습니다",
                    references.size())
            );
        }
    }

    private void cleanupRelationships(String fileAssetId) {
        // 이 파일이 생성한 관계들 삭제
        List<FileRelationship> relationships = relationshipQueryPort
            .findAllRelatedToFile(fileAssetId);

        for (FileRelationship relationship : relationships) {
            // Soft Delete된 파일의 관계는 유지하되 상태만 변경
            relationship.markAsInactive();
            relationshipRepository.save(relationship);
        }
    }

    private void publishFileDeletedEvent(FileAsset fileAsset, Long deletedBy) {
        FileDeletedEvent event = FileDeletedEvent.of(
            fileAsset.getFileAssetId(),
            fileAsset.getTenantId(),
            fileAsset.getOrganizationId(),
            deletedBy,
            Instant.now()
        );

        eventPublisher.publish(event);
    }
}
```

#### 2. FileAsset 도메인 수정
```java
public class FileAsset extends AbstractAggregateRoot<FileAsset> {
    // 기존 필드들...
    private FileStatus status;
    private Instant deletedAt;
    private Long deletedBy;
    private String deleteReason;

    /**
     * 논리 삭제 처리
     * Tell, Don't Ask 패턴
     */
    public void markAsDeleted(Long deletedBy, String reason) {
        if (this.status == FileStatus.DELETED) {
            return;  // 이미 삭제됨
        }

        this.status = FileStatus.DELETED;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.deleteReason = reason;
        this.updatedAt = Instant.now();

        // 도메인 이벤트 등록
        registerEvent(FileDeletedEvent.of(
            this.fileAssetId,
            this.tenantId,
            this.organizationId,
            deletedBy,
            this.deletedAt
        ));
    }

    /**
     * 삭제 취소 (복구)
     */
    public void restore(Long restoredBy) {
        if (this.status != FileStatus.DELETED) {
            throw new IllegalStateException("삭제된 파일만 복구 가능합니다");
        }

        this.status = FileStatus.ACTIVE;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deleteReason = null;
        this.updatedAt = Instant.now();

        registerEvent(FileRestoredEvent.of(
            this.fileAssetId,
            restoredBy,
            Instant.now()
        ));
    }

    public boolean isDeleted() {
        return this.status == FileStatus.DELETED;
    }

    public boolean canBePhysicallyDeleted() {
        // 30일 이상 지난 삭제 파일만 물리 삭제 가능
        if (!isDeleted()) {
            return false;
        }

        return deletedAt != null &&
               deletedAt.plus(Duration.ofDays(30))
                   .isBefore(Instant.now());
    }
}
```

---

## 🎯 KAN-304: QueryFileAccessLogsUseCase 구현

### 작업 내용
파일 접근 로그를 조회하는 UseCase를 구현합니다.

### 구현 체크리스트

#### 1. UseCase 구현
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class QueryFileAccessLogsUseCase {
    private final FileAccessLogQueryPort accessLogQueryPort;
    private final FileAssetQueryPort fileAssetQueryPort;
    private final AccessLogAssembler assembler;

    /**
     * 파일별 접근 로그 조회
     */
    public Page<FileAccessLogResponse> getFileAccessLogs(
        String fileAssetId,
        AccessLogFilter filter,
        Pageable pageable) {

        log.info("파일 접근 로그 조회: fileAssetId={}", fileAssetId);

        // 파일 존재 확인
        fileAssetQueryPort.findByFileAssetId(fileAssetId)
            .orElseThrow(() -> new FileNotFoundException(fileAssetId));

        // 로그 조회
        Page<FileAccessLog> logs = accessLogQueryPort.findByFileAssetId(
            fileAssetId,
            filter,
            pageable
        );

        return logs.map(assembler::toResponse);
    }

    /**
     * 사용자별 파일 접근 이력 조회
     */
    public Page<FileAccessLogResponse> getUserAccessHistory(
        Long userId,
        AccessLogFilter filter,
        Pageable pageable) {

        log.info("사용자 파일 접근 이력 조회: userId={}", userId);

        Page<FileAccessLog> logs = accessLogQueryPort.findByUserId(
            userId,
            filter,
            pageable
        );

        return logs.map(assembler::toResponse);
    }

    /**
     * 파일 접근 통계 조회
     */
    public FileAccessStatistics getAccessStatistics(
        String fileAssetId,
        Instant startDate,
        Instant endDate) {

        log.info("파일 접근 통계 조회: fileAssetId={}, period={} ~ {}",
            fileAssetId, startDate, endDate);

        // 기간별 접근 횟수
        long totalAccess = accessLogQueryPort.countByFileAndPeriod(
            fileAssetId, startDate, endDate
        );

        // 접근 타입별 통계
        Map<AccessType, Long> accessByType = accessLogQueryPort
            .getAccessCountByType(fileAssetId, startDate, endDate);

        // 고유 사용자 수
        long uniqueUsers = accessLogQueryPort
            .countUniqueUsersByFile(fileAssetId, startDate, endDate);

        // 평균 응답 시간
        Double avgResponseTime = accessLogQueryPort
            .getAverageResponseTime(fileAssetId, startDate, endDate);

        // 에러율
        Double errorRate = accessLogQueryPort
            .getErrorRate(fileAssetId, startDate, endDate);

        return FileAccessStatistics.of(
            fileAssetId,
            totalAccess,
            uniqueUsers,
            accessByType,
            avgResponseTime,
            errorRate,
            startDate,
            endDate
        );
    }

    /**
     * 비정상 접근 패턴 감지
     */
    public List<SuspiciousAccessPattern> detectSuspiciousAccess(
        Long tenantId,
        Instant since) {

        log.info("비정상 접근 패턴 감지: tenantId={}, since={}",
            tenantId, since);

        List<SuspiciousAccessPattern> patterns = new ArrayList<>();

        // 1. 짧은 시간 내 대량 다운로드
        patterns.addAll(detectMassDownload(tenantId, since));

        // 2. 비정상적인 접근 시간대
        patterns.addAll(detectUnusualAccessTime(tenantId, since));

        // 3. 반복적인 접근 실패
        patterns.addAll(detectRepeatedFailures(tenantId, since));

        // 4. 비인가 접근 시도
        patterns.addAll(detectUnauthorizedAttempts(tenantId, since));

        return patterns;
    }

    private List<SuspiciousAccessPattern> detectMassDownload(
        Long tenantId, Instant since) {

        // 1시간 내 같은 사용자가 100개 이상 파일 다운로드
        Map<Long, Long> downloadCounts = accessLogQueryPort
            .getDownloadCountByUser(
                tenantId,
                since,
                AccessType.DOWNLOAD
            );

        return downloadCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 100)
            .map(entry -> SuspiciousAccessPattern.of(
                "MASS_DOWNLOAD",
                entry.getKey(),
                String.format("1시간 내 %d개 파일 다운로드", entry.getValue())
            ))
            .collect(Collectors.toList());
    }

    private List<SuspiciousAccessPattern> detectUnusualAccessTime(
        Long tenantId, Instant since) {

        // 새벽 시간대(02:00-05:00) 접근
        List<FileAccessLog> nightAccess = accessLogQueryPort
            .findByTenantAndTimeRange(
                tenantId,
                since,
                2, 5  // 시간대
            );

        Map<Long, Long> userAccessCounts = nightAccess.stream()
            .collect(Collectors.groupingBy(
                FileAccessLog::getUserId,
                Collectors.counting()
            ));

        return userAccessCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 10)
            .map(entry -> SuspiciousAccessPattern.of(
                "UNUSUAL_TIME",
                entry.getKey(),
                String.format("새벽 시간대 %d회 접근", entry.getValue())
            ))
            .collect(Collectors.toList());
    }
}
```

#### 2. Response DTOs
```java
public class FileAccessLogResponse {
    private Long id;
    private String fileAssetId;
    private Long userId;
    private String userName;  // 조인해서 가져옴
    private String accessType;
    private String ipAddress;
    private String userAgent;
    private Instant accessedAt;
    private Long responseTime;
    private Integer statusCode;
    private boolean successful;

    public static FileAccessLogResponse of(FileAccessLog log, String userName) {
        FileAccessLogResponse response = new FileAccessLogResponse();
        response.id = log.getId();
        response.fileAssetId = log.getFileAssetId();
        response.userId = log.getUserId();
        response.userName = userName;
        response.accessType = log.getAccessType().getCode();
        response.ipAddress = log.getIpAddress();
        response.userAgent = log.getUserAgent();
        response.accessedAt = log.getAccessedAt();
        response.responseTime = log.getResponseTime();
        response.statusCode = log.getStatusCode();
        response.successful = log.isSuccessful();
        return response;
    }

    // Getters...
}

public class FileAccessStatistics {
    private String fileAssetId;
    private long totalAccessCount;
    private long uniqueUserCount;
    private Map<String, Long> accessByType;
    private double averageResponseTime;
    private double errorRate;
    private Instant periodStart;
    private Instant periodEnd;

    // Factory method and getters...
}
```

---

## 🎯 KAN-305: ExpireFileAssetsUseCase 구현

### 작업 내용
만료된 파일을 자동으로 처리하는 배치 UseCase를 구현합니다.

### 구현 체크리스트

#### 1. UseCase 구현
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpireFileAssetsUseCase {
    private final FileAssetRepository fileAssetRepository;
    private final FileExpirationPolicyPort policyPort;
    private final S3StorageAdapter s3Adapter;
    private final FileExpirationEventPublisher eventPublisher;

    private static final int BATCH_SIZE = 100;

    /**
     * 만료된 파일 처리 (배치)
     * 매일 새벽에 실행
     */
    @Transactional
    public FileExpirationResult execute(ExpireFileAssetsCommand command) {
        log.info("파일 만료 처리 시작: tenantId={}", command.getTenantId());

        FileExpirationResult result = new FileExpirationResult();
        Instant expirationThreshold = calculateExpirationThreshold(command);

        // 1. 만료 대상 파일 조회
        int page = 0;
        boolean hasMore = true;

        while (hasMore) {
            Page<FileAsset> expirableFiles = fileAssetRepository
                .findExpirableFiles(
                    command.getTenantId(),
                    expirationThreshold,
                    PageRequest.of(page, BATCH_SIZE)
                );

            if (expirableFiles.isEmpty()) {
                hasMore = false;
            } else {
                // 2. 배치 처리
                processBatch(expirableFiles.getContent(), result, command);
                page++;
            }

            // 3. 중간 커밋 (대량 처리 시)
            if (page % 10 == 0) {
                log.info("중간 처리 상태: processed={}, failed={}",
                    result.getProcessedCount(),
                    result.getFailedCount());
            }
        }

        // 4. 최종 결과 이벤트 발행
        publishExpirationResult(result);

        log.info("파일 만료 처리 완료: total={}, expired={}, failed={}",
            result.getProcessedCount(),
            result.getExpiredCount(),
            result.getFailedCount());

        return result;
    }

    private Instant calculateExpirationThreshold(ExpireFileAssetsCommand command) {
        // 정책에 따른 만료 기준 계산
        FileExpirationPolicy policy = policyPort.getPolicy(
            command.getTenantId(),
            command.getOrganizationId()
        );

        return Instant.now().minus(policy.getRetentionPeriod());
    }

    private void processBatch(List<FileAsset> files,
                             FileExpirationResult result,
                             ExpireFileAssetsCommand command) {
        for (FileAsset file : files) {
            try {
                processFileExpiration(file, command);
                result.addExpired(file.getFileAssetId());
            } catch (Exception e) {
                log.error("파일 만료 처리 실패: fileAssetId={}",
                    file.getFileAssetId(), e);
                result.addFailed(file.getFileAssetId(), e.getMessage());
            }
        }
    }

    private void processFileExpiration(FileAsset file,
                                      ExpireFileAssetsCommand command) {
        // 1. 파일 상태 변경
        file.markAsExpired();

        // 2. 정책에 따른 처리
        FileExpirationAction action = determineAction(file, command);

        switch (action) {
            case ARCHIVE:
                archiveFile(file);
                break;
            case DELETE:
                deleteFile(file);
                break;
            case MOVE_TO_GLACIER:
                moveToGlacier(file);
                break;
            case NOTIFY_ONLY:
                // 알림만 발송
                break;
        }

        // 3. 파일 상태 저장
        fileAssetRepository.save(file);

        // 4. 개별 파일 이벤트 발행
        eventPublisher.publish(FileExpiredEvent.of(
            file.getFileAssetId(),
            action,
            Instant.now()
        ));
    }

    private FileExpirationAction determineAction(FileAsset file,
                                                ExpireFileAssetsCommand command) {
        // 파일 타입, 크기, 중요도에 따른 처리 방식 결정
        if (file.isImportant()) {
            return FileExpirationAction.ARCHIVE;
        }

        if (file.getFileSize() > 1_000_000_000) {  // 1GB 이상
            return FileExpirationAction.MOVE_TO_GLACIER;
        }

        if (command.isForceDelete()) {
            return FileExpirationAction.DELETE;
        }

        return FileExpirationAction.NOTIFY_ONLY;
    }

    private void archiveFile(FileAsset file) {
        // S3 Archive 스토리지로 이동
        s3Adapter.moveToArchive(
            file.getBucketName(),
            file.getObjectKey()
        );

        file.updateStorageClass(StorageClass.ARCHIVE);
    }

    private void deleteFile(FileAsset file) {
        // S3에서 물리적 삭제
        s3Adapter.deleteObject(
            file.getBucketName(),
            file.getObjectKey()
        );

        file.markAsPhysicallyDeleted();
    }

    private void moveToGlacier(FileAsset file) {
        // S3 Glacier로 이동
        s3Adapter.moveToGlacier(
            file.getBucketName(),
            file.getObjectKey()
        );

        file.updateStorageClass(StorageClass.GLACIER);
    }
}
```

#### 2. FileExpirationPolicy
```java
public class FileExpirationPolicy {
    private Long id;
    private Long tenantId;
    private Long organizationId;
    private Duration retentionPeriod;  // 보관 기간
    private FileExpirationAction defaultAction;
    private Map<String, Duration> mimeTypeOverrides;  // MIME 타입별 설정
    private boolean autoExpire;
    private Instant createdAt;
    private Instant updatedAt;

    // Static Factory Method
    public static FileExpirationPolicy createDefault(Long tenantId,
                                                    Long organizationId) {
        FileExpirationPolicy policy = new FileExpirationPolicy();
        policy.tenantId = tenantId;
        policy.organizationId = organizationId;
        policy.retentionPeriod = Duration.ofDays(365);  // 기본 1년
        policy.defaultAction = FileExpirationAction.ARCHIVE;
        policy.autoExpire = true;
        policy.mimeTypeOverrides = new HashMap<>();
        policy.createdAt = Instant.now();
        policy.updatedAt = Instant.now();
        return policy;
    }

    public Duration getRetentionPeriodForMimeType(String mimeType) {
        return mimeTypeOverrides.getOrDefault(mimeType, retentionPeriod);
    }

    // Getters...
}
```

---

## 🎯 KAN-306: FileExpirationScheduler 구현

### 작업 내용
스케줄러를 통해 파일 만료 작업을 자동 실행합니다.

### 구현 체크리스트

#### 1. Scheduler 구현
```java
package com.ryuqq.fileflow.adapter.scheduler;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "file.expiration.scheduler.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class FileExpirationScheduler {
    private final ExpireFileAssetsUseCase expireFileAssetsUseCase;
    private final TenantQueryPort tenantQueryPort;
    private final SchedulerLockManager lockManager;
    private final NotificationService notificationService;

    /**
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "${file.expiration.scheduler.cron:0 0 2 * * ?}")
    @SchedulerLock(
        name = "FileExpirationScheduler",
        lockAtMostFor = "4h",
        lockAtLeastFor = "10m"
    )
    public void executeFileExpiration() {
        log.info("파일 만료 스케줄러 시작");

        // 분산 락 획득
        if (!lockManager.tryLock("file-expiration")) {
            log.info("다른 인스턴스에서 실행 중. 스킵.");
            return;
        }

        try {
            processAllTenants();
        } catch (Exception e) {
            log.error("파일 만료 스케줄러 실패", e);
            notificationService.sendAlert(
                "파일 만료 스케줄러 실패",
                e.getMessage()
            );
        } finally {
            lockManager.unlock("file-expiration");
        }
    }

    private void processAllTenants() {
        // 모든 테넌트에 대해 처리
        List<Tenant> activeTenants = tenantQueryPort.findAllActive();

        log.info("처리할 테넌트 수: {}", activeTenants.size());

        for (Tenant tenant : activeTenants) {
            try {
                processTenantExpiration(tenant);
            } catch (Exception e) {
                log.error("테넌트 파일 만료 처리 실패: tenantId={}",
                    tenant.getId(), e);
            }
        }
    }

    private void processTenantExpiration(Tenant tenant) {
        log.info("테넌트 파일 만료 처리 시작: tenantId={}", tenant.getId());

        ExpireFileAssetsCommand command = ExpireFileAssetsCommand.builder()
            .tenantId(tenant.getId())
            .organizationId(null)  // 전체 조직
            .forceDelete(false)
            .build();

        FileExpirationResult result = expireFileAssetsUseCase.execute(command);

        // 결과 로깅
        log.info("테넌트 파일 만료 처리 완료: tenantId={}, expired={}, failed={}",
            tenant.getId(),
            result.getExpiredCount(),
            result.getFailedCount());

        // 실패 건이 있으면 알림
        if (result.getFailedCount() > 0) {
            notificationService.sendAlert(
                String.format("파일 만료 처리 일부 실패: tenantId=%d", tenant.getId()),
                String.format("실패 건수: %d", result.getFailedCount())
            );
        }
    }

    /**
     * 수동 실행용 (관리자 기능)
     */
    public FileExpirationResult executeManually(Long tenantId,
                                               Long organizationId) {
        log.info("파일 만료 수동 실행: tenantId={}, organizationId={}",
            tenantId, organizationId);

        ExpireFileAssetsCommand command = ExpireFileAssetsCommand.builder()
            .tenantId(tenantId)
            .organizationId(organizationId)
            .forceDelete(false)
            .build();

        return expireFileAssetsUseCase.execute(command);
    }
}
```

#### 2. Scheduler Lock 설정
```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()  // DB 시간 사용
                .build()
        );
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("file-scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}
```

---

## 🎯 KAN-307: FileLifecycle REST Controller 구현

### 작업 내용
파일 라이프사이클 관련 REST API를 구현합니다.

### 구현 체크리스트

#### 1. Controller 구현
```java
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Lifecycle", description = "파일 라이프사이클 관리 API")
public class FileLifecycleController {

    private final SoftDeleteFileAssetUseCase softDeleteUseCase;
    private final QueryFileAccessLogsUseCase accessLogUseCase;
    private final ExpireFileAssetsUseCase expireUseCase;
    private final FileLifecycleApiMapper mapper;

    // === 파일 삭제 ===

    @DeleteMapping("/{fileAssetId}/soft")
    @Operation(summary = "파일 논리 삭제",
              description = "파일을 논리적으로 삭제합니다 (복구 가능)")
    public ResponseEntity<Void> softDelete(
        @PathVariable String fileAssetId,
        @RequestParam(required = false) String reason,
        @RequestHeader("X-User-Id") Long userId
    ) {
        SoftDeleteFileAssetCommand command = SoftDeleteFileAssetCommand.builder()
            .fileAssetId(fileAssetId)
            .userId(userId)
            .reason(reason)
            .build();

        softDeleteUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{fileAssetId}/restore")
    @Operation(summary = "파일 복구",
              description = "논리 삭제된 파일을 복구합니다")
    public ResponseEntity<FileAssetResponse> restore(
        @PathVariable String fileAssetId,
        @RequestHeader("X-User-Id") Long userId
    ) {
        RestoreFileAssetCommand command = new RestoreFileAssetCommand(
            fileAssetId, userId
        );

        FileAssetResponse restored = restoreUseCase.execute(command);

        return ResponseEntity.ok(restored);
    }

    // === 접근 로그 ===

    @GetMapping("/{fileAssetId}/access-logs")
    @Operation(summary = "파일 접근 로그 조회",
              description = "파일의 접근 이력을 조회합니다")
    public ResponseEntity<Page<FileAccessLogResponse>> getAccessLogs(
        @PathVariable String fileAssetId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate,
        @RequestParam(required = false) AccessType accessType,
        @PageableDefault(size = 20, sort = "accessedAt,desc") Pageable pageable
    ) {
        AccessLogFilter filter = AccessLogFilter.builder()
            .startDate(startDate)
            .endDate(endDate)
            .accessType(accessType)
            .build();

        Page<FileAccessLogResponse> logs = accessLogUseCase
            .getFileAccessLogs(fileAssetId, filter, pageable);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{fileAssetId}/access-statistics")
    @Operation(summary = "파일 접근 통계",
              description = "파일의 접근 통계를 조회합니다")
    public ResponseEntity<FileAccessStatistics> getAccessStatistics(
        @PathVariable String fileAssetId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate
    ) {
        FileAccessStatistics statistics = accessLogUseCase
            .getAccessStatistics(fileAssetId, startDate, endDate);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/suspicious-access")
    @Operation(summary = "비정상 접근 패턴 조회",
              description = "비정상적인 파일 접근 패턴을 감지합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SuspiciousAccessPattern>> getSuspiciousAccess(
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestParam(defaultValue = "24") int hoursAgo
    ) {
        Instant since = Instant.now().minus(Duration.ofHours(hoursAgo));

        List<SuspiciousAccessPattern> patterns = accessLogUseCase
            .detectSuspiciousAccess(tenantId, since);

        return ResponseEntity.ok(patterns);
    }

    // === 파일 만료 ===

    @PostMapping("/expire")
    @Operation(summary = "파일 만료 처리 (수동)",
              description = "만료된 파일을 수동으로 처리합니다")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FileExpirationResult> expireFiles(
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestParam(required = false) Long organizationId,
        @RequestParam(defaultValue = "false") boolean forceDelete
    ) {
        ExpireFileAssetsCommand command = ExpireFileAssetsCommand.builder()
            .tenantId(tenantId)
            .organizationId(organizationId)
            .forceDelete(forceDelete)
            .build();

        FileExpirationResult result = expireUseCase.execute(command);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/expiration-policy")
    @Operation(summary = "파일 만료 정책 조회")
    public ResponseEntity<FileExpirationPolicyResponse> getExpirationPolicy(
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestHeader("X-Organization-Id") Long organizationId
    ) {
        FileExpirationPolicy policy = policyUseCase.getPolicy(
            tenantId, organizationId
        );

        return ResponseEntity.ok(
            FileExpirationPolicyResponse.of(policy)
        );
    }

    @PutMapping("/expiration-policy")
    @Operation(summary = "파일 만료 정책 수정")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FileExpirationPolicyResponse> updateExpirationPolicy(
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestHeader("X-Organization-Id") Long organizationId,
        @Valid @RequestBody UpdateExpirationPolicyRequest request
    ) {
        UpdateExpirationPolicyCommand command = mapper.toCommand(
            request, tenantId, organizationId
        );

        FileExpirationPolicy updated = policyUseCase.updatePolicy(command);

        return ResponseEntity.ok(
            FileExpirationPolicyResponse.of(updated)
        );
    }
}
```

#### 2. Access Log Interceptor
```java
@Component
@RequiredArgsConstructor
public class FileAccessLogInterceptor implements HandlerInterceptor {
    private final FileAccessLogService accessLogService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {
        // 요청 시작 시간 기록
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) throws Exception {
        // 파일 관련 API만 로깅
        if (!isFileApi(request.getRequestURI())) {
            return;
        }

        // 응답 시간 계산
        Long startTime = (Long) request.getAttribute("startTime");
        Long responseTime = System.currentTimeMillis() - startTime;

        // 접근 로그 기록
        recordAccessLog(request, response, responseTime, ex);
    }

    private void recordAccessLog(HttpServletRequest request,
                                HttpServletResponse response,
                                Long responseTime,
                                Exception ex) {
        try {
            String fileAssetId = extractFileAssetId(request);
            if (fileAssetId == null) {
                return;
            }

            AccessType accessType = determineAccessType(request);
            Long userId = extractUserId(request);
            Long tenantId = extractTenantId(request);
            Long organizationId = extractOrganizationId(request);

            FileAccessLog log = FileAccessLog.create(
                fileAssetId,
                userId,
                accessType,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                tenantId,
                organizationId
            );

            if (ex != null) {
                log.markFailure(
                    response.getStatus(),
                    responseTime,
                    ex.getMessage()
                );
            } else {
                log.markSuccess(response.getStatus(), responseTime);
            }

            // 비동기로 저장
            accessLogService.saveAsync(log);

        } catch (Exception e) {
            log.error("접근 로그 기록 실패", e);
        }
    }

    private AccessType determineAccessType(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.contains("/download")) {
            return AccessType.DOWNLOAD;
        }
        if (uri.contains("/preview")) {
            return AccessType.PREVIEW;
        }
        if (uri.contains("/thumbnail")) {
            return AccessType.THUMBNAIL;
        }
        if (uri.contains("/metadata")) {
            return AccessType.METADATA;
        }

        return AccessType.VIEW;
    }
}
```

---

## 🎯 KAN-308: Phase 3C 통합 테스트 작성

### 작업 내용
Phase 3C에서 구현한 라이프사이클 기능들의 통합 테스트를 작성합니다.

### 구현 체크리스트

#### 1. 파일 삭제/복구 통합 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
class FileLifecycleIntegrationTest {

    @Test
    void 파일_논리삭제_및_복구_테스트() throws Exception {
        // Given - 파일 생성
        String fileAssetId = createTestFile();

        // When - 논리 삭제
        mockMvc.perform(delete("/api/v1/files/" + fileAssetId + "/soft")
                .param("reason", "테스트 삭제")
                .header("X-User-Id", "1"))
            .andExpect(status().isNoContent());

        // Then - 파일 상태 확인
        FileAsset deleted = repository.findByFileAssetId(fileAssetId)
            .orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(FileStatus.DELETED);
        assertThat(deleted.getDeletedAt()).isNotNull();

        // When - 복구
        mockMvc.perform(post("/api/v1/files/" + fileAssetId + "/restore")
                .header("X-User-Id", "1"))
            .andExpect(status().isOk());

        // Then - 복구 확인
        FileAsset restored = repository.findByFileAssetId(fileAssetId)
            .orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(FileStatus.ACTIVE);
        assertThat(restored.getDeletedAt()).isNull();
    }
}
```

#### 2. 접근 로그 통합 테스트
```java
@Test
void 파일_접근_로그_기록_테스트() throws Exception {
    // Given
    String fileAssetId = "FILE-001";

    // When - 파일 조회
    mockMvc.perform(get("/api/v1/files/" + fileAssetId)
            .header("X-User-Id", "1")
            .header("X-Tenant-Id", "1"))
        .andExpect(status().isOk());

    // Then - 접근 로그 확인
    await().atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {
            List<FileAccessLogEntity> logs = accessLogRepository
                .findByFileAssetId(fileAssetId);
            assertThat(logs).hasSize(1);

            FileAccessLogEntity log = logs.get(0);
            assertThat(log.getAccessType()).isEqualTo(AccessType.VIEW);
            assertThat(log.getStatusCode()).isEqualTo(200);
            assertThat(log.getUserId()).isEqualTo(1L);
        });
}
```

#### 3. 파일 만료 통합 테스트
```java
@Test
void 파일_만료_처리_테스트() throws Exception {
    // Given - 오래된 파일 생성
    createExpiredFiles(10);

    // When - 만료 처리
    MvcResult result = mockMvc.perform(post("/api/v1/files/expire")
            .header("X-Tenant-Id", "1")
            .param("forceDelete", "false"))
        .andExpect(status().isOk())
        .andReturn();

    // Then - 결과 확인
    FileExpirationResult expirationResult = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        FileExpirationResult.class
    );

    assertThat(expirationResult.getExpiredCount()).isEqualTo(10);
    assertThat(expirationResult.getFailedCount()).isEqualTo(0);
}
```

---

## 🎯 KAN-309: Phase 3 ArchUnit 아키텍처 검증 규칙 추가

### 작업 내용
Phase 3에서 구현한 기능들의 아키텍처 규칙을 ArchUnit으로 검증합니다.

### 구현 체크리스트

#### ArchUnit 테스트 추가
```java
@AnalyzeClasses(packages = "com.ryuqq.fileflow")
class Phase3ArchitectureTest {

    @Test
    @ArchTest
    void FileAsset은_AbstractAggregateRoot를_상속해야함(JavaClasses classes) {
        classes().that()
            .resideInPackage("..domain.file..")
            .and().haveNameMatching(".*Asset")
            .should().beAssignableTo(AbstractAggregateRoot.class)
            .check(classes);
    }

    @Test
    @ArchTest
    void UseCase는_Transaction을_가져야함(JavaClasses classes) {
        methods().that()
            .areDeclaredInClassesThat()
            .haveNameMatching(".*UseCase")
            .and().arePublic()
            .and().haveName("execute")
            .should().beAnnotatedWith(Transactional.class)
            .check(classes);
    }

    @Test
    @ArchTest
    void Controller는_UseCase만_의존해야함(JavaClasses classes) {
        classes().that()
            .resideInPackage("..adapter.rest..")
            .and().haveNameMatching(".*Controller")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..adapter.rest..",
                "java..",
                "org.springframework.."
            )
            .check(classes);
    }

    @Test
    @ArchTest
    void Entity는_Lombok을_사용하지_않아야함(JavaClasses classes) {
        noClasses().that()
            .resideInPackage("..domain..")
            .or().resideInPackage("..persistence..")
            .should().beAnnotatedWith("lombok.Data")
            .orShould().beAnnotatedWith("lombok.Getter")
            .orShould().beAnnotatedWith("lombok.Setter")
            .check(classes);
    }

    @Test
    @ArchTest
    void JPA_Entity는_관계_어노테이션을_사용하지_않아야함(JavaClasses classes) {
        noMethods().that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(ManyToOne.class)
            .orShould().beAnnotatedWith(OneToMany.class)
            .orShould().beAnnotatedWith(OneToOne.class)
            .orShould().beAnnotatedWith(ManyToMany.class)
            .check(classes);
    }
}
```

---

## 📝 Phase 3C 체크리스트 총정리

### 개발 전
- [ ] Phase 3A, 3B 완료 확인
- [ ] 스케줄러 설정 확인
- [ ] S3 라이프사이클 정책 확인

### 개발 중
- [ ] **NO Lombok** 모든 코드
- [ ] **Law of Demeter** 준수
- [ ] **Long FK** 전략
- [ ] **Transaction** 경계 준수
- [ ] 비동기 처리 구현
- [ ] 배치 처리 최적화

### 개발 후
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] ArchUnit 테스트 추가
- [ ] 성능 테스트
- [ ] 문서 업데이트

## 🎉 KAN-260 에픽 완료

모든 Phase (3A, 3B, 3C)의 구현이 완료되면:

1. **코드 리뷰 요청**
2. **통합 테스트 실행**
3. **성능 테스트 실행**
4. **배포 준비**
5. **문서 최종 업데이트**

축하합니다! 파일 관리 시스템이 완성되었습니다. 🎊