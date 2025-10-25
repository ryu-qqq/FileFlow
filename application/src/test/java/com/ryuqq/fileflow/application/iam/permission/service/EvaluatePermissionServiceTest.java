package com.ryuqq.fileflow.application.iam.permission.service;

import com.ryuqq.fileflow.application.iam.permission.dto.command.EvaluatePermissionCommand;
import com.ryuqq.fileflow.application.iam.permission.dto.context.EvaluationContext;
import com.ryuqq.fileflow.application.iam.permission.dto.context.ResourceAttributes;
import com.ryuqq.fileflow.domain.iam.permission.exception.DenialReason;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;
import com.ryuqq.fileflow.application.iam.abac.port.out.AbacEvaluatorPort;
import com.ryuqq.fileflow.application.iam.permission.port.out.GrantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EvaluatePermissionService Unit Test
 *
 * <p>4단계 권한 평가 파이프라인의 각 단계를 독립적으로 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("EvaluatePermissionService 테스트")
class EvaluatePermissionServiceTest {

    private GrantRepositoryPort grantRepositoryPort;
    private AbacEvaluatorPort abacEvaluatorPort;
    private EvaluatePermissionService evaluatePermissionService;

    @BeforeEach
    void setUp() {
        grantRepositoryPort = mock(GrantRepositoryPort.class);
        abacEvaluatorPort = mock(AbacEvaluatorPort.class);
        evaluatePermissionService = new EvaluatePermissionService(
            grantRepositoryPort,
            abacEvaluatorPort
        );
    }

    @Nested
    @DisplayName("Stage 1: Cache Lookup (Grant 조회)")
    class CacheLookupTests {

        @Test
        @DisplayName("사용자에게 Grant가 없으면 NO_GRANT로 거부한다")
        void shouldDenyWhenNoGrantsFound() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(Collections.emptyList());

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isFalse();
            assertThat(response.denialReason()).isEqualTo(DenialReason.NO_GRANT);

