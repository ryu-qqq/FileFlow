package com.ryuqq.fileflow.domain.file.log;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.time.LocalDateTime;

/**
 * FileAccessLog Entity
 *
 * <p>파일 접근 로그를 관리하는 Entity입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>파일 다운로드/조회 이력 기록</li>
 *   <li>접근 통계 데이터 제공 (집계용)</li>
 *   <li>IP 마스킹 (개인정보 보호)</li>
 * </ul>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>IP는 마스킹되어 저장됨 (192.168.1.123 → 192.168.1.0)</li>
 *   <li>로그는 생성 후 변경 불가 (Immutable)</li>
 *   <li>삭제는 Batch Job에 의해서만 수행 (Retention Policy)</li>
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong></p>
 * <ul>
 *   <li>✅ Long fileAssetId 사용 (JPA 관계 금지)</li>
 *   <li>✅ Long accessorUserId 사용</li>
 *   <li>❌ @ManyToOne FileAsset, User 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAccessLog {

    private final Long id;
    private final Long fileAssetId; // ⭐ Long FK Strategy
    private final Long accessorUserId; // ⭐ Long FK Strategy
    private final AccessType accessType;
    private final String ipAddress; // ⭐ 마스킹됨 (192.168.1.0)
    private final String userAgent;
    private final TenantId tenantId;
    private final Long organizationId;
    private final LocalDateTime accessedAt;

    /**
     * Private Constructor (Static Factory Method 사용)
     */
    private FileAccessLog(
        Long id,
        Long fileAssetId,
        Long accessorUserId,
        AccessType accessType,
        String ipAddress,
        String userAgent,
        TenantId tenantId,
        Long organizationId,
        LocalDateTime accessedAt
    ) {
        this.id = id;
        this.fileAssetId = fileAssetId;
        this.accessorUserId = accessorUserId;
        this.accessType = accessType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.accessedAt = accessedAt;
    }

    /**
     * FileAccessLog 생성 (Static Factory Method)
     *
     * <p>⭐ IP 마스킹 자동 적용</p>
     *
     * @param fileAssetId 파일 ID
     * @param accessorUserId 접근자 사용자 ID
     * @param accessType 접근 유형
     * @param ipAddress 원본 IP 주소 (자동 마스킹됨)
     * @param userAgent User-Agent 헤더
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (optional)
     * @return FileAccessLog Entity
     */
    public static FileAccessLog of(
        FileAssetId fileAssetId,
        Long accessorUserId,
        AccessType accessType,
        String ipAddress,
        String userAgent,
        TenantId tenantId,
        Long organizationId
    ) {
        if (fileAssetId == null || fileAssetId.value() == null) {
            throw new IllegalArgumentException("FileAsset ID는 필수입니다");
        }
        if (accessorUserId == null || accessorUserId <= 0) {
            throw new IllegalArgumentException("Accessor User ID는 필수입니다");
        }
        if (accessType == null) {
            throw new IllegalArgumentException("Access Type은 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }

        // IP 마스킹: 192.168.1.123 → 192.168.1.0
        String maskedIp = maskIpAddress(ipAddress);

        return new FileAccessLog(
            null, // ID는 Persistence Layer에서 생성
            fileAssetId.value(), // ⭐ Long FK 사용
            accessorUserId,
            accessType,
            maskedIp,
            userAgent,
            tenantId,
            organizationId,
            LocalDateTime.now()
        );
    }

    /**
     * DB에서 조회한 데이터로 FileAccessLog 재구성 (Static Factory Method)
     *
     * @param id FileAccessLog ID
     * @param fileAssetId 파일 ID
     * @param accessorUserId 접근자 사용자 ID
     * @param accessType 접근 유형
     * @param ipAddress IP 주소 (이미 마스킹됨)
     * @param userAgent User-Agent 헤더
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param accessedAt 접근 시간
     * @return FileAccessLog Entity
     */
    public static FileAccessLog reconstitute(
        Long id,
        Long fileAssetId,
        Long accessorUserId,
        AccessType accessType,
        String ipAddress,
        String userAgent,
        TenantId tenantId,
        Long organizationId,
        LocalDateTime accessedAt
    ) {
        return new FileAccessLog(
            id,
            fileAssetId,
            accessorUserId,
            accessType,
            ipAddress,
            userAgent,
            tenantId,
            organizationId,
            accessedAt
        );
    }

    /**
     * IP 주소 마스킹
     *
     * <p>개인정보 보호를 위해 마지막 옥텟을 0으로 마스킹합니다.</p>
     *
     * <p><strong>예시:</strong></p>
     * <ul>
     *   <li>192.168.1.123 → 192.168.1.0</li>
     *   <li>10.0.0.5 → 10.0.0.0</li>
     *   <li>null → null</li>
     * </ul>
     *
     * @param ipAddress 원본 IP 주소
     * @return 마스킹된 IP 주소
     */
    private static String maskIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }

        // IPv4 형식인지 확인
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return ipAddress; // 형식이 다르면 그대로 반환
        }

        // 마지막 옥텟을 0으로 변경
        return String.format("%s.%s.%s.0", parts[0], parts[1], parts[2]);
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getFileAssetId() {
        return fileAssetId;
    }

    public Long getAccessorUserId() {
        return accessorUserId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    
}
