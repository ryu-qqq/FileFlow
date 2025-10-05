package com.ryuqq.fileflow.adapter.redis.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ryuqq.fileflow.domain.policy.vo.*;

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

        ImagePolicy imagePolicy = deserializeImagePolicy(node.get("imagePolicy"), p);
        HtmlPolicy htmlPolicy = deserializeHtmlPolicy(node.get("htmlPolicy"), p);
        ExcelPolicy excelPolicy = deserializeExcelPolicy(node.get("excelPolicy"), p);
        PdfPolicy pdfPolicy = deserializePdfPolicy(node.get("pdfPolicy"), p);

        return FileTypePolicies.of(imagePolicy, htmlPolicy, excelPolicy, pdfPolicy);
    }

    private ImagePolicy deserializeImagePolicy(JsonNode node, JsonParser p) throws IOException {
        if (node == null || node.isNull()) {
            return null;
        }
        return p.getCodec().treeToValue(node, ImagePolicy.class);
    }

    private HtmlPolicy deserializeHtmlPolicy(JsonNode node, JsonParser p) throws IOException {
        if (node == null || node.isNull()) {
            return null;
        }
        return p.getCodec().treeToValue(node, HtmlPolicy.class);
    }

    private ExcelPolicy deserializeExcelPolicy(JsonNode node, JsonParser p) throws IOException {
        if (node == null || node.isNull()) {
            return null;
        }
        return p.getCodec().treeToValue(node, ExcelPolicy.class);
    }

    private PdfPolicy deserializePdfPolicy(JsonNode node, JsonParser p) throws IOException {
        if (node == null || node.isNull()) {
            return null;
        }
        return p.getCodec().treeToValue(node, PdfPolicy.class);
    }
}
