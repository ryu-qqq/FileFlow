package com.ryuqq.fileflow.application.session.port.in.query;

import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;

/**
 * UploadSession 단건 조회 UseCase.
 *
 * <p>CQRS Query Side - 업로드 세션 상세 정보 조회
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단일 업로드 세션 메타데이터 조회
 *   <li>테넌트 스코프 검증
 *   <li>Multipart 세션의 경우 Part 정보 포함
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>업로드 진행 상태 확인
 *   <li>Multipart 업로드 재개 시 완료된 Part 확인
 * </ul>
 */
public interface GetUploadSessionUseCase {

    /**
     * UploadSession 단건 조회.
     *
     * @param query 조회 Query
     * @return 업로드 세션 상세 응답
     */
    UploadSessionDetailResponse execute(GetUploadSessionQuery query);
}
