package com.ryuqq.fileflow.application.file.manager;

import com.ryuqq.fileflow.application.file.port.out.FileCommandPort;
import com.ryuqq.fileflow.application.file.port.out.PipelineOutboxPort;
import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset Command Manager (CQRS Command)
 *
 * <p>FileAsset Domain Aggregate의 상태 변경 전담 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>FileAsset 저장 (생성 및 업데이트)</li>
 *   <li>FileAsset 저장 시 PipelineOutbox 동시 저장 (Transactional Outbox Pattern)</li>
 *   <li>FileAsset 삭제</li>
 *   <li>트랜잭션 경계 관리</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command: FileCommandManager (상태 변경만)</li>
 *   <li>Query: FileQueryManager (조회만)</li>
 *   <li>분리 이유: 조회와 변경의 책임 분리, 성능 최적화</li>
 * </ul>
 *
 * <p><strong>Event-Driven Architecture:</strong></p>
 * <ul>
 *   <li>FileAsset 저장 → PipelineOutbox 저장 (Domain Event 자동 발행)</li>
 *   <li>Event Listener가 트랜잭션 커밋 후 비동기로 Pipeline 처리 시작</li>
 *   <li>멱등성 보장: IdempotencyKey로 중복 이벤트 방지</li>
 * </ul>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>모든 메서드: readOnly=false (상태 변경)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileCommandManager {

    private static final Logger log = LoggerFactory.getLogger(FileCommandManager.class);

    private final FileCommandPort fileCommandPort;
    private final PipelineOutboxPort pipelineOutboxPort;

    /**
     * 생성자
     *
     * @param fileCommandPort    File Command Port
     * @param pipelineOutboxPort Pipeline Outbox Port
     */
    public FileCommandManager(
        FileCommandPort fileCommandPort,
        PipelineOutboxPort pipelineOutboxPort
    ) {
        this.fileCommandPort = fileCommandPort;
        this.pipelineOutboxPort = pipelineOutboxPort;
    }

    /**
     * FileAsset 저장 (PipelineOutbox 동시 저장)
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>FileAsset 저장 + PipelineOutbox 저장 (동일 트랜잭션)</li>
     *   <li>트랜잭션 커밋 실패 시 모두 Rollback</li>
     * </ul>
     *
     * <p><strong>실행 순서:</strong></p>
     * <ol>
     *   <li>FileAsset 저장 (DB에 INSERT, ID 자동 생성)</li>
     *   <li>Idempotency Key 생성 (fileAsset-{fileAssetId})</li>
     *   <li>PipelineOutbox 저장 (PENDING 상태, Domain Event 자동 등록)</li>
     *   <li>트랜잭션 커밋 시 PipelineOutboxCreatedEvent 자동 발행</li>
     * </ol>
     *
     * <p><strong>이후 처리:</strong></p>
     * <ul>
     *   <li>트랜잭션 커밋 완료 후 PipelineOutboxEventListener가 이벤트 수신</li>
     *   <li>EventListener가 비동기로 PipelineWorker.startPipeline() 호출</li>
     *   <li>Worker가 Pipeline 처리 실행</li>
     * </ul>
     *
     * @param fileAsset FileAsset Domain Aggregate
     * @return 저장된 FileAsset (ID 포함)
     */
    @Transactional
    public FileAsset save(FileAsset fileAsset) {
        // 1. FileAsset 저장 (ID 자동 생성)
        FileAsset savedFileAsset = fileCommandPort.save(fileAsset);

        log.debug("FileAsset saved: fileAssetId={}", savedFileAsset.getIdValue());

        // 2. PipelineOutbox 생성 및 저장 (동일 트랜잭션)
        IdempotencyKey idempotencyKey = generateIdempotencyKey(savedFileAsset);
        PipelineOutbox outbox = PipelineOutbox.forNew(
            idempotencyKey,
            FileAssetId.of(savedFileAsset.getIdValue())
        );

        pipelineOutboxPort.save(outbox);

        log.info("PipelineOutbox created for FileAsset: fileAssetId={}, idempotencyKey={} (Domain Event will be published on commit)",
            savedFileAsset.getIdValue(), idempotencyKey.value());

        return savedFileAsset;
    }

    /**
     * FileAsset 삭제
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>FileAsset을 영구적으로 삭제합니다</li>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>PipelineOutbox는 삭제하지 않음 (처리 이력 보존)</li>
     * </ul>
     *
     * @param id FileAsset ID
     */
    @Transactional
    public void delete(Long id) {
        fileCommandPort.delete(id);

        log.info("FileAsset deleted: fileAssetId={}", id);
    }

    /**
     * Idempotency Key 생성
     *
     * <p>FileAsset ID 기반 멱등성 키 생성</p>
     * <p>패턴: {@code fileAsset-{fileAssetId}}</p>
     *
     * @param fileAsset FileAsset
     * @return Idempotency Key
     */
    private IdempotencyKey generateIdempotencyKey(FileAsset fileAsset) {
        String key = String.format("fileAsset-%d", fileAsset.getIdValue());
        return IdempotencyKey.of(key);
    }
}
