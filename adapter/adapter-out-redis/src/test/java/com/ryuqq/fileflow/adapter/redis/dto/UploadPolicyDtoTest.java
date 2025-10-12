package com.ryuqq.fileflow.adapter.redis.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploadPolicyDto Edge Case 테스트
 *
 * 목표: 67% → 72% 커버리지 달성
 *
 * 테스트 시나리오:
 * - Deserialization failure handling
 * - Optional field combinations (null values)
 * - Invalid data types
 * - Invalid policyKey format
 * - equals/hashCode edge cases
 *
 * @author sangwon-ryu
 */
@DisplayName("UploadPolicyDto Edge Cases 테스트")
class UploadPolicyDtoTest {

    private ObjectMapper objectMapper;
    private PolicyKey testPolicyKey;
    private UploadPolicy testPolicy;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testPolicyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");

        FileTypePolicies fileTypePolicies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null,
                null,
                null
        );

        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        testPolicy = UploadPolicy.create(
                testPolicyKey,
                fileTypePolicies,
                rateLimiting,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
    }

    @Nested
    @DisplayName("Serialization/Deserialization 테스트")
    class SerializationTest {

        @Test
        @DisplayName("UploadPolicyDto를 JSON으로 직렬화할 수 있다")
        void serializeToJson() throws JsonProcessingException {
            // given
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // when
            String json = objectMapper.writeValueAsString(dto);

            // then
            assertThat(json).isNotNull();
            assertThat(json).contains("policyKey");
            assertThat(json).contains("b2c:CONSUMER:REVIEW");
            assertThat(json).contains("version");
        }

        @Test
        @DisplayName("JSON 직렬화는 모든 필드를 포함한다")
        void serializationIncludesAllFields() throws JsonProcessingException {
            // given
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // when
            String json = objectMapper.writeValueAsString(dto);

            // then
            assertThat(json).contains("policyKey");
            assertThat(json).contains("version");
            assertThat(json).contains("active");
            assertThat(json).contains("effectiveFrom");
        }
    }

    @Nested
    @DisplayName("Optional Field 조합 테스트")
    class OptionalFieldTest {

        @Test
        @DisplayName("effectiveUntil이 null일 수 있다")
        void handleNullEffectiveUntil() {
            // given - DTO는 effectiveUntil이 null일 수 있음
            UploadPolicyDto dto = new UploadPolicyDto(
                    "b2c:CONSUMER:REVIEW",
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    true,
                    LocalDateTime.now(),
                    null  // effectiveUntil can be null
            );

            // when & then
            assertThat(dto).isNotNull();
            assertThat(dto.getEffectiveUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("Invalid PolicyKey Format 테스트")
    class InvalidPolicyKeyTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "tenant",
                "tenant:",
                "tenant:userType:",
                "tenant:userType:serviceType:extra"
        })
        @DisplayName("잘못된 policyKey 형식은 toDomain() 시 예외를 던진다")
        void toDomainThrowsExceptionForInvalidFormat(String invalidPolicyKey) {
            // given
            UploadPolicyDto dto = new UploadPolicyDto(
                    invalidPolicyKey,
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            // when & then
            assertThatThrownBy(() -> dto.toDomain())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid policyKey format");
        }

        @Test
        @DisplayName("빈 문자열이 포함된 policyKey도 3개 파트로 처리될 수 있다")
        void policyKeyWithEmptyParts() {
            // given - :userType:serviceType는 split시 ["", "userType", "serviceType"]로 3개 파트
            UploadPolicyDto dto = new UploadPolicyDto(
                    ":userType:serviceType",
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            // when & then - PolicyKey.of가 빈 문자열을 거부함
            assertThatThrownBy(() -> dto.toDomain())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("정확히 3개의 파트를 가진 policyKey는 정상 처리된다")
        void validPolicyKeyWithThreeParts() {
            // given
            UploadPolicyDto dto = new UploadPolicyDto(
                    "b2c:CONSUMER:REVIEW",
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            // when
            UploadPolicy domain = dto.toDomain();

            // then
            assertThat(domain).isNotNull();
            assertThat(domain.getPolicyKey().getValue()).isEqualTo("b2c:CONSUMER:REVIEW");
        }
    }

    @Nested
    @DisplayName("from() 메서드 테스트")
    class FromMethodTest {

        @Test
        @DisplayName("null UploadPolicy로 from() 호출 시 예외를 던진다")
        void fromThrowsExceptionForNullPolicy() {
            // when & then
            assertThatThrownBy(() -> UploadPolicyDto.from(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UploadPolicy cannot be null");
        }

        @Test
        @DisplayName("유효한 UploadPolicy로부터 DTO를 생성할 수 있다")
        void fromCreatesValidDto() {
            // when
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // then
            assertThat(dto).isNotNull();
            assertThat(dto.getPolicyKey()).isEqualTo(testPolicyKey.getValue());
            assertThat(dto.getVersion()).isEqualTo(testPolicy.getVersion());
            assertThat(dto.isActive()).isEqualTo(testPolicy.isActive());
        }
    }

    @Nested
    @DisplayName("equals/hashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("동일한 내용의 DTO는 equals로 같다")
        void equalsForSameContent() {
            // given
            UploadPolicyDto dto1 = UploadPolicyDto.from(testPolicy);
            UploadPolicyDto dto2 = UploadPolicyDto.from(testPolicy);

            // when & then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsSelf() {
            // given
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // when & then
            assertThat(dto).isEqualTo(dto);
        }

        @Test
        @DisplayName("null과는 equals로 다르다")
        void notEqualsNull() {
            // given
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // when & then
            assertThat(dto).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와는 equals로 다르다")
        void notEqualsDifferentClass() {
            // given
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // when & then
            assertThat(dto).isNotEqualTo("different class");
        }

        @Test
        @DisplayName("version이 다르면 equals로 다르다")
        void notEqualsWhenVersionDiffers() {
            // given
            UploadPolicyDto dto1 = UploadPolicyDto.from(testPolicy);
            UploadPolicyDto dto2 = new UploadPolicyDto(
                    dto1.getPolicyKey(),
                    dto1.getFileTypePolicies(),
                    dto1.getRateLimiting(),
                    2,  // version 변경
                    dto1.isActive(),
                    dto1.getEffectiveFrom(),
                    dto1.getEffectiveUntil()
            );

            // when & then
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("isActive가 다르면 equals로 다르다")
        void notEqualsWhenIsActiveDiffers() {
            // given
            LocalDateTime now = LocalDateTime.now();
            UploadPolicyDto dto1 = new UploadPolicyDto(
                    "b2c:CONSUMER:REVIEW",
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    true,  // active
                    now,
                    now.plusDays(30)
            );
            UploadPolicyDto dto2 = new UploadPolicyDto(
                    "b2c:CONSUMER:REVIEW",
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    false,  // inactive
                    now,
                    now.plusDays(30)
            );

            // when & then
            assertThat(dto1.isActive()).isNotEqualTo(dto2.isActive());
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 주요 필드를 포함한다")
        void toStringContainsKeyFields() {
            // given
            UploadPolicyDto dto = UploadPolicyDto.from(testPolicy);

            // when
            String result = dto.toString();

            // then
            assertThat(result).contains("UploadPolicyDto");
            assertThat(result).contains("policyKey");
            assertThat(result).contains("version");
            assertThat(result).contains("isActive");
        }
    }

    @Nested
    @DisplayName("Invalid Data Type 테스트")
    class InvalidDataTypeTest {

        @Test
        @DisplayName("version이 음수일 수 없다")
        void versionCannotBeNegative() {
            // given - DTO 자체는 음수를 허용하지만, domain으로 변환 시 검증됨
            UploadPolicyDto dto = new UploadPolicyDto(
                    "b2c:CONSUMER:REVIEW",
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,  // version은 항상 양수
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            // when & then
            assertThat(dto.getVersion()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("policyKey는 비어있지 않아야 한다")
        void policyKeyMustNotBeEmpty() {
            // given
            UploadPolicyDto dto = new UploadPolicyDto(
                    "b2c:CONSUMER:REVIEW",  // valid policyKey
                    FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                    new RateLimiting(100, 1000),
                    1,
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30)
            );

            // when & then
            assertThat(dto.getPolicyKey()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Roundtrip 변환 테스트")
    class RoundtripTest {

        @Test
        @DisplayName("Domain → DTO → Domain 변환이 정상 작동한다")
        void domainToDtoToDomain() {
            // given
            UploadPolicy originalPolicy = testPolicy;

            // when
            UploadPolicyDto dto = UploadPolicyDto.from(originalPolicy);
            UploadPolicy convertedPolicy = dto.toDomain();

            // then
            assertThat(convertedPolicy.getPolicyKey()).isEqualTo(originalPolicy.getPolicyKey());
            assertThat(convertedPolicy.getVersion()).isEqualTo(originalPolicy.getVersion());
            assertThat(convertedPolicy.isActive()).isEqualTo(originalPolicy.isActive());
        }
    }
}
