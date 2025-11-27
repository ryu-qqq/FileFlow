package com.ryuqq.fileflow.application.session.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ListUploadSessionsQuery 단위 테스트")
class ListUploadSessionsQueryTest {

    private static final Long TENANT_ID = 10L;
    private static final Long ORGANIZATION_ID = 20L;
    private static final SessionStatus STATUS = SessionStatus.COMPLETED;
    private static final String UPLOAD_TYPE = "MULTIPART";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 Query를 생성할 수 있다")
        void of_ShouldCreateQuery() {
            // given & when
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, 2, 20);

            // then
            assertThat(query.tenantId()).isEqualTo(TENANT_ID);
            assertThat(query.organizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(query.status()).isEqualTo(STATUS);
            assertThat(query.uploadType()).isEqualTo(UPLOAD_TYPE);
            assertThat(query.page()).isEqualTo(2);
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("nullable 필드가 null인 경우에도 생성할 수 있다")
        void of_WithNullFields_ShouldCreateQuery() {
            // given & when
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, 0, 10);

            // then
            assertThat(query.status()).isNull();
            assertThat(query.uploadType()).isNull();
        }
    }

    @Nested
    @DisplayName("offset 계산 테스트")
    class OffsetTest {

        @Test
        @DisplayName("첫 번째 페이지의 offset은 0이다")
        void offset_FirstPage_ShouldBeZero() {
            // given
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, 0, 20);

            // when & then
            assertThat(query.offset()).isZero();
        }

        @Test
        @DisplayName("두 번째 페이지의 offset은 size와 같다")
        void offset_SecondPage_ShouldBeEqualToSize() {
            // given
            int page = 1;
            int size = 20;
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, page, size);

            // when & then
            assertThat(query.offset()).isEqualTo(20L);
        }

        @Test
        @DisplayName("n번째 페이지의 offset은 (n-1) * size와 같다")
        void offset_NthPage_ShouldCalculateCorrectly() {
            // given
            int page = 5;
            int size = 10;
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, page, size);

            // when & then
            assertThat(query.offset()).isEqualTo(50L);
        }

        @Test
        @DisplayName("큰 페이지 번호에서도 offset이 올바르게 계산된다")
        void offset_LargePage_ShouldCalculateCorrectly() {
            // given
            int page = 1000;
            int size = 100;
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, page, size);

            // when & then
            assertThat(query.offset()).isEqualTo(100000L);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 Query는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            ListUploadSessionsQuery query1 =
                    ListUploadSessionsQuery.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, 0, 10);
            ListUploadSessionsQuery query2 =
                    ListUploadSessionsQuery.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, 0, 10);

            // then
            assertThat(query1).isEqualTo(query2);
            assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
        }
    }
}
