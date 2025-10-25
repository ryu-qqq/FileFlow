package com.ryuqq.fileflow.application.iam.permission.dto.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource Attributes Record
 *
 * <p>권한 평가 대상 리소스의 속성을 나타내는 불변 데이터 구조입니다.</p>
 *
 * <p><strong>사용 목적:</strong></p>
 * <ul>
 *   <li>ABAC 조건 평가 시 {@code res.*} 변수로 바인딩</li>
 *   <li>리소스 소유자, 크기, 상태 등 동적 속성 전달</li>
 *   <li>조건식 예시: {@code "res.size_mb <= 20"}, {@code "res.ownerId == ctx.userId"}</li>
 * </ul>
 *
 * <p><strong>예시:</strong></p>
 * <pre>
 * ResourceAttributes res = ResourceAttributes.builder()
 *     .attribute("ownerId", 1001L)
 *     .attribute("size_mb", 15.5)
 *     .attribute("fileType", "pdf")
 *     .attribute("status", "ACTIVE")
 *     .build();
 *
 * // ABAC 평가: "res.ownerId == ctx.userId && res.size_mb <= 20"
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Java 21 Record 패턴 사용</li>
 *   <li>✅ 불변성 보장 (Immutable Map)</li>
 *   <li>✅ Builder 패턴 제공</li>
 * </ul>
 *
 * @param attributes 리소스 속성 맵 (key: 속성명, value: 속성값)
 * @author ryu-qqq
 * @since 2025-10-25
 */
public record ResourceAttributes(
    Map<String, Object> attributes
) {

    /**
     * ResourceAttributes Compact Constructor
     *
     * <p>Record 생성 시 자동으로 호출되어 불변 Map으로 변환합니다.</p>
     *
     * @throws IllegalArgumentException attributes가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public ResourceAttributes {
        if (attributes == null) {
            throw new IllegalArgumentException("리소스 속성 맵은 필수입니다 (빈 Map은 허용)");
        }
        // 방어적 복사 + 불변 변환
        attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    /**
     * 빈 ResourceAttributes를 생성합니다
     *
     * <p>리소스 속성이 필요 없는 경우 사용합니다.</p>
     *
     * @return 빈 ResourceAttributes
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static ResourceAttributes empty() {
        return new ResourceAttributes(Collections.emptyMap());
    }

    /**
     * Builder를 통해 ResourceAttributes를 생성합니다
     *
     * @return ResourceAttributesBuilder
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static ResourceAttributesBuilder builder() {
        return new ResourceAttributesBuilder();
    }

    /**
     * 특정 속성값을 조회합니다
     *
     * @param key 속성 키
     * @return 속성값 (없으면 null)
     * @throws IllegalArgumentException key가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Object getAttribute(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("속성 키는 필수입니다");
        }
        return attributes.get(key.trim());
    }

    /**
     * 특정 속성이 존재하는지 확인합니다
     *
     * @param key 속성 키
     * @return 속성이 존재하면 true
     * @throws IllegalArgumentException key가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasAttribute(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("속성 키는 필수입니다");
        }
        return attributes.containsKey(key.trim());
    }

    /**
     * 속성이 비어있는지 확인합니다
     *
     * @return 속성이 비어있으면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    /**
     * 속성 개수를 반환합니다
     *
     * @return 속성 개수
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public int size() {
        return attributes.size();
    }

    /**
     * ResourceAttributes Builder
     *
     * <p>속성을 단계적으로 추가하여 ResourceAttributes를 생성합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static class ResourceAttributesBuilder {
        private final Map<String, Object> attributes;

        /**
         * Builder 생성자
         *
         * @author ryu-qqq
         * @since 2025-10-25
         */
        private ResourceAttributesBuilder() {
            this.attributes = new HashMap<>();
        }

        /**
         * 속성을 추가합니다
         *
         * @param key 속성 키
         * @param value 속성값 (null 허용)
         * @return Builder (메서드 체이닝)
         * @throws IllegalArgumentException key가 null이거나 빈 문자열인 경우
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public ResourceAttributesBuilder attribute(String key, Object value) {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("속성 키는 필수입니다");
            }
            this.attributes.put(key.trim(), value);
            return this;
        }

        /**
         * 여러 속성을 한번에 추가합니다
         *
         * @param attributes 추가할 속성 맵
         * @return Builder (메서드 체이닝)
         * @throws IllegalArgumentException attributes가 null인 경우
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public ResourceAttributesBuilder attributes(Map<String, Object> attributes) {
            if (attributes == null) {
                throw new IllegalArgumentException("속성 맵은 필수입니다");
            }
            this.attributes.putAll(attributes);
            return this;
        }

        /**
         * ResourceAttributes를 생성합니다
         *
         * @return 생성된 ResourceAttributes
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public ResourceAttributes build() {
            return new ResourceAttributes(this.attributes);
        }
    }

    /**
     * ResourceAttributes의 문자열 표현을 반환합니다 (디버깅 및 로깅용)
     *
     * @return ResourceAttributes의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "ResourceAttributes[empty]";
        }
        return String.format("ResourceAttributes[%d attributes: %s]", size(), attributes);
    }
}
