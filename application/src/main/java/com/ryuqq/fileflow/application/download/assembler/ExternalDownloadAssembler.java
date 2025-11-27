package com.ryuqq.fileflow.application.download.assembler;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.S3UploadResponse;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * 외부 다운로드 Assembler.
 *
 * <p>Application DTO를 Domain Aggregate로 변환합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Command → ExternalDownload 변환
 *   <li>Command → ExternalDownloadBundle (Download + Outbox) 변환
 *   <li>도메인 이벤트 등록
 *   <li>UserContext에서 S3 정보 추출
 * </ul>
 */
@Component
public class ExternalDownloadAssembler {

    private final ClockHolder clockHolder;
    private final Supplier<UserContext> userContextSupplier;

    public ExternalDownloadAssembler(
            ClockHolder clockHolder, Supplier<UserContext> userContextSupplier) {
        this.clockHolder = clockHolder;
        this.userContextSupplier = userContextSupplier;
    }

    /**
     * RequestExternalDownloadCommand를 ExternalDownloadBundle로 변환합니다.
     *
     * <p>Bundle에는 ExternalDownload와 ExternalDownloadOutbox가 포함됩니다. ExternalDownload에는 등록 이벤트가 자동으로
     * 추가됩니다.
     *
     * @param command 외부 다운로드 요청 명령
     * @return ExternalDownloadBundle (Download + Outbox + 등록 이벤트)
     */
    public ExternalDownloadBundle toBundle(RequestExternalDownloadCommand command) {
        Clock clock = clockHolder.getClock();

        // ExternalDownload 생성
        ExternalDownload download = createExternalDownload(command, clock);

        // 등록 이벤트 추가
        download.registerEvent(download.createRegisteredEvent());

        // Outbox 생성
        ExternalDownloadOutbox outbox = ExternalDownloadOutbox.forNew(download.getId(), clock);

        return new ExternalDownloadBundle(download, outbox);
    }

    /**
     * RequestExternalDownloadCommand를 ExternalDownload로 변환합니다.
     *
     * @param command 외부 다운로드 요청 명령
     * @return 신규 ExternalDownload (status: PENDING)
     * @deprecated {@link #toBundle(RequestExternalDownloadCommand)} 사용 권장
     */
    @Deprecated
    public ExternalDownload toDomain(RequestExternalDownloadCommand command) {
        Clock clock = clockHolder.getClock();
        return createExternalDownload(command, clock);
    }

    private ExternalDownload createExternalDownload(
            RequestExternalDownloadCommand command, Clock clock) {
        SourceUrl sourceUrl = SourceUrl.of(command.sourceUrl());
        WebhookUrl webhookUrl = toWebhookUrl(command.webhookUrl());

        // ThreadLocal에서 UserContext 조회하여 S3 정보 추출
        UserContext userContext = userContextSupplier.get();
        S3Bucket s3Bucket = userContext.getS3Bucket();
        String s3PathPrefix = userContext.organization().getS3PathPrefix();

        return ExternalDownload.forNew(
                sourceUrl,
                command.tenantId(),
                command.organizationId(),
                s3Bucket,
                s3PathPrefix,
                webhookUrl,
                clock);
    }

    private WebhookUrl toWebhookUrl(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return null;
        }
        return WebhookUrl.of(webhookUrl);
    }

    /**
     * ExternalDownload와 DownloadResult를 S3UploadResponse로 변환합니다.
     *
     * <p>도메인 객체의 비즈니스 로직을 활용하여 S3Key와 FileName을 생성하고, S3 업로드에 필요한 모든 정보를 조합합니다.
     *
     * @param download 외부 다운로드 Aggregate
     * @param result HTTP 다운로드 결과
     * @return S3UploadResponse
     */
    public S3UploadResponse toS3UploadResponse(ExternalDownload download, DownloadResult result) {
        Clock clock = clockHolder.getClock();
        String extension = result.getExtension();

        // 도메인 로직 활용: S3Key 생성
        S3Key s3Key = download.generateS3Key(extension, clock);

        // 도메인 로직 활용: FileName 추출
        FileName fileName = download.getSourceUrl().extractFileName(extension);

        return new S3UploadResponse(
                s3Key, fileName, ContentType.of(result.contentType()), result.content());
    }
}
