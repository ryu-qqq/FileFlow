package com.ryuqq.fileflow.domain.session.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * S3 경로 생성 도메인 서비스.
 *
 * <p>경로 패턴: {accessType}/{yyyy}/{MM}/{fileId}.{extension}
 * <p>예: public/2026/02/01936a5e-7c4a-7000-8000-000000000001.jpg
 */
public class S3PathResolver {

    private S3PathResolver() {
    }

    /**
     * S3 객체 키를 생성합니다.
     *
     * @param accessType PUBLIC 또는 INTERNAL
     * @param fileId UUID v7 기반 파일 ID (Application에서 생성)
     * @param extension 파일 확장자 (jpg, png, pdf 등)
     * @param now 현재 시각 (Instant.now() 직접 호출 금지)
     * @return S3 객체 키
     */
    public static String resolve(AccessType accessType, String fileId, String extension, Instant now) {
        ZonedDateTime dateTime = now.atZone(ZoneOffset.UTC);
        String year = String.valueOf(dateTime.getYear());
        String month = String.format("%02d", dateTime.getMonthValue());

        return accessType.name().toLowerCase()
                + "/" + year
                + "/" + month
                + "/" + fileId
                + "." + extension;
    }

    /**
     * 파일명에서 확장자를 추출합니다.
     *
     * @param fileName 원본 파일명 (예: "product-image.jpg")
     * @return 확장자 (예: "jpg"), 확장자가 없으면 빈 문자열
     */
    public static String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
