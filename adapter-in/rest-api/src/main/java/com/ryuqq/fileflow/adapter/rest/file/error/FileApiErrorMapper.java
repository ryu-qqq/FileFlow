package com.ryuqq.fileflow.adapter.rest.file.error;

import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.adapter.rest.config.properties.ApiErrorProperties;
import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/**
 * File 바운디드 컨텍스트 에러 매퍼
 *
 * <p>File 도메인에서 발생한 예외를 HTTP 응답 형식으로 변환합니다.</p>
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>File 도메인 에러 코드를 HTTP 상태 코드로 매핑</li>
 *   <li>국제화(i18n) 지원을 통한 다국어 에러 메시지</li>
 *   <li>RFC 7807 Problem Details 형식의 Type URI 생성</li>
 *   <li>ErrorMapperRegistry에 자동 등록 (@Component)</li>
 * </ul>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>FILE_ASSET_NOT_FOUND → 404 Not Found</li>
 *   <li>FILE_ASSET_ALREADY_DELETED → 410 Gone</li>
 *   <li>FILE_ASSET_ACCESS_DENIED → 403 Forbidden</li>
 *   <li>INVALID_FILE_ASSET_STATE → 409 Conflict</li>
 *   <li>FILE_ASSET_PROCESSING → 425 Too Early</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileApiErrorMapper implements ErrorMapper {

    private static final String PREFIX = "FILE-";

    private final MessageSource messageSource;
    private final ApiErrorProperties errorProperties;

    /**
     * FileApiErrorMapper 생성자
     *
     * @param messageSource 국제화 메시지 소스
     * @param errorProperties API 에러 설정
     */
    public FileApiErrorMapper(MessageSource messageSource, ApiErrorProperties errorProperties) {
        this.messageSource = messageSource;
        this.errorProperties = errorProperties;
    }

    /**
     * File 도메인 에러 코드를 지원하는지 확인
     *
     * <p>에러 코드가 "FILE-"로 시작하면 이 매퍼가 처리합니다.</p>
     *
     * @param code 에러 코드
     * @return true if 에러 코드가 "FILE-"로 시작
     */
    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    /**
     * File 도메인 예외를 HTTP 응답으로 매핑
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
        FileErrorCode errorCode = findErrorCode(code);

        if (errorCode == null) {
            return createDefaultMapping(ex, locale);
        }

        return switch (errorCode) {
            case FILE_ASSET_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.file.not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-not-found"))
            );

            case FILE_ASSET_ALREADY_DELETED -> new MappedError(
                HttpStatus.GONE,
                "Gone",
                getLocalizedMessage("error.file.already_deleted", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-already-deleted"))
            );

            case FILE_ASSET_ACCESS_DENIED -> new MappedError(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                getLocalizedMessage("error.file.access_denied", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-access-denied"))
            );

            case INVALID_FILE_ASSET_STATE -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.file.invalid_state", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-invalid-state"))
            );

            case FILE_ASSET_PROCESSING -> new MappedError(
                HttpStatus.TOO_EARLY,  // 425 Too Early
                "Too Early",
                getLocalizedMessage("error.file.processing", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-processing"))
            );

            case FILE_VARIANT_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.file.variant_not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-variant-not-found"))
            );

            case FILE_VARIANT_GENERATION_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.file.variant_generation_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-variant-generation-failed"))
            );

            case EXTRACTED_DATA_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.file.extracted_data_not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("extracted-data-not-found"))
            );

            case METADATA_EXTRACTION_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.file.metadata_extraction_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("metadata-extraction-failed"))
            );

            case PIPELINE_EXECUTION_FAILED -> new MappedError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                getLocalizedMessage("error.file.pipeline_execution_failed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("pipeline-execution-failed"))
            );

            case PIPELINE_TIMEOUT -> new MappedError(
                HttpStatus.REQUEST_TIMEOUT,
                "Request Timeout",
                getLocalizedMessage("error.file.pipeline_timeout", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("pipeline-timeout"))
            );
        };
    }

    /**
     * 에러 코드 문자열을 FileErrorCode enum으로 변환
     *
     * @param code 에러 코드 문자열 (예: "FILE-001")
     * @return FileErrorCode enum 또는 null
     */
    private FileErrorCode findErrorCode(String code) {
        return switch (code) {
            case "FILE-001" -> FileErrorCode.FILE_ASSET_NOT_FOUND;
            case "FILE-002" -> FileErrorCode.FILE_ASSET_ALREADY_DELETED;
            case "FILE-003" -> FileErrorCode.FILE_ASSET_ACCESS_DENIED;
            case "FILE-004" -> FileErrorCode.INVALID_FILE_ASSET_STATE;
            case "FILE-005" -> FileErrorCode.FILE_ASSET_PROCESSING;
            case "FILE-101" -> FileErrorCode.FILE_VARIANT_NOT_FOUND;
            case "FILE-102" -> FileErrorCode.FILE_VARIANT_GENERATION_FAILED;
            case "FILE-201" -> FileErrorCode.EXTRACTED_DATA_NOT_FOUND;
            case "FILE-202" -> FileErrorCode.METADATA_EXTRACTION_FAILED;
            case "FILE-301" -> FileErrorCode.PIPELINE_EXECUTION_FAILED;
            case "FILE-302" -> FileErrorCode.PIPELINE_TIMEOUT;
            default -> null;
        };
    }

    /**
     * 국제화 메시지 조회
     *
     * <p>MessageSource에서 메시지를 찾을 수 없으면 fallback 메시지를 반환합니다.</p>
     *
     * @param key 메시지 키 (예: "error.file.not_found")
     * @param args 메시지 템플릿 파라미터
     * @param locale 로케일
     * @param fallback 메시지를 찾을 수 없을 때 기본값
     * @return 국제화된 메시지
     */
    private String getLocalizedMessage(
        String key,
        java.util.Map<String, Object> args,
        Locale locale,
        String fallback
    ) {
        try {
            Object[] argsArray = args != null ? args.values().toArray() : new Object[0];
            return messageSource.getMessage(key, argsArray, locale);
        } catch (NoSuchMessageException e) {
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

