package com.ryuqq.adapter.in.rest.example.dto.response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Example 페이지 조회 REST API 응답 DTO (Offset 기반)
 *
 * <p>REST API Layer 전용 응답 DTO로, Application Layer의 PageResponse를 변환하여 사용합니다.</p>
 *
 * <p><strong>Offset 기반 페이지네이션:</strong></p>
 * <ul>
 *   <li>전통적인 페이지 번호 기반 페이징</li>
 *   <li>전체 개수와 페이지 정보 제공</li>
 *   <li>관리자 페이지에 적합</li>
 * </ul>
 *
 * <p><strong>응답 형식:</strong></p>
 * <pre>{@code
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 100,
 *   "totalPages": 5,
 *   "first": true,
 *   "last": false
 * }
 * }</pre>
 *
 * @param content 현재 페이지의 데이터 목록
 * @param page 현재 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @param totalElements 전체 데이터 개수
 * @param totalPages 전체 페이지 수
 * @param first 첫 페이지 여부
 * @param last 마지막 페이지 여부
 * @author windsurf
 * @since 1.0.0
 */
public record ExamplePageApiResponse(
    List<ExampleDetailApiResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

    /**
     * Compact Constructor - Defensive Copy
     */
    public ExamplePageApiResponse {
        content = List.copyOf(content);  // Immutability 보장
    }

    /**
     * Application Layer PageResponse로부터 REST API PageResponse 생성
     *
     * @param appPageResponse Application Layer의 PageResponse
     * @return REST API Layer의 ExamplePageApiResponse
     */
    public static ExamplePageApiResponse from(
            com.ryuqq.application.common.dto.response.PageResponse<com.ryuqq.application.example.dto.response.ExampleDetailResponse> appPageResponse) {

        List<ExampleDetailApiResponse> content = appPageResponse.content().stream()
            .map(ExampleDetailApiResponse::from)
            .collect(Collectors.toList());

        return new ExamplePageApiResponse(
            content,
            appPageResponse.page(),
            appPageResponse.size(),
            appPageResponse.totalElements(),
            appPageResponse.totalPages(),
            appPageResponse.first(),
            appPageResponse.last()
        );
    }
}
