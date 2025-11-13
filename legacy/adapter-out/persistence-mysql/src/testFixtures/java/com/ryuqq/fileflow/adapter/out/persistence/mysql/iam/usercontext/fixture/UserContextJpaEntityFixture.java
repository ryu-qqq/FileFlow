package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserContextJpaEntity;

import java.time.LocalDateTime;

/**
 * UserContextJpaEntity Test Fixture
 *
 * <p>테스트에서 UserContextJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성 (ID 없음)
 * UserContextJpaEntity user = UserContextJpaEntityFixture.create();
 *
 * // ID 포함 생성
 * UserContextJpaEntity user = UserContextJpaEntityFixture.createWithId(1L);
 *
 * // 커스텀 생성
 * UserContextJpaEntity user = UserContextJpaEntityFixture.create("auth0|abc123", "user@example.com");
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class UserContextJpaEntityFixture {

    private static final String DEFAULT_EXTERNAL_USER_ID = "auth0|test123";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private UserContextJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final boolean DEFAULT_DELETED = false;

    /**
     * 기본 UserContextJpaEntity 생성 (ID 없음)
     *
     * <p>신규 생성 시나리오 테스트에 사용합니다.</p>
     *
     * @return 새로운 UserContextJpaEntity
     */
    public static UserContextJpaEntity create() {
        return UserContextJpaEntity.create(
            DEFAULT_EXTERNAL_USER_ID,
            DEFAULT_EMAIL,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 커스텀 UserContextJpaEntity 생성 (ID 없음)
     *
     * @param externalUserId 외부 IDP 사용자 ID
     * @param email 사용자 이메일
     * @return 새로운 UserContextJpaEntity
     */
    public static UserContextJpaEntity create(String externalUserId, String email) {
        return UserContextJpaEntity.create(
            externalUserId,
            email,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * ID를 포함한 UserContextJpaEntity 생성 (재구성)
     *
     * <p>DB 조회 시나리오 테스트에 사용합니다.</p>
     *
     * @param id UserContext ID
     * @return 재구성된 UserContextJpaEntity
     */
    public static UserContextJpaEntity createWithId(Long id) {
        return UserContextJpaEntity.reconstitute(
            id,
            DEFAULT_EXTERNAL_USER_ID,
            DEFAULT_EMAIL,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 커스텀 ID를 포함한 UserContextJpaEntity 생성 (재구성)
     *
     * @param id UserContext ID
     * @param externalUserId 외부 IDP 사용자 ID
     * @param email 사용자 이메일
     * @return 재구성된 UserContextJpaEntity
     */
    public static UserContextJpaEntity createWithId(Long id, String externalUserId, String email) {
        return UserContextJpaEntity.reconstitute(
            id,
            externalUserId,
            email,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 삭제된 UserContextJpaEntity 생성 (재구성)
     *
     * @param id UserContext ID
     * @return 삭제된 UserContextJpaEntity
     */
    public static UserContextJpaEntity createDeleted(Long id) {
        return UserContextJpaEntity.reconstitute(
            id,
            DEFAULT_EXTERNAL_USER_ID,
            DEFAULT_EMAIL,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            true
        );
    }

    /**
     * 여러 개의 UserContextJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return UserContextJpaEntity 배열
     */
    public static UserContextJpaEntity[] createMultiple(int count) {
        UserContextJpaEntity[] entities = new UserContextJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                "auth0|user" + (i + 1),
                "user" + (i + 1) + "@example.com"
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 UserContextJpaEntity 생성 (재구성)
     *
     * @param id UserContext ID
     * @param externalUserId 외부 IDP 사용자 ID
     * @param email 사용자 이메일
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 UserContextJpaEntity
     */
    public static UserContextJpaEntity reconstitute(
        Long id,
        String externalUserId,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return UserContextJpaEntity.reconstitute(
            id,
            externalUserId,
            email,
            createdAt,
            updatedAt,
            deleted
        );
    }
}
