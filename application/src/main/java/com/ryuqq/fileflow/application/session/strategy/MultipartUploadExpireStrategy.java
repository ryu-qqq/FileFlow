package com.ryuqq.fileflow.application.session.strategy;

import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Component;

/**
 * Multipart 업로드 세션 만료 전략.
 *
 * <p>Multipart 업로드는 S3에 Part들이 임시 저장되어 있으므로 Domain 만료 + S3 정리를 수행합니다.
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>Domain에서 상태 전환 (PREPARING/ACTIVE → EXPIRED)
 *   <li>S3 AbortMultipartUpload API 호출로 임시 Part들 삭제
 * </ul>
 *
 * <p><strong>S3 정리가 필요한 이유</strong>:
 *
 * <ul>
 *   <li>Multipart Upload는 Complete 전까지 Part들이 S3에 임시 저장됨
 *   <li>정리하지 않으면 S3 스토리지 비용이 누적됨
 * </ul>
 */
@Component
public class MultipartUploadExpireStrategy implements ExpireStrategy<MultipartUploadSession> {

    private final UploadSessionManager uploadSessionManager;
    private final S3ClientPort s3ClientPort;

    public MultipartUploadExpireStrategy(
            UploadSessionManager uploadSessionManager, S3ClientPort s3ClientPort) {
        this.uploadSessionManager = uploadSessionManager;
        this.s3ClientPort = s3ClientPort;
    }

    @Override
    public void expire(MultipartUploadSession session) {
        // 1. Domain 만료 처리
        session.expire();

        // 2. S3 Part 정리
        s3ClientPort.abortMultipartUpload(
                session.getBucket(), session.getS3Key(), session.getS3UploadIdValue());

        uploadSessionManager.save(session);
    }
}
