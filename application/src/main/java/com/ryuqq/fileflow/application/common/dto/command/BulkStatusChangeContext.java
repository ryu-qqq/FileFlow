package com.ryuqq.fileflow.application.common.dto.command;

import java.time.Instant;
import java.util.List;

/**
 * BulkStatusChangeContext - 일괄 상태 변경 컨텍스트.
 *
 * <p>{@link StatusChangeContext}의 일괄 처리 버전입니다.
 *
 * <p>Factory에서 Command를 받아 ID 목록과 변경 시간을 한 번에 생성하여 반환할 때 사용합니다.
 *
 * @param <ID> 도메인 ID 타입
 * @param ids 도메인 ID 목록
 * @param changedAt 상태 변경 시간
 * @author ryu-qqq
 * @since 1.1.0
 */
public record BulkStatusChangeContext<ID>(List<ID> ids, Instant changedAt) {}
