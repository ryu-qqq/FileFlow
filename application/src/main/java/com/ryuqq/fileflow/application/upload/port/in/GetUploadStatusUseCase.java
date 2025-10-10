package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;

/**
 * 업로드 진행률 조회 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 업로드 세션의 진행 상태를 조회하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 세션 ID로 업로드 진행 상태 조회
 * 2. 세션이 존재하지 않으면 예외 발생
 * 3. 만료된 세션도 조회 가능 (상태 확인용)
 * 4. 진행률은 상태 기반으로 계산 (PENDING: 0%, UPLOADING: 50%, COMPLETED: 100%)
 *
 * @author sangwon-ryu
 */
public interface GetUploadStatusUseCase {

    /**
     * 세션 ID로 업로드 진행 상태를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 업로드 진행 상태 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    UploadStatusResponse getUploadStatus(String sessionId);
}
