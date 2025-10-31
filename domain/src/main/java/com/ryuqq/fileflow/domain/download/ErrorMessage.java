package com.ryuqq.fileflow.domain.download;

/**
 * ErrorMessage Value Object
 * 다운로드 에러 메시지를 나타내는 값 객체
 *
 * <p>에러 메시지는 외부 다운로드 실패 시 상세한 에러 내용을 기록합니다.
 * 디버깅, 모니터링, 사용자 안내에 활용됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>에러 메시지는 선택적 값입니다 (에러 발생 시에만 존재)</li>
 *   <li>빈 문자열은 허용되지 않습니다</li>
 *   <li>최대 길이 1000자 (상세한 스택 트레이스 허용)</li>
 * </ul>
 *
 * @param value 에러 메시지
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ErrorMessage(String value) {

    /**
     * 에러 메시지 최대 길이
     */
    public static final int MAX_LENGTH = 1000;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 에러 메시지가 빈 문자열이거나 최대 길이를 초과한 경우
     */
    public ErrorMessage {
        if (value != null) {
            if (value.isBlank()) {
                throw new IllegalArgumentException("에러 메시지는 빈 문자열일 수 없습니다");
            }
            value = value.trim();

            if (value.length() > MAX_LENGTH) {
                // 최대 길이 초과 시 잘라내기 (에러 메시지 손실 방지)
                value = value.substring(0, MAX_LENGTH);
            }
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 에러 메시지
     * @return ErrorMessage 인스턴스
     * @throws IllegalArgumentException 에러 메시지가 유효하지 않은 경우
     */
    public static ErrorMessage of(String value) {
        return new ErrorMessage(value);
    }

    /**
     * 에러 메시지가 존재하는지 확인
     *
     * @return 에러 메시지가 있으면 true
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * 에러 메시지가 특정 키워드를 포함하는지 확인
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

    /**
     * 축약된 메시지 반환
     * UI에 표시할 때 사용
     *
     * @param maxLength 최대 길이
     * @return 축약된 메시지 (말줄임표 포함)
     */
    public String abbreviated(int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
