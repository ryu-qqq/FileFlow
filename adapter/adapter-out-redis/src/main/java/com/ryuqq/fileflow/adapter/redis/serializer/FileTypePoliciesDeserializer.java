package com.ryuqq.fileflow.adapter.redis.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;

import java.io.IOException;

/**
 * FileTypePolicies 커스텀 Jackson Deserializer
 *
 * 목적:
 * - 불변 Value Object인 FileTypePolicies의 역직렬화 지원
 * - private 생성자 + 팩토리 메서드 패턴을 Jackson이 이해할 수 있도록 변환
 *
 * 전략:
 * - JSON에서 각 정책 필드를 읽어서 개별 정책 객체 생성
 * - FileTypePolicies.of() 팩토리 메서드를 통해 최종 객체 생성
 */
public class FileTypePoliciesDeserializer extends JsonDeserializer<FileTypePolicies> {

    @Override
    public FileTypePolicies deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        ImagePolicy imagePolicy = deserializePolicy(node, "imagePolicy", p, ImagePolicy.class);
        HtmlPolicy htmlPolicy = deserializePolicy(node, "htmlPolicy", p, HtmlPolicy.class);
        ExcelPolicy excelPolicy = deserializePolicy(node, "excelPolicy", p, ExcelPolicy.class);
        PdfPolicy pdfPolicy = deserializePolicy(node, "pdfPolicy", p, PdfPolicy.class);

        return FileTypePolicies.of(imagePolicy, htmlPolicy, excelPolicy, pdfPolicy);
    }

    private <T> T deserializePolicy(JsonNode parentNode, String fieldName, JsonParser p, Class<T> policyClass) throws IOException {
        JsonNode node = parentNode.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        return p.getCodec().treeToValue(node, policyClass);
    }
}
