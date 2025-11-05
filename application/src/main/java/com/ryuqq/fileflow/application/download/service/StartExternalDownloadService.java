package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.assembler.ExternalDownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.StartExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionManager;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Start External Download Service
 * 외부 URL로부터 파일 다운로드를 시작하는 UseCase 구현
 *
 * <p>책임:</p>
 * <ul>
 *   <li>멱등키 기반 중복 요청 확인</li>
 *   <li>Command를 받아 UploadSession, ExternalDownload Aggregate 생성</li>
 *   <li>아웃박스 패턴으로 비동기 처리 큐잉</li>
 *   <li>Assembler를 통한 Response 생성 (멱등키 포함)</li>
 * </ul>
 *
 * <p>Transaction 경계:</p>
 * <ul>
 *   <li>✅ UploadSession 생성: 트랜잭션 내</li>
 *   <li>✅ ExternalDownload 생성: 트랜잭션 내 (URL 검증 포함)</li>
 *   <li>✅ Outbox 메시지 저장: 트랜잭션 내</li>
 *   <li>❌ Worker 비동기 호출: 제거 (스케줄러가 처리)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class StartExternalDownloadService implements StartExternalDownloadUseCase {

    private final UploadSessionManager uploadSessionManager;
    private final ExternalDownloadPort externalDownloadPort;
    private final ExternalDownloadOutboxCommandPort outboxCommandPort;
    private final ExternalDownloadOutboxQueryPort outboxQueryPort;

    public StartExternalDownloadService(
        UploadSessionManager uploadSessionManager,
        ExternalDownloadPort externalDownloadPort,
        ExternalDownloadOutboxCommandPort outboxCommandPort,
        ExternalDownloadOutboxQueryPort outboxQueryPort
    ) {
        this.uploadSessionManager = uploadSessionManager;
        this.externalDownloadPort = externalDownloadPort;
        this.outboxCommandPort = outboxCommandPort;
        this.outboxQueryPort = outboxQueryPort;
    }

    @Override
    @Transactional
    public ExternalDownloadResponse execute(StartExternalDownloadCommand command) {
        // 1. 멱등키로 기존 요청 확인 (Query Port 사용)
        IdempotencyKey idempotencyKey = IdempotencyKey.of(command.idempotencyKey());
        Optional<ExternalDownloadOutbox> existingOutbox = outboxQueryPort.findByIdempotencyKey(command.idempotencyKey());

        if (existingOutbox.isPresent()) {
            // 기존 요청이 있으면 기존 응답 반환 (멱등성 보장)
            return buildResponseFromOutbox(existingOutbox.get());
        }

        // 2. UploadSession 생성 (External Download 전용 메서드 사용)
        // External Download는 시스템 작업이므로 Organization/UserContext 없음
        UploadSession session = UploadSession.createForExternalDownload(
            command.tenantId(),
            command.fileName(),
            command.fileSize()
        );

        // 3. UploadSession 저장 (Manager 사용)
        UploadSession savedSession = uploadSessionManager.save(session);

        // 4. ExternalDownload 생성 및 저장
        ExternalDownload download = ExternalDownloadAssembler.toDomain(command, savedSession);
        download = externalDownloadPort.save(download);

        // 5. 아웃박스 메시지 저장 (트랜잭션 내, Command Port 사용)
        ExternalDownloadOutbox outbox = ExternalDownloadOutbox.forNew(
            idempotencyKey,
            download.getId(),
            savedSession.getId()
        );

        outboxCommandPort.save(outbox);

        // 6. Response 생성 (멱등키 포함)
        // 주의: 비동기 다운로드는 시작하지 않음 (스케줄러가 처리)
        return ExternalDownloadAssembler.toResponse(download, savedSession, command.idempotencyKey());
    }

    /**
     * 기존 아웃박스에서 응답 생성
     *
     * @param outbox 기존 아웃박스 메시지
     * @return 기존 요청에 대한 응답
     */
    private ExternalDownloadResponse buildResponseFromOutbox(ExternalDownloadOutbox outbox) {
        // 기존 다운로드 조회
        ExternalDownload download = externalDownloadPort.findById(outbox.getDownloadIdValue())
            .orElseThrow(() -> new IllegalStateException("Download not found for outbox: " + outbox.getId()));

        // UploadSession 조회 (Manager 사용)
        UploadSession session = uploadSessionManager.findById(outbox.getUploadSessionIdValue())
            .orElseThrow(() -> new IllegalStateException("Session not found for outbox: " + outbox.getId()));

        return ExternalDownloadAssembler.toResponse(
            download,
            session,
            outbox.getIdempotencyKeyValue()
        );
    }
}
