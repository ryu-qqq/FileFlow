package com.ryuqq.fileflow.domain.upload.fixture;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.FailureReason;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.UploadType;

import java.time.LocalDateTime;

/**
 * UploadSession Test Fixture
 *
 * <p>테스트에서 UploadSession 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
public class UploadSessionFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private UploadSessionFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final TenantId DEFAULT_TENANT_ID = TenantId.of(1L);
    private static final FileName DEFAULT_FILE_NAME = FileName.of("test-file.txt");
    private static final FileSize DEFAULT_FILE_SIZE = FileSize.of(10485760L); // 10MB

    /**
     * 단일 업로드 세션 생성 (SINGLE 타입, PENDING 상태)
     *
     * @return UploadSession 인스턴스
     */
    public static UploadSession createSingle() {
        return UploadSession.forNew(DEFAULT_TENANT_ID, DEFAULT_FILE_NAME, DEFAULT_FILE_SIZE);
    }

    /**
     * 특정 값으로 단일 업로드 세션 생성
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @return UploadSession 인스턴스
     */
    public static UploadSession createSingle(TenantId tenantId, FileName fileName, FileSize fileSize) {
        return UploadSession.forNew(tenantId, fileName, fileSize);
    }

    /**
     * Multipart 업로드 세션 생성 (MULTIPART 타입, PENDING 상태)
     *
     * @return UploadSession 인스턴스
     */
    public static UploadSession createMultipart() {
        com.ryuqq.fileflow.domain.upload.StorageKey storageKey =
            com.ryuqq.fileflow.domain.upload.StorageKey.of("test/multipart/file.txt");
        return UploadSession.forNewMultipart(DEFAULT_TENANT_ID, DEFAULT_FILE_NAME, DEFAULT_FILE_SIZE, storageKey);
    }

    /**
     * 특정 값으로 Multipart 업로드 세션 생성
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @return UploadSession 인스턴스
     */
    public static UploadSession createMultipart(TenantId tenantId, FileName fileName, FileSize fileSize) {
        com.ryuqq.fileflow.domain.upload.StorageKey storageKey =
            com.ryuqq.fileflow.domain.upload.StorageKey.of("test/multipart/" + fileName.value());
        return UploadSession.forNewMultipart(tenantId, fileName, fileSize, storageKey);
    }

    /**
     * MultipartUpload가 연결된 Multipart 세션 생성
     *
     * @return UploadSession 인스턴스
     */
    public static UploadSession createMultipartWithAttachment() {
        com.ryuqq.fileflow.domain.upload.StorageKey storageKey =
            com.ryuqq.fileflow.domain.upload.StorageKey.of("test/multipart/file.txt");
        UploadSession session = UploadSession.forNewMultipart(
            DEFAULT_TENANT_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            storageKey
        );

        // Note: MultipartUpload를 생성하고 연결하려면 UploadSession의 ID가 필요
        // 실제 테스트에서는 reconstitute를 사용하거나 ID 설정 후 attach 필요
        return session;
    }

    /**
     * 진행 중인 단일 업로드 세션 생성
     *
     * @return UploadSession 인스턴스
     */
    public static UploadSession createSingleInProgress() {
        UploadSession session = UploadSession.forNew(DEFAULT_TENANT_ID, DEFAULT_FILE_NAME, DEFAULT_FILE_SIZE);
        session.start();
        return session;
    }

    /**
     * 완료된 단일 업로드 세션 생성
     *
     * @param fileId 생성된 파일 ID
     * @return UploadSession 인스턴스
     */
    public static UploadSession createSingleCompleted(Long fileId) {
        UploadSession session = UploadSession.forNew(DEFAULT_TENANT_ID, DEFAULT_FILE_NAME, DEFAULT_FILE_SIZE);
        session.start();
        session.complete(fileId);
        return session;
    }

    /**
     * 실패한 업로드 세션 생성
     *
     * @param reason 실패 사유
     * @return UploadSession 인스턴스
     */
    public static UploadSession createFailed(FailureReason reason) {
        UploadSession session = UploadSession.forNew(DEFAULT_TENANT_ID, DEFAULT_FILE_NAME, DEFAULT_FILE_SIZE);
        session.start();
        session.fail(reason);
        return session;
    }

    /**
     * DB에서 복원한 UploadSession 생성 (Reconstitute)
     *
     * @param id 세션 ID
     * @param sessionKey 세션 키
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param uploadType 업로드 타입
     * @param storageKey 스토리지 키
     * @param status 상태
     * @param fileId 파일 ID
     * @param failureReason 실패 사유
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @param completedAt 완료 시간
     * @param failedAt 실패 시간
     * @return UploadSession 인스턴스
     */
    public static UploadSession reconstitute(
        Long id,
        SessionKey sessionKey,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        UploadType uploadType,
        StorageKey storageKey,
        SessionStatus status,
        Long fileId,
        FailureReason failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime failedAt
    ) {
        return UploadSession.reconstitute(
            UploadSessionId.of(id),
            sessionKey,
            tenantId,
            fileName,
            fileSize,
            uploadType,
            storageKey,
            status,
            fileId,
            failureReason,
            createdAt,
            updatedAt,
            completedAt,
            failedAt
        );
    }

    /**
     * 기본값으로 Reconstitute된 UploadSession 생성
     *
     * @param id 세션 ID
     * @return UploadSession 인스턴스
     */
    public static UploadSession reconstituteDefault(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return UploadSession.reconstitute(
            UploadSessionId.of(id),
            SessionKey.of("session-key-" + id),
            DEFAULT_TENANT_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            UploadType.SINGLE,
            null, // storageKey (SINGLE 타입은 null 가능)
            SessionStatus.PENDING,
            null,
            null,
            now,
            now,
            null,
            null
        );
    }

    /**
     * Builder 패턴으로 UploadSession 생성
     *
     * @return UploadSessionBuilder 인스턴스
     */
    public static UploadSessionBuilder builder() {
        return new UploadSessionBuilder();
    }

    /**
     * UploadSession Builder
     */
    public static class UploadSessionBuilder {
        private TenantId tenantId = DEFAULT_TENANT_ID;
        private FileName fileName = DEFAULT_FILE_NAME;
        private FileSize fileSize = DEFAULT_FILE_SIZE;
        private UploadType uploadType = UploadType.SINGLE;
        private boolean shouldStart = false;
        private boolean shouldComplete = false;
        private Long fileId = null;
        private boolean shouldFail = false;
        private FailureReason failureReason = null;

        public UploadSessionBuilder tenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public UploadSessionBuilder fileName(FileName fileName) {
            this.fileName = fileName;
            return this;
        }

        public UploadSessionBuilder fileSize(FileSize fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public UploadSessionBuilder single() {
            this.uploadType = UploadType.SINGLE;
            return this;
        }

        public UploadSessionBuilder multipart() {
            this.uploadType = UploadType.MULTIPART;
            return this;
        }

        public UploadSessionBuilder start() {
            this.shouldStart = true;
            return this;
        }

        public UploadSessionBuilder complete(Long fileId) {
            this.shouldComplete = true;
            this.fileId = fileId;
            return this;
        }

        public UploadSessionBuilder fail(FailureReason reason) {
            this.shouldFail = true;
            this.failureReason = reason;
            return this;
        }

        public UploadSession build() {
            UploadSession session;
            if (uploadType == UploadType.MULTIPART) {
                com.ryuqq.fileflow.domain.upload.StorageKey storageKey =
                    com.ryuqq.fileflow.domain.upload.StorageKey.of("test/multipart/" + fileName.value());
                session = UploadSession.forNewMultipart(tenantId, fileName, fileSize, storageKey);
            } else {
                session = UploadSession.forNew(tenantId, fileName, fileSize);
            }

            if (shouldStart) {
                session.start();
            }

            if (shouldComplete && fileId != null) {
                session.complete(fileId);
            } else if (shouldFail) {
                session.fail(failureReason != null ? failureReason : FailureReason.of("Test failure"));
            }

            return session;
        }
    }
}
