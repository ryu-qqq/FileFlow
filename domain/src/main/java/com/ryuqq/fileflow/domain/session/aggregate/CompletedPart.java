package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.PartNumber;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Clock;
import java.time.Instant;

/**
 * Multipart Upload의 Part를 나타내는 Aggregate Root.
 *
 * <p>MultipartUploadSession과 독립적으로 조회/업데이트가 가능합니다.
 *
 * <p><strong>생명주기</strong>:
 *
 * <ul>
 *   <li>PENDING: Part 생성됨 (Presigned URL 발급, ETag 없음)
 *   <li>COMPLETED: Part 업로드 완료 (ETag, size, uploadedAt 설정)
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>Part 번호는 1 이상이어야 한다.
 *   <li>Presigned URL은 필수 값이다.
 *   <li>완료된 Part는 다시 완료 처리할 수 없다 (멱등성 제외).
 * </ul>
 */
public class CompletedPart {

    private final Long id;
    private final UploadSessionId sessionId;
    private final PartNumber partNumber;
    private final PresignedUrl presignedUrl;
    private ETag etag;
    private long size;
    private Instant uploadedAt;

    /**
     * 초기화용 팩토리 메서드.
     *
     * <p>Part 생성 시점에 호출. ETag, size, uploadedAt은 기본값으로 설정.
     *
     * @param sessionId 세션 ID
     * @param partNumber Part 번호 (1 이상)
     * @param presignedUrl Presigned URL
     * @return CompletedPart (PENDING 상태)
     */
    public static CompletedPart forNew(
            UploadSessionId sessionId, PartNumber partNumber, PresignedUrl presignedUrl) {
        return new CompletedPart(
                null, sessionId, partNumber, presignedUrl, ETag.empty(), 0, Instant.MIN);
    }

    /**
     * 영속성 복원용 팩토리 메서드.
     *
     * @param id Entity PK (신규 생성 시 null)
     * @param sessionId 세션 ID
     * @param partNumber Part 번호 (1 이상)
     * @param presignedUrl Presigned URL
     * @param etag ETag
     * @param size Part 크기
     * @param uploadedAt 업로드 완료 시각
     * @return CompletedPart
     */
    public static CompletedPart of(
            Long id,
            UploadSessionId sessionId,
            PartNumber partNumber,
            PresignedUrl presignedUrl,
            ETag etag,
            long size,
            Instant uploadedAt) {
        return new CompletedPart(id, sessionId, partNumber, presignedUrl, etag, size, uploadedAt);
    }

    /** 생성자 (private). */
    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Validation in constructor is intentional for domain invariants")
    private CompletedPart(
            Long id,
            UploadSessionId sessionId,
            PartNumber partNumber,
            PresignedUrl presignedUrl,
            ETag etag,
            long size,
            Instant uploadedAt) {
        if (sessionId == null) {
            throw new IllegalArgumentException("세션 ID는 null일 수 없습니다.");
        }
        if (partNumber == null) {
            throw new IllegalArgumentException("Part 번호는 null일 수 없습니다.");
        }
        if (presignedUrl == null) {
            throw new IllegalArgumentException("Presigned URL은 null일 수 없습니다.");
        }
        if (etag == null) {
            throw new IllegalArgumentException("ETag는 null일 수 없습니다.");
        }
        if (uploadedAt == null) {
            throw new IllegalArgumentException("업로드 완료 시각은 null일 수 없습니다.");
        }
        this.id = id;
        this.sessionId = sessionId;
        this.partNumber = partNumber;
        this.presignedUrl = presignedUrl;
        this.etag = etag;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * Part 업로드 완료 처리.
     *
     * @param etag S3가 반환한 ETag
     * @param size Part 크기 (바이트)
     * @param clock 시간 제공자
     */
    public void complete(ETag etag, long size, Clock clock) {
        if (etag == null || etag.isEmpty()) {
            throw new IllegalArgumentException("ETag는 null이거나 비어있을 수 없습니다.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Part 크기는 0보다 커야 합니다.");
        }
        this.etag = etag;
        this.size = size;
        this.uploadedAt = clock.instant();
    }

    /**
     * Part가 업로드 완료되었는지 확인.
     *
     * @return 완료 여부
     */
    public boolean isCompleted() {
        return size > 0 && !etag.isEmpty();
    }

    // ==================== Getter ====================

    public Long getId() {
        return id;
    }

    public UploadSessionId getSessionId() {
        return sessionId;
    }

    public String getSessionIdValue() {
        return sessionId.value().toString();
    }

    public PartNumber getPartNumber() {
        return partNumber;
    }

    public int getPartNumberValue() {
        return partNumber.number();
    }

    public PresignedUrl getPresignedUrl() {
        return presignedUrl;
    }

    public String getPresignedUrlValue() {
        return presignedUrl.value();
    }

    public ETag getEtag() {
        return etag;
    }

    public String getETagValue() {
        return etag.value();
    }

    public long getSize() {
        return size;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
