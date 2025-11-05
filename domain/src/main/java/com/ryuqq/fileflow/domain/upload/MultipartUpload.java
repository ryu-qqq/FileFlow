package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.upload.exception.*;
import java.time.Clock;
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

    private final MultipartUploadId id;
    private final UploadSessionId uploadSessionId;
    private final Clock clock;
    private ProviderUploadId providerUploadId;
    private MultipartStatus status;
    private TotalParts totalParts;
    private final List<UploadPart> uploadedParts;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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
     * Package-private 주요 생성자 (검증 포함)
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param id Multipart Upload ID (null 허용 - 신규 엔티티)
     * @param uploadSessionId Upload Session ID
     * @param clock 시간 제공자
     * @throws IllegalArgumentException uploadSessionId가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    MultipartUpload(MultipartUploadId id, UploadSessionId uploadSessionId, Clock clock) {
        if (uploadSessionId == null) {
            throw new InvalidUploadRequestException("uploadSessionId", "Upload Session ID는 필수입니다");
        }
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.clock = clock;
        this.status = MultipartStatus.INIT;
        this.uploadedParts = new ArrayList<>();
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param clock 시간 제공자
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param uploadedParts 업로드된 파트 목록
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private MultipartUpload(
        MultipartUploadId id,
        UploadSessionId uploadSessionId,
        Clock clock,
        ProviderUploadId providerUploadId,
        MultipartStatus status,
        TotalParts totalParts,
        List<UploadPart> uploadedParts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt
    ) {
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.clock = clock;
        this.providerUploadId = providerUploadId;
        this.status = status;
        this.totalParts = totalParts;
        this.uploadedParts = new ArrayList<>(uploadedParts);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.abortedAt = abortedAt;
    }

    /**
     * 신규 Multipart Upload를 생성합니다 (Static Factory Method).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>초기 상태: INIT, ID = null</p>
     *
     * <p><strong>사용 시기</strong>: Application Layer에서 Command를 받아 새로운 Entity를 생성할 때</p>
     *
     * @param uploadSessionId Upload Session ID
     * @return 생성된 MultipartUpload (ID = null)
     * @throws IllegalArgumentException Upload Session ID가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static MultipartUpload forNew(UploadSessionId uploadSessionId) {
        return new MultipartUpload(null, uploadSessionId, Clock.systemDefaultZone());
    }

    /**
     * DB에서 조회한 데이터로 MultipartUpload 재구성 (Static Factory Method)
     *
     * <p><strong>Persistence Layer → Domain Layer 변환 전용</strong></p>
     * <p>DB에서 조회한 데이터를 Domain 객체로 복원할 때 사용합니다.</p>
     * <p>모든 상태(status, totalParts, uploadedParts 포함)를 그대로 복원합니다.</p>
     *
     * <p><strong>사용 시기</strong>: Persistence Layer에서 JPA Entity → Domain 변환 시</p>
     *
     * @param id Multipart Upload ID (필수 - DB에서 조회된 ID)
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param uploadedParts 업로드된 파트 목록
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @return 재구성된 MultipartUpload
     * @throws IllegalArgumentException id가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static MultipartUpload reconstitute(
        MultipartUploadId id,
        UploadSessionId uploadSessionId,
        ProviderUploadId providerUploadId,
        MultipartStatus status,
        TotalParts totalParts,
        List<UploadPart> uploadedParts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt
    ) {
        if (id == null) {
            throw new InvalidUploadRequestException("id", "DB reconstitute는 ID가 필수입니다");
        }
        return new MultipartUpload(
            id,
            uploadSessionId,
            Clock.systemDefaultZone(),
            providerUploadId,
            status,
            totalParts,
            uploadedParts,
            createdAt,
            updatedAt,
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
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void initiate(ProviderUploadId providerUploadId, TotalParts totalParts) {
        validateInitiation();

        if (providerUploadId == null) {
            throw new InvalidUploadRequestException("providerUploadId", "Provider Upload ID는 필수입니다");
        }
        if (totalParts == null) {
            throw new InvalidUploadRequestException("totalParts", "Total Parts는 필수입니다");
        }

        this.providerUploadId = providerUploadId;
        this.totalParts = totalParts;
        this.status = MultipartStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 파트 추가
     * 파트 번호 중복 및 순서 검증
     *
     * @param part 업로드된 파트
     * @throws IllegalArgumentException part가 null이거나 중복된 경우
     * @throws IllegalStateException 진행 중 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void addPart(UploadPart part) {
        validatePartAddition(part);
        this.uploadedParts.add(part);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Multipart 업로드 완료
     * 상태: IN_PROGRESS → COMPLETED
     *
     * @throws IllegalStateException 완료 조건을 만족하지 않는 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void complete() {
        if (!canComplete()) {
            int uploadedCount = this.uploadedParts.size();
            int totalCount = this.totalParts != null ? this.totalParts.value() : 0;
            throw new IncompleteMultipartUploadException(uploadedCount, totalCount);
        }
        this.status = MultipartStatus.COMPLETED;
        this.completedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Multipart 업로드 중단
     * 상태: * → ABORTED
     *
     * @throws IllegalStateException 이미 완료된 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void abort() {
        if (this.status == MultipartStatus.COMPLETED) {
            String sessionKey = this.uploadSessionId != null ?
                String.valueOf(this.uploadSessionId.value()) : "unknown";
            throw new UploadAlreadyCompletedException(sessionKey, "abort()");
        }
        this.status = MultipartStatus.ABORTED;
        this.abortedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Multipart 업로드 실패
     * 상태: * → FAILED
     *
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void fail() {
        this.status = MultipartStatus.FAILED;
        this.updatedAt = LocalDateTime.now(clock);
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
            throw new InvalidUploadRequestException(
                "status",
                "이미 초기화된 업로드입니다: " + status
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
            throw new InvalidUploadRequestException("part", "Part cannot be null");
        }

        if (this.status != MultipartStatus.IN_PROGRESS) {
            String sessionKey = this.uploadSessionId != null ?
                String.valueOf(this.uploadSessionId.value()) : "unknown";
            throw new MultipartNotInitializedException(sessionKey, "addPart()");
        }

        PartNumber partNumber = part.getPartNumber();
        boolean duplicate = uploadedParts.stream()
            .anyMatch(p -> p.getPartNumber().equals(partNumber));

        if (duplicate) {
            throw new DuplicatePartNumberException(partNumber.value());
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
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getId() {
        return id != null ? id.value() : null;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    /**
     * Upload Session ID를 반환합니다.
     *
     * @return Upload Session ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public UploadSessionId getUploadSessionId() {
        return uploadSessionId;
    }

    public Long getUploadSessionIdValue() {
        return uploadSessionId.value();
    }

    /**
     * Provider Upload ID를 반환합니다.
     *
     * @return Provider Upload ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ProviderUploadId getProviderUploadId() {
        return providerUploadId;
    }

    public String getProviderUploadIdValue() {
        return providerUploadId != null ? providerUploadId.value() : null;
    }

    /**
     * 시작 시간을 반환합니다.
     *
     * @return 시작 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getStartedAt() {
        return createdAt;
    }


    public MultipartStatus getStatus() {
        return status;
    }


    public TotalParts getTotalParts() {
        return totalParts;
    }


    public List<UploadPart> getUploadedParts() {
        return Collections.unmodifiableList(uploadedParts);
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public LocalDateTime getCompletedAt() {
        return completedAt;
    }


    public LocalDateTime getAbortedAt() {
        return abortedAt;
    }

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
