package com.ryuqq.fileflow.application.asset.port.out.client;

import com.ryuqq.fileflow.application.asset.dto.message.FileProcessingMessage;

/**
 * 파일 가공 SQS 발행 포트.
 *
 * <p>FileAsset 가공 요청을 Resizing Worker로 전달합니다.
 *
 * <p><strong>Transactional Outbox 패턴</strong>:
 *
 * <ul>
 *   <li>트랜잭션 커밋 후 호출됨 (AFTER_COMMIT)
 *   <li>발행 성공/실패 여부 반환
 *   <li>실패 시 Outbox 상태 업데이트 필요
 * </ul>
 */
public interface FileProcessingSqsPublishPort {

    /**
     * SQS에 파일 가공 메시지를 발행합니다.
     *
     * @param message 파일 가공 메시지
     * @return 발행 성공 여부
     */
    boolean publish(FileProcessingMessage message);
}
