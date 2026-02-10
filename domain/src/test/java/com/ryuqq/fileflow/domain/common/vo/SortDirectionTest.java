package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SortDirection")
class SortDirectionTest {

    @Nested
    @DisplayName("enum 값")
    class EnumValues {

        @Test
        @DisplayName("ASC와 DESC 두 가지 값이 존재한다")
        void hasTwoValues() {
            assertThat(SortDirection.values()).hasSize(2);
            assertThat(SortDirection.values())
                    .containsExactly(SortDirection.ASC, SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("displayName")
    class DisplayNameMethod {

        @Test
        @DisplayName("ASC는 '오름차순'을 반환한다")
        void asc_returnsDisplayName() {
            assertThat(SortDirection.ASC.displayName()).isEqualTo("오름차순");
        }

        @Test
        @DisplayName("DESC는 '내림차순'을 반환한다")
        void desc_returnsDisplayName() {
            assertThat(SortDirection.DESC.displayName()).isEqualTo("내림차순");
        }
    }

    @Nested
    @DisplayName("defaultDirection")
    class DefaultDirection {

        @Test
        @DisplayName("기본 정렬 방향은 DESC이다")
        void returnsDesc() {
            assertThat(SortDirection.defaultDirection()).isEqualTo(SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("isAscending / isDescending")
    class DirectionCheck {

        @Test
        @DisplayName("ASC는 isAscending=true, isDescending=false이다")
        void asc_isAscendingTrue() {
            assertThat(SortDirection.ASC.isAscending()).isTrue();
            assertThat(SortDirection.ASC.isDescending()).isFalse();
        }

        @Test
        @DisplayName("DESC는 isAscending=false, isDescending=true이다")
        void desc_isDescendingTrue() {
            assertThat(SortDirection.DESC.isAscending()).isFalse();
            assertThat(SortDirection.DESC.isDescending()).isTrue();
        }
    }

    @Nested
    @DisplayName("reverse")
    class Reverse {

        @Test
        @DisplayName("ASC의 reverse는 DESC이다")
        void asc_reversesToDesc() {
            assertThat(SortDirection.ASC.reverse()).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("DESC의 reverse는 ASC이다")
        void desc_reversesToAsc() {
            assertThat(SortDirection.DESC.reverse()).isEqualTo(SortDirection.ASC);
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @Test
        @DisplayName("'asc'를 ASC로 파싱한다 (대소문자 무관)")
        void lowercaseAsc_parsedCorrectly() {
            assertThat(SortDirection.fromString("asc")).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("'DESC'를 DESC로 파싱한다")
        void uppercaseDesc_parsedCorrectly() {
            assertThat(SortDirection.fromString("DESC")).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("'Asc'를 ASC로 파싱한다 (혼합 대소문자)")
        void mixedCaseAsc_parsedCorrectly() {
            assertThat(SortDirection.fromString("Asc")).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("앞뒤 공백이 있어도 정상 파싱된다")
        void whitespace_trimmedAndParsed() {
            assertThat(SortDirection.fromString(" asc ")).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("null이면 기본값(DESC)을 반환한다")
        void nullValue_returnsDefault() {
            assertThat(SortDirection.fromString(null)).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("빈 문자열이면 기본값(DESC)을 반환한다")
        void blankValue_returnsDefault() {
            assertThat(SortDirection.fromString("")).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("유효하지 않은 문자열이면 기본값(DESC)을 반환한다")
        void invalidValue_returnsDefault() {
            assertThat(SortDirection.fromString("INVALID")).isEqualTo(SortDirection.DESC);
        }
    }
}
