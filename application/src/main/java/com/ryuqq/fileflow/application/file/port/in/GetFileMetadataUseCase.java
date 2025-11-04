package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.response.FileMetadataResponse;

/**
 * 파일 메타데이터 조회 UseCase
 *
 * <p>CQRS Query Side - 파일 상세 정보 조회</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>단일 파일의 메타데이터 조회</li>
 *   <li>테넌트/조직 스코프 검증</li>
 *   <li>Soft Delete 필터링</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>파일 상세 페이지 조회</li>
 *   <li>다운로드 전 파일 정보 확인</li>
 *   <li>파일 권한 검증</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface GetFileMetadataUseCase {

    /**
     * 파일 메타데이터 조회
     *
     * @param query 파일 메타데이터 조회 Query
     * @return 파일 메타데이터 응답
     * @throws com.ryuqq.fileflow.domain.exception.FileNotFoundException 파일이 존재하지 않는 경우
     * @throws com.ryuqq.fileflow.domain.exception.AccessDeniedException 테넌트/조직 스코프 위반
     */
    FileMetadataResponse execute(FileMetadataQuery query);
}
