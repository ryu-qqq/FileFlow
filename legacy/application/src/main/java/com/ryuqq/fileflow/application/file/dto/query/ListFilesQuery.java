package com.ryuqq.fileflow.application.file.dto.query;

import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.time.LocalDateTime;

/**
 * 파일 목록 조회 Query
 *
 * <p>CQRS Query Side - 페이징 및 필터링이 적용된 파일 목록 조회</p>
 *
 * <p><strong>필터링 조건:</strong></p>
 * <ul>
 *   <li>테넌트/조직 스코프 (필수)</li>
 *   <li>소유자 (선택)</li>
 *   <li>상태 (AVAILABLE, PROCESSING 등)</li>
 *   <li>가시성 (PRIVATE, INTERNAL, PUBLIC)</li>
 *   <li>업로드 기간</li>
 * </ul>
 *
 * <p><strong>정렬:</strong></p>
 * <ul>
 *   <li>기본: 최근 업로드 순 (uploadedAt DESC)</li>
 *   <li>선택: 파일명, 크기, 상태 등</li>
 * </ul>
 *
 * @param tenantId 테넌트 ID (필수)
 * @param organizationId 조직 ID (선택)
 * @param ownerUserId 파일 소유자 ID (선택)
 * @param status 파일 상태 필터 (선택)
 * @param visibility 가시성 필터 (선택)
 * @param uploadedAfter 업로드 시작 시간 (선택)
 * @param uploadedBefore 업로드 종료 시간 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ListFilesQuery(
    TenantId tenantId,
    Long organizationId,
    Long ownerUserId,
    FileStatus status,
    Visibility visibility,
    LocalDateTime uploadedAfter,
    LocalDateTime uploadedBefore,
    int page,
    int size
) {

    /**
     * Compact Constructor - 기본값 및 검증
     */
    public ListFilesQuery {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page는 0 이상이어야 합니다");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size는 1~100 사이여야 합니다");
        }
    }

    /**
     * Static Factory Method - 기본 페이징 (필터 없음)
     *
     * @param tenantId 테넌트 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return ListFilesQuery
     */
    public static ListFilesQuery of(
        TenantId tenantId,
        int page,
        int size
    ) {
        return new ListFilesQuery(
            tenantId,
            null,
            null,
            null,
            null,
            null,
            null,
            page,
            size
        );
    }

    /**
     * Static Factory Method - 소유자 필터
     *
     * @param tenantId 테넌트 ID
     * @param ownerUserId 소유자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return ListFilesQuery
     */
    public static ListFilesQuery ofOwner(
        TenantId tenantId,
        Long ownerUserId,
        int page,
        int size
    ) {
        return new ListFilesQuery(
            tenantId,
            null,
            ownerUserId,
            null,
            null,
            null,
            null,
            page,
            size
        );
    }

    /**
     * Static Factory Method - 상태 필터
     *
     * @param tenantId 테넌트 ID
     * @param status 파일 상태
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return ListFilesQuery
     */
    public static ListFilesQuery ofStatus(
        TenantId tenantId,
        FileStatus status,
        int page,
        int size
    ) {
        return new ListFilesQuery(
            tenantId,
            null,
            null,
            status,
            null,
            null,
            null,
            page,
            size
        );
    }

    /**
     * Offset 계산 (페이징용)
     *
     * @return Offset (page * size)
     */
    public int offset() {
        return page * size;
    }

    /**
     * Limit 반환 (페이징용)
     *
     * @return Limit (size)
     */
    public int limit() {
        return size;
    }
}
