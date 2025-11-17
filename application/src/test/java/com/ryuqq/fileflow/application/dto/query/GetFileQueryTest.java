package com.ryuqq.fileflow.application.dto.query;

import com.ryuqq.fileflow.application.fixture.GetFileQueryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GetFileQuery Record 테스트
 * <p>
 * Query DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Query
 * - 패키지: ..application..dto.query..
 * - 불변 객체 (final fields)
 * </p>
 */
class GetFileQueryTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * GetFileQuery Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("GetFileQuery는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Query 생성
        GetFileQuery query = GetFileQueryFixture.aQuery();

        // When & Then: Record 타입 검증
        assertThat(query).isNotNull();
        assertThat(query.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("GetFileQuery는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Query 생성
        Long expectedFileId = 100L;
        GetFileQuery query = GetFileQueryFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(query.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("GetFileQuery는 fileId를 받는 생성자를 가져야 한다")
    void shouldHaveConstructorWithFileId() {
        // Given: Fixture로 커스텀 fileId Query 생성
        Long expectedFileId = 200L;

        // When: fileId로 Query 생성
        GetFileQuery query = GetFileQueryFixture.withFileId(expectedFileId);

        // Then: fileId가 올바르게 설정됨
        assertThat(query.fileId()).isEqualTo(expectedFileId);
    }
}
