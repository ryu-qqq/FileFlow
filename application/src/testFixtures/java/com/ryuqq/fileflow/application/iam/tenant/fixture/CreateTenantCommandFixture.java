package com.ryuqq.fileflow.application.iam.tenant.fixture;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;

/**
 * CreateTenantCommand 테스트 Fixture
 *
 * <p>테스트에서 CreateTenantCommand 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see CreateTenantCommand
 */
public class CreateTenantCommandFixture {

    /**
     * 기본값으로 CreateTenantCommand 생성
     *
     * @return 기본값을 가진 CreateTenantCommand
     */
    public static CreateTenantCommand create() {
        return new CreateTenantCommand("Test Tenant");
    }

    /**
     * 특정 이름으로 CreateTenantCommand 생성
     *
     * @param name Tenant 이름
     * @return 지정된 이름을 가진 CreateTenantCommand
     */
    public static CreateTenantCommand createWith(String name) {
        return new CreateTenantCommand(name);
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private CreateTenantCommandFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
