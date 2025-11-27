package com.ryuqq.fileflow.application.download.port.in.query;

import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;

/**
 * 외부 다운로드 조회 UseCase.
 *
 * <p>CQRS Query Side - 외부 다운로드 상태 조회
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>ExternalDownload 상세 정보 조회
 *   <li>테넌트 스코프 검증
 * </ul>
 */
public interface GetExternalDownloadUseCase {

    /**
     * 외부 다운로드 상태를 조회합니다.
     *
     * @param query 조회 Query
     * @return 외부 다운로드 상세 응답
     */
    ExternalDownloadDetailResponse execute(GetExternalDownloadQuery query);
}
