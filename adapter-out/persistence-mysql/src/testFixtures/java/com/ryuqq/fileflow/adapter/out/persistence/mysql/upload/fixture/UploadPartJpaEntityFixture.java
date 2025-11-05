package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;

import java.time.LocalDateTime;

/**
 * UploadPartJpaEntity Test Fixture
 *
 * <p>테스트에서 UploadPartJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성
 * UploadPartJpaEntity part = UploadPartJpaEntityFixture.create();
 *
 * // ID 포함 생성
 * UploadPartJpaEntity part = UploadPartJpaEntityFixture.createWithId(1L);
 *
 * // 커스텀 생성
 * UploadPartJpaEntity part = UploadPartJpaEntityFixture.create(1L, 1);
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadPartJpaEntityFixture {

    private static final Long DEFAULT_MULTIPART_UPLOAD_ID = 1L;
    private static final Integer DEFAULT_PART_NUMBER = 1;
    private static final String DEFAULT_ETAG = "d41d8cd98f00b204e9800998ecf8427e";
    private static final Long DEFAULT_SIZE = 5242880L; // 5MB
    private static final String DEFAULT_CHECKSUM = "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final LocalDateTime DEFAULT_UPLOADED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private UploadPartJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 UploadPartJpaEntity 생성 (ID 없음)
     *
     * <p>신규 생성 시나리오 테스트에 사용합니다.</p>
     *
     * @return 새로운 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity create() {
        return UploadPartJpaEntity.create(
            DEFAULT_MULTIPART_UPLOAD_ID,
            DEFAULT_PART_NUMBER,
            DEFAULT_ETAG,
            DEFAULT_SIZE,
            DEFAULT_CHECKSUM,
            DEFAULT_UPLOADED_AT
        );
    }

    /**
     * 커스텀 UploadPartJpaEntity 생성 (ID 없음)
     *
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @return 새로운 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity create(Long multipartUploadId, Integer partNumber) {
        return UploadPartJpaEntity.create(
            multipartUploadId,
            partNumber,
            DEFAULT_ETAG,
            DEFAULT_SIZE,
            DEFAULT_CHECKSUM,
            DEFAULT_UPLOADED_AT
        );
    }

    /**
     * ID를 포함한 UploadPartJpaEntity 생성 (재구성)
     *
     * <p>DB 조회 시나리오 테스트에 사용합니다.</p>
     *
     * @param id Upload Part ID
     * @return 재구성된 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity createWithId(Long id) {
        return UploadPartJpaEntity.reconstitute(
            id,
            DEFAULT_MULTIPART_UPLOAD_ID,
            DEFAULT_PART_NUMBER,
            DEFAULT_ETAG,
            DEFAULT_SIZE,
            DEFAULT_CHECKSUM,
            DEFAULT_UPLOADED_AT,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 커스텀 ID를 포함한 UploadPartJpaEntity 생성 (재구성)
     *
     * @param id Upload Part ID
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @return 재구성된 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity createWithId(Long id, Long multipartUploadId, Integer partNumber) {
        return UploadPartJpaEntity.reconstitute(
            id,
            multipartUploadId,
            partNumber,
            DEFAULT_ETAG,
            DEFAULT_SIZE,
            DEFAULT_CHECKSUM,
            DEFAULT_UPLOADED_AT,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 특정 크기의 UploadPartJpaEntity 생성 (재구성)
     *
     * @param id Upload Part ID
     * @param size 파트 크기
     * @return 재구성된 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity createWithSize(Long id, Long size) {
        return UploadPartJpaEntity.reconstitute(
            id,
            DEFAULT_MULTIPART_UPLOAD_ID,
            DEFAULT_PART_NUMBER,
            DEFAULT_ETAG,
            size,
            DEFAULT_CHECKSUM,
            DEFAULT_UPLOADED_AT,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 여러 개의 UploadPartJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return UploadPartJpaEntity 배열
     */
    public static UploadPartJpaEntity[] createMultiple(int count) {
        UploadPartJpaEntity[] entities = new UploadPartJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_MULTIPART_UPLOAD_ID,
                i + 1
            );
        }
        return entities;
    }

    /**
     * 특정 Multipart Upload에 속한 여러 파트 생성 (재구성)
     *
     * @param multipartUploadId Multipart Upload ID
     * @param count 생성할 개수
     * @return UploadPartJpaEntity 배열
     */
    public static UploadPartJpaEntity[] createMultipleForUpload(Long multipartUploadId, int count) {
        UploadPartJpaEntity[] entities = new UploadPartJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                multipartUploadId,
                i + 1
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 UploadPartJpaEntity 생성 (재구성)
     *
     * @param id Upload Part ID
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @param etag ETag
     * @param size 파트 크기
     * @param checksum 체크섬
     * @param uploadedAt 업로드 완료 시간
     * @param createdAt 생성 시간
     * @return 재구성된 UploadPartJpaEntity
     */
    public static UploadPartJpaEntity reconstitute(
        Long id,
        Long multipartUploadId,
        Integer partNumber,
        String etag,
        Long size,
        String checksum,
        LocalDateTime uploadedAt,
        LocalDateTime createdAt
    ) {
        return UploadPartJpaEntity.reconstitute(
            id,
            multipartUploadId,
            partNumber,
            etag,
            size,
            checksum,
            uploadedAt,
            createdAt
        );
    }
}
