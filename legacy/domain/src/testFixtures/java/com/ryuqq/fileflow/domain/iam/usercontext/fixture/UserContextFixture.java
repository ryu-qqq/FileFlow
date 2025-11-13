package com.ryuqq.fileflow.domain.iam.usercontext.fixture;

import com.ryuqq.fileflow.domain.iam.usercontext.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * UserContext Test Fixture
 *
 * <p>테스트에서 UserContext 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class UserContextFixture {

    private static final String DEFAULT_EXTERNAL_USER_ID = "auth0|test-user-001";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private UserContextFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_EMAIL = "test@example.com";

    public static UserContext createNew() {
        return UserContext.forNew(
            ExternalUserId.of(DEFAULT_EXTERNAL_USER_ID),
            Email.of(DEFAULT_EMAIL)
        );
    }

    public static UserContext createNew(String externalUserId, String email) {
        return UserContext.forNew(
            ExternalUserId.of(externalUserId),
            Email.of(email)
        );
    }

    public static UserContext createWithId(Long id) {
        return UserContext.of(
            UserContextId.of(id),
            ExternalUserId.of(DEFAULT_EXTERNAL_USER_ID),
            Email.of(DEFAULT_EMAIL)
        );
    }

    public static UserContext createWithId(Long id, String externalUserId, String email) {
        return UserContext.of(
            UserContextId.of(id),
            ExternalUserId.of(externalUserId),
            Email.of(email)
        );
    }

    public static java.util.List<UserContext> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> UserContext.of(
                UserContextId.of((long) i),
                ExternalUserId.of("auth0|test-user-" + String.format("%03d", i)),
                Email.of("test" + i + "@example.com")
            ))
            .toList();
    }

    public static UserContext reconstitute(
        Long id,
        String externalUserId,
        String email,
        List<Membership> memberships,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return UserContext.reconstitute(
            UserContextId.of(id),
            ExternalUserId.of(externalUserId),
            Email.of(email),
            memberships,
            createdAt,
            updatedAt,
            deleted
        );
    }

    public static UserContext createDeleted(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return UserContext.reconstitute(
            UserContextId.of(id),
            ExternalUserId.of(DEFAULT_EXTERNAL_USER_ID),
            Email.of(DEFAULT_EMAIL),
            List.of(),
            now,
            now,
            true
        );
    }

    public static UserContextBuilder builder() {
        return new UserContextBuilder();
    }

    public static class UserContextBuilder {
        private Long id;
        private String externalUserId = DEFAULT_EXTERNAL_USER_ID;
        private String email = DEFAULT_EMAIL;
        private List<Membership> memberships = List.of();
        private Clock clock = Clock.systemDefaultZone();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean deleted = false;

        public UserContextBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserContextBuilder externalUserId(String externalUserId) {
            this.externalUserId = externalUserId;
            return this;
        }

        public UserContextBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserContextBuilder memberships(List<Membership> memberships) {
            this.memberships = memberships;
            return this;
        }

        public UserContextBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public UserContextBuilder fixedClock(Instant instant) {
            this.clock = Clock.fixed(instant, ZoneId.systemDefault());
            return this;
        }

        public UserContextBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserContextBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserContextBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public UserContext build() {
            if (id == null) {
                return UserContext.forNew(
                    ExternalUserId.of(externalUserId),
                    Email.of(email)
                );
            }

            LocalDateTime now = LocalDateTime.now(clock);
            return UserContext.reconstitute(
                UserContextId.of(id),
                ExternalUserId.of(externalUserId),
                Email.of(email),
                memberships,
                createdAt != null ? createdAt : now,
                updatedAt != null ? updatedAt : now,
                deleted
            );
        }
    }
}
