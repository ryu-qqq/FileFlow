package com.ryuqq.fileflow.domain.upload;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Multipart Upload Aggregate Root
 * 대용량 파일의 분할 업로드 상태를 관리하는 Aggregate
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>파트 번호는 1부터 시작하며 연속되어야 함</li>
 *   <li>모든 파트가 업로드된 후에만 완료 가능</li>
 *   <li>상태 전환은 정의된 규칙에 따라서만 가능 (INIT → IN_PROGRESS → COMPLETED/ABORTED/FAILED)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MultipartUpload {

    private final Long id;
    private final UploadSessionId uploadSessionId;
    private ProviderUploadId providerUploadId;
    private MultipartStatus status;
    private TotalParts totalParts;
    private final List<UploadPart> uploadedParts;
    private final LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime abortedAt;

    /**
     * Multipart Upload 상태 Enum
     */
    public enum MultipartStatus {
        INIT,
        IN_PROGRESS,
        COMPLETED,
        ABORTED,
        FAILED
    }

    /**
     * Private 생성자 (외부에서 직접 생성 불가)
     *
     * @param id Multipart Upload ID (null 가능 - 신규 생성 시)
     * @param uploadSessionId Upload Session ID
     */
    private MultipartUpload(Long id, UploadSessionId uploadSessionId) {
        if (uploadSessionId == null) {
            throw new IllegalArgumentException("Upload Session ID는 필수입니다");
        }
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.status = MultipartStatus.INIT;
        this.uploadedParts = new ArrayList<>();
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param uploadedParts 업로드된 파트 목록
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     */
    private MultipartUpload(
        Long id,
        UploadSessionId uploadSessionId,
        ProviderUploadId providerUploadId,
        MultipartStatus status,
        TotalParts totalParts,
        List<UploadPart> uploadedParts,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt
    ) {
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.providerUploadId = providerUploadId;
        this.status = status;
        this.totalParts = totalParts;
        this.uploadedParts = new ArrayList<>(uploadedParts);
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.abortedAt = abortedAt;
    }

    /**
     * Static Factory Method - 신규 Multipart Upload 생성
     *
     * @param uploadSessionId Upload Session ID
     * @return 생성된 MultipartUpload (ID = null)
     * @throws IllegalArgumentException Upload Session ID가 null인 경우
     */
    public static MultipartUpload create(UploadSessionId uploadSessionId) {
        return new MultipartUpload(null, uploadSessionId);
    }

    /**
     * DB에서 조회한 데이터로 MultipartUpload 재구성 (Static Factory Method)
     *
     * @param id Multipart Upload ID (필수)
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param uploadedParts 업로드된 파트 목록
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @return 재구성된 MultipartUpload
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static MultipartUpload reconstitute(
        Long id,
        UploadSessionId uploadSessionId,
        ProviderUploadId providerUploadId,
        MultipartStatus status,
        TotalParts totalParts,
        List<UploadPart> uploadedParts,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new MultipartUpload(
            id,
            uploadSessionId,
            providerUploadId,
            status,
            totalParts,
            uploadedParts,
            startedAt,
            completedAt,
            abortedAt
        );
    }

    /**
     * Multipart 업로드 시작
     * 상태: INIT → IN_PROGRESS
     *
     * @param providerUploadId S3 UploadId
     * @param totalParts 총 파트 수
     * @throws IllegalStateException 이미 초기화된 경우
     * @throws IllegalArgumentException providerUploadId나 totalParts가 유효하지 않은 경우
     */
    public void initiate(ProviderUploadId providerUploadId, TotalParts totalParts) {
        validateInitiation();

        if (providerUploadId == null) {
            throw new IllegalArgumentException("Provider Upload ID는 필수입니다");
        }
        if (totalParts == null) {
            throw new IllegalArgumentException("Total Parts는 필수입니다");
        }

        this.providerUploadId = providerUploadId;
        this.totalParts = totalParts;
        this.status = MultipartStatus.IN_PROGRESS;
    }

    /**
     * 파트 추가
     * 파트 번호 중복 및 순서 검증
     *
     * @param part 업로드된 파트
     * @throws IllegalArgumentException part가 null이거나 중복된 경우
     * @throws IllegalStateException 진행 중 상태가 아닌 경우
     */
    public void addPart(UploadPart part) {
        validatePartAddition(part);
        this.uploadedParts.add(part);
    }

    /**
     * Multipart 업로드 완료
     * 상태: IN_PROGRESS → COMPLETED
     *
     * @throws IllegalStateException 완료 조건을 만족하지 않는 경우
     */
    public void complete() {
        if (!canComplete()) {
            throw new IllegalStateException(
                "Cannot complete: not all parts uploaded or invalid state"
            );
        }
        this.status = MultipartStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Multipart 업로드 중단
     * 상태: * → ABORTED
     *
     * @throws IllegalStateException 이미 완료된 경우
     */
    public void abort() {
        if (this.status == MultipartStatus.COMPLETED) {
            throw new IllegalStateException("Cannot abort completed upload");
        }
        this.status = MultipartStatus.ABORTED;
        this.abortedAt = LocalDateTime.now();
    }

    /**
     * Multipart 업로드 실패
     * 상태: * → FAILED
     */
    public void fail() {
        this.status = MultipartStatus.FAILED;
    }

    /**
     * 완료 가능 여부 확인
     *
     * @return 완료 가능하면 true
     */
    public boolean canComplete() {
        return status == MultipartStatus.IN_PROGRESS
            && totalParts != null
            && uploadedParts.size() == totalParts.value()
            && hasAllPartsInSequence();
    }

    /**
     * 진행 중인지 확인
     *
     * @return 진행 중이면 true
     */
    public boolean isInProgress() {
        return status == MultipartStatus.IN_PROGRESS;
    }

    /**
     * 완료되었는지 확인
     *
     * @return 완료되었으면 true
     */
    public boolean isCompleted() {
        return status == MultipartStatus.COMPLETED;
    }

    /**
     * 초기화 가능 여부 검증
     *
     * @throws IllegalStateException 이미 초기화된 경우
     */
    private void validateInitiation() {
        if (this.status != MultipartStatus.INIT) {
            throw new IllegalStateException(
                "Multipart already initiated: " + status
            );
        }
    }

    /**
     * 파트 추가 가능 여부 검증
     *
     * @param part 추가할 파트
     * @throws IllegalArgumentException part가 null이거나 중복된 경우
     * @throws IllegalStateException 진행 중 상태가 아닌 경우
     */
    private void validatePartAddition(UploadPart part) {
        if (part == null) {
            throw new IllegalArgumentException("Part cannot be null");
        }

        if (this.status != MultipartStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot add part in status: " + status
            );
        }

        PartNumber partNumber = part.getPartNumber();
        boolean duplicate = uploadedParts.stream()
            .anyMatch(p -> p.getPartNumber().equals(partNumber));

        if (duplicate) {
            throw new IllegalArgumentException(
                "Duplicate part number: " + partNumber.value()
            );
        }
    }

    /**
     * 모든 파트가 순서대로 존재하는지 검증
     *
     * @return 모든 파트가 순서대로 존재하면 true
     */
    private boolean hasAllPartsInSequence() {
        if (totalParts == null) {
            return false;
        }

        Set<Integer> partNumbers = uploadedParts.stream()
            .map(part -> part.getPartNumber().value())
            .collect(Collectors.toSet());

        for (int i = 1; i <= totalParts.value(); i++) {
            if (!partNumbers.contains(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Multipart Upload ID를 반환합니다.
     *
     * @return Multipart Upload ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Upload Session ID를 반환합니다.
     *
     * @return Upload Session ID
     */
    public UploadSessionId getUploadSessionId() {
        return uploadSessionId;
    }

    /**
     * Provider Upload ID를 반환합니다.
     *
     * @return Provider Upload ID
     */
    public ProviderUploadId getProviderUploadId() {
        return providerUploadId;
    }

    /**
     * 상태를 반환합니다.
     *
     * @return 상태
     */
    public MultipartStatus getStatus() {
        return status;
    }

    /**
     * 총 파트 수를 반환합니다.
     *
     * @return 총 파트 수
     */
    public TotalParts getTotalParts() {
        return totalParts;
    }

    /**
     * 업로드된 파트 목록을 반환합니다 (방어적 복사).
     *
     * @return 업로드된 파트 목록 (불변)
     */
    public List<UploadPart> getUploadedParts() {
        return Collections.unmodifiableList(uploadedParts);
    }

    /**
     * 시작 시간을 반환합니다.
     *
     * @return 시작 시간
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 완료 시간을 반환합니다.
     *
     * @return 완료 시간
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * 중단 시간을 반환합니다.
     *
     * @return 중단 시간
     */
    public LocalDateTime getAbortedAt() {
        return abortedAt;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultipartUpload that = (MultipartUpload) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return MultipartUpload 정보 문자열
     */
    @Override
    public String toString() {
        return "MultipartUpload{" +
            "id=" + id +
            ", uploadSessionId=" + (uploadSessionId != null ? uploadSessionId.value() : null) +
            ", status=" + status +
            ", totalParts=" + (totalParts != null ? totalParts.value() : null) +
            ", uploadedParts=" + uploadedParts.size() +
            '}';
    }
}
