package com.ryuqq.fileflow.adapter.rest.iam.tenant.error;

import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.adapter.rest.config.properties.ApiErrorProperties;
import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.iam.tenant.exception.TenantErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/**
 * Tenant 바운디드 컨텍스트 에러 매퍼
 *
 * <p>Tenant 도메인에서 발생한 예외를 HTTP 응답 형식으로 변환합니다.</p>
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>Tenant 도메인 에러 코드를 HTTP 상태 코드로 매핑</li>
 *   <li>국제화(i18n) 지원을 통한 다국어 에러 메시지</li>
 *   <li>RFC 7807 Problem Details 형식의 Type URI 생성</li>
 *   <li>에러별 상세 정보 및 문서 링크 제공</li>
 * </ul>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>TENANT_NOT_FOUND → 404 Not Found</li>
 *   <li>TENANT_NAME_DUPLICATED → 409 Conflict</li>
 *   <li>TENANT_CREATION_FAILED → 500 Internal Server Error</li>
 *   <li>TENANT_UPDATE_FAILED → 500 Internal Server Error</li>
 *   <li>TENANT_DELETION_FAILED → 500 Internal Server Error</li>
 *   <li>INVALID_TENANT_STATUS → 400 Bad Request</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 */
@Component
public class TenantApiErrorMapper implements ErrorMapper {

    private static final String PREFIX = "TENANT-";

    private final MessageSource messageSource;
    private final ApiErrorProperties errorProperties;

    /**
     * TenantApiErrorMapper 생성자
     *
     * @param messageSource 국제화 메시지 소스
     * @param errorProperties API 에러 설정
     */
    public TenantApiErrorMapper(MessageSource messageSource, ApiErrorProperties errorProperties) {
        this.messageSource = messageSource;
        this.errorProperties = errorProperties;
    }

    /**
     * Tenant 도메인 에러 코드를 지원하는지 확인
     *
     * <p>에러 코드가 "TENANT-"로 시작하면 이 매퍼가 처리합니다.</p>
     *
     * @param code 에러 코드
     * @return true if 에러 코드가 "TENANT-"로 시작
     */
    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    /**
     * Tenant 도메인 예외를 HTTP 응답으로 매핑
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

        // TenantErrorCode enum에 정의된 코드인지 확인
        TenantErrorCode errorCode = findErrorCode(code);

        if (errorCode == null) {
            // 정의되지 않은 에러 코드는 기본 매핑
            return createDefaultMapping(ex, locale);
        }

        return switch (errorCode) {
            case TENANT_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.tenant.not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-not-found"))
            );

            case TENANT_NAME_DUPLICATED -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.tenant.name_duplicated", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-name-duplicated"))
            );

            case TENANT_CREATION_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.tenant.creation_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-creation-failed"))
            );

            case TENANT_UPDATE_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.tenant.update_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-update-failed"))
            );

            case TENANT_DELETION_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.tenant.deletion_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-deletion-failed"))
            );

            case INVALID_TENANT_STATUS -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.tenant.invalid_status", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("invalid-tenant-status"))
            );
        };
    }

    /**
     * 에러 코드 문자열을 TenantErrorCode enum으로 변환
     *
     * @param code 에러 코드 문자열 (예: "TENANT-001")
     * @return TenantErrorCode enum 또는 null
     */
    private TenantErrorCode findErrorCode(String code) {
        try {
            // "TENANT-001" → "TENANT_NOT_FOUND"
            return switch (code) {
                case "TENANT-001" -> TenantErrorCode.TENANT_NOT_FOUND;
                case "TENANT-002" -> TenantErrorCode.TENANT_NAME_DUPLICATED;
                case "TENANT-003" -> TenantErrorCode.TENANT_CREATION_FAILED;
                case "TENANT-004" -> TenantErrorCode.TENANT_UPDATE_FAILED;
                case "TENANT-005" -> TenantErrorCode.TENANT_DELETION_FAILED;
                case "TENANT-006" -> TenantErrorCode.INVALID_TENANT_STATUS;
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
     * @param key 메시지 키 (예: "error.tenant.not_found")
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
