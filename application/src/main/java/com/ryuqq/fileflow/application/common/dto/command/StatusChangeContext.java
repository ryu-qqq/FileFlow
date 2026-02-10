package com.ryuqq.fileflow.application.common.dto.command;

import java.time.Instant;

/**
 * StatusChangeContext - 상태 변경 컨텍스트
 *
 * <p>단순 상태 변경 작업에 필요한 ID와 변경 시간을 함께 담는 컨텍스트입니다.
 *
 * <p>Factory에서 Command를 받아 ID와 변경 시간을 한 번에 생성하여 반환할 때 사용합니다. TimeProvider는 Factory에서만 사용하고,
 * Service에서는 이 컨텍스트를 통해 시간을 전달받습니다.
 *
 * <p><strong>사용 케이스:</strong>
 *
 * <ul>
 *   <li>단순 상태 변경 (cancel, approve, reject 등)
 *   <li>소프트 삭제 (deletedAt 시간 필요)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // Factory
 * public StatusChangeContext<OrderId> createCancelContext(CancelOrderCommand command) {
 *     return new StatusChangeContext<>(
 *         OrderId.of(command.orderId()),
 *         timeProvider.now()
 *     );
 * }
 *
 * // Service
 * StatusChangeContext<OrderId> context = factory.createCancelContext(command);
 * facade.cancel(context.id(), context.changedAt());
 * }</pre>
 *
 * @param <ID> 도메인 ID 타입
 * @param id 도메인 ID
 * @param changedAt 상태 변경 시간
 * @author development-team
 * @since 1.0.0
 */
public record StatusChangeContext<ID>(ID id, Instant changedAt) {}
