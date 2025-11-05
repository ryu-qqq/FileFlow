package com.ryuqq.fileflow.domain.download;

/**
 * Idempotency Key Value Object
 *
 * <p>멱등성 키를 Value Object로 래핑하여 타입 안전성과 검증 로직을 보장합니다.</p>
 * <p>중복 이벤트 발행을 방지하기 위한 고유 식별자입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ Compact Constructor로 검증 로직 캡슐화</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * <h3>생성 규칙</h3>
 * <ul>
 *   <li>null 또는 빈 문자열 불가</li>
 *   <li>최대 255자 제한</li>
 *   <li>공백만으로 구성 불가</li>
 * </ul>
 *
 * @param value 멱등성 키 문자열
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record IdempotencyKey(String value) {

    /**
     * 최대 길이 제한
     */
    private static final int MAX_LENGTH = 255;

    /**
     * Compact Constructor - 검증 로직
     *
     * @throws IllegalArgumentException value가 null, 빈 문자열, 또는 최대 길이 초과인 경우
     */
    public IdempotencyKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Idempotency Key는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Idempotency Key는 " + MAX_LENGTH + "자를 초과할 수 없습니다"
            );
        }
    }

    public static IdempotencyKey of(String value){
        return new IdempotencyKey(value);
    }

    /**
     * 두 IdempotencyKey가 동일한지 확인합니다.
     *
     * <p>Law of Demeter 준수: 비교 로직 캡슐화</p>
     *
     * @param other 비교 대상
     * @return 동일하면 true
     */
    public boolean isSameAs(IdempotencyKey other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }
}
