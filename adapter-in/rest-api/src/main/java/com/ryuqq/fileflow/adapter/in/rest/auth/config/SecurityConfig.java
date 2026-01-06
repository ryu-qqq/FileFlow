package com.ryuqq.fileflow.adapter.in.rest.auth.config;

import com.ryuqq.fileflow.adapter.in.rest.auth.handler.SecurityExceptionHandler;
import com.ryuqq.fileflow.adapter.in.rest.auth.paths.SecurityPaths;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.UserContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정.
 *
 * <p>Gateway 연동 기반의 Stateless 인증을 구성합니다. Gateway에서 JWT 검증 후 X-* 헤더로 인증 정보를 전달합니다.
 *
 * <p><strong>인증/인가 흐름</strong>:
 *
 * <pre>
 * Gateway (JWT 검증) → X-* 헤더 → UserContextFilter → UserContext
 *                                                    ↓
 *                                      @PreAuthorize (권한 기반 접근 제어)
 * </pre>
 *
 * <p><strong>엔드포인트 권한 분류</strong> (SecurityPaths 참조):
 *
 * <ul>
 *   <li>PUBLIC: 인증 불필요 (헬스체크, Actuator, 에러 페이지)
 *   <li>DOCS: 인증 필요 (API 문서 - Service Token 또는 JWT로 접근)
 *   <li>AUTHENTICATED: 인증된 사용자 + @PreAuthorize 권한 검사 (파일 API)
 * </ul>
 *
 * <p><strong>권한 처리</strong>:
 *
 * <ul>
 *   <li>URL 기반 역할 검사 제거 → @PreAuthorize 어노테이션으로 대체
 *   <li>ResourceAccessChecker SpEL 함수로 리소스 접근 제어
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final SecurityExceptionHandler securityExceptionHandler;
    private final UserContextFilter userContextFilter;

    public SecurityConfig(
            SecurityExceptionHandler securityExceptionHandler,
            UserContextFilter userContextFilter) {
        this.securityExceptionHandler = securityExceptionHandler;
        this.userContextFilter = userContextFilter;
    }

    /**
     * Security Filter Chain을 구성합니다.
     *
     * @param http HttpSecurity 설정
     * @return SecurityFilterChain
     * @throws Exception 설정 예외
     */
    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화 (Stateless JWT 사용)
        http.csrf(AbstractHttpConfigurer::disable)
                // HTTP Basic 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // Form Login 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 세션 비활성화 (Stateless)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 인증 실패 / 접근 거부 핸들러 설정
                .exceptionHandling(
                        exception ->
                                exception
                                        .authenticationEntryPoint(securityExceptionHandler)
                                        .accessDeniedHandler(securityExceptionHandler))
                // UserContextFilter를 UsernamePasswordAuthenticationFilter 전에 등록
                .addFilterBefore(userContextFilter, UsernamePasswordAuthenticationFilter.class)
                // 엔드포인트 권한 설정
                .authorizeHttpRequests(this::configureAuthorization);

        return http.build();
    }

    /**
     * 엔드포인트 권한을 설정합니다.
     *
     * <p>SecurityPaths에서 정의된 경로별 권한을 설정합니다. API의 세부 권한은 @PreAuthorize 어노테이션으로 처리됩니다.
     *
     * <p><strong>권한 분류</strong>:
     *
     * <ul>
     *   <li>PUBLIC: 인증 불필요 (헬스체크, Actuator, 에러 페이지)
     *   <li>DOCS: 인증 불필요 (API 문서 - Swagger, OpenAPI, REST Docs)
     *   <li>AUTHENTICATED: 인증 필요 + @PreAuthorize 권한 검사 (파일 API)
     * </ul>
     *
     * @param auth AuthorizationManagerRequestMatcherRegistry
     */
    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                    auth) {

        // PUBLIC 엔드포인트 설정 (인증 불필요 - 헬스체크, Actuator, 에러 페이지)
        auth.requestMatchers(SecurityPaths.Public.PATTERNS.toArray(String[]::new)).permitAll();

        // DOCS 엔드포인트 설정 (인증 불필요 - Swagger, OpenAPI, REST Docs)
        auth.requestMatchers(SecurityPaths.Docs.PATTERNS.toArray(String[]::new)).permitAll();

        // 그 외 모든 요청은 인증 필요 + @PreAuthorize로 세부 권한 검사
        // UserContextFilter에서 Spring Security Authentication을 설정하면 통과
        // @PreAuthorize 어노테이션으로 세부 권한 검사
        auth.anyRequest().authenticated();
    }
}
