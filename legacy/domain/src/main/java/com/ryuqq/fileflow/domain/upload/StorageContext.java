package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Storage Context Value Object
 * IAM 컨텍스트 기반 스토리지 경로 및 버킷 결정 정책
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>Tenant별 독립된 S3 버킷 할당</li>
 *   <li>Organization별 경로 분리 (Optional)</li>
 *   <li>User별 경로 분리 (Optional)</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Immutable Value Object</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class StorageContext {

    private static final String DEFAULT_BUCKET_PREFIX = "fileflow-uploads-";
    private static final String DEFAULT_PATH_TEMPLATE = "uploads/%d/%s/%s/%s_%s";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    // 불변 필드
    private final TenantId tenantId;
    private final OrganizationId organizationId;  // Optional
    private final Long userContextId;             // Optional
    private final String bucketPrefix;
    private final String storagePathTemplate;

    /**
     * Private 생성자
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID (Optional)
     * @param userContextId UserContext ID (Optional)
     * @param bucketPrefix S3 버킷 접두사
     * @param storagePathTemplate 스토리지 경로 템플릿
     */
    private StorageContext(
        TenantId tenantId,
        OrganizationId organizationId,
        Long userContextId,
        String bucketPrefix,
        String storagePathTemplate
    ) {
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.userContextId = userContextId;
        this.bucketPrefix = bucketPrefix;
        this.storagePathTemplate = storagePathTemplate;
    }

    /**
     * IAM 컨텍스트로부터 StorageContext 생성 (Static Factory Method)
     *
     * <p>Tenant, Organization, UserContext 정보를 기반으로
     * S3 버킷 및 스토리지 경로 정책을 결정합니다.</p>
     *
     * <p><strong>버킷 결정 규칙:</strong></p>
     * <ul>
     *   <li>기본: fileflow-uploads-{tenantId}</li>
     *   <li>Organization 있음: fileflow-uploads-{tenantId}-{orgCode}</li>
     * </ul>
     *
     * <p><strong>경로 결정 규칙:</strong></p>
     * <ul>
     *   <li>기본: uploads/{tenantId}/default/{date}/{uuid}_{fileName}</li>
     *   <li>Organization 있음: uploads/{tenantId}/{orgId}/{date}/{uuid}_{fileName}</li>
     * </ul>
     *
     * @param tenant Tenant Aggregate (필수)
     * @param organization Organization Aggregate (Optional)
     * @param userContext UserContext Aggregate (Optional)
     * @return StorageContext
     * @throws IllegalArgumentException tenant가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static StorageContext from(
        Tenant tenant,
        Organization organization,
        UserContext userContext
    ) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant는 필수입니다");
        }

        String bucketPrefix = determineBucketPrefix(tenant, organization);

        return new StorageContext(
            tenant.getId(),
            organization != null ? organization.getId() : null,
            userContext != null ? userContext.getIdValue() : null,
            bucketPrefix,
            DEFAULT_PATH_TEMPLATE
        );
    }

    /**
     * 최소 컨텍스트로 StorageContext 생성 (Static Factory Method)
     *
     * <p>Tenant ID만으로 StorageContext를 생성합니다.
     * Organization 및 UserContext는 null로 처리됩니다.</p>
     *
     * @param tenantId Tenant ID
     * @return StorageContext
     * @throws IllegalArgumentException tenantId가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static StorageContext forTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }

        return new StorageContext(
            tenantId,
            null,
            null,
            DEFAULT_BUCKET_PREFIX,
            DEFAULT_PATH_TEMPLATE
        );
    }

    /**
     * 시스템 작업용 StorageContext 생성 (Static Factory Method)
     *
     * <p>External Download 등 시스템이 수행하는 작업을 위한 StorageContext입니다.</p>
     * <p>Organization 및 UserContext는 null로 처리되며, 특별한 시스템 경로를 사용합니다.</p>
     *
     * @param tenantId Tenant ID
     * @return 시스템용 StorageContext
     * @throws IllegalArgumentException tenantId가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static StorageContext forSystemWithTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }

        // External Download는 시스템 작업이므로 특별한 경로 사용
        String systemPathTemplate = "system/external-downloads/%d/%s/%s/%s_%s";

        return new StorageContext(
            tenantId,
            null,  // Organization 없음 (시스템 작업)
            null,  // UserContext 없음 (시스템 작업)
            DEFAULT_BUCKET_PREFIX,
            systemPathTemplate
        );
    }

    /**
     * S3 버킷 이름 생성
     *
     * <p>패턴: {bucketPrefix}{tenantId}</p>
     *
     * @return S3 버킷 이름
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public String generateBucketName() {
        return bucketPrefix + tenantId.value();
    }

    /**
     * S3 Storage Key 생성
     *
     * <p>패턴: uploads/{tenantId}/{orgId}/{date}/{uuid}_{fileName}</p>
     *
     * @param fileName 파일명
     * @param uuid UUID
     * @param date 날짜
     * @return Storage Key
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public String generateStorageKey(FileName fileName, UUID uuid, LocalDate date) {
        if (fileName == null) {
            throw new IllegalArgumentException("File Name은 필수입니다");
        }
        if (uuid == null) {
            throw new IllegalArgumentException("UUID는 필수입니다");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date는 필수입니다");
        }

        String orgPath = organizationId != null
            ? organizationId.value().toString()
            : "default";

        String dateString = date.format(DATE_FORMATTER);

        return String.format(
            storagePathTemplate,
            tenantId.value(),
            orgPath,
            dateString,
            uuid.toString(),
            fileName.value()
        );
    }

    /**
     * Tenant ID를 반환합니다.
     *
     * @return Tenant ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * Organization ID를 반환합니다 (Optional).
     *
     * @return Organization ID (없으면 null)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    /**
     * UserContext ID를 반환합니다 (Optional).
     *
     * @return UserContext ID (없으면 null)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getUserContextId() {
        return userContextId;
    }

    /**
     * 버킷 접두사를 결정합니다 (Private Helper).
     *
     * @param tenant Tenant Aggregate
     * @param organization Organization Aggregate (Optional)
     * @return 버킷 접두사
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private static String determineBucketPrefix(Tenant tenant, Organization organization) {
        if (organization != null) {
            // Organization 있음: fileflow-uploads-{tenantId}-{orgCode}
            return DEFAULT_BUCKET_PREFIX;
        }
        // 기본: fileflow-uploads-{tenantId}
        return DEFAULT_BUCKET_PREFIX;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StorageContext that = (StorageContext) o;
        return Objects.equals(tenantId, that.tenantId) &&
               Objects.equals(organizationId, that.organizationId) &&
               Objects.equals(userContextId, that.userContextId);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(tenantId, organizationId, userContextId);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return StorageContext 정보 문자열
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "StorageContext{" +
            "tenantId=" + tenantId +
            ", organizationId=" + organizationId +
            ", userContextId=" + userContextId +
            ", bucketPrefix='" + bucketPrefix + '\'' +
            '}';
    }
}
