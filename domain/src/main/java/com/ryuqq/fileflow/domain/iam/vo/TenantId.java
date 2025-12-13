package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;

/**
 * 테넌트 ID Value Object.
 *
 * <p>UUIDv7 기반의 테넌트 식별자로, 다음 특성을 가진다:
 *
 * <ul>
 *   <li>시간순 정렬 가능
 *   <li>분산 시스템에서 충돌 없는 고유 ID
 *   <li>불변 객체
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>값은 null이거나 빈 문자열일 수 없다.
 *   <li>값은 유효한 UUIDv7 형식이어야 한다.
 * </ul>
 *
 * @param value UUIDv7 문자열
 * @author development-team
 * @since 1.0.0
 */
public record TenantId(String value) {

    /**
     * Compact Constructor (검증 로직).
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 UUIDv7인 경우
     */
    public TenantId {
        UuidV7Generator.validate(value, "TenantId");
    }

    /**
     * 값 기반 생성.
     *
     * @param value UUIDv7 문자열
     * @return TenantId
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static TenantId of(String value) {
        return new TenantId(value);
    }

    /**
     * 새로운 TenantId를 생성한다.
     *
     * @return 새로운 UUIDv7 기반 TenantId
     */
    public static TenantId generate() {
        return new TenantId(UuidV7Generator.generate());
    }

    @Override
    public String toString() {
        return value;
    }
}
