package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;

import java.time.LocalDateTime;

/**
 * MultipartUploadJpaEntity Test Fixture
 *
 * <p>테스트에서 MultipartUploadJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성
 * MultipartUploadJpaEntity upload = MultipartUploadJpaEntityFixture.create();
 *
 * // ID 포함 생성
 * MultipartUploadJpaEntity upload = MultipartUploadJpaEntityFixture.createWithId(1L);
 *
 * // 커스텀 생성
 * MultipartUploadJpaEntity upload = MultipartUploadJpaEntityFixture.create(1L, "upload-123");
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MultipartUploadJpaEntityFixture {

    private static final Long DEFAULT_UPLOAD_SESSION_ID = 1L;
    private static final String DEFAULT_PROVIDER_UPLOAD_ID = "aws-multipart-upload-123";
    private static final MultipartUpload.MultipartStatus DEFAULT_STATUS = MultipartUpload.MultipartStatus.IN_PROGRESS;
    private static final Integer DEFAULT_TOTAL_PARTS = 10;
    private static final LocalDateTime DEFAULT_STARTED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private MultipartUploadJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 MultipartUploadJpaEntity 생성 (ID 없음)
     *
     * <p>신규 생성 시나리오 테스트에 사용합니다.</p>
     *
     * @return 새로운 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity create() {
        return MultipartUploadJpaEntity.create(
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_PROVIDER_UPLOAD_ID,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PARTS,
            DEFAULT_STARTED_AT
        );
    }

    /**
     * 커스텀 MultipartUploadJpaEntity 생성 (ID 없음)
     *
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @return 새로운 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity create(Long uploadSessionId, String providerUploadId) {
        return MultipartUploadJpaEntity.create(
            uploadSessionId,
            providerUploadId,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PARTS,
            DEFAULT_STARTED_AT
        );
    }

    /**
     * ID를 포함한 MultipartUploadJpaEntity 생성 (재구성)
     *
     * <p>DB 조회 시나리오 테스트에 사용합니다.</p>
     *
     * @param id Multipart Upload ID
     * @return 재구성된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity createWithId(Long id) {
        return MultipartUploadJpaEntity.reconstitute(
            id,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_PROVIDER_UPLOAD_ID,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PARTS,
            DEFAULT_STARTED_AT,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 커스텀 ID를 포함한 MultipartUploadJpaEntity 생성 (재구성)
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @return 재구성된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity createWithId(Long id, Long uploadSessionId, String providerUploadId) {
        return MultipartUploadJpaEntity.reconstitute(
            id,
            uploadSessionId,
            providerUploadId,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PARTS,
            DEFAULT_STARTED_AT,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 특정 상태의 MultipartUploadJpaEntity 생성 (재구성)
     *
     * @param id Multipart Upload ID
     * @param status Multipart Status
     * @return 재구성된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity createWithStatus(Long id, MultipartUpload.MultipartStatus status) {
        return MultipartUploadJpaEntity.reconstitute(
            id,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_PROVIDER_UPLOAD_ID,
            status,
            DEFAULT_TOTAL_PARTS,
            DEFAULT_STARTED_AT,
            status == MultipartUpload.MultipartStatus.COMPLETED ? LocalDateTime.now() : null,
            status == MultipartUpload.MultipartStatus.ABORTED ? LocalDateTime.now() : null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 완료된 MultipartUploadJpaEntity 생성 (재구성)
     *
     * @param id Multipart Upload ID
     * @return 완료된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity createCompleted(Long id) {
        return createWithStatus(id, MultipartUpload.MultipartStatus.COMPLETED);
    }

    /**
     * 중단된 MultipartUploadJpaEntity 생성 (재구성)
     *
     * @param id Multipart Upload ID
     * @return 중단된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity createAborted(Long id) {
        return createWithStatus(id, MultipartUpload.MultipartStatus.ABORTED);
    }

    /**
     * 여러 개의 MultipartUploadJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return MultipartUploadJpaEntity 배열
     */
    public static MultipartUploadJpaEntity[] createMultiple(int count) {
        MultipartUploadJpaEntity[] entities = new MultipartUploadJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_UPLOAD_SESSION_ID,
                DEFAULT_PROVIDER_UPLOAD_ID + "-" + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 MultipartUploadJpaEntity 생성 (재구성)
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 총 파트 수
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return 재구성된 MultipartUploadJpaEntity
     */
    public static MultipartUploadJpaEntity reconstitute(
        Long id,
        Long uploadSessionId,
        String providerUploadId,
        MultipartUpload.MultipartStatus status,
        Integer totalParts,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime abortedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return MultipartUploadJpaEntity.reconstitute(
            id,
            uploadSessionId,
            providerUploadId,
            status,
            totalParts,
            startedAt,
            completedAt,
            abortedAt,
            createdAt,
            updatedAt
        );
    }
}
