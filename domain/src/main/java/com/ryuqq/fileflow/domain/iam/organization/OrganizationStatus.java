package com.ryuqq.fileflow.domain.iam.organization;

/**
 * Organization 상태
 *
 * <p>Organization의 운영 상태를 나타내는 Enum입니다.</p>
 *
 * <p><strong>상태 정의:</strong></p>
 * <ul>
 *   <li>{@link #ACTIVE} - 활성 상태 (정상 운영)</li>
 *   <li>{@link #INACTIVE} - 비활성 상태 (사용 중지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public enum OrganizationStatus {

    /**
     * 활성 상태
     *
     * <p>Organization이 정상적으로 운영되고 있는 상태입니다.</p>
     */
    ACTIVE,

    /**
     * 비활성 상태
     *
     * <p>Organization이 일시적으로 사용 중지된 상태입니다.</p>
     * <p>재활성화가 불가능하며, 주로 소프트 삭제 시 설정됩니다.</p>
     */
    INACTIVE
}
