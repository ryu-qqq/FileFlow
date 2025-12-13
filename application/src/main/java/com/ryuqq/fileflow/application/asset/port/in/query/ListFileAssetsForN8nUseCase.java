package com.ryuqq.fileflow.application.asset.port.in.query;

import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsForN8nQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetForN8nResponse;
import java.util.List;

/**
 * N8N 워크플로우용 FileAsset 목록 조회 UseCase.
 *
 * <p>CQRS Query Side - N8N 처리 대상 파일 목록 조회
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>RESIZED 상태의 FileAsset 목록 조회
 *   <li>N8N 워크플로우에 필요한 정보 제공
 *   <li>처리된 파일 (ProcessedFileAsset) 정보 포함
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>N8N 워크플로우 Polling 조회
 *   <li>배치 처리 대상 파일 목록 조회
 * </ul>
 *
 * <p><strong>반환 정보:</strong>
 *
 * <ul>
 *   <li>원본 FileAsset 메타데이터
 *   <li>처리된 이미지 (리사이징 결과) 목록
 *   <li>S3 위치 정보
 * </ul>
 */
public interface ListFileAssetsForN8nUseCase {

    /**
     * N8N 처리 대상 FileAsset 목록 조회.
     *
     * @param query 조회 조건 Query
     * @return N8N 처리용 FileAsset 응답 목록
     */
    List<FileAssetForN8nResponse> execute(ListFileAssetsForN8nQuery query);
}
