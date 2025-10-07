package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;

/**
 * 업로드 세션 취소 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 업로드 세션을 취소하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. PENDING 또는 UPLOADING 상태의 세션만 취소 가능
 * 2. COMPLETED 또는 FAILED 상태는 취소 불가
 * 3. 세션 상태를 CANCELLED로 변경
 * 4. 임시 파일 정리 (향후 구현)
 *
 * @author sangwon-ryu
 */
public interface CancelUploadSessionUseCase {

    /**
     * 업로드 세션을 취소합니다.
     *
     * @param sessionId 세션 ID
     * @return 취소된 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 취소 불가능한 상태인 경우
     */
    UploadSessionResponse cancelSession(String sessionId);
}
