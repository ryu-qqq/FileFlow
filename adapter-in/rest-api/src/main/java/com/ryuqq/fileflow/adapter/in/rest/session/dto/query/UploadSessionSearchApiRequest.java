package com.ryuqq.fileflow.adapter.in.rest.session.dto.query;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * UploadSession 검색 API Request.
 *
 * <p>업로드 세션 목록 조회를 위한 검색 조건을 담습니다.
 *
 * @param status 세션 상태 (Optional)
 * @param uploadType 업로드 타입 (SINGLE/MULTIPART, Optional)
 * @param page 페이지 번호 (0부터 시작, 기본값 0)
 * @param size 페이지 크기 (기본값 20, 최대 100)
 * @author development-team
 * @since 1.0.0
 */
public record UploadSessionSearchApiRequest(
        SessionStatus status,
        UploadTypeFilter uploadType,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                Integer size) {

    /** Compact Constructor - 기본값 설정. */
    public UploadSessionSearchApiRequest {
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
    }

    /** 업로드 타입 필터. */
    public enum UploadTypeFilter {
        SINGLE,
        MULTIPART
    }
}
