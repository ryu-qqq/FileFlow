package com.ryuqq.fileflow.domain.file.vo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MultipartUpload Value Object
 * <p>
 * S3 멀티파트 업로드 관련 모든 정보를 캡슐화합니다.
 * File Aggregate에서 분리하여 단일 책임 원칙을 준수합니다.
 * </p>
 *
 * <p>
 * 불변 객체 (Immutable):
 * - 상태 변경 시 새로운 인스턴스 반환
 * - Thread-safe
 * </p>
 *
 * <p>
 * S3 Multipart Upload 흐름:
 * 1. Initiate Multipart Upload → MultipartUploadId 발급
 * 2. Upload Parts → UploadedPart 추가 (파트별 ETag 저장)
 * 3. Complete Multipart Upload → COMPLETED 상태
 * 4. Abort Multipart Upload → ABORTED 상태 (실패 시)
 * </p>
 */
public final class MultipartUpload {

    private final MultipartUploadId uploadId;
    private final MultipartStatus status;
    private final int totalParts;
    private final List<UploadedPart> uploadedParts;
    private final LocalDateTime initiatedAt;
    private final LocalDateTime completedAt; // Nullable
    private final LocalDateTime abortedAt; // Nullable

    /**
     * Private Constructor (정적 팩토리 메서드 사용)
     */
    private MultipartUpload(
            MultipartUploadId uploadId,
            MultipartStatus status,
            int totalParts,
            List<UploadedPart> uploadedParts,
            LocalDateTime initiatedAt,
            LocalDateTime completedAt,
            LocalDateTime abortedAt
    ) {
        this.uploadId = Objects.requireNonNull(uploadId, "uploadId는 null일 수 없습니다");
        this.status = Objects.requireNonNull(status, "status는 null일 수 없습니다");
        this.totalParts = totalParts;
        this.uploadedParts = List.copyOf(uploadedParts); // Immutable copy
        this.initiatedAt = Objects.requireNonNull(initiatedAt, "initiatedAt은 null일 수 없습니다");
        this.completedAt = completedAt;
        this.abortedAt = abortedAt;

        validateTotalParts(totalParts);
    }

    /**
     * 새로운 멀티파트 업로드 생성
     *
     * @param uploadId   S3 Multipart Upload ID
     * @param totalParts 전체 파트 수
     * @param clock      시각 생성용 Clock
     * @return 새로운 MultipartUpload (INITIATED 상태)
     */
    public static MultipartUpload forNew(MultipartUploadId uploadId, int totalParts, Clock clock) {
        return new MultipartUpload(
                uploadId,
                MultipartStatus.INITIATED,
                totalParts,
                Collections.emptyList(),
                LocalDateTime.now(clock),
                null,
                null
        );
    }

    /**
     * 파트 추가 (새 인스턴스 반환)
     * <p>
     * 상태 전이:
     * INITIATED → IN_PROGRESS (첫 번째 파트 추가 시)
     * </p>
     *
     * @param part 업로드된 파트
     * @return 파트가 추가된 새로운 MultipartUpload
     */
    public MultipartUpload withAddedPart(UploadedPart part) {
        Objects.requireNonNull(part, "part는 null일 수 없습니다");

        // 중복 파트 번호 검증
        if (uploadedParts.stream().anyMatch(p -> p.partNumber() == part.partNumber())) {
            throw new IllegalArgumentException(
                    String.format("이미 업로드된 파트 번호입니다: %d", part.partNumber())
            );
        }

        // 전체 파트 수 초과 검증
        if (uploadedParts.size() >= totalParts) {
            throw new IllegalArgumentException(
                    String.format("전체 파트 수를 초과했습니다 (최대: %d)", totalParts)
            );
        }

        List<UploadedPart> newParts = new ArrayList<>(uploadedParts);
        newParts.add(part);

        return new MultipartUpload(
                uploadId,
                MultipartStatus.IN_PROGRESS, // 상태 전이
                totalParts,
                newParts,
                initiatedAt,
                completedAt,
                abortedAt
        );
    }

    /**
     * 모든 파트 업로드 완료 여부 확인
     *
     * @return 모든 파트가 업로드되었으면 true
     */
    public boolean isAllPartsUploaded() {
        return uploadedParts.size() == totalParts;
    }

    /**
     * 완료 표시 (새 인스턴스 반환)
     * <p>
     * 상태 전이: ANY → COMPLETED
     * </p>
     *
     * @param clock 완료 시각 생성용 Clock
     * @return COMPLETED 상태의 새로운 MultipartUpload
     */
    public MultipartUpload markAsCompleted(Clock clock) {
        return new MultipartUpload(
                uploadId,
                MultipartStatus.COMPLETED,
                totalParts,
                uploadedParts,
                initiatedAt,
                LocalDateTime.now(clock),
                abortedAt
        );
    }

    /**
     * 중단 표시 (새 인스턴스 반환)
     * <p>
     * 상태 전이: ANY → ABORTED
     * </p>
     *
     * @param clock 중단 시각 생성용 Clock
     * @return ABORTED 상태의 새로운 MultipartUpload
     */
    public MultipartUpload markAsAborted(Clock clock) {
        return new MultipartUpload(
                uploadId,
                MultipartStatus.ABORTED,
                totalParts,
                uploadedParts,
                initiatedAt,
                completedAt,
                LocalDateTime.now(clock)
        );
    }

    /**
     * 전체 파트 수 검증
     */
    private static void validateTotalParts(int totalParts) {
        if (totalParts < 1) {
            throw new IllegalArgumentException(
                    String.format("전체 파트 수는 1 이상이어야 합니다 (현재: %d)", totalParts)
            );
        }
    }

    // Getters (Immutable)

    public MultipartUploadId uploadId() {
        return uploadId;
    }

    public MultipartStatus status() {
        return status;
    }

    public int totalParts() {
        return totalParts;
    }

    public List<UploadedPart> uploadedParts() {
        return uploadedParts; // Already immutable
    }

    public LocalDateTime initiatedAt() {
        return initiatedAt;
    }

    public LocalDateTime completedAt() {
        return completedAt;
    }

    public LocalDateTime abortedAt() {
        return abortedAt;
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipartUpload that = (MultipartUpload) o;
        return totalParts == that.totalParts &&
                Objects.equals(uploadId, that.uploadId) &&
                status == that.status &&
                Objects.equals(uploadedParts, that.uploadedParts) &&
                Objects.equals(initiatedAt, that.initiatedAt) &&
                Objects.equals(completedAt, that.completedAt) &&
                Objects.equals(abortedAt, that.abortedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uploadId, status, totalParts, uploadedParts, initiatedAt, completedAt, abortedAt);
    }

    @Override
    public String toString() {
        return "MultipartUpload{" +
                "uploadId=" + uploadId +
                ", status=" + status +
                ", totalParts=" + totalParts +
                ", uploadedParts=" + uploadedParts.size() +
                ", initiatedAt=" + initiatedAt +
                ", completedAt=" + completedAt +
                ", abortedAt=" + abortedAt +
                '}';
    }
}
