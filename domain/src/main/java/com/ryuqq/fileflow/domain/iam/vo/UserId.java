package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;

/**
 * 사용자 ID Value Object.
 *
 * <p>UUIDv7 기반의 사용자 식별자로, 다음 특성을 가진다:
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
 * <p><strong>사용 컨텍스트</strong>:
 *
 * <ul>
 *   <li>Customer 사용자: userId 필수
 *   <li>Admin/Seller 사용자: userId 없음 (email로 식별)
 * </ul>
 *
 * @param value UUIDv7 문자열
 * @author development-team
 * @since 1.0.0
 */
public record UserId(String value) {

    /**
     * Compact Constructor (검증 로직).
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 UUIDv7인 경우
     */
    public UserId {
        UuidV7Generator.validate(value, "UserId");
    }

    /**
     * 값 기반 생성.
     *
     * @param value UUIDv7 문자열
     * @return UserId
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static UserId of(String value) {
        return new UserId(value);
    }

    /**
     * 새로운 UserId를 생성한다.
     *
     * @return 새로운 UUIDv7 기반 UserId
     */
    public static UserId generate() {
        return new UserId(UuidV7Generator.generate());
    }

    @Override
    public String toString() {
        return value;
    }
}
