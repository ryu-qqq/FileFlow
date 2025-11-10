package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.dto.GrantReadModel;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * Grant Entity Mapper - GrantReadModel ↔ Grant 변환
 *
 * <p><strong>역할</strong>: Query DTO (GrantReadModel)를 Domain Model (Grant)로 변환합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Static Utility Class - 인스턴스 불필요</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Scope 문자열 → Scope enum 변환</li>
 *   <li>✅ Null 처리 (conditionExpr nullable)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
public final class GrantEntityMapper {

    /**
     * Private Constructor - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    private GrantEntityMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * GrantReadModel → Grant 변환
     *
     * <p>QueryDSL 조회 결과 (GrantReadModel)를 Domain Model (Grant)로 변환합니다.</p>
     *
     * <p><strong>변환 규칙</strong>:</p>
     * <ul>
     *   <li>roleCode: String → String (그대로 전달)</li>
     *   <li>permissionCode: String → String (그대로 전달)</li>
     *   <li>defaultScope: String → Scope enum (대소문자 무시)</li>
     *   <li>conditionExpr: null (현재 GrantReadModel에 없음)</li>
     * </ul>
     *
     * @param readModel GrantReadModel (Not null)
     * @return Grant domain object
     * @throws IllegalArgumentException readModel이 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public static Grant toDomain(GrantReadModel readModel) {
        if (readModel == null) {
            throw new IllegalArgumentException("GrantReadModel must not be null");
        }

        Scope scope = parseScope(readModel.getDefaultScope());

        return new Grant(
            readModel.getRoleCode(),
            readModel.getPermissionCode(),
            scope,
            null  // conditionExpr는 현재 GrantReadModel에 없음
        );
    }

    /**
     * Scope 문자열 파싱
     *
     * <p>데이터베이스 문자열 값을 Scope enum으로 변환합니다.</p>
     * <p>대소문자를 무시하고 변환합니다 (SELF, self, Self 모두 허용).</p>
     *
     * @param scopeString Scope 문자열 (예: "SELF", "ORGANIZATION", "TENANT")
     * @return Scope enum
     * @throws IllegalArgumentException scopeString이 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-27
     */
    private static Scope parseScope(String scopeString) {
        if (scopeString == null || scopeString.isBlank()) {
            throw new IllegalArgumentException("Scope string must not be null or blank");
        }

        try {
            return Scope.valueOf(scopeString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("유효하지 않은 Scope 값입니다: %s (허용: SELF, ORGANIZATION, TENANT)", scopeString),
                e
            );
        }
    }
}
