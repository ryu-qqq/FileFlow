package com.ryuqq.fileflow.domain.vo;

/**
 * File 검색 조건 Value Object
 * <p>
 * QueryPort의 findByCriteria, countByCriteria에서 사용하는 검색 조건입니다.
 * </p>
 */
public record FileSearchCriteria(
        UploaderId uploaderId,
        FileStatus status,
        String category
) {
    /**
     * 모든 조건을 만족하는 검색 조건 생성
     */
    public static FileSearchCriteria of(UploaderId uploaderId, FileStatus status, String category) {
        return new FileSearchCriteria(uploaderId, status, category);
    }

    /**
     * uploaderId로만 검색
     */
    public static FileSearchCriteria byUploaderId(UploaderId uploaderId) {
        return new FileSearchCriteria(uploaderId, null, null);
    }

    /**
     * status로만 검색
     */
    public static FileSearchCriteria byStatus(FileStatus status) {
        return new FileSearchCriteria(null, status, null);
    }

    /**
     * category로만 검색
     */
    public static FileSearchCriteria byCategory(String category) {
        return new FileSearchCriteria(null, null, category);
    }
}
