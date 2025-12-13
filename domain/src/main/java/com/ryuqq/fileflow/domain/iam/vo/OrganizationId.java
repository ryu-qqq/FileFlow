package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;

/**
 * 조직 ID Value Object.
 *
 * <p>UUIDv7 기반의 조직 식별자로, 다음 특성을 가진다:
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
 * <p><strong>S3 경로 사용</strong>:
 *
 * <p>S3 경로에서 조직 식별에 사용될 때는 전체 UUID를 사용한다. 예: {@code setof/seller-01912345-6789-7abc.../}
 *
 * @param value UUIDv7 문자열
 * @author development-team
 * @since 1.0.0
 */
public record OrganizationId(String value) {

    /**
     * Compact Constructor (검증 로직).
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 UUIDv7인 경우
     */
    public OrganizationId {
        UuidV7Generator.validate(value, "OrganizationId");
    }

    /**
     * 값 기반 생성.
     *
     * @param value UUIDv7 문자열
     * @return OrganizationId
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static OrganizationId of(String value) {
        return new OrganizationId(value);
    }

    /**
     * 새로운 OrganizationId를 생성한다.
     *
     * @return 새로운 UUIDv7 기반 OrganizationId
     */
    public static OrganizationId generate() {
        return new OrganizationId(UuidV7Generator.generate());
    }

    @Override
    public String toString() {
        return value;
    }
}
