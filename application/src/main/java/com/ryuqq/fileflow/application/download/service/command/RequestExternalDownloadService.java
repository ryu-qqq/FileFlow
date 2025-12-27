package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.facade.ExternalDownloadFacade;
import com.ryuqq.fileflow.application.download.factory.command.ExternalDownloadCommandFactory;
import com.ryuqq.fileflow.application.download.port.in.command.RequestExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.common.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 외부 다운로드 요청 서비스.
 *
 * <p>외부 URL 다운로드 요청을 처리하고 비동기 처리를 위한 Outbox 생성
 *
 * <p><strong>구조</strong>:
 *
 * <ul>
 *   <li>CommandFactory: Command → Bundle (Download + Outbox + 이벤트) 변환
 *   <li>Facade: Bundle 저장 + 이벤트 발행
 *   <li>EventListener: 커밋 후 SQS 발행 처리
 * </ul>
 *
 * <p><strong>SQS 발행</strong>: ExternalDownloadRegisteredEventListener에서 처리
 */
@Service
public class RequestExternalDownloadService implements RequestExternalDownloadUseCase {

    private final ExternalDownloadCommandFactory commandFactory;
    private final ExternalDownloadFacade facade;
    private final ExternalDownloadQueryPort queryPort;

    public RequestExternalDownloadService(
            ExternalDownloadCommandFactory commandFactory,
            ExternalDownloadFacade facade,
            ExternalDownloadQueryPort queryPort) {
        this.commandFactory = commandFactory;
        this.facade = facade;
        this.queryPort = queryPort;
    }

    @Override
    public ExternalDownloadResponse execute(RequestExternalDownloadCommand command) {
        TenantId tenantId = TenantId.of(command.tenantId());
        IdempotencyKey idempotencyKey = IdempotencyKey.fromString(command.idempotencyKey());

        // 1. 멱등성 체크 - 동일한 (tenantId, idempotencyKey) 조합이 있으면 기존 결과 반환
        Optional<ExternalDownload> existing =
                queryPort.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);
        if (existing.isPresent()) {
            ExternalDownload existingDownload = existing.get();
            return new ExternalDownloadResponse(
                    existingDownload.getId().value().toString(),
                    existingDownload.getStatus().name(),
                    existingDownload.getCreatedAt());
        }

        // 2. Command → Bundle 변환 (Download + Outbox + 등록 이벤트)
        ExternalDownloadBundle bundle = commandFactory.createBundle(command);

        // 3. 저장 + 이벤트 발행 (Facade가 트랜잭션 관리)
        // SQS 발행은 EventListener에서 커밋 후 처리
        ExternalDownloadId savedId = facade.saveAndPublishEvent(bundle);

        // 4. 응답 반환
        return new ExternalDownloadResponse(
                savedId.value().toString(),
                bundle.download().getStatus().name(),
                bundle.download().getCreatedAt());
    }
}
