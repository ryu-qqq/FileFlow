package com.ryuqq.fileflow.application.session.scheduler;

import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Multipart Upload Session 만료 스케줄러.
 *
 * <p>Redis TTL 만료 이벤트를 놓친 세션을 주기적으로 정리합니다.
 *
 * <p><strong>실행 주기</strong>: 1시간
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>만료 시간이 지난 PREPARING/ACTIVE 상태의 세션 조회
 *   <li>각 세션에 대해 ExpireUploadSessionUseCase 호출
 *   <li>S3 AbortMultipartUpload 처리
 *   <li>실패한 세션은 로깅 후 다음 주기에 재시도
 * </ol>
 */
@Component
public class MultipartUploadSessionExpirationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(MultipartUploadSessionExpirationScheduler.class);

    private static final int BATCH_SIZE = 100;

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final ExpireUploadSessionUseCase expireUploadSessionUseCase;

    public MultipartUploadSessionExpirationScheduler(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            ExpireUploadSessionUseCase expireUploadSessionUseCase) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.expireUploadSessionUseCase = expireUploadSessionUseCase;
    }

    /**
     * 만료된 멀티파트 업로드 세션을 정리합니다.
     *
     * <p>1시간마다 실행됩니다.
     */
    @Scheduled(fixedRate = 3600000) // 1시간
    public void expireStalessMultipartUploadSessions() {
        log.info("Starting multipart upload session expiration cleanup");

        Instant now = Instant.now();
        int totalExpired = 0;
        int totalFailed = 0;

        List<MultipartUploadSession> expiredSessions =
                findUploadSessionQueryPort.findExpiredMultipartUploads(now, BATCH_SIZE);

        while (!expiredSessions.isEmpty()) {
            for (MultipartUploadSession session : expiredSessions) {
                try {
                    ExpireUploadSessionCommand command =
                            ExpireUploadSessionCommand.of(session.getId().value().toString());
                    expireUploadSessionUseCase.execute(command);
                    totalExpired++;
                    log.debug(
                            "Expired multipart upload session: {}",
                            session.getId().value().toString());
                } catch (Exception e) {
                    totalFailed++;
                    log.warn(
                            "Failed to expire multipart upload session: {}. Reason: {}",
                            session.getId().value().toString(),
                            e.getMessage());
                }
            }

            if (expiredSessions.size() < BATCH_SIZE) {
                break;
            }

            expiredSessions =
                    findUploadSessionQueryPort.findExpiredMultipartUploads(now, BATCH_SIZE);
        }

        log.info(
                "Multipart upload session expiration cleanup completed. Expired: {}, Failed: {}",
                totalExpired,
                totalFailed);
    }
}
