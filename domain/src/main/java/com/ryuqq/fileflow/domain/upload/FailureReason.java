package com.ryuqq.fileflow.domain.upload;

/**
 * FailureReason Value Object
 * 업로드 실패 사유를 나타내는 값 객체
 *
 * <p>실패 사유는 업로드 세션이 실패했을 때 그 이유를 기록합니다.
 * 디버깅, 모니터링, 사용자 안내에 활용됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>실패 사유는 선택적 값입니다 (실패 시에만 존재)</li>
 *   <li>빈 문자열은 허용되지 않습니다</li>
 *   <li>최대 길이 500자 (상세한 에러 메시지 허용)</li>
 * </ul>
 *
 * @param value 실패 사유
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FailureReason(String value) {

    /**
     * 실패 사유 최대 길이
     */
    public static final int MAX_LENGTH = 500;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 실패 사유가 빈 문자열이거나 최대 길이를 초과한 경우
     */
    public FailureReason {
        if (value != null) {
            if (value.isBlank()) {
                throw new IllegalArgumentException("실패 사유는 빈 문자열일 수 없습니다");
            }
            value = value.trim();

            if (value.length() > MAX_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("실패 사유는 %d자를 초과할 수 없습니다: %d", MAX_LENGTH, value.length())
                );
            }
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 실패 사유
     * @return FailureReason 인스턴스
     * @throws IllegalArgumentException 실패 사유가 유효하지 않은 경우
     */
    public static FailureReason of(String value) {
        return new FailureReason(value);
    }

    /**
     * 실패 사유가 존재하는지 확인
     *
     * @return 실패 사유가 있으면 true
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * 실패 사유가 특정 키워드를 포함하는지 확인
     * 에러 타입 분류에 활용
     *
     * @param keyword 검색할 키워드
     * @return 키워드가 포함되면 true
     */
    public boolean contains(String keyword) {
        if (value == null || keyword == null || keyword.isBlank()) {
            return false;
        }
        return value.toLowerCase().contains(keyword.toLowerCase());
    }
}
