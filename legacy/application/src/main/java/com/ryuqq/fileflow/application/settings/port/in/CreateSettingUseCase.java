package com.ryuqq.fileflow.application.settings.port.in;

/**
 * CreateSettingUseCase - 설정 생성 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller에서 이 인터페이스를 의존하여 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>새로운 Setting 생성</li>
 *   <li>중복 검증 ((key, level, contextId) 복합 유니크)</li>
 *   <li>SettingLevel 및 ValueType Enum 검증</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command UseCase - 데이터 변경 (Create)</li>
 *   <li>구현체: SettingCommandService</li>
 * </ul>
 *
 * <p><strong>내부 Nested Record 패턴:</strong></p>
 * <ul>
 *   <li>✅ Command/Response를 인터페이스 내부에 정의 (응집도 향상)</li>
 *   <li>✅ Command에 Compact Constructor로 validation 수행</li>
 *   <li>✅ Response는 불변 데이터 전달용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public interface CreateSettingUseCase {

    /**
     * 설정 생성
     *
     * @param command 설정 생성 Command
     * @return Response 생성된 설정 정보
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     * @throws IllegalArgumentException level 또는 valueType이 유효하지 않은 경우
     * @throws IllegalStateException 동일한 (key, level, contextId) 조합이 이미 존재하는 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    Response execute(Command command);

    /**
     * Create Setting Command - 내부 Record
     *
     * <p>설정 생성 요청 정보를 담는 Command 객체입니다.</p>
     *
     * <p><strong>필드 규칙:</strong></p>
     * <ul>
     *   <li>key: 필수, 빈 문자열 불가</li>
     *   <li>value: 필수, null 불가</li>
     *   <li>level: 필수, "ORG", "TENANT", "DEFAULT" 중 하나</li>
     *   <li>contextId: ORG/TENANT 레벨은 필수, DEFAULT는 null</li>
     *   <li>valueType: 필수, "STRING", "NUMBER", "BOOLEAN", "JSON" 중 하나</li>
     *   <li>secret: 필수, true/false (기본값 false)</li>
     * </ul>
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨 (ORG, TENANT, DEFAULT)
     * @param contextId 컨텍스트 ID (orgId 또는 tenantId, DEFAULT는 null)
     * @param valueType 값 타입 (STRING, NUMBER, BOOLEAN, JSON)
     * @param secret 비밀 설정 여부
     * @author ryu-qqq
     * @since 2025-10-26
     */
    record Command(
        String key,
        String value,
        String level,
        Long contextId,
        String valueType,
        boolean secret
    ) {
        /**
         * Compact Constructor - 비즈니스 규칙 검증
         *
         * <p>Record 생성 시 자동으로 호출되어 필드 검증을 수행합니다.</p>
         *
         * @throws IllegalArgumentException 필수 필드가 null이거나 빈 문자열인 경우
         * @author ryu-qqq
         * @since 2025-10-26
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
            if (valueType == null || valueType.isBlank()) {
                throw new IllegalArgumentException("설정 타입은 필수입니다");
            }
        }
    }

    /**
     * Create Setting Response - 내부 Record
     *
     * <p>생성된 설정 정보를 담는 Response 객체입니다.</p>
     *
     * @param id 설정 ID
     * @param key 설정 키
     * @param value 설정 값
     * @param valueType 설정 타입 (STRING, NUMBER, BOOLEAN, JSON)
     * @param level 설정 레벨 (ORG, TENANT, DEFAULT)
     * @param contextId 컨텍스트 ID
     * @param secret 비밀 설정 여부
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @author ryu-qqq
     * @since 2025-10-26
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
