package com.ryuqq.fileflow.application.session.assembler;

import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.ExpireUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * 단일 업로드 세션 Assembler.
 *
 * <p>Application DTO를 Domain Aggregate로 변환합니다.
 */
@Component
public class SingleUploadAssembler {

    private static final Duration SINGLE_UPLOAD_EXPIRATION = Duration.ofMinutes(15);

    private final ClockHolder clockHolder;
    private final Supplier<UserContext> userContextSupplier;

    public SingleUploadAssembler(
            ClockHolder clockHolder, Supplier<UserContext> userContextSupplier) {
        this.clockHolder = clockHolder;
        this.userContextSupplier = userContextSupplier;
    }

    /**
     * InitSingleUploadCommand를 SingleUploadSession으로 변환합니다.
     *
     * @param command 초기화 명령
     * @return 신규 SingleUploadSession (status: PREPARING)
     */
    public SingleUploadSession toDomain(InitSingleUploadCommand command) {
        Clock clock = clockHolder.getClock();

        // 멱등성 키 변환
        IdempotencyKey idempotencyKey = IdempotencyKey.fromString(command.idempotencyKey());

        // ThreadLocal에서 UserContext 조회
        UserContext userContext = userContextSupplier.get();

        FileName fileName = FileName.of(command.fileName());
        FileSize fileSize = FileSize.of(command.fileSize());
        ContentType contentType = ContentType.of(command.contentType());

        // uploadCategory 변환 (String → Enum)
        // Customer는 uploadCategory를 전달하지 않음 (null), Admin/Seller는 필수
        UploadCategory uploadCategory = null;
        if (command.uploadCategory() != null && !command.uploadCategory().isBlank()) {
            uploadCategory = UploadCategory.fromPath(command.uploadCategory());
        }

        // S3 경로 생성 (UserContext 기반)
        S3Bucket bucket = userContext.getS3Bucket();
        S3Key s3Key = userContext.generateS3KeyToday(uploadCategory, command.fileName());

        // 만료 시각 계산 (15분 후)
        LocalDateTime expiresAt = LocalDateTime.now(clock).plus(SINGLE_UPLOAD_EXPIRATION);
        ExpirationTime expirationTime = ExpirationTime.of(expiresAt);

        return SingleUploadSession.forNew(
                idempotencyKey,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expirationTime,
                clock);
    }

    /**
     * Domain → Response DTO 변환 (초기화).
     *
     * @param session 활성화된 세션
     * @return InitSingleUploadResponse
     */
    public InitSingleUploadResponse toResponse(SingleUploadSession session) {
        return InitSingleUploadResponse.of(
                session.getIdValue(),
                session.getPresignedUrlValue(),
                session.getExpiresAt(),
                session.getBucketValue(),
                session.getS3KeyValue());
    }

    /**
     * Domain → Response DTO 변환 (완료).
     *
     * @param session 완료된 세션
     * @return CompleteSingleUploadResponse
     */
    public CompleteSingleUploadResponse toCompleteResponse(SingleUploadSession session) {
        return CompleteSingleUploadResponse.of(
                session.getIdValue(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getETagValue(),
                session.getCompletedAt());
    }

    /**
     * Domain → Response DTO 변환 (만료).
     *
     * @param session 만료된 세션
     * @return ExpireUploadSessionResponse
     */
    public ExpireUploadSessionResponse toExpireResponse(SingleUploadSession session) {
        return ExpireUploadSessionResponse.of(
                session.getIdValue(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getExpiresAt());
    }

    /**
     * Domain → Response DTO 변환 (취소).
     *
     * @param session 취소된 세션 (FAILED 상태)
     * @return CancelUploadSessionResponse
     */
    public CancelUploadSessionResponse toCancelResponse(SingleUploadSession session) {
        return CancelUploadSessionResponse.of(
                session.getIdValue(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue());
    }
}
