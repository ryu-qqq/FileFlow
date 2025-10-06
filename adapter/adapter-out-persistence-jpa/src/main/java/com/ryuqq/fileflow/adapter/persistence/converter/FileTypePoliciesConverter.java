package com.ryuqq.fileflow.adapter.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.domain.policy.vo.Dimension;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

/**
 * FileTypePolicies를 JSON으로 변환하는 JPA AttributeConverter
 *
 * 변환 전략:
 * - FileTypePolicies의 각 정책을 Map으로 분해
 * - Jackson ObjectMapper를 사용하여 JSON 직렬화/역직렬화
 * - NULL 정책은 JSON에서 제외
 *
 * @author sangwon-ryu
 */
@Converter
public class FileTypePoliciesConverter implements AttributeConverter<FileTypePolicies, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(FileTypePolicies attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            java.util.Map<String, Object> policyMap = new java.util.HashMap<>();
            if (attribute.getImagePolicy() != null) {
                policyMap.put("imagePolicy", attribute.getImagePolicy());
            }
            if (attribute.getHtmlPolicy() != null) {
                policyMap.put("htmlPolicy", attribute.getHtmlPolicy());
            }
            if (attribute.getExcelPolicy() != null) {
                policyMap.put("excelPolicy", attribute.getExcelPolicy());
            }
            if (attribute.getPdfPolicy() != null) {
                policyMap.put("pdfPolicy", attribute.getPdfPolicy());
            }

            return objectMapper.writeValueAsString(policyMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert FileTypePolicies to JSON", e);
        }
    }

    @Override
    public FileTypePolicies convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> policyMap = objectMapper.readValue(dbData, Map.class);

            ImagePolicy imagePolicy = convertToImagePolicy(policyMap.get("imagePolicy"));
            HtmlPolicy htmlPolicy = convertToHtmlPolicy(policyMap.get("htmlPolicy"));
            ExcelPolicy excelPolicy = convertToExcelPolicy(policyMap.get("excelPolicy"));
            PdfPolicy pdfPolicy = convertToPdfPolicy(policyMap.get("pdfPolicy"));

            return FileTypePolicies.of(imagePolicy, htmlPolicy, excelPolicy, pdfPolicy);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert JSON to FileTypePolicies", e);
        }
    }

    private ImagePolicy convertToImagePolicy(Object obj) {
        if (obj == null || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;

        int maxFileSizeMB = ((Number) map.get("maxFileSizeMB")).intValue();
        int maxFileCount = ((Number) map.get("maxFileCount")).intValue();

        @SuppressWarnings("unchecked")
        java.util.List<String> allowedFormats = (java.util.List<String>) map.get("allowedFormats");

        Dimension maxDimension = null;
        Object dimensionObj = map.get("maxDimension");
        if (dimensionObj != null && dimensionObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dimMap = (Map<String, Object>) dimensionObj;
            int width = ((Number) dimMap.get("width")).intValue();
            int height = ((Number) dimMap.get("height")).intValue();
            maxDimension = Dimension.of(width, height);
        }

        return new ImagePolicy(maxFileSizeMB, maxFileCount, allowedFormats, maxDimension);
    }

    private HtmlPolicy convertToHtmlPolicy(Object obj) {
        if (obj == null || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;

        int maxFileSizeMB = ((Number) map.get("maxFileSizeMB")).intValue();
        int maxImageCount = ((Number) map.get("maxImageCount")).intValue();
        boolean downloadExternalImages = (Boolean) map.get("downloadExternalImages");

        return new HtmlPolicy(maxFileSizeMB, maxImageCount, downloadExternalImages);
    }

    private ExcelPolicy convertToExcelPolicy(Object obj) {
        if (obj == null || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;

        int maxFileSizeMB = ((Number) map.get("maxFileSizeMB")).intValue();
        int maxSheetCount = ((Number) map.get("maxSheetCount")).intValue();

        return new ExcelPolicy(maxFileSizeMB, maxSheetCount);
    }

    private PdfPolicy convertToPdfPolicy(Object obj) {
        if (obj == null || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;

        int maxFileSizeMB = ((Number) map.get("maxFileSizeMB")).intValue();
        int maxPageCount = ((Number) map.get("maxPageCount")).intValue();

        return new PdfPolicy(maxFileSizeMB, maxPageCount);
    }
}
