package com.ryuqq.fileflow.domain.upload.vo;

import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdempotencyValidator 테스트")
class IdempotencyValidatorTest {

    private IdempotencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IdempotencyValidator();
    }

    @Nested
    @DisplayName("기본 검증 테스트")
    class BasicValidationTest {

        @Test
        @DisplayName("처음 사용하는 키는 유효하다")
        void isValid_NewKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when
            boolean result = validator.isValid(key);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("이미 처리된 키는 유효하지 않다")
        void isValid_ProcessedKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);

            // when
            boolean result = validator.isValid(key);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 키는 예외를 발생시킨다")
        void isValid_NullKey() {
            // when & then
            assertThatThrownBy(() ->
                    validator.isValid(null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("IdempotencyKey cannot be null");
        }
    }

    @Nested
    @DisplayName("예외 발생 검증 테스트")
    class ThrowingValidationTest {

        @Test
        @DisplayName("새로운 키는 예외를 발생시키지 않는다")
        void validateOrThrow_NewKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when & then
            assertThatNoException().isThrownBy(() ->
                    validator.validateOrThrow(key)
            );
        }

        @Test
        @DisplayName("중복된 키는 DuplicateRequestException을 발생시킨다")
        void validateOrThrow_DuplicateKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);

            // when & then
            assertThatThrownBy(() ->
                    validator.validateOrThrow(key)
            ).isInstanceOf(IdempotencyValidator.DuplicateRequestException.class)
             .hasMessageContaining("Duplicate request detected")
             .hasMessageContaining(key.value());
        }
    }

    @Nested
    @DisplayName("처리 완료 표시 테스트")
    class MarkAsProcessedTest {

        @Test
        @DisplayName("새로운 키를 처리 완료로 표시할 수 있다")
        void markAsProcessed_NewKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when
            boolean result = validator.markAsProcessed(key);

            // then
            assertThat(result).isTrue();
            assertThat(validator.isProcessed(key)).isTrue();
        }

        @Test
        @DisplayName("이미 처리된 키를 다시 표시하면 false를 반환한다")
        void markAsProcessed_AlreadyProcessed() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);

            // when
            boolean result = validator.markAsProcessed(key);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("처리 완료 표시 시 null 키는 예외를 발생시킨다")
        void markAsProcessed_NullKey() {
            // when & then
            assertThatThrownBy(() ->
                    validator.markAsProcessed(null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("IdempotencyKey cannot be null");
        }
    }

    @Nested
    @DisplayName("처리 여부 확인 테스트")
    class IsProcessedTest {

        @Test
        @DisplayName("처리되지 않은 키는 false를 반환한다")
        void isProcessed_NotProcessed() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when
            boolean result = validator.isProcessed(key);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("처리된 키는 true를 반환한다")
        void isProcessed_Processed() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);

            // when
            boolean result = validator.isProcessed(key);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("원자적 검증 및 처리 테스트")
    class AtomicValidationTest {

        @Test
        @DisplayName("새로운 키를 원자적으로 검증하고 처리할 수 있다")
        void validateAndMarkAsProcessed_NewKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when & then
            assertThatNoException().isThrownBy(() ->
                    validator.validateAndMarkAsProcessed(key)
            );
            assertThat(validator.isProcessed(key)).isTrue();
        }

        @Test
        @DisplayName("중복된 키는 원자적 검증 시 예외를 발생시킨다")
        void validateAndMarkAsProcessed_DuplicateKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.validateAndMarkAsProcessed(key);

            // when & then
            assertThatThrownBy(() ->
                    validator.validateAndMarkAsProcessed(key)
            ).isInstanceOf(IdempotencyValidator.DuplicateRequestException.class)
             .hasMessageContaining("Duplicate request detected");
        }

        @Test
        @DisplayName("동시에 같은 키로 원자적 검증을 수행하면 하나만 성공한다")
        void validateAndMarkAsProcessed_Concurrent() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when
            validator.validateAndMarkAsProcessed(key);

            // then
            assertThatThrownBy(() ->
                    validator.validateAndMarkAsProcessed(key)
            ).isInstanceOf(IdempotencyValidator.DuplicateRequestException.class);
        }
    }

    @Nested
    @DisplayName("키 제거 및 초기화 테스트")
    class RemovalAndClearTest {

        @Test
        @DisplayName("처리된 키를 제거할 수 있다")
        void remove_ProcessedKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);

            // when
            boolean result = validator.remove(key);

            // then
            assertThat(result).isTrue();
            assertThat(validator.isProcessed(key)).isFalse();
        }

        @Test
        @DisplayName("처리되지 않은 키를 제거하면 false를 반환한다")
        void remove_NotProcessedKey() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when
            boolean result = validator.remove(key);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("모든 처리 이력을 초기화할 수 있다")
        void clear_AllKeys() {
            // given
            IdempotencyKey key1 = IdempotencyKey.generate();
            IdempotencyKey key2 = IdempotencyKey.generate();
            IdempotencyKey key3 = IdempotencyKey.generate();
            validator.markAsProcessed(key1);
            validator.markAsProcessed(key2);
            validator.markAsProcessed(key3);

            // when
            validator.clear();

            // then
            assertThat(validator.isProcessed(key1)).isFalse();
            assertThat(validator.isProcessed(key2)).isFalse();
            assertThat(validator.isProcessed(key3)).isFalse();
            assertThat(validator.size()).isZero();
        }

        @Test
        @DisplayName("제거 후 같은 키를 다시 사용할 수 있다")
        void reuseAfterRemoval() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);
            validator.remove(key);

            // when
            boolean result = validator.isValid(key);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("크기 확인 테스트")
    class SizeTest {

        @Test
        @DisplayName("초기 크기는 0이다")
        void initialSize() {
            // when
            int size = validator.size();

            // then
            assertThat(size).isZero();
        }

        @Test
        @DisplayName("키를 추가하면 크기가 증가한다")
        void sizeIncreasesOnAdd() {
            // given
            IdempotencyKey key1 = IdempotencyKey.generate();
            IdempotencyKey key2 = IdempotencyKey.generate();

            // when
            validator.markAsProcessed(key1);
            validator.markAsProcessed(key2);

            // then
            assertThat(validator.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("중복 키는 크기를 증가시키지 않는다")
        void sizeDoesNotIncreaseOnDuplicate() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key);

            // when
            validator.markAsProcessed(key);

            // then
            assertThat(validator.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("키를 제거하면 크기가 감소한다")
        void sizeDecreasesOnRemove() {
            // given
            IdempotencyKey key1 = IdempotencyKey.generate();
            IdempotencyKey key2 = IdempotencyKey.generate();
            validator.markAsProcessed(key1);
            validator.markAsProcessed(key2);

            // when
            validator.remove(key1);

            // then
            assertThat(validator.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("초기화하면 크기가 0이 된다")
        void sizeBecomesZeroOnClear() {
            // given
            validator.markAsProcessed(IdempotencyKey.generate());
            validator.markAsProcessed(IdempotencyKey.generate());
            validator.markAsProcessed(IdempotencyKey.generate());

            // when
            validator.clear();

            // then
            assertThat(validator.size()).isZero();
        }
    }

    @Nested
    @DisplayName("비즈니스 시나리오 테스트")
    class BusinessScenarioTest {

        @Test
        @DisplayName("동일한 멱등성 키로 두 번 요청하면 두 번째 요청은 실패한다")
        void duplicateRequest_Scenario() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();

            // when - 첫 번째 요청
            validator.validateAndMarkAsProcessed(key);

            // then - 두 번째 요청 실패
            assertThatThrownBy(() ->
                    validator.validateAndMarkAsProcessed(key)
            ).isInstanceOf(IdempotencyValidator.DuplicateRequestException.class);
        }

        @Test
        @DisplayName("다른 멱등성 키로 요청하면 모두 성공한다")
        void differentKeys_Scenario() {
            // given
            IdempotencyKey key1 = IdempotencyKey.generate();
            IdempotencyKey key2 = IdempotencyKey.generate();
            IdempotencyKey key3 = IdempotencyKey.generate();

            // when & then
            assertThatNoException().isThrownBy(() -> {
                validator.validateAndMarkAsProcessed(key1);
                validator.validateAndMarkAsProcessed(key2);
                validator.validateAndMarkAsProcessed(key3);
            });

            assertThat(validator.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("실패한 요청의 키를 제거하고 재시도할 수 있다")
        void retryAfterFailure_Scenario() {
            // given
            IdempotencyKey key = IdempotencyKey.generate();
            validator.markAsProcessed(key); // 첫 요청 성공
            validator.remove(key); // 실패로 간주하고 제거

            // when - 재시도
            boolean result = validator.isValid(key);

            // then
            assertThat(result).isTrue();
            assertThatNoException().isThrownBy(() ->
                    validator.validateAndMarkAsProcessed(key)
            );
        }
    }
}
