package com.ryuqq.fileflow.application.session.port.in.query;

import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;

/**
 * UploadSession 목록 조회 UseCase.
 *
 * <p>CQRS Query Side - 업로드 세션 목록 조회 (페이징)
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>업로드 세션 목록 조회 (상태, 타입 필터링)
 *   <li>테넌트/조직 스코프 적용
 *   <li>Slice 기반 페이징 응답
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>Admin 대시보드 업로드 현황 조회
 *   <li>사용자 업로드 이력 조회
 * </ul>
 */
public interface GetUploadSessionsUseCase {

    /**
     * UploadSession 목록 조회.
     *
     * @param query 조회 Query
     * @return 업로드 세션 목록 (Slice 응답)
     */
    SliceResponse<UploadSessionResponse> execute(ListUploadSessionsQuery query);
}
