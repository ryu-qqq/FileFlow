package com.ryuqq.fileflow.adapter.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * RateLimiting을 JSON으로 변환하는 JPA AttributeConverter
 *
 * 변환 전략:
 * - RateLimiting Record를 JSON 문자열로 직렬화
 * - Jackson ObjectMapper 사용
 *
 * @author sangwon-ryu
 */
@Converter
public class RateLimitingConverter implements AttributeConverter<RateLimiting, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(RateLimiting attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert RateLimiting to JSON", e);
        }
    }

    @Override
    public RateLimiting convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(dbData, RateLimiting.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert JSON to RateLimiting", e);
        }
    }
}
