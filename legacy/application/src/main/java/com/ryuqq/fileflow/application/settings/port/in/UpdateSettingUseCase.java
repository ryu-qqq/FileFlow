package com.ryuqq.fileflow.application.settings.port.in;

/**
 * Update Setting UseCase
 *
 * <p>설정 값을 업데이트하는 Command UseCase입니다.</p>
 *
 * <p><strong>CQRS 패턴 준수:</strong></p>
 * <ul>
 *   <li>✅ Command/Response - 내부 Record로 정의</li>
 *   <li>✅ 상태 변경 (쓰기 작업)</li>
 *   <li>✅ Response 반환 (수정된 설정 정보)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface UpdateSettingUseCase {

    /**
     * 설정을 업데이트합니다.
     *
     * @param command 업데이트 Command
     * @return 업데이트된 설정 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Response execute(Command command);

    /**
     * Update Setting Command - 내부 Record
     *
     * <p>설정 업데이트 요청 정보를 담는 Command 객체입니다.</p>
     *
     * @param key 설정 키
     * @param value 새로운 설정 값
     * @param level 설정 레벨 (ORG, TENANT, DEFAULT)
     * @param contextId 컨텍스트 ID (orgId 또는 tenantId, DEFAULT는 null)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    record Command(
        String key,
        String value,
        String level,
        Long contextId
    ) {
        /**
         * Compact Constructor - 비즈니스 규칙 검증
         */
        public Command {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("설정 키는 필수입니다");
            }
            if (value == null) {
                throw new IllegalArgumentException("설정 값은 필수입니다");
            }
            if (level == null || level.isBlank()) {
                throw new IllegalArgumentException("설정 레벨은 필수입니다");
            }
        }
    }

    /**
     * Update Setting Response - 내부 Record
     *
     * <p>업데이트된 설정 정보를 담는 Response 객체입니다.</p>
     *
     * @param id 설정 ID
     * @param key 설정 키
     * @param value 설정 값 (비밀 설정은 마스킹됨)
     * @param valueType 설정 타입 (STRING, NUMBER, BOOLEAN, JSON)
     * @param level 설정 레벨 (ORG, TENANT, DEFAULT)
     * @param contextId 컨텍스트 ID
     * @param secret 비밀 설정 여부
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @author ryu-qqq
     * @since 2025-10-25
     */
    record Response(
        Long id,
        String key,
        String value,
        String valueType,
        String level,
        Long contextId,
        boolean secret,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {}
}
