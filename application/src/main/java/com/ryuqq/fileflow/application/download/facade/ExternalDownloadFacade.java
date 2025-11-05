package com.ryuqq.fileflow.application.download.facade;

import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.exception.DownloadNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * External Download Facade
 *
 * <p>ExternalDownload Aggregate의 Command/Query 작업을 통합하는 Facade입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownload 조회 (Query Port)</li>
 *   <li>ExternalDownload 저장 (Command Port)</li>
 *   <li>조회/저장 로직 캡슐화로 Service 의존성 단순화</li>
 * </ul>
 *
 * <p><strong>설계 의도:</strong></p>
 * <ul>
 *   <li>✅ Service의 DI 의존성 감소 (6개 → 3개)</li>
 *   <li>✅ Command/Query Port 통합 관리</li>
 *   <li>✅ 일관된 트랜잭션 경계 제공</li>
 *   <li>✅ 예외 처리 중앙화</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Before (Service에서 직접 Port 주입)
 * private final ExternalDownloadCommandPort downloadCommandPort;
 * private final ExternalDownloadQueryPort downloadQueryPort;
 * ExternalDownload download = downloadQueryPort.findById(id).orElseThrow(...);
 * downloadCommandPort.save(download);
 *
 * // After (Facade 사용)
 * private final ExternalDownloadFacade downloadFacade;
 * ExternalDownload download = downloadFacade.getById(id);
 * downloadFacade.save(download);
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadFacade {

    private final ExternalDownloadCommandPort commandPort;
    private final ExternalDownloadQueryPort queryPort;

    /**
     * 생성자
     *
     * @param commandPort ExternalDownload Command Port
     * @param queryPort ExternalDownload Query Port
     */
    public ExternalDownloadFacade(
        ExternalDownloadCommandPort commandPort,
        ExternalDownloadQueryPort queryPort
    ) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
    }

    /**
     * ExternalDownload 조회 (Optional)
     *
     * <p><strong>트랜잭션:</strong> readOnly = true</p>
     *
     * @param downloadId ExternalDownload ID
     * @return ExternalDownload Optional
     */
    @Transactional(readOnly = true)
    public Optional<ExternalDownload> findById(Long downloadId) {
        return queryPort.findById(downloadId);
    }

    /**
     * ExternalDownload 조회 (필수)
     *
     * <p><strong>트랜잭션:</strong> readOnly = true</p>
     *
     * <p>다운로드가 존재하지 않으면 {@link DownloadNotFoundException} 발생</p>
     *
     * @param downloadId ExternalDownload ID
     * @return ExternalDownload
     * @throws DownloadNotFoundException 다운로드가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public ExternalDownload getById(Long downloadId) {
        return queryPort.findById(downloadId)
            .orElseThrow(() -> new DownloadNotFoundException(ExternalDownloadId.of(downloadId)));
    }

    /**
     * ExternalDownload 저장
     *
     * <p><strong>트랜잭션:</strong> 호출자의 트랜잭션 컨텍스트 사용</p>
     *
     * @param download ExternalDownload Aggregate
     * @return 저장된 ExternalDownload
     */
    @Transactional
    public ExternalDownload save(ExternalDownload download) {
        return commandPort.save(download);
    }

    /**
     * UploadSession ID로 ExternalDownload 조회
     *
     * <p><strong>트랜잭션:</strong> readOnly = true</p>
     *
     * <p><strong>사용 시나리오:</strong> 다운로드 완료 시 UploadSession과 연계</p>
     *
     * @param uploadSessionId UploadSession ID
     * @return ExternalDownload Optional
     */
    @Transactional(readOnly = true)
    public Optional<ExternalDownload> findByUploadSessionId(Long uploadSessionId) {
        return queryPort.findByUploadSessionId(uploadSessionId);
    }

    /**
     * UploadSession ID로 ExternalDownload 조회 (필수)
     *
     * <p><strong>트랜잭션:</strong> readOnly = true</p>
     *
     * @param uploadSessionId UploadSession ID
     * @return ExternalDownload
     * @throws DownloadNotFoundException 다운로드가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public ExternalDownload getByUploadSessionId(Long uploadSessionId) {
        return queryPort.findByUploadSessionId(uploadSessionId)
            .orElseThrow(() -> new DownloadNotFoundException());
    }
}
