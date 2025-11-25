package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import java.time.LocalDateTime;

/**
 * Part 업로드 완료 표시 API Response.
 *
 * <p>Part 업로드 완료 기록 결과를 반환합니다.
 *
 * <p><strong>설계 결정</strong>:
 *
 * <ul>
 *   <li>진행률 정보 제외: 병렬 요청 시 정확하지 않음
 *   <li>클라이언트가 진행률 관리: 전송한 Part 수로 직접 계산
 * </ul>
 *
 * @param sessionId 세션 ID
 * @param partNumber Part 번호 (1-based)
 * @param etag Part ETag
 * @param uploadedAt 업로드 시각 (UTC)
 * @author development-team
 * @since 1.0.0
 */
public record MarkPartUploadedApiResponse(
        String sessionId, int partNumber, String etag, LocalDateTime uploadedAt) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param partNumber Part 번호
     * @param etag Part ETag
     * @param uploadedAt 업로드 시각
     * @return MarkPartUploadedApiResponse
     */
    public static MarkPartUploadedApiResponse of(
            String sessionId, int partNumber, String etag, LocalDateTime uploadedAt) {
        return new MarkPartUploadedApiResponse(sessionId, partNumber, etag, uploadedAt);
    }
}
