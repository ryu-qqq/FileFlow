package com.ryuqq.fileflow.application.dto.query;

import com.ryuqq.fileflow.application.fixture.ListFilesQueryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ListFilesQuery Record 테스트
 * <p>
 * Query DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Query
 * - 패키지: ..application..dto.query..
 * - 불변 객체 (final fields)
 * </p>
 */
class ListFilesQueryTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * ListFilesQuery Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("ListFilesQuery는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Query 생성
        ListFilesQuery query = ListFilesQueryFixture.aQuery();

        // When & Then: Record 타입 검증
        assertThat(query).isNotNull();
        assertThat(query.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("ListFilesQuery는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: Fixture로 커스텀 uploaderId Query 생성
        Long expectedUploaderId = 100L;
        ListFilesQuery query = ListFilesQueryFixture.withUploaderId(expectedUploaderId);

        // When & Then: uploaderId 필드 검증
        assertThat(query.uploaderId()).isEqualTo(expectedUploaderId);
    }

    @Test
    @DisplayName("ListFilesQuery는 status 필드를 가져야 한다")
    void shouldHaveStatusField() {
        // Given: Fixture로 커스텀 status Query 생성
        String expectedStatus = "PENDING";
        ListFilesQuery query = ListFilesQueryFixture.withStatus(expectedStatus);

        // When & Then: status 필드 검증
        assertThat(query.status()).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("ListFilesQuery는 category 필드를 가져야 한다")
    void shouldHaveCategoryField() {
        // Given: Fixture로 커스텀 category Query 생성
        String expectedCategory = "IMAGE";
        ListFilesQuery query = ListFilesQueryFixture.withCategory(expectedCategory);

        // When & Then: category 필드 검증
        assertThat(query.category()).isEqualTo(expectedCategory);
    }

    @Test
    @DisplayName("ListFilesQuery는 cursor 필드를 가져야 한다")
    void shouldHaveCursorField() {
        // Given: Fixture로 커스텀 cursor Query 생성
        String expectedCursor = "eyJpZCI6MTAwfQ==";
        ListFilesQuery query = ListFilesQueryFixture.withCursor(expectedCursor);

        // When & Then: cursor 필드 검증
        assertThat(query.cursor()).isEqualTo(expectedCursor);
    }

    @Test
    @DisplayName("ListFilesQuery는 size 필드를 가져야 한다")
    void shouldHaveSizeField() {
        // Given: Fixture로 커스텀 size Query 생성
        Integer expectedSize = 50;
        ListFilesQuery query = ListFilesQueryFixture.withSize(expectedSize);

        // When & Then: size 필드 검증
        assertThat(query.size()).isEqualTo(expectedSize);
    }
}
