package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;

/**
 * 업로드 세션 실패 처리 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 업로드 세션을 실패 상태로 전환하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. PENDING 또는 UPLOADING 상태의 세션만 실패 처리 가능
 * 2. 세션 상태를 FAILED로 변경
 * 3. 실패 사유를 기록
 * 4. UploadFailedEvent 도메인 이벤트 발행
 * 5. 부분 업로드된 파일 정리 (향후 구현)
 *
 * @author sangwon-ryu
 */
public interface FailUploadUseCase {

    /**
     * 업로드 세션을 실패 처리합니다.
     *
     * @param sessionId 세션 ID
     * @param reason 실패 사유
     * @return 실패 처리된 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    UploadSessionResponse failSession(String sessionId, String reason);
}
