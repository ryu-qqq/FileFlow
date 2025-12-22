package com.ryuqq.fileflow.application.download.port.in.query;

import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.application.download.dto.query.ListExternalDownloadsQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;

/**
 * 외부 다운로드 목록 조회 UseCase.
 *
 * <p>CQRS Query Side - 외부 다운로드 목록 조회
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>ExternalDownload 목록 조회 (페이징, 필터링)
 *   <li>테넌트 스코프 검증
 * </ul>
 */
public interface GetExternalDownloadsUseCase {

    /**
     * 외부 다운로드 목록을 조회합니다.
     *
     * @param query 목록 조회 Query
     * @return 외부 다운로드 목록 (페이징)
     */
    PageResponse<ExternalDownloadDetailResponse> execute(ListExternalDownloadsQuery query);
}
