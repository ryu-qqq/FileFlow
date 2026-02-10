package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CursorPageRequest")
class CursorPageRequestTest {

    @Nested
    @DisplayName("생성 및 정규화")
    class Creation {

        @Test
        @DisplayName("정상적인 cursor와 size로 생성한다")
        void createWithValidCursorAndSize() {
            CursorPageRequest request = CursorPageRequest.of("cursor-123", 30);

            assertThat(request.cursor()).isEqualTo("cursor-123");
            assertThat(request.size()).isEqualTo(30);
        }

        @Test
        @DisplayName("size가 0이면 DEFAULT_SIZE로 정규화된다")
        void zeroSize_normalizedToDefault() {
            CursorPageRequest request = CursorPageRequest.of("cursor", 0);

            assertThat(request.size()).isEqualTo(CursorPageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("size가 음수이면 DEFAULT_SIZE로 정규화된다")
        void negativeSize_normalizedToDefault() {
            CursorPageRequest request = CursorPageRequest.of("cursor", -5);

            assertThat(request.size()).isEqualTo(CursorPageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("size가 MAX_SIZE를 초과하면 MAX_SIZE로 정규화된다")
        void exceedMaxSize_normalizedToMax() {
            CursorPageRequest request = CursorPageRequest.of("cursor", 200);

            assertThat(request.size()).isEqualTo(CursorPageRequest.MAX_SIZE);
        }

        @Test
        @DisplayName("빈 문자열 cursor는 null로 정규화된다")
        void blankCursor_normalizedToNull() {
            CursorPageRequest request = CursorPageRequest.of("", 20);

            assertThat(request.cursor()).isNull();
        }

        @Test
        @DisplayName("공백만 있는 cursor는 null로 정규화된다")
        void whitespaceCursor_normalizedToNull() {
            CursorPageRequest request = CursorPageRequest.of("   ", 20);

            assertThat(request.cursor()).isNull();
        }

        @Test
        @DisplayName("null cursor는 그대로 null이다")
        void nullCursor_staysNull() {
            CursorPageRequest request = CursorPageRequest.of(null, 20);

            assertThat(request.cursor()).isNull();
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("first는 cursor가 null인 요청을 반환한다")
        void first_returnsCursorNull() {
            CursorPageRequest request = CursorPageRequest.first(10);

            assertThat(request.cursor()).isNull();
            assertThat(request.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("defaultPage는 cursor=null, size=DEFAULT_SIZE인 요청을 반환한다")
        void defaultPage_returnsDefaults() {
            CursorPageRequest request = CursorPageRequest.defaultPage();

            assertThat(request.cursor()).isNull();
            assertThat(request.size()).isEqualTo(CursorPageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("afterId는 Long ID를 문자열 cursor로 변환한다")
        void afterId_convertsLongToString() {
            CursorPageRequest request = CursorPageRequest.afterId(100L, 20);

            assertThat(request.cursor()).isEqualTo("100");
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("afterId에 null을 전달하면 cursor가 null이다")
        void afterId_nullId_cursorIsNull() {
            CursorPageRequest request = CursorPageRequest.afterId(null, 20);

            assertThat(request.cursor()).isNull();
        }
    }

    @Nested
    @DisplayName("isFirstPage")
    class IsFirstPage {

        @Test
        @DisplayName("cursor가 null이면 true를 반환한다")
        void nullCursor_returnsTrue() {
            CursorPageRequest request = CursorPageRequest.first(20);

            assertThat(request.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("cursor가 있으면 false를 반환한다")
        void hasCursor_returnsFalse() {
            CursorPageRequest request = CursorPageRequest.of("cursor-123", 20);

            assertThat(request.isFirstPage()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasCursor")
    class HasCursor {

        @Test
        @DisplayName("cursor가 있으면 true를 반환한다")
        void hasCursor_returnsTrue() {
            CursorPageRequest request = CursorPageRequest.of("cursor-123", 20);

            assertThat(request.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("cursor가 null이면 false를 반환한다")
        void nullCursor_returnsFalse() {
            CursorPageRequest request = CursorPageRequest.first(20);

            assertThat(request.hasCursor()).isFalse();
        }
    }

    @Nested
    @DisplayName("cursorAsLong")
    class CursorAsLong {

        @Test
        @DisplayName("숫자 문자열 cursor를 Long으로 파싱한다")
        void numericCursor_parsedToLong() {
            CursorPageRequest request = CursorPageRequest.of("12345", 20);

            assertThat(request.cursorAsLong()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("숫자가 아닌 cursor는 null을 반환한다")
        void nonNumericCursor_returnsNull() {
            CursorPageRequest request = CursorPageRequest.of("abc", 20);

            assertThat(request.cursorAsLong()).isNull();
        }

        @Test
        @DisplayName("cursor가 null이면 null을 반환한다")
        void nullCursor_returnsNull() {
            CursorPageRequest request = CursorPageRequest.first(20);

            assertThat(request.cursorAsLong()).isNull();
        }
    }

    @Nested
    @DisplayName("next")
    class NextPage {

        @Test
        @DisplayName("다음 커서로 새로운 요청을 생성한다")
        void createsNextPageWithNewCursor() {
            CursorPageRequest request = CursorPageRequest.of("cursor-1", 20);

            CursorPageRequest next = request.next("cursor-2");

            assertThat(next.cursor()).isEqualTo("cursor-2");
            assertThat(next.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("fetchSize")
    class FetchSize {

        @Test
        @DisplayName("size + 1을 반환한다 (hasNext 판단용)")
        void returnsSizePlusOne() {
            CursorPageRequest request = CursorPageRequest.of(null, 20);

            assertThat(request.fetchSize()).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 cursor, size를 가진 요청은 동일하다")
        void sameValues_areEqual() {
            CursorPageRequest request1 = CursorPageRequest.of("cursor", 20);
            CursorPageRequest request2 = CursorPageRequest.of("cursor", 20);

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 cursor를 가진 요청은 다르다")
        void differentCursor_notEqual() {
            CursorPageRequest request1 = CursorPageRequest.of("cursor-1", 20);
            CursorPageRequest request2 = CursorPageRequest.of("cursor-2", 20);

            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
