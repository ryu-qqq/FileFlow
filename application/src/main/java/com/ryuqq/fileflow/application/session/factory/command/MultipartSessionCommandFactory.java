package com.ryuqq.fileflow.application.session.factory.command;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.dto.command.UpdateContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.session.dto.bundle.MultipartSessionCreationBundle;
import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.service.S3PathResolver;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.MultipartUploadSessionUpdateData;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpec;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * 멀티파트 업로드 세션 커맨드 팩토리.
 *
 * <p>순수 도메인 계산만 수행합니다. 외부 데이터(bucket, uploadId) 해결은 코디네이터가 담당합니다.
 */
@Component
public class MultipartSessionCommandFactory {

    private static final Duration MULTIPART_SESSION_TTL = Duration.ofHours(24);

    private final IdGeneratorPort idGeneratorPort;
    private final TimeProvider timeProvider;

    public MultipartSessionCommandFactory(
            IdGeneratorPort idGeneratorPort, TimeProvider timeProvider) {
        this.idGeneratorPort = idGeneratorPort;
        this.timeProvider = timeProvider;
    }

    public MultipartSessionCreationBundle create(CreateMultipartUploadSessionCommand command) {
        Instant now = timeProvider.now();
        String sessionId = idGeneratorPort.generate();
        String extension = S3PathResolver.extractExtension(command.fileName());
        String s3Key = S3PathResolver.resolve(command.accessType(), sessionId, extension, now);

        Instant expiresAt = now.plus(MULTIPART_SESSION_TTL);

        SessionExpiration expiration =
                SessionExpiration.of(sessionId, "MULTIPART", MULTIPART_SESSION_TTL);

        return MultipartSessionCreationBundle.of(
                MultipartUploadSessionId.of(sessionId),
                s3Key,
                command.accessType(),
                command.fileName(),
                command.contentType(),
                command.partSize(),
                command.purpose(),
                command.source(),
                expiresAt,
                now,
                expiration);
    }

    public PartPresignedUrlSpec createPartPresignedUrlSpec(
            MultipartUploadSession session, int partNumber) {
        Instant now = timeProvider.now();
        return PartPresignedUrlSpec.of(
                session.s3Key(), session.uploadId(), partNumber, session.expiresAt(), now);
    }

    public CompletedPart createCompletedPart(AddCompletedPartCommand command) {
        return CompletedPart.of(
                command.partNumber(), command.etag(), command.size(), timeProvider.now());
    }

    public StatusChangeContext<String> createAbortContext(String sessionId) {
        return new StatusChangeContext<>(sessionId, timeProvider.now());
    }

    public StatusChangeContext<String> createExpireContext(String sessionId) {
        return new StatusChangeContext<>(sessionId, timeProvider.now());
    }

    public UpdateContext<String, MultipartUploadSessionUpdateData> createCompleteContext(
            CompleteMultipartUploadSessionCommand command) {
        return new UpdateContext<>(
                command.sessionId(),
                MultipartUploadSessionUpdateData.of(command.totalFileSize(), command.etag()),
                timeProvider.now());
    }
}
