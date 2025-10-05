package com.ryuqq.fileflow.adapter.redis.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ryuqq.fileflow.domain.policy.vo.Dimension;

import java.io.IOException;

/**
 * Dimension 커스텀 Jackson Deserializer
 *
 * 목적:
 * - 불변 Value Object인 Dimension의 역직렬화 지원
 * - private 생성자 + 팩토리 메서드 패턴을 Jackson이 이해할 수 있도록 변환
 */
public class DimensionDeserializer extends JsonDeserializer<Dimension> {

    @Override
    public Dimension deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        int width = node.get("width").asInt();
        int height = node.get("height").asInt();

        return Dimension.of(width, height);
    }
}
