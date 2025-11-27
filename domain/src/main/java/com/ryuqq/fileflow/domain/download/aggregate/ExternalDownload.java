package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 외부 다운로드 요청 Aggregate Root.
 *
 * <p><strong>책임</strong>: 외부 URL 다운로드 요청의 생명주기 관리
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <ul>
 *   <li>PENDING → PROCESSING: Worker가 처리 시작
 *   <li>PROCESSING → COMPLETED: 다운로드 + 업로드 성공
 *   <li>PROCESSING → FAILED: 2회 재시도 후 최종 실패
 *   <li>PROCESSING → PENDING: 재시도 (retryCount < 2)
 * </ul>
 *
 * <p><strong>재시도 규칙</strong>:
 *
 * <ul>
 *   <li>최대 재시도 횟수: 2회
 *   <li>재시도 시 retryCount 증가
 *   <li>2회 초과 시 FAILED 상태로 전환
 * </ul>
 */
public class ExternalDownload {

    private final ExternalDownloadId id;
    private final SourceUrl sourceUrl;
    private final long tenantId;
    private final long organizationId;
    private final S3Bucket s3Bucket;
    private final String s3PathPrefix;
    private ExternalDownloadStatus status;
    private RetryCount retryCount;
    private FileAssetId fileAssetId;
    private String errorMessage;
    private final WebhookUrl webhookUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    /** 도메인 이벤트 컬렉션 (발행 전 임시 저장). */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private ExternalDownload(
            ExternalDownloadId id,
            SourceUrl sourceUrl,
            long tenantId,
            long organizationId,
            S3Bucket s3Bucket,
            String s3PathPrefix,
            ExternalDownloadStatus status,
            RetryCount retryCount,
            FileAssetId fileAssetId,
            String errorMessage,
            WebhookUrl webhookUrl,
            Instant createdAt,
            Instant updatedAt) {
        Objects.requireNonNull(sourceUrl, "sourceUrl must not be null");
        Objects.requireNonNull(s3Bucket, "s3Bucket must not be null");
        Objects.requireNonNull(s3PathPrefix, "s3PathPrefix must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(retryCount, "retryCount must not be null");

        this.id = id;
        this.sourceUrl = sourceUrl;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.s3Bucket = s3Bucket;
        this.s3PathPrefix = s3PathPrefix;
        this.status = status;
        this.retryCount = retryCount;
        this.fileAssetId = fileAssetId;
        this.errorMessage = errorMessage;
        this.webhookUrl = webhookUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 ExternalDownload 생성 (ID null, PENDING 상태).
     *
     * @param sourceUrl 외부 이미지 URL
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param s3Bucket S3 버킷 (UserContext에서 추출)
     * @param s3PathPrefix S3 경로 prefix (예: "admin/", "seller-123/", "customer/")
     * @param webhookUrl 콜백 URL (nullable)
     * @param clock 시간 소스
     * @return 신규 ExternalDownload
     */
    public static ExternalDownload forNew(
            SourceUrl sourceUrl,
            long tenantId,
            long organizationId,
            S3Bucket s3Bucket,
            String s3PathPrefix,
            WebhookUrl webhookUrl,
            Clock clock) {
        Instant now = Instant.now(clock);
        return new ExternalDownload(
                ExternalDownloadId.forNew(),
                sourceUrl,
                tenantId,
                organizationId,
                s3Bucket,
                s3PathPrefix,
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                webhookUrl,
                now,
                now);
    }

    /**
     * 기존 ExternalDownload 재구성 (조회용).
     *
     * @param id 다운로드 ID
     * @param sourceUrl 외부 이미지 URL
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param s3Bucket S3 버킷
     * @param s3PathPrefix S3 경로 prefix
     * @param status 현재 상태
     * @param retryCount 재시도 횟수
     * @param fileAssetId 생성된 파일 자산 ID (nullable)
     * @param errorMessage 에러 메시지 (nullable)
     * @param webhookUrl 콜백 URL (nullable)
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return 재구성된 ExternalDownload
     */
    public static ExternalDownload of(
            ExternalDownloadId id,
            SourceUrl sourceUrl,
            long tenantId,
            long organizationId,
            S3Bucket s3Bucket,
            String s3PathPrefix,
            ExternalDownloadStatus status,
            RetryCount retryCount,
            FileAssetId fileAssetId,
            String errorMessage,
            WebhookUrl webhookUrl,
            Instant createdAt,
            Instant updatedAt) {
        return new ExternalDownload(
                id,
                sourceUrl,
                tenantId,
                organizationId,
                s3Bucket,
                s3PathPrefix,
                status,
                retryCount,
                fileAssetId,
                errorMessage,
                webhookUrl,
                createdAt,
                updatedAt);
    }

    /**
     * 처리 시작 (PENDING → PROCESSING).
     *
     * @param clock 시간 소스
     * @throws IllegalStateException 현재 상태가 PENDING이 아닌 경우
     */
    public void startProcessing(Clock clock) {
        if (status != ExternalDownloadStatus.PENDING) {
            throw new IllegalStateException("처리 시작은 PENDING 상태에서만 가능합니다. 현재 상태: " + status);
        }
        this.status = ExternalDownloadStatus.PROCESSING;
        this.updatedAt = Instant.now(clock);
    }

    /**
     * 처리 완료 (PROCESSING → COMPLETED).
     *
     * <p>완료 시 FileAsset 생성을 위한 도메인 이벤트를 자동으로 등록합니다.
     *
     * @param contentType 컨텐츠 타입
     * @param contentLength 컨텐츠 길이
     * @param s3Key 업로드된 S3 키
     * @param etag S3 ETag
     * @param clock 시간 소스
     * @throws IllegalStateException 현재 상태가 PROCESSING이 아닌 경우
     */
    public void complete(
            String contentType, long contentLength, S3Key s3Key, ETag etag, Clock clock) {
        if (status != ExternalDownloadStatus.PROCESSING) {
            throw new IllegalStateException("완료는 PROCESSING 상태에서만 가능합니다. 현재 상태: " + status);
        }

        this.status = ExternalDownloadStatus.COMPLETED;
        this.updatedAt = Instant.now(clock);

        // 도메인 이벤트 자동 등록 (비즈니스 로직의 자연스러운 결과)
        ExternalDownloadFileCreatedEvent event =
                createFileCreatedEvent(contentType, contentLength, s3Key, etag, clock);
        registerEvent(event);
    }

    /**
     * 처리 완료 (PROCESSING → COMPLETED) - FileAssetId 포함.
     *
     * @param fileAssetId 생성된 파일 자산 ID
     * @param clock 시간 소스
     * @throws IllegalStateException 현재 상태가 PROCESSING이 아닌 경우
     * @deprecated FileAsset은 도메인 이벤트로 생성됩니다. {@link #complete(String, long, S3Key, ETag, Clock)} 사용
     *     권장
     */
    @Deprecated
    public void complete(FileAssetId fileAssetId, Clock clock) {
        if (status != ExternalDownloadStatus.PROCESSING) {
            throw new IllegalStateException("완료는 PROCESSING 상태에서만 가능합니다. 현재 상태: " + status);
        }
        this.status = ExternalDownloadStatus.COMPLETED;
        this.fileAssetId = fileAssetId;
        this.updatedAt = Instant.now(clock);
    }

    /**
     * 재시도 (PROCESSING → PENDING).
     *
     * @param clock 시간 소스
     * @throws IllegalStateException 현재 상태가 PROCESSING이 아니거나 재시도 횟수 초과 시
     */
    public void retry(Clock clock) {
        if (status != ExternalDownloadStatus.PROCESSING) {
            throw new IllegalStateException("재시도는 PROCESSING 상태에서만 가능합니다. 현재 상태: " + status);
        }
        if (!canRetry()) {
            throw new IllegalStateException(
                    "최대 재시도 횟수를 초과했습니다. 현재 retryCount: " + retryCount.value());
        }
        this.status = ExternalDownloadStatus.PENDING;
        this.retryCount = retryCount.increment();
        this.updatedAt = Instant.now(clock);
    }

    /**
     * 최종 실패 (PROCESSING → FAILED).
     *
     * @param errorMessage 에러 메시지
     * @param defaultFileAssetId 디폴트 이미지 파일 자산 ID
     * @param clock 시간 소스
     * @throws IllegalStateException 현재 상태가 PROCESSING이 아닌 경우
     */
    public void fail(String errorMessage, FileAssetId defaultFileAssetId, Clock clock) {
        if (status != ExternalDownloadStatus.PROCESSING) {
            throw new IllegalStateException("실패 처리는 PROCESSING 상태에서만 가능합니다. 현재 상태: " + status);
        }
        this.status = ExternalDownloadStatus.FAILED;
        this.errorMessage = errorMessage;
        this.fileAssetId = defaultFileAssetId;
        this.updatedAt = Instant.now(clock);
    }

    /**
     * DLQ에서 최종 실패 처리.
     *
     * <p><strong>도메인 규칙</strong>:
     *
     * <ul>
     *   <li>이미 COMPLETED 상태인 경우: 처리 완료된 것이므로 무시 (false 반환)
     *   <li>이미 FAILED 상태인 경우: 이미 실패 처리된 것이므로 무시 (false 반환)
     *   <li>PENDING 상태인 경우: PROCESSING → FAILED로 전환
     *   <li>PROCESSING 상태인 경우: FAILED로 전환
     * </ul>
     *
     * @param errorMessage 에러 메시지
     * @param defaultFileAssetId 디폴트 이미지 파일 자산 ID
     * @param clock 시간 소스
     * @return 실패 처리 수행 여부 (true: 처리함, false: skip)
     */
    public boolean markAsFailed(String errorMessage, FileAssetId defaultFileAssetId, Clock clock) {
        // 이미 완료 또는 실패된 경우 무시
        if (status == ExternalDownloadStatus.COMPLETED || status == ExternalDownloadStatus.FAILED) {
            return false;
        }

        // PENDING 상태인 경우 먼저 PROCESSING으로 전환
        if (status == ExternalDownloadStatus.PENDING) {
            startProcessing(clock);
        }

        // PROCESSING → FAILED
        fail(errorMessage, defaultFileAssetId, clock);
        return true;
    }

    /**
     * 재시도 가능 여부 확인.
     *
     * @return retryCount가 최대 재시도 횟수 미만이면 true
     */
    public boolean canRetry() {
        return retryCount.canRetry();
    }

    /**
     * Webhook URL 존재 여부 확인.
     *
     * @return webhookUrl이 null이 아니면 true
     */
    public boolean hasWebhook() {
        return webhookUrl != null;
    }

    /**
     * 등록 이벤트 생성.
     *
     * <p>Application Layer에서 저장 후 이벤트 발행 시 호출합니다.
     *
     * @return ExternalDownloadRegisteredEvent
     */
    public ExternalDownloadRegisteredEvent createRegisteredEvent() {
        return ExternalDownloadRegisteredEvent.of(
                id, sourceUrl, tenantId, organizationId, webhookUrl, createdAt);
    }

    // ========================================
    // 도메인 이벤트 관리
    // ========================================

    /**
     * 도메인 이벤트 등록.
     *
     * <p>Aggregate 상태 변경 시 발행할 이벤트를 등록합니다.
     *
     * @param event 등록할 도메인 이벤트
     */
    public void registerEvent(DomainEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        this.domainEvents.add(event);
    }

    /**
     * 등록된 도메인 이벤트 조회.
     *
     * <p>Facade에서 이벤트 발행 시 사용합니다.
     *
     * @return 불변 도메인 이벤트 목록
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트 초기화.
     *
     * <p>이벤트 발행 완료 후 호출하여 중복 발행을 방지합니다.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * ID 값 조회.
     *
     * @return ID의 Long 값
     */
    public Long getIdValue() {
        return id.value();
    }

    // Getters
    public ExternalDownloadId getId() {
        return id;
    }

    public SourceUrl getSourceUrl() {
        return sourceUrl;
    }

    public long getTenantId() {
        return tenantId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public S3Bucket getS3Bucket() {
        return s3Bucket;
    }

    public String getS3PathPrefix() {
        return s3PathPrefix;
    }

    public ExternalDownloadStatus getStatus() {
        return status;
    }

    public RetryCount getRetryCount() {
        return retryCount;
    }

    /**
     * 재시도 횟수 값 조회 (primitive).
     *
     * @return 재시도 횟수
     */
    public int getRetryCountValue() {
        return retryCount.value();
    }

    public FileAssetId getFileAssetId() {
        return fileAssetId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public WebhookUrl getWebhookUrl() {
        return webhookUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ========================================
    // 도메인 로직
    // ========================================

    /**
     * S3 키 생성.
     *
     * <p>형식: {s3PathPrefix}external-download/{yyyy/MM/dd}/{uuid}.{extension}
     *
     * @param extension 파일 확장자
     * @param clock 시간 소스
     * @return S3Key
     */
    public S3Key generateS3Key(String extension, Clock clock) {
        LocalDate today = LocalDate.now(clock);
        String datePath =
                String.format(
                        "%04d/%02d/%02d",
                        today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        String uuid = UUID.randomUUID().toString();
        String key =
                String.format(
                        "%sexternal-download/%s/%s.%s",
                        this.s3PathPrefix, datePath, uuid, extension);

        return S3Key.of(key);
    }

    /**
     * FileAsset 생성 이벤트 생성 (내부용).
     *
     * <p>complete() 메서드에서 자동으로 호출되어 도메인 이벤트로 등록됩니다.
     *
     * @param contentType 컨텐츠 타입
     * @param contentLength 컨텐츠 길이
     * @param s3Key S3 키
     * @param etag ETag
     * @param clock 시간 소스
     * @return ExternalDownloadFileCreatedEvent
     */
    private ExternalDownloadFileCreatedEvent createFileCreatedEvent(
            String contentType, long contentLength, S3Key s3Key, ETag etag, Clock clock) {

        String extension = extractExtension(contentType);
        FileName fileName = this.sourceUrl.extractFileName(extension);

        return ExternalDownloadFileCreatedEvent.of(
                this.id,
                this.sourceUrl,
                fileName,
                FileSize.of(contentLength),
                ContentType.of(contentType),
                FileCategory.fromMimeType(contentType),
                this.s3Bucket,
                s3Key,
                etag,
                this.organizationId,
                this.tenantId,
                LocalDateTime.now(clock));
    }

    /**
     * Content-Type에서 확장자 추출.
     *
     * @param contentType 컨텐츠 타입
     * @return 확장자
     */
    private String extractExtension(String contentType) {
        if (contentType == null) {
            return "bin";
        }

        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            case "image/bmp" -> "bmp";
            case "image/tiff" -> "tiff";
            default -> {
                int slashIndex = contentType.lastIndexOf('/');
                yield slashIndex >= 0 ? contentType.substring(slashIndex + 1) : "bin";
            }
        };
    }
}
