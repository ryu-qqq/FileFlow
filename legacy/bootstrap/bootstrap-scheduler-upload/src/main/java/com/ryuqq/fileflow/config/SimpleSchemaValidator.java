package com.ryuqq.fileflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.domain.settings.SettingType;
import org.springframework.stereotype.Component;

/**
 * Simple Schema Validator Implementation
 *
 * <p>JSON 스키마 검증을 담당하는 Adapter 구현체입니다.</p>
 * <p>헥사고날 아키텍처: Application Port의 구현체 (Driving Adapter)</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Port-Adapter 패턴 - SchemaValidator Port 구현</li>
 *   <li>✅ Jackson ObjectMapper 사용 - JSON 파싱</li>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 * </ul>
 *
 * @author FileFlow Team
 * @since 2025-10-27
 */
@Component
public class SimpleSchemaValidator implements SchemaValidator {

    private final ObjectMapper objectMapper;

    /**
     * 생성자 (Constructor Injection)
     *
     * <p>Spring이 ObjectMapper 빈을 자동 주입합니다.</p>
     *
     * @param objectMapper Jackson ObjectMapper
     * @since 2025-10-27
     */
    public SimpleSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 설정 값이 타입과 호환되는지 검증합니다.
     *
     * <p>JSON_OBJECT, JSON_ARRAY 타입의 경우 JSON 파싱을 시도합니다.</p>
     * <p>다른 타입(STRING, NUMBER, BOOLEAN)은 SettingType의 검증 로직을 사용합니다.</p>
     *
     * @param value 검증할 값
     * @param type 설정 타입
     * @return 검증 결과 (성공 시 true)
     * @throws IllegalArgumentException 검증 실패 시
     * @since 2025-10-27
     */
    @Override
    public boolean validate(String value, SettingType type) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Setting 값은 필수입니다");
        }

        if (type == null) {
            throw new IllegalArgumentException("Setting 타입은 필수입니다");
        }

        // JSON 타입은 실제 파싱으로 검증
        if (type == SettingType.JSON_OBJECT || type == SettingType.JSON_ARRAY) {
            return isValidJson(value);
        }

        // 다른 타입은 SettingType의 검증 로직 사용
        if (!type.isCompatibleWith(value)) {
            throw new IllegalArgumentException(
                "Setting 값 '" + value + "'이(가) 타입 " + type + "과(와) 호환되지 않습니다"
            );
        }

        return true;
    }

    /**
     * JSON 문자열이 유효한 JSON인지 검증합니다.
     *
     * <p>Jackson ObjectMapper를 사용하여 파싱을 시도합니다.</p>
     *
     * @param jsonString JSON 문자열
     * @return 유효한 JSON이면 true
     * @since 2025-10-27
     */
    @Override
    public boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return false;
        }

        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "유효하지 않은 JSON 형식입니다: " + e.getMessage(),
                e
            );
        }
    }
}
