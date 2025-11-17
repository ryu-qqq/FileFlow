package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import com.ryuqq.fileflow.domain.vo.MessageOutboxSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * MessageOutbox Query Port (Outbound Port)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *QueryPort
 * - 패키지: ..application..port.out.query..
 * - 필수 메서드 4개: findById, existsById, findByCriteria, countByCriteria
 * - Value Object 파라미터: MessageOutboxId, MessageOutboxSearchCriteria
 * - Domain 반환: MessageOutbox Aggregate (DTO/Entity 반환 금지)
 * </p>
 * <p>
 * Application Layer에서 Persistence Layer로 MessageOutbox Aggregate 조회 요청을 위한 Port입니다.
 * CQRS 분리: 조회 전용 Port (저장/수정/삭제 메서드 금지)
 * </p>
 */
public interface MessageOutboxQueryPort {

    /**
     * ID로 메시지 아웃박스 조회
     *
     * @param id 메시지 아웃박스 ID (MessageOutboxId VO)
     * @return 조회된 MessageOutbox Aggregate (Optional)
     */
    Optional<MessageOutbox> findById(MessageOutboxId id);

    /**
     * ID로 메시지 아웃박스 존재 여부 확인
     *
     * @param id 메시지 아웃박스 ID (MessageOutboxId VO)
     * @return 존재 여부 (boolean)
     */
    boolean existsById(MessageOutboxId id);

    /**
     * 검색 조건으로 메시지 아웃박스 목록 조회
     *
     * @param criteria 검색 조건 (MessageOutboxSearchCriteria VO)
     * @return 조회된 MessageOutbox Aggregate 목록
     */
    List<MessageOutbox> findByCriteria(MessageOutboxSearchCriteria criteria);

    /**
     * 검색 조건으로 메시지 아웃박스 개수 조회
     *
     * @param criteria 검색 조건 (MessageOutboxSearchCriteria VO)
     * @return 메시지 아웃박스 개수
     */
    long countByCriteria(MessageOutboxSearchCriteria criteria);
}
