package com.ryuqq.fileflow.application.asset.port.in.query;

import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;

/**
 * FileAsset 목록 조회 UseCase.
 *
 * <p>CQRS Query Side - 페이징 및 필터링이 적용된 파일 자산 목록 조회
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>페이징된 파일 자산 목록 조회
 *   <li>상태, 카테고리 필터 적용
 *   <li>테넌트/조직 스코프 검증
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>관리자 파일 자산 목록 페이지
 *   <li>파일 검색 결과
 * </ul>
 */
public interface GetFileAssetsUseCase {

    /**
     * FileAsset 목록 조회.
     *
     * @param query 목록 조회 Query
     * @return 페이징된 파일 자산 목록 응답
     */
    PageResponse<FileAssetResponse> execute(ListFileAssetsQuery query);
}
