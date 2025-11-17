package com.ryuqq.fileflow.domain.vo;

import java.util.Set;

/**
 * FileCategory Value Object
 * <p>
 * 파일 카테고리를 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 검증 규칙:
 * - 허용 목록: "상품", "전시영역", "외부몰연동", "문서"
 * - Null 입력 시: 기본값 "기타" 반환
 * - Empty/허용되지 않은 값: 예외 발생
 * </p>
 */
public record FileCategory(String value) {

    /**
     * 기본 카테고리
     */
    private static final String DEFAULT_CATEGORY = "기타";

    /**
     * 허용된 카테고리 목록
     */
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "상품",
            "전시영역",
            "외부몰연동",
            "문서",
            DEFAULT_CATEGORY
    );

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * 카테고리 검증 로직을 수행합니다.
     * </p>
     */
    public FileCategory {
        validateAllowedCategory(value);
    }

    /**
     * 정적 팩토리 메서드 (of 패턴)
     * <p>
     * Null 입력 시 기본값 "기타"를 반환합니다.
     * </p>
     *
     * @param value 파일 카테고리
     * @return FileCategory VO
     * @throws IllegalArgumentException 허용되지 않은 카테고리일 때
     */
    public static FileCategory of(String value) {
        if (value == null) {
            return new FileCategory(DEFAULT_CATEGORY);
        }
        return new FileCategory(value);
    }

    /**
     * 기본 카테고리 생성 팩토리 메서드
     *
     * @return 기본값 "기타"를 가진 FileCategory
     */
    public static FileCategory ofDefault() {
        return new FileCategory(DEFAULT_CATEGORY);
    }

    /**
     * 허용 목록 검증
     */
    private static void validateAllowedCategory(String value) {
        if (value == null || value.isBlank() || !ALLOWED_CATEGORIES.contains(value)) {
            throw new IllegalArgumentException(
                    String.format("허용되지 않은 파일 카테고리입니다: %s (허용 목록: %s)",
                            value, String.join(", ", ALLOWED_CATEGORIES))
            );
        }
    }

    /**
     * 카테고리 값 조회
     *
     * @return 카테고리 문자열
     */
    public String getValue() {
        return value;
    }
}
