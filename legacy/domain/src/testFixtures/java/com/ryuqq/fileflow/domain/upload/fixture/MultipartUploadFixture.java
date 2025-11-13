package com.ryuqq.fileflow.domain.upload.fixture;

import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.MultipartUpload.MultipartStatus;
import com.ryuqq.fileflow.domain.upload.MultipartUploadId;
import com.ryuqq.fileflow.domain.upload.ProviderUploadId;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MultipartUpload Test Fixture
 *
 * <p>테스트에서 MultipartUpload 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
public class MultipartUploadFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private MultipartUploadFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final UploadSessionId DEFAULT_UPLOAD_SESSION_ID = UploadSessionId.of(1L);
    private static final ProviderUploadId DEFAULT_PROVIDER_UPLOAD_ID = ProviderUploadId.of("aws-s3-upload-id-12345");
    private static final TotalParts DEFAULT_TOTAL_PARTS = TotalParts.of(3);

    /**
     * 초기화되지 않은 신규 MultipartUpload 생성 (INIT 상태)
     *
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createNew() {
        return MultipartUpload.forNew(DEFAULT_UPLOAD_SESSION_ID);
    }

    /**
     * 특정 uploadSessionId로 신규 MultipartUpload 생성
     *
     * @param uploadSessionId Upload Session ID
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createNew(UploadSessionId uploadSessionId) {
        return MultipartUpload.forNew(uploadSessionId);
    }

    /**
     * 초기화된 MultipartUpload 생성 (IN_PROGRESS 상태)
     *
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createInitiated() {
        MultipartUpload upload = MultipartUpload.forNew(DEFAULT_UPLOAD_SESSION_ID);
        upload.initiate(DEFAULT_PROVIDER_UPLOAD_ID, DEFAULT_TOTAL_PARTS);
        return upload;
    }

    /**
     * 특정 값으로 초기화된 MultipartUpload 생성
     *
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param totalParts 전체 파트 수
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createInitiated(
        UploadSessionId uploadSessionId,
        ProviderUploadId providerUploadId,
        TotalParts totalParts
    ) {
        MultipartUpload upload = MultipartUpload.forNew(uploadSessionId);
        upload.initiate(providerUploadId, totalParts);
        return upload;
    }

    /**
     * 일부 파트가 업로드된 MultipartUpload 생성
     *
     * @param partsCount 업로드된 파트 개수
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createWithParts(int partsCount) {
        MultipartUpload upload = MultipartUpload.forNew(DEFAULT_UPLOAD_SESSION_ID);
        upload.initiate(DEFAULT_PROVIDER_UPLOAD_ID, DEFAULT_TOTAL_PARTS);

        for (int i = 1; i <= partsCount; i++) {
            upload.addPart(UploadPartFixture.create(i, 5242880L));
        }

        return upload;
    }

    /**
     * 완료된 MultipartUpload 생성 (COMPLETED 상태)
     *
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createCompleted() {
        MultipartUpload upload = MultipartUpload.forNew(DEFAULT_UPLOAD_SESSION_ID);
        upload.initiate(DEFAULT_PROVIDER_UPLOAD_ID, DEFAULT_TOTAL_PARTS);

        // 모든 파트 추가
        for (int i = 1; i <= DEFAULT_TOTAL_PARTS.value(); i++) {
            upload.addPart(UploadPartFixture.create(i, 5242880L));
        }

        upload.complete();
        return upload;
    }

    /**
     * 중단된 MultipartUpload 생성 (ABORTED 상태)
     *
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createAborted() {
        MultipartUpload upload = MultipartUpload.forNew(DEFAULT_UPLOAD_SESSION_ID);
        upload.initiate(DEFAULT_PROVIDER_UPLOAD_ID, DEFAULT_TOTAL_PARTS);
        upload.abort();
        return upload;
    }

    /**
     * 실패한 MultipartUpload 생성 (FAILED 상태)
     *
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload createFailed() {
        MultipartUpload upload = MultipartUpload.forNew(DEFAULT_UPLOAD_SESSION_ID);
        upload.initiate(DEFAULT_PROVIDER_UPLOAD_ID, DEFAULT_TOTAL_PARTS);
        upload.fail();
        return upload;
    }

    /**
     * DB에서 복원한 MultipartUpload 생성 (Reconstitute)
     *
     * @param id Multipart Upload ID
     * @param uploadSessionId Upload Session ID
     * @param providerUploadId Provider Upload ID
     * @param status 상태
     * @param totalParts 전체 파트 수
     * @param uploadedParts 업로드된 파트 리스트
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @param completedAt 완료 시간
     * @param abortedAt 중단 시간
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload reconstitute(
        Long id,
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
        return MultipartUpload.reconstitute(
            MultipartUploadId.of(id),
            uploadSessionId,
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
     * 기본값으로 Reconstitute된 MultipartUpload 생성
     *
     * @param id Multipart Upload ID
     * @return MultipartUpload 인스턴스
     */
    public static MultipartUpload reconstituteDefault(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return MultipartUpload.reconstitute(
            MultipartUploadId.of(id),
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_PROVIDER_UPLOAD_ID,
            MultipartStatus.IN_PROGRESS,
            DEFAULT_TOTAL_PARTS,
            List.of(),
            now,
            now,
            null,
            null
        );
    }

    /**
     * Builder 패턴으로 MultipartUpload 생성
     *
     * @return MultipartUploadBuilder 인스턴스
     */
    public static MultipartUploadBuilder builder() {
        return new MultipartUploadBuilder();
    }

    /**
     * MultipartUpload Builder
     */
    public static class MultipartUploadBuilder {
        private UploadSessionId uploadSessionId = DEFAULT_UPLOAD_SESSION_ID;
        private ProviderUploadId providerUploadId = DEFAULT_PROVIDER_UPLOAD_ID;
        private TotalParts totalParts = DEFAULT_TOTAL_PARTS;
        private boolean shouldInitiate = false;
        private int addPartsCount = 0;
        private boolean shouldComplete = false;
        private boolean shouldAbort = false;
        private boolean shouldFail = false;

        public MultipartUploadBuilder uploadSessionId(UploadSessionId uploadSessionId) {
            this.uploadSessionId = uploadSessionId;
            return this;
        }

        public MultipartUploadBuilder providerUploadId(ProviderUploadId providerUploadId) {
            this.providerUploadId = providerUploadId;
            return this;
        }

        public MultipartUploadBuilder totalParts(TotalParts totalParts) {
            this.totalParts = totalParts;
            return this;
        }

        public MultipartUploadBuilder initiate() {
            this.shouldInitiate = true;
            return this;
        }

        public MultipartUploadBuilder addParts(int count) {
            this.addPartsCount = count;
            return this;
        }

        public MultipartUploadBuilder complete() {
            this.shouldComplete = true;
            return this;
        }

        public MultipartUploadBuilder abort() {
            this.shouldAbort = true;
            return this;
        }

        public MultipartUploadBuilder fail() {
            this.shouldFail = true;
            return this;
        }

        public MultipartUpload build() {
            MultipartUpload upload = MultipartUpload.forNew(uploadSessionId);

            if (shouldInitiate) {
                upload.initiate(providerUploadId, totalParts);
            }

            if (addPartsCount > 0) {
                for (int i = 1; i <= addPartsCount; i++) {
                    upload.addPart(UploadPartFixture.create(i, 5242880L));
                }
            }

            if (shouldComplete) {
                upload.complete();
            } else if (shouldAbort) {
                upload.abort();
            } else if (shouldFail) {
                upload.fail();
            }

            return upload;
        }
    }
}
