package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PageRequest")
class PageRequestTest {

    @Nested
    @DisplayName("생성 및 정규화")
    class Creation {

        @Test
        @DisplayName("정상적인 page와 size로 생성한다")
        void createWithValidPageAndSize() {
            PageRequest request = PageRequest.of(2, 30);

            assertThat(request.page()).isEqualTo(2);
            assertThat(request.size()).isEqualTo(30);
        }

        @Test
        @DisplayName("page가 음수이면 0으로 정규화된다")
        void negativePage_normalizedToZero() {
            PageRequest request = PageRequest.of(-1, 20);

            assertThat(request.page()).isZero();
        }

        @Test
        @DisplayName("size가 0이면 DEFAULT_SIZE로 정규화된다")
        void zeroSize_normalizedToDefault() {
            PageRequest request = PageRequest.of(0, 0);

            assertThat(request.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("size가 음수이면 DEFAULT_SIZE로 정규화된다")
        void negativeSize_normalizedToDefault() {
            PageRequest request = PageRequest.of(0, -5);

            assertThat(request.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("size가 MAX_SIZE를 초과하면 MAX_SIZE로 정규화된다")
        void exceedMaxSize_normalizedToMax() {
            PageRequest request = PageRequest.of(0, 200);

            assertThat(request.size()).isEqualTo(PageRequest.MAX_SIZE);
        }

        @Test
        @DisplayName("size가 정확히 MAX_SIZE이면 그대로 유지된다")
        void exactMaxSize_preserved() {
            PageRequest request = PageRequest.of(0, PageRequest.MAX_SIZE);

            assertThat(request.size()).isEqualTo(PageRequest.MAX_SIZE);
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("first는 page=0인 PageRequest를 반환한다")
        void first_returnsPageZero() {
            PageRequest request = PageRequest.first(10);

            assertThat(request.page()).isZero();
            assertThat(request.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("defaultPage는 page=0, size=DEFAULT_SIZE인 PageRequest를 반환한다")
        void defaultPage_returnsDefaults() {
            PageRequest request = PageRequest.defaultPage();

            assertThat(request.page()).isZero();
            assertThat(request.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        }
    }

    @Nested
    @DisplayName("offset")
    class Offset {

        @Test
        @DisplayName("page * size로 오프셋을 계산한다")
        void calculatesOffset() {
            PageRequest request = PageRequest.of(3, 20);

            assertThat(request.offset()).isEqualTo(60L);
        }

        @Test
        @DisplayName("첫 페이지의 오프셋은 0이다")
        void firstPage_offsetIsZero() {
            PageRequest request = PageRequest.of(0, 20);

            assertThat(request.offset()).isZero();
        }
    }

    @Nested
    @DisplayName("next")
    class Next {

        @Test
        @DisplayName("다음 페이지 요청을 반환한다")
        void returnsNextPage() {
            PageRequest request = PageRequest.of(2, 20);

            PageRequest next = request.next();

            assertThat(next.page()).isEqualTo(3);
            assertThat(next.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("previous")
    class Previous {

        @Test
        @DisplayName("이전 페이지 요청을 반환한다")
        void returnsPreviousPage() {
            PageRequest request = PageRequest.of(3, 20);

            PageRequest previous = request.previous();

            assertThat(previous.page()).isEqualTo(2);
            assertThat(previous.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("첫 페이지에서 이전 페이지를 요청하면 그대로 반환한다")
        void firstPage_returnsSelf() {
            PageRequest request = PageRequest.of(0, 20);

            PageRequest previous = request.previous();

            assertThat(previous).isSameAs(request);
        }
    }

    @Nested
    @DisplayName("isFirst")
    class IsFirst {

        @Test
        @DisplayName("page가 0이면 true를 반환한다")
        void pageZero_returnsTrue() {
            PageRequest request = PageRequest.of(0, 20);

            assertThat(request.isFirst()).isTrue();
        }

        @Test
        @DisplayName("page가 0이 아니면 false를 반환한다")
        void nonZeroPage_returnsFalse() {
            PageRequest request = PageRequest.of(1, 20);

            assertThat(request.isFirst()).isFalse();
        }
    }

    @Nested
    @DisplayName("totalPages")
    class TotalPages {

        @Test
        @DisplayName("전체 항목 수로 페이지 수를 계산한다")
        void calculatesTotalPages() {
            PageRequest request = PageRequest.of(0, 20);

            assertThat(request.totalPages(100)).isEqualTo(5);
        }

        @Test
        @DisplayName("나머지가 있으면 올림 처리한다")
        void roundsUpWithRemainder() {
            PageRequest request = PageRequest.of(0, 20);

            assertThat(request.totalPages(101)).isEqualTo(6);
        }

        @Test
        @DisplayName("전체 항목이 0이면 0페이지를 반환한다")
        void zeroElements_returnsZero() {
            PageRequest request = PageRequest.of(0, 20);

            assertThat(request.totalPages(0)).isZero();
        }
    }

    @Nested
    @DisplayName("isLast")
    class IsLast {

        @Test
        @DisplayName("마지막 페이지이면 true를 반환한다")
        void lastPage_returnsTrue() {
            PageRequest request = PageRequest.of(4, 20);

            assertThat(request.isLast(100)).isTrue();
        }

        @Test
        @DisplayName("마지막 페이지가 아니면 false를 반환한다")
        void notLastPage_returnsFalse() {
            PageRequest request = PageRequest.of(3, 20);

            assertThat(request.isLast(100)).isFalse();
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 page, size를 가진 PageRequest는 동일하다")
        void sameValues_areEqual() {
            PageRequest request1 = PageRequest.of(1, 20);
            PageRequest request2 = PageRequest.of(1, 20);

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 page를 가진 PageRequest는 다르다")
        void differentPage_notEqual() {
            PageRequest request1 = PageRequest.of(1, 20);
            PageRequest request2 = PageRequest.of(2, 20);

            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
