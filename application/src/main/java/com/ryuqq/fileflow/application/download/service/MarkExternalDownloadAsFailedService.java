package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.port.in.command.MarkExternalDownloadAsFailedUseCase;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 외부 다운로드 실패 처리 서비스.
 *
 * <p>DLQ에서 호출되어 최종 실패 처리를 수행합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>ExternalDownload 조회 (QueryPort)
 *   <li>도메인 비즈니스 로직 실행 (Aggregate.markAsFailed())
 *   <li>영속화 (Manager)
 * </ul>
 *
 * <p><strong>도메인 로직</strong>은 {@link ExternalDownload#markAsFailed}에 캡슐화되어 있습니다.
 */
@Service
public class MarkExternalDownloadAsFailedService implements MarkExternalDownloadAsFailedUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(MarkExternalDownloadAsFailedService.class);

    /** 디폴트 이미지 FileAssetId UUID (설정에서 주입받거나 하드코딩). */
    private static final String DEFAULT_IMAGE_FILE_ASSET_UUID =
            "00000000-0000-0000-0000-000000000001";

    private final ExternalDownloadQueryPort queryPort;
    private final ExternalDownloadManager externalDownloadManager;
    private final ClockHolder clockHolder;

    public MarkExternalDownloadAsFailedService(
            ExternalDownloadQueryPort queryPort,
            ExternalDownloadManager externalDownloadManager,
            ClockHolder clockHolder) {
        this.queryPort = queryPort;
        this.externalDownloadManager = externalDownloadManager;
        this.clockHolder = clockHolder;
    }

    @Override
    public void markAsFailed(Long externalDownloadId, String errorMessage) {
        Clock clock = clockHolder.getClock();

        // 1. ExternalDownload 조회
        Optional<ExternalDownload> downloadOpt =
                queryPort.findById(ExternalDownloadId.of(externalDownloadId));

        if (downloadOpt.isEmpty()) {
            log.warn("ExternalDownload not found for DLQ processing: id={}", externalDownloadId);
            return;
        }

        ExternalDownload download = downloadOpt.get();

        // 2. 도메인 비즈니스 로직 실행 (상태 검증 및 실패 처리)
        FileAssetId defaultFileAssetId = FileAssetId.of(DEFAULT_IMAGE_FILE_ASSET_UUID);
        boolean processed = download.markAsFailed(errorMessage, defaultFileAssetId, clock);

        // 3. 결과에 따라 로깅
        if (!processed) {
            log.info(
                    "ExternalDownload 이미 처리됨, DLQ 처리 skip: id={}, status={}",
                    externalDownloadId,
                    download.getStatus());
            return;
        }

        // 4. 영속화
        externalDownloadManager.save(download);

        log.info(
                "ExternalDownload FAILED 처리 완료: id={}, errorMessage={}",
                externalDownloadId,
                errorMessage);
    }
}
