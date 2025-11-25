package com.ryuqq.fileflow.application.asset.port.in.query;

import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;

/**
 * FileAsset 단건 조회 UseCase.
 *
 * <p>CQRS Query Side - 파일 자산 상세 정보 조회
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단일 파일 자산 메타데이터 조회
 *   <li>테넌트/조직 스코프 검증
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>파일 상세 페이지 조회
 *   <li>파일 처리 상태 확인
 * </ul>
 */
public interface GetFileAssetUseCase {

    /**
     * FileAsset 단건 조회.
     *
     * @param query 조회 Query
     * @return 파일 자산 응답
     */
    FileAssetResponse execute(GetFileAssetQuery query);
}
