package com.ryuqq.fileflow.domain.iam.usercontext;

/**
 * 조직 멤버십 타입
 *
 * <p>사용자가 조직에서 가질 수 있는 역할/권한 유형을 정의하는 Enum입니다.</p>
 * <p>각 타입은 조직 내에서 사용자의 역할과 접근 권한을 결정합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java Enum 사용</li>
 *   <li>✅ 설명 필드 포함</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * <p><strong>타입 설명:</strong></p>
 * <ul>
 *   <li><strong>EMPLOYEE</strong>: 조직의 정규 직원 (일반 권한)</li>
 *   <li><strong>SELLER_MEMBER</strong>: 판매자 멤버 (판매 관련 권한)</li>
 *   <li><strong>GUEST</strong>: 게스트 사용자 (제한된 읽기 권한)</li>
 *   <li><strong>SYSTEM</strong>: 시스템 사용자 (내부 처리용, 특별 권한)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public enum MembershipType {

    /**
     * 조직의 정규 직원
     *
     * <p>일반적인 업무 처리 권한을 가진 조직 멤버입니다.</p>
     * <p>데이터 조회, 생성, 수정 등 일반적인 업무 권한이 부여됩니다.</p>
     */
    EMPLOYEE("조직 직원", "일반 업무 처리 권한을 가진 조직 멤버"),

    /**
     * 판매자 멤버
     *
     * <p>판매 관련 업무를 처리하는 외부 판매자입니다.</p>
     * <p>상품 관리, 주문 처리 등 판매 관련 권한이 부여됩니다.</p>
     */
    SELLER_MEMBER("판매자 멤버", "판매 관련 업무를 처리하는 외부 판매자"),

    /**
     * 게스트 사용자
     *
     * <p>임시 또는 제한된 접근 권한을 가진 사용자입니다.</p>
     * <p>읽기 권한만 부여되며, 데이터 수정은 불가능합니다.</p>
     */
    GUEST("게스트", "제한된 읽기 권한만 가진 임시 사용자"),

    /**
     * 시스템 사용자
     *
     * <p>내부 시스템 처리를 위한 특수 사용자입니다.</p>
     * <p>배치 작업, 자동화 프로세스 등에서 사용되며, 모든 권한이 부여됩니다.</p>
     */
    SYSTEM("시스템", "내부 시스템 처리를 위한 특수 사용자");

    private final String displayName;
    private final String description;

    /**
     * MembershipType 생성자
     *
     * @param displayName 표시 이름
     * @param description 타입 설명
     * @author ryu-qqq
     * @since 2025-10-24
     */
    MembershipType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 표시 이름을 반환합니다.
     *
     * @return 표시 이름
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 타입 설명을 반환합니다.
     *
     * @return 타입 설명
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDescription() {
        return description;
    }

    /**
     * 시스템 사용자인지 확인합니다.
     *
     * <p>시스템 사용자는 특별한 권한을 가지므로, 권한 검증 시 별도 처리가 필요합니다.</p>
     *
     * @return 시스템 사용자이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }

    /**
     * 게스트 사용자인지 확인합니다.
     *
     * <p>게스트 사용자는 제한된 권한을 가지므로, 권한 검증 시 별도 처리가 필요합니다.</p>
     *
     * @return 게스트 사용자이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isGuest() {
        return this == GUEST;
    }

    /**
     * 판매자 멤버인지 확인합니다.
     *
     * <p>판매자 멤버는 판매 관련 권한을 가지므로, 판매 기능 접근 시 별도 처리가 필요합니다.</p>
     *
     * @return 판매자 멤버이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isSellerMember() {
        return this == SELLER_MEMBER;
    }
}
