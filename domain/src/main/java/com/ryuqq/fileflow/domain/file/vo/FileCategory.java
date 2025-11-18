package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.iam.vo.UploaderType;

import java.util.Set;

/**
 * FileCategory Value Object
 * <p>
 * 파일 카테고리를 UploaderType별로 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * UploaderType별 허용 카테고리:
 * - ADMIN: banner, event, excel, notice, default
 * - SELLER: product, review, promotion, default
 * - CUSTOMER: default만 허용
 * </p>
 */
public record FileCategory(String value) {

    /**
     * 기본 카테고리
     */
    private static final String DEFAULT_CATEGORY = "default";

    /**
     * Admin 허용 카테고리 목록
     */
    private static final Set<String> ADMIN_CATEGORIES = Set.of(
            "banner",
            "event",
            "excel",
            "notice",
            DEFAULT_CATEGORY
    );

    /**
     * Seller 허용 카테고리 목록
     */
    private static final Set<String> SELLER_CATEGORIES = Set.of(
            "product",
            "review",
            "promotion",
            DEFAULT_CATEGORY
    );

    /**
     * Customer 허용 카테고리 목록
     */
    private static final Set<String> CUSTOMER_CATEGORIES = Set.of(
            DEFAULT_CATEGORY
    );

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * 카테고리 검증 로직을 수행합니다.
     * Null이나 빈 문자열은 기본값으로 처리되지 않고 예외 발생합니다.
     * </p>
     */
    public FileCategory {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("파일 카테고리는 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * UploaderType별 카테고리 검증 및 생성
     * <p>
     * Null 입력 시 기본값 "default"를 반환합니다.
     * </p>
     *
     * @param value 파일 카테고리
     * @param uploaderType 업로더 타입
     * @return FileCategory VO
     * @throws IllegalArgumentException UploaderType에서 허용하지 않는 카테고리일 때
     */
    public static FileCategory of(String value, UploaderType uploaderType) {
        // Null이거나 빈 문자열이면 기본값 반환
        String normalized = (value != null && !value.isBlank())
                ? value.toLowerCase()
                : DEFAULT_CATEGORY;

        // UploaderType별 허용 카테고리 확인
        Set<String> allowedCategories = switch (uploaderType) {
            case ADMIN -> ADMIN_CATEGORIES;
            case SELLER -> SELLER_CATEGORIES;
            case CUSTOMER -> CUSTOMER_CATEGORIES;
        };

        if (!allowedCategories.contains(normalized)) {
            throw new IllegalArgumentException(
                    String.format("%s에서 지원하지 않는 카테고리입니다: %s (허용 목록: %s)",
                            uploaderType,
                            value,
                            String.join(", ", allowedCategories))
            );
        }

        return new FileCategory(normalized);
    }

    /**
     * 기본 카테고리 생성 팩토리 메서드
     *
     * @return 기본값 "default"를 가진 FileCategory
     */
    public static FileCategory defaultCategory() {
        return new FileCategory(DEFAULT_CATEGORY);
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
