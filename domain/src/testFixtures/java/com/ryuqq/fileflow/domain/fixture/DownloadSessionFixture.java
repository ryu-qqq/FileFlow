package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.DownloadSession;
import com.ryuqq.fileflow.domain.vo.*;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * DownloadSession Aggregate TestFixture (Object Mother 패턴)
 */
public class DownloadSessionFixture {

    /**
     * DownloadSession.forNew() 팩토리 메서드 (영속화 전, ID null)
     * <p>
     * 기본값으로 DownloadSession을 생성합니다. INITIATED 상태, UUID v7 자동 생성.
     * </p>
     *
     * @return 생성된 DownloadSession Aggregate (ID null)
     */
    public static DownloadSession forNew() {
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/test-file.pdf");
        FileName fileName = FileName.of("test-file.pdf");
        return DownloadSession.forNew(externalUrl, fileName, Clock.systemUTC());
    }

    /**
     * DownloadSession.of() 팩토리 메서드 (ID 필수, 비즈니스 로직용)
     * <p>
     * 모든 필드를 커스터마이징할 수 있는 팩토리 메서드입니다.
     * </p>
     *
     * @param sessionId   세션 ID (필수, null 불가)
     * @param externalUrl 외부 URL
     * @param fileName    파일명
     * @param fileSize    파일 크기
     * @param mimeType    MIME 타입
     * @param status      세션 상태
     * @return 생성된 DownloadSession Aggregate
     */
    public static DownloadSession of(
            SessionId sessionId,
            ExternalUrl externalUrl,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            SessionStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(60);

        return DownloadSession.of(
                sessionId,
                null, // tenantId
                externalUrl,
                fileName,
                fileSize,
                mimeType,
                null, // checksum
                null, // etag
                RetryCount.forFile(),
                expiresAt,
                status,
                Clock.systemUTC(),
                now, // createdAt
                now  // updatedAt
        );
    }

    /**
     * DownloadSession.reconstitute() 팩토리 메서드 (영속성 복원용)
     * <p>
     * 모든 필드를 지정하여 DownloadSession을 복원합니다.
     * </p>
     *
     * @param sessionId   세션 ID (필수, null 불가)
     * @param tenantId    테넌트 ID
     * @param externalUrl 외부 URL
     * @param fileName    파일명
     * @param fileSize    파일 크기
     * @param mimeType    MIME 타입
     * @param checksum    체크섬
     * @param etag        ETag
     * @param retryCount  재시도 횟수
     * @param expiresAt   만료 시각
     * @param status      세션 상태
     * @param createdAt   생성 시각
     * @param updatedAt   수정 시각
     * @return 복원된 DownloadSession Aggregate
     */
    public static DownloadSession reconstitute(
            SessionId sessionId,
            TenantId tenantId,
            ExternalUrl externalUrl,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            Checksum checksum,
            ETag etag,
            RetryCount retryCount,
            LocalDateTime expiresAt,
            SessionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return DownloadSession.reconstitute(
                sessionId,
                tenantId,
                externalUrl,
                fileName,
                fileSize,
                mimeType,
                checksum,
                etag,
                retryCount,
                expiresAt,
                status,
                Clock.systemUTC(),
                createdAt,
                updatedAt
        );
    }

    /**
     * PDF 다운로드 세션
     */
    public static DownloadSession aPdfDownloadSession() {
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/document.pdf");
        FileName fileName = FileName.of("document.pdf");
        return DownloadSession.forNew(externalUrl, fileName, Clock.systemUTC());
    }

    /**
     * 이미지 다운로드 세션
     */
    public static DownloadSession anImageDownloadSession() {
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/image.jpg");
        FileName fileName = FileName.of("image.jpg");
        return DownloadSession.forNew(externalUrl, fileName, Clock.systemUTC());
    }

    /**
     * IN_PROGRESS 상태 세션
     */
    public static DownloadSession anInProgressSession() {
        DownloadSession session = forNew();
        session.updateToInProgress();
        return session;
    }

    /**
     * COMPLETED 상태 세션
     */
    public static DownloadSession aCompletedSession() {
        DownloadSession session = forNew();
        session.updateToInProgress();
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L); // 10MB
        MimeType mimeType = MimeType.of("application/pdf");
        ETag etag = ETag.of("abc123def456");
        session.completeWithFileInfo(fileSize, mimeType, etag);
        return session;
    }

    /**
     * EXPIRED 상태 세션
     */
    public static DownloadSession anExpiredSession() {
        DownloadSession session = forNew();
        session.updateToExpired();
        return session;
    }

    /**
     * FAILED 상태 세션
     */
    public static DownloadSession aFailedSession() {
        DownloadSession session = forNew();
        session.fail("Download failed");
        return session;
    }

    /**
     * 재시도가 있는 세션 (1회 재시도)
     */
    public static DownloadSession aSessionWithRetry() {
        DownloadSession session = forNew();
        session.incrementRetryCount();
        return session;
    }

    /**
     * 최대 재시도 횟수 도달 세션 (3회 재시도)
     */
    public static DownloadSession aSessionWithMaxRetries() {
        DownloadSession session = forNew();
        session.incrementRetryCount();
        session.incrementRetryCount();
        session.incrementRetryCount();
        return session;
    }
}
