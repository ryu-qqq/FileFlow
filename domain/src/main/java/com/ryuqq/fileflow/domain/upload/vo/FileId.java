package com.ryuqq.fileflow.domain.upload.vo;

import java.util.UUID;

/**
 * 파일 고유 식별자 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - UUID 기반의 전역 고유 식별자
 *
 * 용도:
 * - 업로드된 파일의 전역 고유 식별
 * - 파일 추적 및 참조
 */
public record FileId(String value) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public FileId {
        validateValue(value);
    }

    /**
     * 새로운 FileId를 생성합니다.
     *
     * @return 새로운 FileId 인스턴스
     */
    public static FileId generate() {
        return new FileId(UUID.randomUUID().toString());
    }

    /**
     * 주어진 문자열로부터 FileId를 생성합니다.
     *
     * @param value 파일 ID 문자열
     * @return FileId 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static FileId of(String value) {
        return new FileId(value);
    }

    // ========== Validation Methods ==========

    private static void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("FileId cannot be null or empty");
        }

        // UUID 형식 검증
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("FileId must be a valid UUID format", e);
        }
    }
}
