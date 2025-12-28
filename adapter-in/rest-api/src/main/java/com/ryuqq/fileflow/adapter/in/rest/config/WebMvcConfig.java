package com.ryuqq.fileflow.adapter.in.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 *
 * <p>정적 리소스 경로 설정을 담당합니다.
 *
 * <p>Gateway를 통해 접근 시 /file prefix가 필요하므로, REST Docs를 /file/docs 경로로 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * REST Docs 정적 리소스 핸들러 등록
     *
     * <p>Gateway를 통해 접근 시:
     *
     * <ul>
     *   <li>/api/v1/file/docs/** -> classpath:/static/docs/
     * </ul>
     *
     * @param registry 리소스 핸들러 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // REST Docs - Gateway 전체 경로 적용 (Gateway가 prefix를 strip하지 않음)
        registry.addResourceHandler("/api/v1/file/docs/**")
                .addResourceLocations("classpath:/static/docs/");

        // 직접 접근용 (로컬 개발 시)
        registry.addResourceHandler("/docs/**").addResourceLocations("classpath:/static/docs/");
    }
}
