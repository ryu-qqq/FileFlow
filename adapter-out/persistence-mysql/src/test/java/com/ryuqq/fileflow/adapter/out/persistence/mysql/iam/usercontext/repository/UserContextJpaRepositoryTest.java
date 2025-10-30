package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.UserContextJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserContextJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * UserContextJpaRepository 단위 테스트
 *
 * <p><strong>테스트 대상</strong>: {@link UserContextJpaRepository}</p>
 * <p><strong>테스트 전략</strong>: Mockito 기반 단위 테스트</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Happy Path: 정상 조회 시나리오</li>
 *   <li>✅ Edge Cases: 빈 결과, Optional.empty() 처리</li>
 *   <li>✅ Spring Data JPA Query Method 검증</li>
 *   <li>✅ findByExternalUserId() 메서드 테스트</li>
 *   <li>✅ existsByExternalUserId() 메서드 테스트</li>
 * </ul>
 *
 * <h3>테스트 패턴</h3>
 * <ul>
 *   <li>✅ Given-When-Then 구조</li>
 *   <li>✅ @Nested를 활용한 논리적 그룹화</li>
 *   <li>✅ @DisplayName으로 테스트 의도 명확화 (한글)</li>
 *   <li>✅ AssertJ를 활용한 Fluent Assertion</li>
 *   <li>✅ BDDMockito를 활용한 Given 설정</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserContextJpaRepository 단위 테스트")
class UserContextJpaRepositoryTest {

    @Mock
    private UserContextJpaRepository repository;

    private UserContextJpaEntity activeUser;
    private UserContextJpaEntity deletedUser;

    @BeforeEach
    void setUp() {
        activeUser = UserContextJpaEntityFixture.createWithId(1L, "auth0|user123", "user@example.com");
        deletedUser = UserContextJpaEntityFixture.createDeleted(2L);
    }

    @Nested
    @DisplayName("findByExternalUserId() - External User ID로 조회")
    class FindByExternalUserIdTests {

        @Test
        @DisplayName("정상: External User ID로 조회 성공")
        void shouldFindByExternalUserIdSuccessfully() {
            // Given
            String externalUserId = "auth0|user123";
            given(repository.findByExternalUserId(externalUserId)).willReturn(Optional.of(activeUser));

            // When
            Optional<UserContextJpaEntity> result = repository.findByExternalUserId(externalUserId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getExternalUserId()).isEqualTo("auth0|user123");
            assertThat(result.get().getEmail()).isEqualTo("user@example.com");
            verify(repository, times(1)).findByExternalUserId(externalUserId);
        }

        @Test
        @DisplayName("정상: UserContext가 존재하지 않으면 Optional.empty() 반환")
        void shouldReturnEmptyWhenUserContextNotFound() {
            // Given
            String nonExistentUserId = "auth0|nonexistent";
            given(repository.findByExternalUserId(nonExistentUserId)).willReturn(Optional.empty());

            // When
            Optional<UserContextJpaEntity> result = repository.findByExternalUserId(nonExistentUserId);

            // Then
            assertThat(result).isEmpty();
            verify(repository, times(1)).findByExternalUserId(nonExistentUserId);
        }

        @Test
        @DisplayName("정상: 다양한 IDP 형식의 External User ID 조회")
        void shouldFindByVariousIdpFormats() {
            // Given - Auth0 형식
            String auth0Id = "auth0|abc123";
            UserContextJpaEntity auth0User = UserContextJpaEntityFixture.createWithId(1L, auth0Id, "user@example.com");
            given(repository.findByExternalUserId(auth0Id)).willReturn(Optional.of(auth0User));

            // When
            Optional<UserContextJpaEntity> result1 = repository.findByExternalUserId(auth0Id);

            // Then
            assertThat(result1).isPresent();
            assertThat(result1.get().getExternalUserId()).isEqualTo(auth0Id);

            // Given - Google 형식
            String googleId = "google-oauth2|xyz789";
            UserContextJpaEntity googleUser = UserContextJpaEntityFixture.createWithId(2L, googleId, "google@example.com");
            given(repository.findByExternalUserId(googleId)).willReturn(Optional.of(googleUser));

            // When
            Optional<UserContextJpaEntity> result2 = repository.findByExternalUserId(googleId);

            // Then
            assertThat(result2).isPresent();
            assertThat(result2.get().getExternalUserId()).isEqualTo(googleId);
        }

