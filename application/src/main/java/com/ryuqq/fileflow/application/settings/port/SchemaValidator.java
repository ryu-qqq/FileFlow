package com.ryuqq.fileflow.application.settings.port;

import com.ryuqq.fileflow.domain.settings.SettingType;

/**
 * Schema Validator Port Interface
 *
 * <p>JSON 스키마 검증을 담당하는 Port 인터페이스입니다.</p>
 * <p>헥사고날 아키텍처: Application이 Port를 정의하고, Infrastructure가 Adapter를 구현합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Port-Adapter 패턴 - Application이 인터페이스 정의</li>
 *   <li>✅ 외부 라이브러리 추상화 - JSON 검증 로직 분리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface SchemaValidator {

    /**
     * 설정 값이 타입과 호환되는지 검증합니다.
     *
     * <p>JSON_OBJECT, JSON_ARRAY 타입의 경우 JSON 스키마 검증을 수행합니다.</p>
     * <p>다른 타입(STRING, NUMBER, BOOLEAN)은 기본 타입 검증을 수행합니다.</p>
     *
     * @param value 검증할 값
     * @param type 설정 타입
     * @return 검증 결과 (성공 시 true)
     * @throws IllegalArgumentException 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    boolean validate(String value, SettingType type);

    /**
     * JSON 문자열이 유효한 JSON인지 검증합니다.
     *
     * @param jsonString JSON 문자열
     * @return 유효한 JSON이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    boolean isValidJson(String jsonString);
}
