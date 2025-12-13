package com.ryuqq.fileflow.adapter.out.persistence.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

/**
 * Mapper 단위 테스트 지원 클래스
 *
 * <p>Mapper 테스트를 위한 공통 유틸리티를 제공합니다. Spring Context 없이 순수 JUnit 5로 테스트합니다.
 *
 * <p><strong>특징:</strong>
 *
 * <ul>
 *   <li>Spring Context 불필요 - 가장 빠른 실행
 *   <li>변환 로직 검증에 집중
 *   <li>리플렉션 기반 필드 비교 유틸리티 제공
 * </ul>
 *
 * <h2>사용 예시:</h2>
 *
 * <pre>{@code
 * @DisplayName("OrderJpaEntityMapper 단위 테스트")
 * class OrderJpaEntityMapperTest extends MapperTestSupport {
 *
 *     @Test
 *     @DisplayName("성공 - Entity를 Domain으로 변환")
 *     void toDomain_success() {
 *         // Given
 *         OrderJpaEntity entity = createTestEntity();
 *
 *         // When
 *         Order domain = OrderJpaEntityMapper.toDomain(entity);
 *
 *         // Then
 *         assertFieldsMatch(entity, domain, "customerId", "status");
 *     }
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 */
public abstract class MapperTestSupport {

    /**
     * 두 객체의 지정된 필드 값이 동일한지 비교
     *
     * <p>리플렉션을 사용하여 필드 값을 추출하고 비교합니다. 필드 이름이 동일해야 합니다.
     *
     * @param source 원본 객체
     * @param target 대상 객체
     * @param fieldNames 비교할 필드 이름들
     */
    protected void assertFieldsMatch(Object source, Object target, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object sourceValue = getFieldValue(source, fieldName);
            Object targetValue = getFieldValue(target, fieldName);

            assertThat(targetValue)
                    .as(
                            "Field '%s' should match. Source: %s, Target: %s",
                            fieldName, sourceValue, targetValue)
                    .isEqualTo(sourceValue);
        }
    }

    /**
     * 두 객체의 지정된 필드 값이 동일한지 비교 (다른 필드 이름)
     *
     * <p>원본과 대상의 필드 이름이 다를 때 사용합니다.
     *
     * @param source 원본 객체
     * @param sourceField 원본 필드 이름
     * @param target 대상 객체
     * @param targetField 대상 필드 이름
     */
    protected void assertFieldMatch(
            Object source, String sourceField, Object target, String targetField) {

        Object sourceValue = getFieldValue(source, sourceField);
        Object targetValue = getFieldValue(target, targetField);

        assertThat(targetValue)
                .as(
                        "Field mapping '%s' -> '%s' should match. Source: %s, Target: %s",
                        sourceField, targetField, sourceValue, targetValue)
                .isEqualTo(sourceValue);
    }

    /**
     * 객체가 null이 아니고 모든 지정된 필드가 null이 아닌지 검증
     *
     * @param object 검증할 객체
     * @param fieldNames 검증할 필드 이름들
     */
    protected void assertAllFieldsNotNull(Object object, String... fieldNames) {
        assertThat(object).isNotNull();

        for (String fieldName : fieldNames) {
            Object fieldValue = getFieldValue(object, fieldName);
            assertThat(fieldValue).as("Field '%s' should not be null", fieldName).isNotNull();
        }
    }

    /**
     * null 변환 테스트 유틸리티
     *
     * <p>null 입력 시 null을 반환하는지 검증합니다.
     *
     * @param result 변환 결과
     * @param inputDescription 입력 설명 (로깅용)
     */
    protected void assertNullConversion(Object result, String inputDescription) {
        assertThat(result).as("Conversion of %s should return null", inputDescription).isNull();
    }

    /**
     * 양방향 변환 동등성 검증
     *
     * <p>Domain -> Entity -> Domain 또는 Entity -> Domain -> Entity 변환 후 동등성을 검증합니다.
     *
     * @param original 원본 객체
     * @param converted 변환 후 객체
     * @param fieldNames 비교할 필드 이름들
     */
    protected void assertRoundTripEquality(
            Object original, Object converted, String... fieldNames) {

        assertThat(converted).isNotNull();

        for (String fieldName : fieldNames) {
            Object originalValue = getFieldValue(original, fieldName);
            Object convertedValue = getFieldValue(converted, fieldName);

            assertThat(convertedValue)
                    .as(
                            "Round-trip conversion should preserve field '%s'. Original: %s,"
                                    + " Converted: %s",
                            fieldName, originalValue, convertedValue)
                    .isEqualTo(originalValue);
        }
    }

    /**
     * 컬렉션 크기가 동일한지 검증
     *
     * @param sourceCollection 원본 컬렉션
     * @param targetCollection 대상 컬렉션
     * @param collectionName 컬렉션 이름 (로깅용)
     */
    protected void assertCollectionSizeMatch(
            java.util.Collection<?> sourceCollection,
            java.util.Collection<?> targetCollection,
            String collectionName) {

        int sourceSize = sourceCollection != null ? sourceCollection.size() : 0;
        int targetSize = targetCollection != null ? targetCollection.size() : 0;

        assertThat(targetSize)
                .as(
                        "Collection '%s' size should match. Source: %d, Target: %d",
                        collectionName, sourceSize, targetSize)
                .isEqualTo(sourceSize);
    }

    /**
     * 리플렉션으로 필드 값 추출
     *
     * @param object 대상 객체
     * @param fieldName 필드 이름
     * @return 필드 값
     */
    private Object getFieldValue(Object object, String fieldName) {
        if (object == null) {
            return null;
        }

        try {
            // 현재 클래스에서 필드 찾기
            Field field = findField(object.getClass(), fieldName);
            if (field == null) {
                throw new IllegalArgumentException(
                        "Field '"
                                + fieldName
                                + "' not found in class "
                                + object.getClass().getName());
            }
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + fieldName, e);
        }
    }

    /**
     * 클래스 계층에서 필드 찾기 (상위 클래스 포함)
     *
     * @param clazz 시작 클래스
     * @param fieldName 필드 이름
     * @return Field 객체 (없으면 null)
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * VO 값 추출 유틸리티
     *
     * <p>value() 또는 getValue() 메서드를 호출하여 VO의 원시 값을 추출합니다.
     *
     * @param vo Value Object
     * @return VO의 원시 값
     */
    protected Object extractVoValue(Object vo) {
        if (vo == null) {
            return null;
        }

        try {
            // value() 메서드 시도
            try {
                return vo.getClass().getMethod("value").invoke(vo);
            } catch (NoSuchMethodException ignored) {
            }

            // getValue() 메서드 시도
            try {
                return vo.getClass().getMethod("getValue").invoke(vo);
            } catch (NoSuchMethodException ignored) {
            }

            // 둘 다 없으면 객체 자체 반환
            return vo;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract VO value from " + vo.getClass().getName(), e);
        }
    }

    /**
     * 두 VO의 값이 동일한지 비교
     *
     * @param vo1 첫 번째 VO
     * @param vo2 두 번째 VO
     */
    protected void assertVoValuesEqual(Object vo1, Object vo2) {
        Object value1 = extractVoValue(vo1);
        Object value2 = extractVoValue(vo2);

        assertThat(value2)
                .as("VO values should be equal. VO1: %s, VO2: %s", value1, value2)
                .isEqualTo(value1);
    }
}
