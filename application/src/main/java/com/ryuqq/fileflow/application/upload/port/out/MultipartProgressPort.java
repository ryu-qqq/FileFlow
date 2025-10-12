package com.ryuqq.fileflow.application.upload.port.out;

import java.time.Duration;

/**
 * 멀티파트 업로드 진행률 추적을 위한 Port 인터페이스
 *
 * Redis Hash를 활용하여 각 파트의 완료 상태를 추적하고,
 * 실시간 업로드 진행률을 계산합니다.
 *
 * 설계 원칙:
 * - Best Effort: Redis 저장 실패해도 업로드는 계속 진행
 * - TTL 기반 자동 만료: 세션 만료 시 진행 상태도 자동 삭제
 * - 원자적 연산: 멀티파트 환경에서 동시성 보장
 *
 * @author sangwon-ryu
 */
public interface MultipartProgressPort {

    /**
     * 멀티파트 업로드 진행 상태를 초기화합니다.
     *
     * Redis Hash 구조:
     * Key: "upload:multipart:progress:{sessionId}"
     * Fields:
     *   - "total_parts": 전체 파트 수
     *   - "part:1": "false" (초기값)
     *   - "part:2": "false"
     *   - ...
     *
     * @param sessionId 세션 ID
     * @param totalParts 전체 파트 수
     * @param ttl TTL (세션 만료 시간과 동일)
     */
    void initializeProgress(String sessionId, int totalParts, Duration ttl);

    /**
     * 특정 파트가 완료되었음을 표시합니다.
     *
     * Redis Hash의 "part:{partNumber}" 필드를 "true"로 업데이트합니다.
     *
     * @param sessionId 세션 ID
     * @param partNumber 완료된 파트 번호 (1-based)
     */
    void markPartCompleted(String sessionId, int partNumber);

    /**
     * 멀티파트 업로드 진행률을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return MultipartProgress (완료된 파트 수, 전체 파트 수, 진행률)
     */
    MultipartProgress getProgress(String sessionId);

    /**
     * 멀티파트 업로드 진행 상태를 삭제합니다.
     *
     * 세션 완료/실패/취소 시 호출됩니다.
     *
     * @param sessionId 세션 ID
     */
    void deleteProgress(String sessionId);

    /**
     * 멀티파트 업로드 진행률 정보를 나타내는 Value Object
     *
     * @param completedParts 완료된 파트 수
     * @param totalParts 전체 파트 수
     */
    record MultipartProgress(
            int completedParts,
            int totalParts
    ) {
        /**
         * 진행률을 퍼센트로 계산합니다.
         *
         * @return 진행률 (0-100)
         */
        public int getProgressPercentage() {
            if (totalParts == 0) {
                return 0;
            }
            return (int) Math.round((completedParts * 100.0) / totalParts);
        }

        /**
         * 모든 파트가 완료되었는지 확인합니다.
         *
         * @return 완료 여부
         */
        public boolean isCompleted() {
            return completedParts == totalParts && totalParts > 0;
        }
    }
}
