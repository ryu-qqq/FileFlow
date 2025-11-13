package com.ryuqq.fileflow.adapter.rest.iam.permission.error;

import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.adapter.rest.config.properties.ApiErrorProperties;
import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.iam.permission.exception.PermissionErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/**
 * Permission 바운디드 컨텍스트 에러 매퍼
 *
 * <p>Permission 도메인에서 발생한 예외를 HTTP 응답 형식으로 변환합니다.</p>
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>Permission 도메인 에러 코드를 HTTP 상태 코드로 매핑</li>
 *   <li>국제화(i18n) 지원을 통한 다국어 에러 메시지</li>
 *   <li>RFC 7807 Problem Details 형식의 Type URI 생성</li>
 *   <li>에러별 상세 정보 및 문서 링크 제공</li>
 * </ul>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>PERMISSION_DENIED → 403 Forbidden</li>
 *   <li>PERMISSION_NOT_FOUND → 404 Not Found</li>
 *   <li>ROLE_NOT_FOUND → 404 Not Found</li>
 *   <li>GRANT_NOT_FOUND → 404 Not Found</li>
 *   <li>INVALID_SCOPE → 400 Bad Request</li>
 *   <li>ABAC_EVALUATION_FAILED → 500 Internal Server Error</li>
 *   <li>PERMISSION_CODE_DUPLICATED → 409 Conflict</li>
 *   <li>ROLE_CODE_DUPLICATED → 409 Conflict</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 */
@Component
public class PermissionApiErrorMapper implements ErrorMapper {

    private static final String PREFIX = "PERMISSION-";

    private final MessageSource messageSource;
    private final ApiErrorProperties errorProperties;

    /**
     * PermissionApiErrorMapper 생성자
     *
     * @param messageSource 국제화 메시지 소스
     * @param errorProperties API 에러 설정
     */
    public PermissionApiErrorMapper(MessageSource messageSource, ApiErrorProperties errorProperties) {
        this.messageSource = messageSource;
        this.errorProperties = errorProperties;
    }

    /**
     * Permission 도메인 에러 코드를 지원하는지 확인
     *
     * <p>에러 코드가 "PERMISSION-"로 시작하면 이 매퍼가 처리합니다.</p>
     *
     * @param code 에러 코드
     * @return true if 에러 코드가 "PERMISSION-"로 시작
     */
    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    /**
     * Permission 도메인 예외를 HTTP 응답으로 매핑
     *
     * <p>에러 코드에 따라 적절한 HTTP 상태 코드와 메시지를 반환합니다.</p>
     * <p>MessageSource를 통해 다국어 메시지를 지원합니다.</p>
     *
     * @param ex 도메인 예외
     * @param locale 로케일 (다국어 지원)
     * @return HTTP 응답으로 매핑된 에러 정보
     */
    @Override
    public MappedError map(DomainException ex, Locale locale) {
        String code = ex.code();

        // PermissionErrorCode enum에 정의된 코드인지 확인
        PermissionErrorCode errorCode = findErrorCode(code);

        if (errorCode == null) {
            // 정의되지 않은 에러 코드는 기본 매핑
            return createDefaultMapping(ex, locale);
        }

        return switch (errorCode) {
            case PERMISSION_DENIED -> new MappedError(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                getLocalizedMessage("error.permission.denied", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("permission-denied"))
            );

            case PERMISSION_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.permission.not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("permission-not-found"))
            );

            case ROLE_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.permission.role_not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("role-not-found"))
            );

            case GRANT_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.permission.grant_not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("grant-not-found"))
            );

            case INVALID_SCOPE -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.permission.invalid_scope", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("invalid-scope"))
            );

            case ABAC_EVALUATION_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.permission.abac_evaluation_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("abac-evaluation-failed"))
            );

            case PERMISSION_CODE_DUPLICATED -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.permission.code_duplicated", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("permission-code-duplicated"))
            );

            case ROLE_CODE_DUPLICATED -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.permission.role_code_duplicated", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("role-code-duplicated"))
            );
        };
    }

    /**
     * 에러 코드 문자열을 PermissionErrorCode enum으로 변환
     *
     * @param code 에러 코드 문자열 (예: "PERMISSION-001")
     * @return PermissionErrorCode enum 또는 null
     */
    private PermissionErrorCode findErrorCode(String code) {
        try {
            // "PERMISSION-001" → "PERMISSION_DENIED"
            return switch (code) {
                case "PERMISSION-001" -> PermissionErrorCode.PERMISSION_DENIED;
                case "PERMISSION-002" -> PermissionErrorCode.PERMISSION_NOT_FOUND;
                case "PERMISSION-003" -> PermissionErrorCode.ROLE_NOT_FOUND;
                case "PERMISSION-004" -> PermissionErrorCode.GRANT_NOT_FOUND;
                case "PERMISSION-005" -> PermissionErrorCode.INVALID_SCOPE;
                case "PERMISSION-006" -> PermissionErrorCode.ABAC_EVALUATION_FAILED;
                case "PERMISSION-007" -> PermissionErrorCode.PERMISSION_CODE_DUPLICATED;
                case "PERMISSION-008" -> PermissionErrorCode.ROLE_CODE_DUPLICATED;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 국제화 메시지 조회
     *
     * <p>MessageSource에서 메시지를 찾을 수 없으면 fallback 메시지를 반환합니다.</p>
     *
     * @param key 메시지 키 (예: "error.permission.denied")
     * @param args 메시지 템플릿 파라미터
     * @param locale 로케일
     * @param fallback 메시지를 찾을 수 없을 때 기본값
     * @return 국제화된 메시지
     */
    private String getLocalizedMessage(String key, java.util.Map<String, Object> args, Locale locale, String fallback) {
        try {
            // args Map을 Object[] 배열로 변환
            Object[] argsArray = args != null ? args.values().toArray() : new Object[0];
            return messageSource.getMessage(key, argsArray, locale);
        } catch (NoSuchMessageException e) {
            // 메시지를 찾을 수 없으면 fallback 반환
            return fallback != null ? fallback : "An error occurred";
        }
    }

    /**
     * 기본 에러 매핑 (정의되지 않은 에러 코드)
     *
     * @param ex 도메인 예외
     * @param locale 로케일
     * @return 기본 매핑 결과
     */
    private MappedError createDefaultMapping(DomainException ex, Locale locale) {
        return new MappedError(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.getMessage() != null ? ex.getMessage() : "Invalid request",
            URI.create(errorProperties.buildTypeUri("bad-request"))
        );
    }
}
