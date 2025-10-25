package com.ryuqq.fileflow.application.iam.permission.dto.command;

import com.ryuqq.fileflow.application.iam.permission.dto.context.EvaluationContext;
import com.ryuqq.fileflow.application.iam.permission.dto.context.ResourceAttributes;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * Evaluate Permission Command
 *
 * <p>권한 평가 요청을 나타내는 불변 Command DTO입니다.</p>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command - 권한 평가 요청 (상태 변경 없음, Query에 가까움)</li>
 *   <li>UseCase: EvaluatePermissionUseCase</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
 *     .userContext(EvaluationContext.withRole(1001L, 10L, 100L, "UPLOADER"))
 *     .permissionCode("file.upload")
 *     .scope(Scope.ORGANIZATION)
 *     .resourceAttributes(ResourceAttributes.builder()
 *         .attribute("ownerId", 1001L)
 *         .attribute("size_mb", 15.5)
 *         .build())
 *     .build();
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Java 21 Record 패턴 사용</li>
 *   <li>✅ 불변성 보장 (Immutable)</li>
 *   <li>✅ Builder 패턴 제공</li>
 * </ul>
 *
 * @param userContext 사용자 컨텍스트 (Not null)
 * @param permissionCode 평가할 권한 코드 (Not null)
 * @param scope 요청된 Scope (Not null)
 * @param resourceAttributes 리소스 속성 (Nullable - 조건 없는 권한일 경우 생략 가능)
 * @author ryu-qqq
 * @since 2025-10-25
 */
public record EvaluatePermissionCommand(
    EvaluationContext userContext,
    String permissionCode,
    Scope scope,
    ResourceAttributes resourceAttributes
) {

    /**
     * EvaluatePermissionCommand Compact Constructor
     *
     * <p>Record 생성 시 자동으로 호출되어 유효성을 검증합니다.</p>
     *
     * @throws IllegalArgumentException userContext, permissionCode, scope가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public EvaluatePermissionCommand {
        if (userContext == null) {
            throw new IllegalArgumentException("사용자 컨텍스트는 필수입니다");
        }

        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("권한 코드는 필수입니다");
        }

        if (scope == null) {
            throw new IllegalArgumentException("Scope는 필수입니다");
        }

        // 문자열 정규화
        permissionCode = permissionCode.trim();

        // resourceAttributes가 null이면 빈 속성으로 초기화
        if (resourceAttributes == null) {
            resourceAttributes = ResourceAttributes.empty();
        }
    }

    /**
     * Builder를 통해 EvaluatePermissionCommand를 생성합니다
     *
     * @return EvaluatePermissionCommandBuilder
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluatePermissionCommandBuilder builder() {
        return new EvaluatePermissionCommandBuilder();
    }

    /**
     * 리소스 속성이 있는지 확인합니다
     *
     * @return 리소스 속성이 비어있지 않으면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasResourceAttributes() {
        return !resourceAttributes.isEmpty();
    }

    /**
     * Cache Lookup 키를 생성합니다
     *
     * <p>사용자 컨텍스트로부터 캐시 키를 추출합니다.</p>
     *
     * @return Cache 키 문자열
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getCacheKey() {
        return userContext.toCacheKey();
    }

    /**
     * EvaluatePermissionCommand Builder
     *
     * <p>필드를 단계적으로 설정하여 Command를 생성합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static class EvaluatePermissionCommandBuilder {
        private EvaluationContext userContext;
        private String permissionCode;
        private Scope scope;
        private ResourceAttributes resourceAttributes;

        /**
         * Builder 생성자
         *
         * @author ryu-qqq
         * @since 2025-10-25
         */
        private EvaluatePermissionCommandBuilder() {
        }

        /**
         * 사용자 컨텍스트를 설정합니다
         *
         * @param userContext 사용자 컨텍스트
         * @return Builder (메서드 체이닝)
         * @throws IllegalArgumentException userContext가 null인 경우
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public EvaluatePermissionCommandBuilder userContext(EvaluationContext userContext) {
            if (userContext == null) {
                throw new IllegalArgumentException("사용자 컨텍스트는 필수입니다");
            }
            this.userContext = userContext;
            return this;
        }

        /**
         * 권한 코드를 설정합니다
         *
         * @param permissionCode 권한 코드
         * @return Builder (메서드 체이닝)
         * @throws IllegalArgumentException permissionCode가 null이거나 빈 문자열인 경우
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public EvaluatePermissionCommandBuilder permissionCode(String permissionCode) {
            if (permissionCode == null || permissionCode.isBlank()) {
                throw new IllegalArgumentException("권한 코드는 필수입니다");
            }
            this.permissionCode = permissionCode.trim();
            return this;
        }

        /**
         * Scope를 설정합니다
         *
         * @param scope Scope
         * @return Builder (메서드 체이닝)
         * @throws IllegalArgumentException scope가 null인 경우
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public EvaluatePermissionCommandBuilder scope(Scope scope) {
            if (scope == null) {
                throw new IllegalArgumentException("Scope는 필수입니다");
            }
            this.scope = scope;
            return this;
        }

        /**
         * 리소스 속성을 설정합니다
         *
         * @param resourceAttributes 리소스 속성
         * @return Builder (메서드 체이닝)
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public EvaluatePermissionCommandBuilder resourceAttributes(ResourceAttributes resourceAttributes) {
            this.resourceAttributes = resourceAttributes;
            return this;
        }

        /**
         * EvaluatePermissionCommand를 생성합니다
         *
         * @return 생성된 EvaluatePermissionCommand
         * @throws IllegalArgumentException 필수 필드가 누락된 경우
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public EvaluatePermissionCommand build() {
            return new EvaluatePermissionCommand(
                userContext,
                permissionCode,
                scope,
                resourceAttributes
            );
        }
    }

    /**
     * EvaluatePermissionCommand의 문자열 표현을 반환합니다 (디버깅 및 로깅용)
     *
     * @return Command의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        return String.format(
            "EvaluatePermissionCommand[user=%s, permission='%s', scope=%s, hasResource=%b]",
            userContext, permissionCode, scope, hasResourceAttributes()
        );
    }
}
