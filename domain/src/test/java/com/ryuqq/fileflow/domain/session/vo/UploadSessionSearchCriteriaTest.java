package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionSearchCriteria 단위 테스트")
class UploadSessionSearchCriteriaTest {

    private static final Long TENANT_ID = 10L;
    private static final Long ORGANIZATION_ID = 20L;
    private static final SessionStatus STATUS = SessionStatus.ACTIVE;
    private static final String UPLOAD_TYPE = "MULTIPART";
    private static final long OFFSET = 0L;
    private static final int LIMIT = 20;

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 검색 조건을 생성할 수 있다")
        void of_ShouldCreateSearchCriteria() {
            // given & when
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, OFFSET, LIMIT);

            // then
            assertThat(criteria.tenantId()).isEqualTo(TENANT_ID);
            assertThat(criteria.organizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(criteria.status()).isEqualTo(STATUS);
            assertThat(criteria.uploadType()).isEqualTo(UPLOAD_TYPE);
            assertThat(criteria.offset()).isEqualTo(OFFSET);
            assertThat(criteria.limit()).isEqualTo(LIMIT);
        }

        @Test
        @DisplayName("nullable 필드(status, uploadType)가 null인 경우에도 생성할 수 있다")
        void of_WithNullableFields_ShouldCreateSearchCriteria() {
            // given & when
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, null, null, OFFSET, LIMIT);

            // then
            assertThat(criteria.tenantId()).isEqualTo(TENANT_ID);
            assertThat(criteria.organizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(criteria.status()).isNull();
            assertThat(criteria.uploadType()).isNull();
            assertThat(criteria.offset()).isEqualTo(OFFSET);
            assertThat(criteria.limit()).isEqualTo(LIMIT);
        }

        @Test
        @DisplayName("SINGLE 업로드 타입으로 검색 조건을 생성할 수 있다")
        void of_WithSingleUploadType_ShouldCreateSearchCriteria() {
            // given & when
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, "SINGLE", OFFSET, LIMIT);

            // then
            assertThat(criteria.uploadType()).isEqualTo("SINGLE");
        }

        @Test
        @DisplayName("다양한 SessionStatus로 검색 조건을 생성할 수 있다")
        void of_WithVariousStatuses_ShouldCreateSearchCriteria() {
            // given
            SessionStatus[] statuses = SessionStatus.values();

            for (SessionStatus status : statuses) {
                // when
                UploadSessionSearchCriteria criteria =
                        UploadSessionSearchCriteria.of(
                                TENANT_ID, ORGANIZATION_ID, status, UPLOAD_TYPE, OFFSET, LIMIT);

                // then
                assertThat(criteria.status()).isEqualTo(status);
            }
        }
    }

    @Nested
    @DisplayName("레코드 동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 검색 조건은 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            UploadSessionSearchCriteria criteria1 =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, OFFSET, LIMIT);
            UploadSessionSearchCriteria criteria2 =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, OFFSET, LIMIT);

            // then
            assertThat(criteria1).isEqualTo(criteria2);
            assertThat(criteria1.hashCode()).isEqualTo(criteria2.hashCode());
        }

        @Test
        @DisplayName("다른 값으로 생성된 두 검색 조건은 동등하지 않아야 한다")
        void equals_DifferentValues_ShouldNotBeEqual() {
            // given
            UploadSessionSearchCriteria criteria1 =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, OFFSET, LIMIT);
            UploadSessionSearchCriteria criteria2 =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID,
                            ORGANIZATION_ID,
                            SessionStatus.COMPLETED,
                            UPLOAD_TYPE,
                            OFFSET,
                            LIMIT);

            // then
            assertThat(criteria1).isNotEqualTo(criteria2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString()은 모든 필드 값을 포함해야 한다")
        void toString_ShouldContainAllFields() {
            // given
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, STATUS, UPLOAD_TYPE, OFFSET, LIMIT);

            // when
            String result = criteria.toString();

            // then
            assertThat(result).contains(String.valueOf(TENANT_ID));
            assertThat(result).contains(String.valueOf(ORGANIZATION_ID));
            assertThat(result).contains(STATUS.name());
            assertThat(result).contains(UPLOAD_TYPE);
        }
    }
}