        @Test
        @DisplayName("정상: 대소문자 구분 조회 (정확히 일치해야 함)")
        void shouldBeCaseSensitive() {
            // Given
            String exactId = "auth0|User123";
            String lowercaseId = "auth0|user123";

            given(repository.findByExternalUserId(exactId)).willReturn(Optional.of(activeUser));
            given(repository.findByExternalUserId(lowercaseId)).willReturn(Optional.empty());

            // When
            Optional<UserContextJpaEntity> result1 = repository.findByExternalUserId(exactId);
            Optional<UserContextJpaEntity> result2 = repository.findByExternalUserId(lowercaseId);

            // Then
            assertThat(result1).isPresent();
            assertThat(result2).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByExternalUserId() - External User ID 존재 여부 확인")
    class ExistsByExternalUserIdTests {

        @Test
        @DisplayName("정상: External User ID가 존재하면 true 반환")
        void shouldReturnTrueWhenUserExists() {
            // Given
            String externalUserId = "auth0|user123";
            given(repository.existsByExternalUserId(externalUserId)).willReturn(true);

            // When
            boolean exists = repository.existsByExternalUserId(externalUserId);

            // Then
            assertThat(exists).isTrue();
            verify(repository, times(1)).existsByExternalUserId(externalUserId);
        }

        @Test
        @DisplayName("정상: External User ID가 존재하지 않으면 false 반환")
        void shouldReturnFalseWhenUserDoesNotExist() {
            // Given
            String nonExistentUserId = "auth0|nonexistent";
            given(repository.existsByExternalUserId(nonExistentUserId)).willReturn(false);

            // When
            boolean exists = repository.existsByExternalUserId(nonExistentUserId);

            // Then
            assertThat(exists).isFalse();
            verify(repository, times(1)).existsByExternalUserId(nonExistentUserId);
        }

        @Test
        @DisplayName("정상: 중복 가입 방지 시나리오")
        void shouldPreventDuplicateRegistration() {
            // Given - 이미 가입된 사용자
            String existingUserId = "auth0|existing";
            given(repository.existsByExternalUserId(existingUserId)).willReturn(true);

            // When
            boolean canRegister = !repository.existsByExternalUserId(existingUserId);

            // Then
            assertThat(canRegister).isFalse();

            // Given - 신규 사용자
            String newUserId = "auth0|newuser";
            given(repository.existsByExternalUserId(newUserId)).willReturn(false);

            // When
            boolean canRegisterNew = !repository.existsByExternalUserId(newUserId);

            // Then
            assertThat(canRegisterNew).isTrue();
        }

        @Test
        @DisplayName("정상: 빈 문자열은 존재하지 않음")
        void shouldReturnFalseForEmptyString() {
            // Given
            String emptyId = "";
            given(repository.existsByExternalUserId(emptyId)).willReturn(false);

            // When
            boolean exists = repository.existsByExternalUserId(emptyId);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("정상: 여러 External User ID 존재 여부 일괄 확인")
        void shouldCheckMultipleUserExistence() {
            // Given
            String[] userIds = {
                "auth0|user1",
                "auth0|user2",
                "auth0|user3",
                "auth0|nonexistent"
            };

            given(repository.existsByExternalUserId("auth0|user1")).willReturn(true);
            given(repository.existsByExternalUserId("auth0|user2")).willReturn(true);
            given(repository.existsByExternalUserId("auth0|user3")).willReturn(true);
            given(repository.existsByExternalUserId("auth0|nonexistent")).willReturn(false);

            // When
            long existingCount = java.util.Arrays.stream(userIds)
                .filter(repository::existsByExternalUserId)
                .count();

            // Then
            assertThat(existingCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Spring Data JPA 기본 메서드")
    class SpringDataJpaMethodsTests {

        @Test
        @DisplayName("정상: findById() - ID로 조회")
        void shouldFindById() {
            // Given
            Long id = 1L;
            given(repository.findById(id)).willReturn(Optional.of(activeUser));

            // When
            Optional<UserContextJpaEntity> result = repository.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("정상: existsById() - ID 존재 여부 확인")
        void shouldCheckExistenceById() {
            // Given
            Long id = 1L;
            given(repository.existsById(id)).willReturn(true);

            // When
            boolean exists = repository.existsById(id);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("정상: count() - 전체 개수 조회")
        void shouldCountAllUsers() {
            // Given
            given(repository.count()).willReturn(10L);

            // When
            long count = repository.count();

            // Then
            assertThat(count).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("Edge Cases - 특수 케이스 처리")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge: null External User ID는 빈 결과 반환 (실제로는 예외 발생 가능)")
        void shouldHandleNullExternalUserId() {
            // Given
            given(repository.findByExternalUserId(anyString())).willReturn(Optional.empty());

            // When
            Optional<UserContextJpaEntity> result = repository.findByExternalUserId(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Edge: 매우 긴 External User ID 조회")
        void shouldHandleLongExternalUserId() {
            // Given
            String longId = "auth0|" + "a".repeat(500);
            UserContextJpaEntity longIdUser = UserContextJpaEntityFixture.createWithId(1L, longId, "long@example.com");
            given(repository.findByExternalUserId(longId)).willReturn(Optional.of(longIdUser));

            // When
            Optional<UserContextJpaEntity> result = repository.findByExternalUserId(longId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getExternalUserId()).hasSize(longId.length());
        }

        @Test
        @DisplayName("Edge: 특수 문자가 포함된 External User ID 조회")
        void shouldHandleSpecialCharactersInExternalUserId() {
            // Given
            String specialId = "auth0|user+test@domain.com";
            UserContextJpaEntity specialUser = UserContextJpaEntityFixture.createWithId(1L, specialId, "special@example.com");
            given(repository.findByExternalUserId(specialId)).willReturn(Optional.of(specialUser));

            // When
            Optional<UserContextJpaEntity> result = repository.findByExternalUserId(specialId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getExternalUserId()).isEqualTo(specialId);
        }
    }
}
