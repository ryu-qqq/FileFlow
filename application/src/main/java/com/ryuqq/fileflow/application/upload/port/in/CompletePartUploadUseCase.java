package com.ryuqq.fileflow.application.upload.port.in;

/**
 * 멀티파트 업로드의 개별 파트 완료를 처리하는 UseCase
 *
 * 클라이언트가 개별 파트 업로드를 완료했을 때 호출되며,
 * Redis에 진행 상태를 기록하여 실시간 진행률 추적을 가능하게 합니다.
 *
 * 처리 흐름:
 * 1. 세션 조회 및 검증
 * 2. 멀티파트 업로드 여부 확인
 * 3. 파트 번호 유효성 검증
 * 4. Redis에 파트 완료 상태 기록
 *
 * @author sangwon-ryu
 */
public interface CompletePartUploadUseCase {

    /**
     * 멀티파트 업로드의 개별 파트 완료를 처리합니다.
     *
     * @param command 파트 완료 Command
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 멀티파트 업로드가 아니거나 유효하지 않은 파트 번호인 경우
     */
    void completePart(CompletePartCommand command);

    /**
     * 파트 완료 Command
     *
     * @param sessionId 세션 ID
     * @param partNumber 완료된 파트 번호 (1-based)
     */
    record CompletePartCommand(
            String sessionId,
            int partNumber
    ) {
        /**
         * Compact constructor로 검증 로직 수행
         */
        public CompletePartCommand {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new IllegalArgumentException("SessionId must not be null or empty");
            }

            if (partNumber < 1) {
                throw new IllegalArgumentException(
                        "PartNumber must be positive. Got: " + partNumber
                );
            }
        }
    }
}
