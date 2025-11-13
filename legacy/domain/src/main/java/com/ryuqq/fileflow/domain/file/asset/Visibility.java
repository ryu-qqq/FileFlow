package com.ryuqq.fileflow.domain.file.asset;

/**
 * File Visibility Enum
 *
 * <p>파일의 공개 범위를 정의합니다.</p>
 *
 * <p><strong>가시성 레벨:</strong></p>
 * <ul>
 *   <li><strong>PRIVATE</strong>: 소유자만 접근 가능 (SELF scope)</li>
 *   <li><strong>INTERNAL</strong>: 조직 내 사용자 접근 가능 (ORGANIZATION scope)</li>
 *   <li><strong>PUBLIC</strong>: 테넌트 내 모든 사용자 접근 가능 (TENANT scope)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum Visibility {

    /**
     * 소유자만 접근 가능
     * IAM ABAC scope: SELF
     */
    PRIVATE,

    /**
     * 조직 내 사용자 접근 가능
     * IAM ABAC scope: ORGANIZATION
     */
    INTERNAL,

    /**
     * 테넌트 내 모든 사용자 접근 가능
     * IAM ABAC scope: TENANT
     */
    PUBLIC
}
