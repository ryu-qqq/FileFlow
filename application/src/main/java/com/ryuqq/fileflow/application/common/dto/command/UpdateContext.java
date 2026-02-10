package com.ryuqq.fileflow.application.common.dto.command;

import java.time.Instant;

/**
 * UpdateContext - 업데이트 컨텍스트
 *
 * <p>업데이트 작업에 필요한 ID, UpdateData, 변경 시간을 함께 담는 컨텍스트입니다.
 *
 * <p>Factory에서 Command를 받아 ID, UpdateData, 변경 시간을 한 번에 생성하여 반환할 때 사용합니다. TimeProvider는 Factory에서만
 * 사용하고, Service에서는 이 컨텍스트를 통해 시간을 전달받습니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * UpdateContext<PackagePurposeId, PackagePurposeUpdateData> context =
 *         factory.createUpdateContext(command);
 *
 * validator.validateNameNotDuplicateExcluding(context.updateData().name(), context.id());
 * Domain domain = validator.findExistingOrThrow(context.id());
 * domain.update(context.updateData(), context.changedAt());
 * }</pre>
 *
 * @param <ID> 도메인 ID 타입
 * @param <UPDATE_DATA> 도메인 UpdateData 타입
 * @param id 도메인 ID
 * @param updateData 업데이트 데이터
 * @param changedAt 변경 시간
 * @author development-team
 * @since 1.0.0
 */
public record UpdateContext<ID, UPDATE_DATA>(ID id, UPDATE_DATA updateData, Instant changedAt) {}
