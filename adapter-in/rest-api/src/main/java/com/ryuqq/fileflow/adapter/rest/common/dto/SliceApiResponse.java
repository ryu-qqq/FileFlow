package com.ryuqq.adapter.in.rest.example.dto.response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Example 슬라이스 조회 REST API 응답 DTO (Cursor 기반)
 *
 * <p>REST API Layer 전용 응답 DTO로, Application Layer의 SliceResponse를 변환하여 사용합니다.</p>
 *
 * <p><strong>Cursor 기반 페이지네이션:</strong></p>
 * <ul>
 *   <li>무한 스크롤 UI에 적합</li>
 *   <li>COUNT 쿼리 불필요 (고성능)</li>
 *   <li>다음 페이지 존재 여부만 제공</li>
 *   <li>일반 사용자 페이지에 적합</li>
 * </ul>
 *
 * <p><strong>응답 형식:</strong></p>
 * <pre>{@code
 * {
 *   "content": [...],
 *   "size": 20,
 *   "hasNext": true,
 *   "nextCursor": "xyz"
 * }
 * }</pre>
 *
 * @param content 현재 슬라이스의 데이터 목록
 * @param size 슬라이스 크기
 * @param hasNext 다음 슬라이스 존재 여부
 * @param nextCursor 다음 슬라이스 조회를 위한 커서
 * @author windsurf
 * @since 1.0.0
 */
public record ExampleSliceApiResponse(
    List<ExampleDetailApiResponse> content,
    int size,
    boolean hasNext,
    String nextCursor
) {

    /**
     * Compact Constructor - Defensive Copy
     */
    public ExampleSliceApiResponse {
        content = List.copyOf(content);  // Immutability 보장
    }

    /**
     * Application Layer SliceResponse로부터 REST API SliceResponse 생성
     *
     * @param appSliceResponse Application Layer의 SliceResponse
     * @return REST API Layer의 ExampleSliceApiResponse
     */
    public static ExampleSliceApiResponse from(
            com.ryuqq.application.common.dto.response.SliceResponse<com.ryuqq.application.example.dto.response.ExampleDetailResponse> appSliceResponse) {

        List<ExampleDetailApiResponse> content = appSliceResponse.content().stream()
            .map(ExampleDetailApiResponse::from)
            .collect(Collectors.toList());

        return new ExampleSliceApiResponse(
            content,
            appSliceResponse.size(),
            appSliceResponse.hasNext(),
            appSliceResponse.nextCursor()
        );
    }
}
