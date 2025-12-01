package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.assembler.ExternalDownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.facade.ExternalDownloadFacade;
import com.ryuqq.fileflow.application.download.port.in.command.RequestExternalDownloadUseCase;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.springframework.stereotype.Service;

/**
 * 외부 다운로드 요청 서비스.
 *
 * <p>외부 URL 다운로드 요청을 처리하고 비동기 처리를 위한 Outbox 생성
 *
 * <p><strong>구조</strong>:
 *
 * <ul>
 *   <li>Assembler: Command → Bundle (Download + Outbox + 이벤트) 변환
 *   <li>Facade: Bundle 저장 + 이벤트 발행
 *   <li>EventListener: 커밋 후 SQS 발행 처리
 * </ul>
 *
 * <p><strong>SQS 발행</strong>: ExternalDownloadRegisteredEventListener에서 처리
 */
@Service
public class RequestExternalDownloadService implements RequestExternalDownloadUseCase {

    private final ExternalDownloadAssembler assembler;
    private final ExternalDownloadFacade facade;

    public RequestExternalDownloadService(
            ExternalDownloadAssembler assembler, ExternalDownloadFacade facade) {
        this.assembler = assembler;
        this.facade = facade;
    }

    @Override
    public ExternalDownloadResponse execute(RequestExternalDownloadCommand command) {
        // 1. Command → Bundle 변환 (Download + Outbox + 등록 이벤트)
        ExternalDownloadBundle bundle = assembler.toBundle(command);

        // 2. 저장 + 이벤트 발행 (Facade가 트랜잭션 관리)
        // SQS 발행은 EventListener에서 커밋 후 처리
        ExternalDownloadId savedId = facade.saveAndPublishEvent(bundle);

        // 3. 응답 반환
        return new ExternalDownloadResponse(
                savedId.value().toString(),
                bundle.download().getStatus().name(),
                bundle.download().getCreatedAt());
    }
}
