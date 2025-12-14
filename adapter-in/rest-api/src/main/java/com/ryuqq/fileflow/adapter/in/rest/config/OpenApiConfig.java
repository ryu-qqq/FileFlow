package com.ryuqq.fileflow.adapter.in.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI(Swagger) 설정
 *
 * <p>API 문서화 및 보안 스키마 정의
 *
 * <p>지원하는 인증 방식:
 *
 * <ul>
 *   <li>Bearer Token (JWT) - Authorization 헤더
 * </ul>
 *
 * <p>권한 체계:
 *
 * <ul>
 *   <li>file:read - 파일 조회 권한
 *   <li>file:write - 파일 업로드 권한
 *   <li>file:delete - 파일 삭제 권한
 *   <li>file:download - 파일 다운로드 권한
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "FileFlow API",
                        version = "1.0.0",
                        description =
                                """
                                FileFlow 파일 관리 서비스 API

                                ## 인증
                                모든 API는 Bearer Token 인증이 필요합니다.
                                Authorization 헤더에 JWT 토큰을 포함하여 요청하세요.

                                ## 권한 체계
                                | 권한 | 설명 |
                                |------|------|
                                | `file:read` | 파일 자산 조회, 업로드 세션 조회 |
                                | `file:write` | 업로드 세션 생성/완료/취소 |
                                | `file:delete` | 파일 자산 삭제 |
                                | `file:download` | 다운로드 URL 생성, 외부 다운로드 요청 |

                                ## 역할별 기본 권한
                                | 역할 | 권한 |
                                |------|------|
                                | `SUPER_ADMIN` | 모든 권한 |
                                | `ADMIN` | 테넌트 내 모든 권한 |
                                | `SELLER` | file:read, file:write, file:download |
                                | `CUSTOMER` | file:read, file:download |
                                """,
                        contact =
                                @Contact(
                                        name = "FileFlow Development Team",
                                        email = "dev@fileflow.io")),
        servers = {
            @Server(url = "/", description = "Current Server"),
            @Server(url = "http://localhost:8080", description = "Local Development")
        })
@SecuritySchemes({
    @SecurityScheme(
            name = "bearerAuth",
            type = SecuritySchemeType.HTTP,
            scheme = "bearer",
            bearerFormat = "JWT",
            description = "JWT 토큰을 입력하세요. 'Bearer ' prefix는 자동으로 추가됩니다."),
    @SecurityScheme(
            name = "apiKeyAuth",
            type = SecuritySchemeType.APIKEY,
            in = SecuritySchemeIn.HEADER,
            paramName = "X-API-Key",
            description = "API Key 인증 (서비스 간 통신용)")
})
public class OpenApiConfig {}
