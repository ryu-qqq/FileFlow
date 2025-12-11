package com.ryuqq.fileflow.application.download.factory.command;

import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.S3UploadResponse;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * ExternalDownload Command Factory.
 *
 * <p>ExternalDownload 관련 Domain 생성 및 상태 변경을 담당합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Command → Domain 변환 (createBundle, createDomain)
 *   <li>Domain + DTO → Response 변환 (createS3UploadResponse)
 *   <li>처리 시작 (startProcessing)
 *   <li>처리 완료 (complete)
 *   <li>실패 처리 (markAsFailed)
 *   <li>Outbox 발행 완료 (markAsPublished)
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>@Component 어노테이션 (Service 아님)
 *   <li>비즈니스 로직 금지 (순수 변환)
 *   <li>Port 호출 금지 (조회 없음)
 *   <li>@Transactional 금지
 * </ul>
 */
@Component
public class ExternalDownloadCommandFactory {

    private final ClockHolder clockHolder;
    private final Supplier<UserContext> userContextSupplier;

    public ExternalDownloadCommandFactory(
            ClockHolder clockHolder, Supplier<UserContext> userContextSupplier) {
        this.clockHolder = clockHolder;
        this.userContextSupplier = userContextSupplier;
    }

    /**
     * 현재 Clock을 반환합니다.
     *
     * @return Clock 인스턴스
     */
    public Clock getClock() {
        return clockHolder.getClock();
    }

    // ==================== Command → Domain 변환 ====================

    /**
     * RequestExternalDownloadCommand를 ExternalDownloadBundle로 변환합니다.
     *
     * <p>Bundle에는 ExternalDownload와 ExternalDownloadOutbox가 포함됩니다. ExternalDownload에는 등록 이벤트가 자동으로
     * 추가됩니다.
     *
     * @param command 외부 다운로드 요청 명령
     * @return ExternalDownloadBundle (Download + Outbox + 등록 이벤트)
     */
    public ExternalDownloadBundle createBundle(RequestExternalDownloadCommand command) {
        Clock clock = clockHolder.getClock();

        // ExternalDownload 생성
        ExternalDownload download = createExternalDownload(command, clock);

        // 등록 이벤트 생성 및 등록
        download.registerEvent(download.createRegisteredEvent());

        // Outbox 생성 (download.getId()는 이미 생성된 상태)
        ExternalDownloadOutbox outbox = ExternalDownloadOutbox.forNew(download.getId(), clock);

        return new ExternalDownloadBundle(download, outbox);
    }

    /**
     * RequestExternalDownloadCommand를 ExternalDownload로 변환합니다.
     *
     * @param command 외부 다운로드 요청 명령
     * @return 신규 ExternalDownload (status: PENDING)
     * @deprecated {@link #createBundle(RequestExternalDownloadCommand)} 사용 권장
     */
    @Deprecated
    public ExternalDownload createDomain(RequestExternalDownloadCommand command) {
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

        // String → VO 변환
        TenantId tenantId = TenantId.of(command.tenantId());
        OrganizationId organizationId = OrganizationId.of(command.organizationId());

        return ExternalDownload.forNew(
                sourceUrl,
                tenantId,
                organizationId,
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
    public S3UploadResponse createS3UploadResponse(
            ExternalDownload download, DownloadResult result) {
        Clock clock = clockHolder.getClock();
        String extension = result.getExtension();

        // 도메인 로직 활용: S3Key 생성
        S3Key s3Key = download.generateS3Key(extension, clock);

        // 도메인 로직 활용: FileName 추출
        FileName fileName = download.getSourceUrl().extractFileName(extension);

        return new S3UploadResponse(
                s3Key, fileName, ContentType.of(result.contentType()), result.content());
    }

    // ==================== Domain 상태 변경 ====================

    /**
     * ExternalDownload 처리를 시작합니다.
     *
     * @param download 대상 ExternalDownload
     */
    public void startProcessing(ExternalDownload download) {
        download.startProcessing(clockHolder.getClock());
    }

    /**
     * ExternalDownload를 완료 처리합니다.
     *
     * @param download 대상 ExternalDownload
     * @param contentType 컨텐츠 타입
     * @param contentLength 파일 크기
     * @param s3Key S3 키
     * @param etag ETag
     */
    public void complete(
            ExternalDownload download,
            ContentType contentType,
            long contentLength,
            S3Key s3Key,
            ETag etag) {
        download.complete(contentType.type(), contentLength, s3Key, etag, clockHolder.getClock());
    }

    /**
     * ExternalDownload를 실패 처리합니다.
     *
     * @param download 대상 ExternalDownload
     * @param errorMessage 에러 메시지
     * @param defaultFileAssetId 기본 이미지 FileAsset ID
     * @return 처리 여부 (이미 처리된 경우 false)
     */
    public boolean markAsFailed(
            ExternalDownload download, String errorMessage, FileAssetId defaultFileAssetId) {
        return download.markAsFailed(errorMessage, defaultFileAssetId, clockHolder.getClock());
    }

    /**
     * ExternalDownloadOutbox를 발행 완료 상태로 변경합니다.
     *
     * @param outbox 대상 Outbox
     */
    public void markAsPublished(ExternalDownloadOutbox outbox) {
        outbox.markAsPublished(clockHolder.getClock());
    }
}