            verify(grantRepositoryPort).findEffectiveGrants(1001L, 10L, 100L);
            verify(abacEvaluatorPort, never()).evaluateCondition(anyString(), anyMap());
        }
    }

    @Nested
    @DisplayName("Stage 2: Permission Filtering (Permission 코드 매칭)")
    class PermissionFilteringTests {

        @Test
        @DisplayName("요청한 Permission과 일치하는 Grant가 없으면 NO_GRANT로 거부한다")
        void shouldDenyWhenNoMatchingPermission() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant differentPermissionGrant = new Grant(
                "UPLOADER",
                "file.delete",
                Scope.ORGANIZATION,
                null
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(differentPermissionGrant));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isFalse();
            assertThat(response.denialReason()).isEqualTo(DenialReason.NO_GRANT);
        }

        @Test
        @DisplayName("요청한 Permission과 일치하는 Grant가 있으면 허용한다")
        void shouldAllowWhenPermissionMatches() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant matchingGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.ORGANIZATION,
                null
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(matchingGrant));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isTrue();
            assertThat(response.denialReason()).isNull();
        }
    }

    @Nested
    @DisplayName("Stage 3: Scope Matching (Scope 계층 검증)")
    class ScopeMatchingTests {

        @Test
        @DisplayName("Grant의 Scope가 요청 Scope보다 작으면 SCOPE_MISMATCH로 거부한다")
        void shouldDenyWhenGrantScopeIsSmallerThanRequestedScope() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant selfScopeGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.SELF,
                null
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(selfScopeGrant));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isFalse();
            assertThat(response.denialReason()).isEqualTo(DenialReason.SCOPE_MISMATCH);
        }

        @Test
        @DisplayName("Grant의 Scope가 요청 Scope를 포함하면 허용한다")
        void shouldAllowWhenGrantScopeCoversRequestedScope() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "ADMIN");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant tenantScopeGrant = new Grant(
                "ADMIN",
                "file.upload",
                Scope.TENANT,
                null
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(tenantScopeGrant));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Stage 4: ABAC Evaluation (CEL 조건 평가)")
    class AbacEvaluationTests {

        @Test
        @DisplayName("조건 없는 Grant는 즉시 허용한다")
        void shouldAllowImmediatelyWhenNoCondition() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant unconditionalGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.ORGANIZATION,
                null
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(unconditionalGrant));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isTrue();
            verify(abacEvaluatorPort, never()).evaluateCondition(anyString(), anyMap());
        }

        @Test
        @DisplayName("ABAC 조건이 true를 반환하면 허용한다")
        void shouldAllowWhenAbacConditionReturnsTrue() {
            // Arrange
            String conditionExpr = "res.size_mb <= 20";
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            ResourceAttributes resourceAttributes = ResourceAttributes.builder()
                .attribute("size_mb", 15.5)
                .build();

            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(resourceAttributes)
                .build();

            Grant conditionalGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.ORGANIZATION,
                conditionExpr
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(conditionalGrant));
            when(abacEvaluatorPort.evaluateCondition(eq(conditionExpr), anyMap()))
                .thenReturn(true);

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isTrue();
            verify(abacEvaluatorPort).evaluateCondition(eq(conditionExpr), anyMap());
        }

        @Test
        @DisplayName("ABAC 조건이 false를 반환하면 CONDITION_NOT_MET로 거부한다")
        void shouldDenyWhenAbacConditionReturnsFalse() {
            // Arrange
            String conditionExpr = "res.size_mb <= 20";
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            ResourceAttributes resourceAttributes = ResourceAttributes.builder()
                .attribute("size_mb", 25.0)
                .build();

            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(resourceAttributes)
                .build();

            Grant conditionalGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.ORGANIZATION,
                conditionExpr
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(conditionalGrant));
            when(abacEvaluatorPort.evaluateCondition(eq(conditionExpr), anyMap()))
                .thenReturn(false);

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isFalse();
            assertThat(response.denialReason()).isEqualTo(DenialReason.CONDITION_NOT_MET);
        }

        @Test
        @DisplayName("ABAC 평가 중 예외 발생 시 CONDITION_EVALUATION_FAILED로 거부한다")
        void shouldDenyWhenAbacEvaluationThrowsException() {
            // Arrange
            String conditionExpr = "invalid.expression";
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "UPLOADER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant conditionalGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.ORGANIZATION,
                conditionExpr
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(conditionalGrant));
            when(abacEvaluatorPort.evaluateCondition(eq(conditionExpr), anyMap()))
                .thenThrow(new IllegalArgumentException("Invalid CEL expression"));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isFalse();
            assertThat(response.denialReason()).isEqualTo(DenialReason.CONDITION_EVALUATION_FAILED);
        }
    }

    @Nested
    @DisplayName("Edge Cases 및 예외 처리")
    class EdgeCasesTests {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> evaluatePermissionService.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EvaluatePermissionCommand");

            verify(grantRepositoryPort, never()).findEffectiveGrants(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("여러 Role의 Grant가 있을 때 올바르게 평가한다")
        void shouldEvaluateGrantsFromMultipleRoles() {
            // Arrange
            EvaluationContext context = new EvaluationContext(1001L, 10L, 100L, "MULTI_ROLE_USER");
            EvaluatePermissionCommand command = EvaluatePermissionCommand.builder()
                .userContext(context)
                .permissionCode("file.upload")
                .scope(Scope.ORGANIZATION)
                .resourceAttributes(ResourceAttributes.empty())
                .build();

            Grant uploaderGrant = new Grant(
                "UPLOADER",
                "file.upload",
                Scope.SELF,
                null
            );

            Grant adminGrant = new Grant(
                "ADMIN",
                "file.upload",
                Scope.TENANT,
                null
            );

            when(grantRepositoryPort.findEffectiveGrants(1001L, 10L, 100L))
                .thenReturn(List.of(uploaderGrant, adminGrant));

            // Act
            EvaluatePermissionResponse response = evaluatePermissionService.execute(command);

            // Assert
            assertThat(response.allowed()).isTrue();
        }
    }
}
