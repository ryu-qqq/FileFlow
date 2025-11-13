package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.PermissionJpaEntity;

import java.time.LocalDateTime;

/**
 * PermissionJpaEntity Test Fixture
 *
 * <p>테스트에서 PermissionJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성
 * PermissionJpaEntity permission = PermissionJpaEntityFixture.create();
 *
 * // 커스텀 생성
 * PermissionJpaEntity permission = PermissionJpaEntityFixture.create("file.upload", "Upload files");
 *
 * // 여러 개 생성
 * PermissionJpaEntity[] permissions = PermissionJpaEntityFixture.createMultiple(5);
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class PermissionJpaEntityFixture {

    private static final String DEFAULT_CODE = "file.upload";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private PermissionJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_DESCRIPTION = "Upload files to the system";
    private static final String DEFAULT_SCOPE = "TENANT";
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_DELETED_AT = null;

    /**
     * 기본 PermissionJpaEntity 생성
     *
     * @return 새로운 PermissionJpaEntity
     */
    public static PermissionJpaEntity create() {
        return new PermissionJpaEntity(
            DEFAULT_CODE,
            DEFAULT_DESCRIPTION,
            DEFAULT_SCOPE,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED_AT
        );
    }

    /**
     * 커스텀 PermissionJpaEntity 생성
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @return 새로운 PermissionJpaEntity
     */
    public static PermissionJpaEntity create(String code, String description) {
        return new PermissionJpaEntity(
            code,
            description,
            DEFAULT_SCOPE,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED_AT
        );
    }

    /**
     * 특정 Scope의 PermissionJpaEntity 생성
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @return 새로운 PermissionJpaEntity
     */
    public static PermissionJpaEntity createWithScope(String code, String description, String defaultScope) {
        return new PermissionJpaEntity(
            code,
            description,
            defaultScope,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED_AT
        );
    }

    /**
     * 삭제된 PermissionJpaEntity 생성
     *
     * @param code Permission 코드
     * @return 삭제된 PermissionJpaEntity
     */
    public static PermissionJpaEntity createDeleted(String code) {
        return new PermissionJpaEntity(
            code,
            DEFAULT_DESCRIPTION,
            DEFAULT_SCOPE,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            LocalDateTime.now()
        );
    }

    /**
     * 여러 개의 PermissionJpaEntity 생성
     *
     * @param count 생성할 개수
     * @return PermissionJpaEntity 배열
     */
    public static PermissionJpaEntity[] createMultiple(int count) {
        PermissionJpaEntity[] entities = new PermissionJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = create(
                "permission.code." + (i + 1),
                "Permission Description " + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 PermissionJpaEntity 생성
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deletedAt 삭제 일시
     * @return 새로운 PermissionJpaEntity
     */
    public static PermissionJpaEntity reconstitute(
        String code,
        String description,
        String defaultScope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        return new PermissionJpaEntity(
            code,
            description,
            defaultScope,
            createdAt,
            updatedAt,
            deletedAt
        );
    }
}
