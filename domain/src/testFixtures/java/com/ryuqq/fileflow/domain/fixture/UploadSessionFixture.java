package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.vo.*;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * UploadSession Aggregate TestFixture (Object Mother 패턴)
 */
public class UploadSessionFixture {

    /**
     * UploadSession.forNew() 팩토리 메서드 (영속화 전, ID null)
     * <p>
     * 기본값으로 UploadSession을 생성합니다. INITIATED 상태, UUID v7 자동 생성.
     * </p>
     *
     * @return 생성된 UploadSession Aggregate (ID null)
     */
    public static UploadSession forNew() {
        FileName fileName = FileName.of("test-file.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L); // 50MB (SINGLE)
        MimeType mimeType = MimeType.of("image/jpeg");
        return UploadSession.forNew(fileName, fileSize, mimeType, Clock.systemUTC());
    }

    /**
     * UploadSession.of() 팩토리 메서드 (ID 필수, 비즈니스 로직용)
     * <p>
     * 모든 필드를 커스터마이징할 수 있는 팩토리 메서드입니다.
     * </p>
     *
     * @param sessionId  세션 ID (필수, null 불가)
     * @param fileName   파일명
     * @param fileSize   파일 크기
     * @param mimeType   MIME 타입
     * @param uploadType 업로드 타입
     * @param status     세션 상태
     * @return 생성된 UploadSession Aggregate
     */
    public static UploadSession of(
            SessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            UploadType uploadType,
            SessionStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(5);

        return UploadSession.of(
                sessionId,
                null, // tenantId
                fileName,
                fileSize,
                mimeType,
                uploadType,
                null, // multipartUpload
                null, // checksum
                null, // etag
                null, // presignedUrl
                expiresAt,
                status,
                Clock.systemUTC(),
                now, // createdAt
                now  // updatedAt
        );
    }

    /**
     * UploadSession.reconstitute() 팩토리 메서드 (영속성 복원용)
     * <p>
     * 모든 필드를 지정하여 UploadSession을 복원합니다.
     * </p>
     *
     * @param sessionId       세션 ID (필수, null 불가)
     * @param tenantId        테넌트 ID
     * @param fileName        파일명
     * @param fileSize        파일 크기
     * @param mimeType        MIME 타입
     * @param uploadType      업로드 타입
     * @param multipartUpload 멀티파트 업로드 정보
     * @param checksum        체크섬
     * @param etag            ETag
     * @param presignedUrl    Presigned URL
     * @param expiresAt       만료 시각
     * @param status          세션 상태
     * @param createdAt       생성 시각
     * @param updatedAt       수정 시각
     * @return 복원된 UploadSession Aggregate
     */
    public static UploadSession reconstitute(
            SessionId sessionId,
            TenantId tenantId,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            UploadType uploadType,
            MultipartUpload multipartUpload,
            Checksum checksum,
            ETag etag,
            String presignedUrl,
            LocalDateTime expiresAt,
            SessionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return UploadSession.reconstitute(
                sessionId,
                tenantId,
                fileName,
                fileSize,
                mimeType,
                uploadType,
                multipartUpload,
                checksum,
                etag,
                presignedUrl,
                expiresAt,
                status,
                Clock.systemUTC(),
                createdAt,
                updatedAt
        );
    }

    /**
     * SINGLE 업로드 타입 세션 (50MB)
     */
    public static UploadSession aSingleUploadSession() {
        FileName fileName = FileName.of("small-file.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L); // 50MB
        MimeType mimeType = MimeType.of("image/jpeg");
        return UploadSession.forNew(fileName, fileSize, mimeType, Clock.systemUTC());
    }

    /**
     * MULTIPART 업로드 타입 세션 (200MB)
     */
    public static UploadSession aMultipartUploadSession() {
        FileName fileName = FileName.of("large-file.pdf");
        FileSize fileSize = FileSize.of(200 * 1024 * 1024L); // 200MB
        MimeType mimeType = MimeType.of("application/pdf");
        return UploadSession.forNew(fileName, fileSize, mimeType, Clock.systemUTC());
    }

    /**
     * IN_PROGRESS 상태 세션
     */
    public static UploadSession anInProgressSession() {
        UploadSession session = forNew();
        session.updateToInProgress();
        return session;
    }

    /**
     * COMPLETED 상태 세션
     */
    public static UploadSession aCompletedSession() {
        UploadSession session = forNew();
        session.updateToInProgress();
        session.completeWithETag(ETag.of("abc123def456"));
        return session;
    }

    /**
     * EXPIRED 상태 세션
     */
    public static UploadSession anExpiredSession() {
        UploadSession session = forNew();
        session.updateToExpired();
        return session;
    }

    /**
     * FAILED 상태 세션
     */
    public static UploadSession aFailedSession() {
        UploadSession session = forNew();
        session.fail("Upload failed");
        return session;
    }
}
