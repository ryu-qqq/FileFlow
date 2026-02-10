package com.ryuqq.fileflow.application.common.dto.query;

/**
 * 공통 커서 기반 페이징 파라미터
 *
 * <p>모든 Cursor Query에서 공통으로 사용하는 파라미터입니다. Composition 방식으로 사용되며, SearchParams DTO는 delegate 메서드를 통해
 * 이 파라미터들을 노출해야 합니다.
 *
 * <p><strong>사용 규칙:</strong>
 *
 * <ul>
 *   <li>SearchParams는 이 record를 필드로 포함해야 함
 *   <li>SearchParams는 delegate 메서드를 제공하여 직접 접근 허용
 *   <li>중첩 접근(params.cursorParams().cursor()) 금지 - delegate 사용
 * </ul>
 *
 * <p><strong>기본값:</strong>
 *
 * <ul>
 *   <li>cursor: null (첫 페이지)
 *   <li>size: 20
 * </ul>
 *
 * <p><strong>커서 타입:</strong>
 *
 * <ul>
 *   <li>API 경계에서는 String으로 통일 (Long, UUID v7 등 다양한 타입 지원)
 *   <li>도메인별 실제 타입 변환은 Factory에서 수행
 * </ul>
 *
 * @param cursor 커서 값 (마지막 항목의 식별자, null 또는 빈 문자열이면 첫 페이지)
 * @param size 페이지 크기 (기본: 20)
 * @author development-team
 * @since 1.0.0
 */
public record CommonCursorParams(String cursor, Integer size) {

    private static final Integer DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    /**
     * Compact Constructor - null 방어를 위한 기본값 설정
     *
     * <p>REST API 경계에서 기본값이 설정되지만, 혹시 모를 null 전달에 대비하여 Application 레이어에서도 방어합니다.
     */
    public CommonCursorParams {
        if (size == null) {
            size = DEFAULT_SIZE;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    /**
     * CommonCursorParams 생성 (static factory method)
     *
     * @param cursor 커서 값 (null 또는 빈 문자열이면 첫 페이지)
     * @param size 페이지 크기
     * @return CommonCursorParams 인스턴스
     */
    public static CommonCursorParams of(String cursor, Integer size) {
        return new CommonCursorParams(cursor, size);
    }

    /**
     * 첫 페이지 요청 생성
     *
     * @param size 페이지 크기
     * @return CommonCursorParams (cursor=null)
     */
    public static CommonCursorParams first(Integer size) {
        return new CommonCursorParams(null, size);
    }

    /**
     * 기본 설정 CommonCursorParams 생성
     *
     * @return CommonCursorParams (cursor=null, size=DEFAULT_SIZE)
     */
    public static CommonCursorParams defaultPage() {
        return new CommonCursorParams(null, DEFAULT_SIZE);
    }

    /**
     * 첫 페이지인지 확인
     *
     * @return cursor가 null이거나 빈 문자열이면 true
     */
    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }

    /**
     * 커서가 있는지 확인
     *
     * @return cursor가 유효한 값이면 true
     */
    public boolean hasCursor() {
        return cursor != null && !cursor.isBlank();
    }
}
