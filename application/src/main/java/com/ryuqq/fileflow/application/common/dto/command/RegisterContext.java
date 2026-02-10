package com.ryuqq.fileflow.application.common.dto.command;

import java.time.Instant;

/**
 * RegisterContext - 등록 컨텍스트
 *
 * <p>등록 작업에 필요한 새 Aggregate와 변경 시간을 함께 담는 컨텍스트입니다.
 *
 * <p>Factory에서 Command를 받아 새 엔티티와 변경 시간을 한 번에 생성하여 반환할 때 사용합니다. TimeProvider는 Factory에서만 사용하고,
 * Service에서는 이 컨텍스트를 통해 시간을 전달받습니다. (예: 기존 기본 배송지 해제 시 context.changedAt() 사용)
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * RegisterContext<SellerAddress> context = factory.createRegisterContext(command);
 * if (command.defaultAddress()) {
 *     readManager.findDefaultBySellerId(...).ifPresent(addr -> {
 *         addr.unmarkDefault(context.changedAt());
 *         commandManager.persist(addr);
 *     });
 * }
 * return commandManager.persist(context.newEntity());
 * }</pre>
 *
 * @param <T> 등록할 Aggregate 타입
 * @param newEntity 새로 생성된 Aggregate
 * @param changedAt 등록/변경 시각 (동일 플로우 내 부가 작업에 재사용)
 * @author development-team
 * @since 1.0.0
 */
public record RegisterContext<T>(T newEntity, Instant changedAt) {}
