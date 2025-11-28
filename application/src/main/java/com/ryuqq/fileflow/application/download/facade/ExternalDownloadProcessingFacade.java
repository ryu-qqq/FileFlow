package com.ryuqq.fileflow.application.download.facade;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.application.download.assembler.ExternalDownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.response.S3UploadResponse;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.port.out.client.HttpDownloadPort;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 외부 다운로드 처리 Facade.
 *
 * <p>외부 다운로드의 전체 프로세스를 오케스트레이션합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>ExternalDownload 조회 및 PROCESSING 상태 전환 (트랜잭션)
 *   <li>HTTP 다운로드 (트랜잭션 없음 - 외부 통신)
 *   <li>S3 업로드 준비 (Assembler 활용)
 *   <li>S3 업로드 (트랜잭션 없음 - 외부 통신)
 *   <li>완료 처리 및 도메인 이벤트 발행 (트랜잭션)
 * </ol>
 *
 * <p><strong>트랜잭션 분리</strong>:
 *
 * <ul>
 *   <li>1단계: 독립 트랜잭션 (PENDING → PROCESSING)
 *   <li>2-4단계: 외부 통신 (트랜잭션 없음)
 *   <li>5단계: 독립 트랜잭션 (PROCESSING → COMPLETED + 이벤트 발행)
 * </ul>
 */
@Component
@ConditionalOnBean(HttpDownloadPort.class)
public class ExternalDownloadProcessingFacade {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadProcessingFacade.class);

    private final ExternalDownloadQueryPort queryPort;
    private final ExternalDownloadManager externalDownloadManager;
    private final ExternalDownloadAssembler assembler;
    private final HttpDownloadPort httpDownloadPort;
    private final S3ClientPort s3ClientPort;
    private final ApplicationEventPublisher eventPublisher;
    private final ClockHolder clockHolder;

    public ExternalDownloadProcessingFacade(
            ExternalDownloadQueryPort queryPort,
            ExternalDownloadManager externalDownloadManager,
            ExternalDownloadAssembler assembler,
            HttpDownloadPort httpDownloadPort,
            S3ClientPort s3ClientPort,
            ApplicationEventPublisher eventPublisher,
            ClockHolder clockHolder) {
        this.queryPort = queryPort;
        this.externalDownloadManager = externalDownloadManager;
        this.assembler = assembler;
        this.httpDownloadPort = httpDownloadPort;
        this.s3ClientPort = s3ClientPort;
        this.eventPublisher = eventPublisher;
        this.clockHolder = clockHolder;
    }

    /**
     * 외부 다운로드 전체 프로세스를 실행합니다.
     *
     * @param downloadId 외부 다운로드 ID
     */
    public void process(Long downloadId) {
        Clock clock = clockHolder.getClock();

        // 1단계: ExternalDownload 조회 및 처리 시작 (트랜잭션)
        ExternalDownload download = startProcessing(downloadId, clock);

        log.info(
                "외부 다운로드 처리 시작: id={}, sourceUrl={}",
                download.getIdValue(),
                download.getSourceUrl().value());

        // 2단계: HTTP 다운로드 (트랜잭션 없음)
        DownloadResult result = httpDownloadPort.download(download.getSourceUrl());

        // 3단계: S3 업로드 준비 (Assembler 활용)
        S3UploadResponse uploadResponse = assembler.toS3UploadResponse(download, result);

        // 4단계: S3 업로드 (트랜잭션 없음)
        ETag etag =
                s3ClientPort.putObject(
                        download.getS3Bucket(),
                        uploadResponse.s3Key(),
                        uploadResponse.contentType(),
                        uploadResponse.content());

        // 5단계: 완료 처리 및 이벤트 발행 (트랜잭션)
        completeProcessing(download, result, uploadResponse.s3Key(), etag, clock);

        log.info("외부 다운로드 처리 완료: id={}", download.getIdValue());
    }

    /**
     * 1단계: 처리 시작 (PENDING → PROCESSING).
     *
     * @param downloadId 외부 다운로드 ID
     * @param clock 시간 소스
     * @return ExternalDownload
     */
    private ExternalDownload startProcessing(Long downloadId, Clock clock) {
        ExternalDownload download =
                queryPort
                        .findById(ExternalDownloadId.of(downloadId))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "ExternalDownload not found: " + downloadId));

        download.startProcessing(clock);
        externalDownloadManager.save(download);
        return download;
    }

    /**
     * 5단계: 완료 처리 (PROCESSING → COMPLETED) 및 도메인 이벤트 발행.
     *
     * @param download ExternalDownload
     * @param result 다운로드 결과
     * @param s3Key S3 키
     * @param etag ETag
     * @param clock 시간 소스
     */
    private void completeProcessing(
            ExternalDownload download, DownloadResult result, S3Key s3Key, ETag etag, Clock clock) {

        // 도메인 비즈니스 로직 실행 (이벤트 자동 등록됨)
        download.complete(result.contentType(), result.contentLength(), s3Key, etag, clock);

        // 영속화 및 도메인 이벤트 발행
        externalDownloadManager.save(download);
        download.getDomainEvents().forEach(eventPublisher::publishEvent);
        download.clearDomainEvents();
    }
}
