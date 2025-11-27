package com.ryuqq.fileflow.application.session.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GetUploadSessionQuery 단위 테스트")
class GetUploadSessionQueryTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final Long TENANT_ID = 10L;

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 Query를 생성할 수 있다")
        void of_ShouldCreateQuery() {
            // given & when
            GetUploadSessionQuery query = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);

            // then
            assertThat(query.sessionId()).isEqualTo(SESSION_ID);
            assertThat(query.tenantId()).isEqualTo(TENANT_ID);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 Query는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            GetUploadSessionQuery query1 = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);
            GetUploadSessionQuery query2 = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);

            // then
            assertThat(query1).isEqualTo(query2);
            assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
        }

        @Test
        @DisplayName("다른 세션 ID를 가진 두 Query는 동등하지 않아야 한다")
        void equals_DifferentSessionId_ShouldNotBeEqual() {
            // given
            GetUploadSessionQuery query1 = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);
            GetUploadSessionQuery query2 =
                    GetUploadSessionQuery.of("different-session-id", TENANT_ID);

            // then
            assertThat(query1).isNotEqualTo(query2);
        }
    }
}
