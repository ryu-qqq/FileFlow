package com.ryuqq.fileflow.adapter.out.persistence.redis.settings.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CachedSetting JSON 직렬화/역직렬화 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@DisplayName("CachedSetting JSON 직렬화 테스트")
class CachedSettingSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("CachedSetting을 JSON으로 직렬화할 수 있다")
    void serialize() throws Exception {
        // given
        CachedSetting cached = new CachedSetting();
        cached.setId(1L);
        cached.setKeyValue("test.key");
        cached.setSettingValue("test value");
        cached.setSettingType("STRING");
        cached.setIsSecret(false);
        cached.setLevel("DEFAULT");
        cached.setContextId(null);
        cached.setCreatedAt(LocalDateTime.of(2025, 10, 26, 10, 0, 0));
        cached.setUpdatedAt(LocalDateTime.of(2025, 10, 26, 10, 0, 0));

        // when
        String json = objectMapper.writeValueAsString(cached);

        // then
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"keyValue\":\"test.key\"");
        assertThat(json).contains("\"settingValue\":\"test value\"");
        assertThat(json).contains("\"isSecret\":false");
    }

    @Test
    @DisplayName("JSON을 CachedSetting으로 역직렬화할 수 있다")
    void deserialize() throws Exception {
        // given
        String json = """
            {
                "id": 1,
                "keyValue": "test.key",
                "settingValue": "test value",
                "settingType": "STRING",
                "isSecret": false,
                "level": "DEFAULT",
                "contextId": null,
                "createdAt": "2025-10-26T10:00:00",
                "updatedAt": "2025-10-26T10:00:00"
            }
            """;

        // when
        CachedSetting cached = objectMapper.readValue(json, CachedSetting.class);

        // then
        assertThat(cached.getId()).isEqualTo(1L);
        assertThat(cached.getKeyValue()).isEqualTo("test.key");
        assertThat(cached.getSettingValue()).isEqualTo("test value");
        assertThat(cached.getIsSecret()).isFalse();
        assertThat(cached.getLevel()).isEqualTo("DEFAULT");
        assertThat(cached.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 10, 26, 10, 0, 0));
    }

    @Test
    @DisplayName("직렬화 후 역직렬화하면 동일한 데이터가 유지된다")
    void roundTrip() throws Exception {
        // given
        CachedSetting original = new CachedSetting();
        original.setId(1L);
        original.setKeyValue("test.key");
        original.setSettingValue("test value");
        original.setSettingType("STRING");
        original.setIsSecret(true);
        original.setLevel("ORG");
        original.setContextId(123L);
        original.setCreatedAt(LocalDateTime.of(2025, 10, 26, 10, 0, 0));
        original.setUpdatedAt(LocalDateTime.of(2025, 10, 26, 11, 0, 0));

        // when
        String json = objectMapper.writeValueAsString(original);
        CachedSetting deserialized = objectMapper.readValue(json, CachedSetting.class);

        // then
        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getKeyValue()).isEqualTo(original.getKeyValue());
        assertThat(deserialized.getSettingValue()).isEqualTo(original.getSettingValue());
        assertThat(deserialized.getSettingType()).isEqualTo(original.getSettingType());
        assertThat(deserialized.getIsSecret()).isEqualTo(original.getIsSecret());
        assertThat(deserialized.getLevel()).isEqualTo(original.getLevel());
        assertThat(deserialized.getContextId()).isEqualTo(original.getContextId());
        assertThat(deserialized.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(deserialized.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }
}
