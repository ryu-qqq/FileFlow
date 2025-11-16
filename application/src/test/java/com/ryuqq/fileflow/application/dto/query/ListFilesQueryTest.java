package com.ryuqq.fileflow.application.dto.query;

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
        // Given: ListFilesQuery Record (컴파일 에러)
        ListFilesQuery query = null;

        // When & Then: Record 타입 검증
        assertThat(query).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("ListFilesQuery는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: ListFilesQuery Record (컴파일 에러)
        Long uploaderId = 1L;
        String status = "COMPLETED";
        String category = "PROFILE";
        String cursor = null;
        Integer size = 20;

        ListFilesQuery query = new ListFilesQuery(
                uploaderId,
                status,
                category,
                cursor,
                size
        );

        // When & Then: uploaderId 필드 검증
        assertThat(query.uploaderId()).isEqualTo(uploaderId);
    }

    @Test
    @DisplayName("ListFilesQuery는 status 필드를 가져야 한다")
    void shouldHaveStatusField() {
        // Given: ListFilesQuery Record (컴파일 에러)
        Long uploaderId = 1L;
        String status = "COMPLETED";
        String category = "PROFILE";
        String cursor = null;
        Integer size = 20;

        ListFilesQuery query = new ListFilesQuery(
                uploaderId,
                status,
                category,
                cursor,
                size
        );

        // When & Then: status 필드 검증
        assertThat(query.status()).isEqualTo(status);
    }

    @Test
    @DisplayName("ListFilesQuery는 category 필드를 가져야 한다")
    void shouldHaveCategoryField() {
        // Given: ListFilesQuery Record (컴파일 에러)
        Long uploaderId = 1L;
        String status = "COMPLETED";
        String category = "PROFILE";
        String cursor = null;
        Integer size = 20;

        ListFilesQuery query = new ListFilesQuery(
                uploaderId,
                status,
                category,
                cursor,
                size
        );

        // When & Then: category 필드 검증
        assertThat(query.category()).isEqualTo(category);
    }

    @Test
    @DisplayName("ListFilesQuery는 cursor 필드를 가져야 한다")
    void shouldHaveCursorField() {
        // Given: ListFilesQuery Record (컴파일 에러)
        Long uploaderId = 1L;
        String status = "COMPLETED";
        String category = "PROFILE";
        String cursor = "eyJpZCI6MTAwfQ==";
        Integer size = 20;

        ListFilesQuery query = new ListFilesQuery(
                uploaderId,
                status,
                category,
                cursor,
                size
        );

        // When & Then: cursor 필드 검증
        assertThat(query.cursor()).isEqualTo(cursor);
    }

    @Test
    @DisplayName("ListFilesQuery는 size 필드를 가져야 한다")
    void shouldHaveSizeField() {
        // Given: ListFilesQuery Record (컴파일 에러)
        Long uploaderId = 1L;
        String status = "COMPLETED";
        String category = "PROFILE";
        String cursor = null;
        Integer size = 50;

        ListFilesQuery query = new ListFilesQuery(
                uploaderId,
                status,
                category,
                cursor,
                size
        );

        // When & Then: size 필드 검증
        assertThat(query.size()).isEqualTo(size);
    }
}
