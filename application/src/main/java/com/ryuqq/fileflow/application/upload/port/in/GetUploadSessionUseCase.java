package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;

/**
 * 업로드 세션 조회 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 업로드 세션을 조회하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 세션 ID로 업로드 세션 조회
 * 2. 세션이 존재하지 않으면 예외 발생
 * 3. 만료된 세션도 조회 가능 (상태 확인용)
 *
 * @author sangwon-ryu
 */
public interface GetUploadSessionUseCase {

    /**
     * 세션 ID로 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 업로드 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    UploadSessionResponse getSession(String sessionId);
}
