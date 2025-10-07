package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;

/**
 * 업로드 세션 완료 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 업로드 세션을 완료 처리하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. PENDING 또는 UPLOADING 상태의 세션만 완료 가능
 * 2. 만료된 세션은 완료 불가
 * 3. 세션 상태를 COMPLETED로 변경
 * 4. 파일 메타데이터 저장 (향후 구현)
 *
 * @author sangwon-ryu
 */
public interface CompleteUploadSessionUseCase {

    /**
     * 업로드 세션을 완료 처리합니다.
     *
     * @param sessionId 세션 ID
     * @return 완료된 세션 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 완료 가능한 상태가 아니거나 세션이 만료된 경우
     */
    UploadSessionResponse completeSession(String sessionId);
}
