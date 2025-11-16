package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.query.ListFilesQuery;

/**
 * ListFilesQuery TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aQuery(), create()
 * </p>
 */
public class ListFilesQueryFixture {

    /**
     * 기본 ListFilesQuery 생성
     */
    public static ListFilesQuery aQuery() {
        return new ListFilesQuery(
                1L,
                "COMPLETED",
                "PROFILE",
                null,
                20
        );
    }

    /**
     * 기본 ListFilesQuery 생성 (alias)
     */
    public static ListFilesQuery create() {
        return aQuery();
    }

    /**
     * 커스텀 업로더 ID로 Query 생성
     */
    public static ListFilesQuery withUploaderId(Long uploaderId) {
        return new ListFilesQuery(
                uploaderId,
                "COMPLETED",
                "PROFILE",
                null,
                20
        );
    }

    /**
     * 커스텀 상태로 Query 생성
     */
    public static ListFilesQuery withStatus(String status) {
        return new ListFilesQuery(
                1L,
                status,
                "PROFILE",
                null,
                20
        );
    }

    /**
     * 커스텀 카테고리로 Query 생성
     */
    public static ListFilesQuery withCategory(String category) {
        return new ListFilesQuery(
                1L,
                "COMPLETED",
                category,
                null,
                20
        );
    }

    /**
     * 커스텀 커서로 Query 생성
     */
    public static ListFilesQuery withCursor(String cursor) {
        return new ListFilesQuery(
                1L,
                "COMPLETED",
                "PROFILE",
                cursor,
                20
        );
    }

    /**
     * 커스텀 페이지 크기로 Query 생성
     */
    public static ListFilesQuery withSize(Integer size) {
        return new ListFilesQuery(
                1L,
                "COMPLETED",
                "PROFILE",
                null,
                size
        );
    }

    /**
     * PENDING 상태 파일 조회 Query (시나리오)
     */
    public static ListFilesQuery pendingFiles() {
        return new ListFilesQuery(
                2L,
                "PENDING",
                null,
                null,
                10
        );
    }

    /**
     * 이미지 카테고리 파일 조회 Query (시나리오)
     */
    public static ListFilesQuery imageFiles() {
        return new ListFilesQuery(
                3L,
                "COMPLETED",
                "IMAGE",
                null,
                50
        );
    }

    /**
     * 다음 페이지 조회 Query (시나리오)
     */
    public static ListFilesQuery nextPage() {
        return new ListFilesQuery(
                1L,
                "COMPLETED",
                "PROFILE",
                "eyJpZCI6MTAwfQ==",
                20
        );
    }
}
