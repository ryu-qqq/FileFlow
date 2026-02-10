package com.ryuqq.fileflow.adapter.in.rest.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Service Token 인증 필터.
 *
 * <p>X-Service-Name / X-Service-Token 헤더를 검증하여 내부 서비스 간 인증을 처리합니다.
 *
 * <ul>
 *   <li>{@code enabled=false}: 토큰 검증 없이 anonymous 서비스로 인증 (로컬 개발용)
 *   <li>{@code enabled=true}: X-Service-Token이 설정된 secret과 일치해야 인증 통과
 * </ul>
 */
public class ServiceTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(ServiceTokenAuthenticationFilter.class);

    static final String SERVICE_NAME_HEADER = "X-Service-Name";
    static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final String ROLE_SERVICE = "ROLE_SERVICE";

    private final ServiceTokenProperties properties;

    public ServiceTokenAuthenticationFilter(ServiceTokenProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            grantServiceAccess("anonymous");
            filterChain.doFilter(request, response);
            return;
        }

        String serviceName = request.getHeader(SERVICE_NAME_HEADER);
        String serviceToken = request.getHeader(SERVICE_TOKEN_HEADER);

        if (serviceToken != null && properties.getSecret().equals(serviceToken)) {
            String resolvedName =
                    (serviceName != null && !serviceName.isBlank()) ? serviceName : "unknown";
            grantServiceAccess(resolvedName);
            log.debug(
                    "Service authenticated: name={}, uri={}",
                    resolvedName,
                    request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private void grantServiceAccess(String serviceName) {
        var authorities = List.of(new SimpleGrantedAuthority(ROLE_SERVICE));
        var authentication =
                new PreAuthenticatedAuthenticationToken(serviceName, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
