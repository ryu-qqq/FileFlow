package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TenantResponseFixtures - Tenant Response Object Mother Pattern
 *
 * <p>Tenant Response DTO의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트에서 필요한 다양한 Response를 제공합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 TenantResponse
 * TenantResponse response = TenantResponseFixtures.tenantResponse();
 *
 * // 특정 ID와 이름으로 생성
 * TenantResponse response = TenantResponseFixtures.tenantResponse("tenant-id-123", "My Company");
 *
 * // SUSPENDED 상태의 TenantResponse
 * TenantResponse response = TenantResponseFixtures.suspendedTenantResponse();
 *
 * // 다수의 TenantResponse 리스트
 * List<TenantResponse> responses = TenantResponseFixtures.tenantResponseList(10);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public final class TenantResponseFixtures {

    private TenantResponseFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 기본 TenantResponse를 생성합니다 (ACTIVE 상태).
     *
     * <p>랜덤 UUID를 ID로 사용하고, "Test Company"를 이름으로 사용합니다.</p>
     *
     * @return TenantResponse (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantResponse tenantResponse() {
        return new TenantResponse(
            UUID.randomUUID().toString(),
            "Test Company",
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * 특정 ID와 이름을 가진 TenantResponse를 생성합니다 (ACTIVE 상태).
     *
     * @param tenantId Tenant ID
     * @param name Tenant 이름
     * @return TenantResponse (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantResponse tenantResponse(String tenantId, String name) {
        return new TenantResponse(
            tenantId,
            name,
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * 완전히 커스터마이징된 TenantResponse를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param name Tenant 이름
     * @param status Tenant 상태 (ACTIVE, SUSPENDED)
     * @param deleted 삭제 여부
     * @return TenantResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantResponse tenantResponse(String tenantId, String name, String status, boolean deleted) {
        return new TenantResponse(
            tenantId,
            name,
            status,
            deleted,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * SUSPENDED 상태의 TenantResponse를 생성합니다.
     *
     * @return TenantResponse (SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantResponse suspendedTenantResponse() {
        return new TenantResponse(
            UUID.randomUUID().toString(),
            "Suspended Company",
            "SUSPENDED",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * 특정 ID를 가진 SUSPENDED 상태의 TenantResponse를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return TenantResponse (SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantResponse suspendedTenantResponse(String tenantId) {
        return new TenantResponse(
            tenantId,
            "Suspended Company",
            "SUSPENDED",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * 삭제된 TenantResponse를 생성합니다 (Soft Delete).
     *
     * @return TenantResponse (deleted = true, status = SUSPENDED)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantResponse deletedTenantResponse() {
        return new TenantResponse(
            UUID.randomUUID().toString(),
            "Deleted Company",
            "SUSPENDED",
            true,  // deleted
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30)
        );
    }

    /**
     * 특정 개수의 TenantResponse 리스트를 생성합니다.
     *
     * <p>테스트에서 다수의 Tenant가 필요할 때 사용합니다 (Pagination 테스트 등).</p>
     *
     * @param count 생성할 TenantResponse 개수
     * @return TenantResponse 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<TenantResponse> tenantResponseList(int count) {
        List<TenantResponse> responses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            responses.add(new TenantResponse(
                UUID.randomUUID().toString(),
                "Test Company " + (i + 1),
                "ACTIVE",
                false,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
            ));
        }
        return responses;
    }

    /**
     * 특정 개수의 SUSPENDED 상태 TenantResponse 리스트를 생성합니다.
     *
     * @param count 생성할 TenantResponse 개수
     * @return SUSPENDED TenantResponse 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<TenantResponse> suspendedTenantResponseList(int count) {
        List<TenantResponse> responses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            responses.add(new TenantResponse(
                UUID.randomUUID().toString(),
                "Suspended Company " + (i + 1),
                "SUSPENDED",
                false,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(1)
            ));
        }
        return responses;
    }
}
