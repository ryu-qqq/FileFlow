package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RoleJpaEntity;

import java.time.LocalDateTime;

/**
 * RoleJpaEntity Test Fixture
 *
 * <p>테스트에서 RoleJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성
 * RoleJpaEntity role = RoleJpaEntityFixture.create();
 *
 * // 커스텀 생성
 * RoleJpaEntity role = RoleJpaEntityFixture.create("org.uploader", "Organization Uploader");
 *
 * // 여러 개 생성
 * RoleJpaEntity[] roles = RoleJpaEntityFixture.createMultiple(3);
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class RoleJpaEntityFixture {

    private static final String DEFAULT_CODE = "org.uploader";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private RoleJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_DESCRIPTION = "Organization file uploader role";
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_DELETED_AT = null;

    /**
     * 기본 RoleJpaEntity 생성
     *
     * @return 새로운 RoleJpaEntity
     */
    public static RoleJpaEntity create() {
        return new RoleJpaEntity(
            DEFAULT_CODE,
            DEFAULT_DESCRIPTION,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED_AT
        );
    }

    /**
     * 커스텀 RoleJpaEntity 생성
     *
     * @param code Role 코드
     * @param description Role 설명
     * @return 새로운 RoleJpaEntity
     */
    public static RoleJpaEntity create(String code, String description) {
        return new RoleJpaEntity(
            code,
            description,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED_AT
        );
    }

    /**
     * 삭제된 RoleJpaEntity 생성
     *
     * @param code Role 코드
     * @return 삭제된 RoleJpaEntity
     */
    public static RoleJpaEntity createDeleted(String code) {
        return new RoleJpaEntity(
            code,
            DEFAULT_DESCRIPTION,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            LocalDateTime.now()
        );
    }

    /**
     * 여러 개의 RoleJpaEntity 생성
     *
     * @param count 생성할 개수
     * @return RoleJpaEntity 배열
     */
    public static RoleJpaEntity[] createMultiple(int count) {
        RoleJpaEntity[] entities = new RoleJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = create(
                "role.code." + (i + 1),
                "Role Description " + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 RoleJpaEntity 생성
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deletedAt 삭제 일시
     * @return 새로운 RoleJpaEntity
     */
    public static RoleJpaEntity reconstitute(
        String code,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        return new RoleJpaEntity(
            code,
            description,
            createdAt,
            updatedAt,
            deletedAt
        );
    }
}
