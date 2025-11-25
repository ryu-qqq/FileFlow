package com.ryuqq.fileflow.adapter.in.rest.config;

import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * UserContext Supplier 설정.
 *
 * <p>UserContextHolder를 통해 ThreadLocal에서 UserContext를 조회하는 Supplier를 제공합니다. Application 레이어의 기본
 * UserContextSupplier를 오버라이드합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class UserContextSupplierConfig {

    /**
     * UserContext Supplier Bean (Primary).
     *
     * <p>UserContextFilter에서 설정한 ThreadLocal의 UserContext를 조회합니다.
     *
     * @return UserContext Supplier
     */
    @Bean
    @Primary
    public Supplier<UserContext> userContextSupplier() {
        return UserContextHolder::getRequired;
    }
}
