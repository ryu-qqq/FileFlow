package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * SliceApiResponse - 슬라이스 조회 REST API 응답 DTO (Cursor 기반)
 *
 * <p>REST API Layer 전용 응답 DTO입니다.
 *
 * <p><strong>Cursor 기반 페이지네이션:</strong>
 *
 * <ul>
 *   <li>무한 스크롤 UI에 적합
 *   <li>COUNT 쿼리 불필요 (고성능)
 *   <li>다음 페이지 존재 여부만 제공
 *   <li>일반 사용자 페이지에 적합
 * </ul>
 *
 * <p><strong>응답 형식:</strong>
 *
 * <pre>{@code
 * {
 *   "content": [...],
 *   "size": 20,
 *   "hasNext": true,
 *   "nextCursor": "xyz"
 * }
 * }</pre>
 *
 * @param <T> 콘텐츠 타입
 * @param content 현재 슬라이스의 데이터 목록
 * @param size 슬라이스 크기
 * @param hasNext 다음 슬라이스 존재 여부
 * @param nextCursor 다음 슬라이스 조회를 위한 커서
 * @author windsurf
 * @since 1.0.0
 */
@Schema(description = "슬라이스 조회 응답 (Cursor 기반)")
public record SliceApiResponse<T>(
        @Schema(description = "현재 슬라이스의 데이터 목록") List<T> content,
        @Schema(description = "슬라이스 크기", example = "20") int size,
        @Schema(description = "다음 슬라이스 존재 여부", example = "true") boolean hasNext,
        @Schema(description = "다음 슬라이스 조회를 위한 커서", example = "xyz", nullable = true)
                String nextCursor) {

    /** Compact Constructor - Defensive Copy */
    public SliceApiResponse {
        content = content != null ? List.copyOf(content) : List.of();
    }

    /**
     * 개별 파라미터를 사용하여 SliceApiResponse 생성
     *
     * <p>REST API Layer에서 Domain Layer 의존 없이 응답을 생성합니다.
     *
     * @param <T> 콘텐츠 타입
     * @param content 콘텐츠 목록
     * @param size 슬라이스 크기
     * @param hasNext 다음 슬라이스 존재 여부
     * @param nextCursor 다음 슬라이스 조회를 위한 커서
     * @return REST API Layer의 SliceApiResponse
     * @author development-team
     * @since 1.0.0
     */
    public static <T> SliceApiResponse<T> of(
            List<T> content, int size, boolean hasNext, String nextCursor) {
        return new SliceApiResponse<>(content, size, hasNext, nextCursor);
    }
}
