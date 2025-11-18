package com.ryuqq.fileflow.application.file.fixture;

import com.ryuqq.fileflow.application.dto.query.GetFileQuery;

/**
 * GetFileQuery TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aQuery(), create()
 * </p>
 */
public class GetFileQueryFixture {

    /**
     * 기본 GetFileQuery 생성
     */
    public static GetFileQuery aQuery() {
        return new GetFileQuery(1L);
    }

    /**
     * 기본 GetFileQuery 생성 (alias)
     */
    public static GetFileQuery create() {
        return aQuery();
    }

    /**
     * 커스텀 파일 ID로 Query 생성
     */
    public static GetFileQuery withFileId(Long fileId) {
        return new GetFileQuery(fileId);
    }

    /**
     * 특정 사용자의 파일 조회 Query (예시)
     */
    public static GetFileQuery forUser(Long userId) {
        // userId를 기반으로 fileId 생성 (예시: userId * 100)
        return new GetFileQuery(userId * 100);
    }
}
