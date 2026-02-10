package com.ryuqq.fileflow.domain.session.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("S3PathResolver 도메인 서비스 단위 테스트")
class S3PathResolverTest {

    @Nested
    @DisplayName("resolve - S3 경로 생성")
    class Resolve {

        @Test
        @DisplayName("PUBLIC accessType으로 올바른 경로를 생성한다")
        void resolvesPublicPath() {
            Instant now = Instant.parse("2026-02-15T10:30:00Z");

            String path = S3PathResolver.resolve(AccessType.PUBLIC, "file-001", "jpg", now);

            assertThat(path).isEqualTo("public/2026/02/file-001.jpg");
        }

        @Test
        @DisplayName("INTERNAL accessType으로 올바른 경로를 생성한다")
        void resolvesInternalPath() {
            Instant now = Instant.parse("2026-02-15T10:30:00Z");

            String path = S3PathResolver.resolve(AccessType.INTERNAL, "file-002", "xlsx", now);

            assertThat(path).isEqualTo("internal/2026/02/file-002.xlsx");
        }

        @Test
        @DisplayName("월이 한 자리일 때 0으로 패딩된다")
        void padsMonthWithZero() {
            Instant now = Instant.parse("2026-01-05T10:30:00Z");

            String path = S3PathResolver.resolve(AccessType.PUBLIC, "file-003", "png", now);

            assertThat(path).isEqualTo("public/2026/01/file-003.png");
        }

        @Test
        @DisplayName("12월에도 올바른 경로를 생성한다")
        void resolvesDecemberPath() {
            Instant now = Instant.parse("2026-12-25T10:30:00Z");

            String path = S3PathResolver.resolve(AccessType.PUBLIC, "file-004", "pdf", now);

            assertThat(path).isEqualTo("public/2026/12/file-004.pdf");
        }
    }

    @Nested
    @DisplayName("extractExtension - 확장자 추출")
    class ExtractExtension {

        @Test
        @DisplayName("일반 파일명에서 확장자를 추출한다")
        void extractsExtension() {
            assertThat(S3PathResolver.extractExtension("test.jpg")).isEqualTo("jpg");
        }

        @Test
        @DisplayName("대문자 확장자를 소문자로 변환한다")
        void convertsToLowerCase() {
            assertThat(S3PathResolver.extractExtension("test.JPG")).isEqualTo("jpg");
        }

        @Test
        @DisplayName("여러 점이 있는 파일명에서 마지막 확장자를 추출한다")
        void extractsLastExtension() {
            assertThat(S3PathResolver.extractExtension("test.tar.gz")).isEqualTo("gz");
        }

        @Test
        @DisplayName("확장자가 없는 파일명에서 빈 문자열을 반환한다")
        void returnsEmptyForNoExtension() {
            assertThat(S3PathResolver.extractExtension("noextension")).isEmpty();
        }

        @Test
        @DisplayName("null 파일명에서 빈 문자열을 반환한다")
        void returnsEmptyForNull() {
            assertThat(S3PathResolver.extractExtension(null)).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 파일명에서 빈 문자열을 반환한다")
        void returnsEmptyForEmptyString() {
            assertThat(S3PathResolver.extractExtension("")).isEmpty();
        }
    }
}
