package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.application.file.dto.response.FileListResponse;

/**
 * 파일 목록 조회 UseCase
 *
 * <p>CQRS Query Side - 페이징 및 필터링이 적용된 파일 목록 조회</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>페이징된 파일 목록 조회</li>
 *   <li>다양한 필터 조건 적용 (상태, 가시성, 소유자, 기간)</li>
 *   <li>테넌트/조직 스코프 검증</li>
 *   <li>Soft Delete 필터링</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>사용자 파일 목록 페이지</li>
 *   <li>관리자 파일 관리 페이지</li>
 *   <li>파일 검색 결과</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>인덱스 활용: (tenant_id, organization_id, uploaded_at)</li>
 *   <li>페이징 처리: LIMIT/OFFSET 또는 Cursor 기반</li>
 *   <li>Count 쿼리 최적화: 필요 시에만 실행</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface GetFilesUseCase {

    /**
     * 파일 목록 조회
     *
     * @param query 파일 목록 조회 Query
     * @return 페이징된 파일 목록 응답
     */
    FileListResponse execute(ListFilesQuery query);
}
