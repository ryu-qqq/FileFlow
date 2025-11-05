package com.ryuqq.fileflow.adapter.rest.upload.error;

import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.adapter.rest.config.properties.ApiErrorProperties;
import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.upload.exception.UploadErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/**
 * Upload 바운디드 컨텍스트 에러 매퍼
 *
 * <p>Upload 도메인에서 발생한 예외를 HTTP 응답 형식으로 변환합니다.</p>
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>Upload 도메인 에러 코드를 HTTP 상태 코드로 매핑</li>
 *   <li>국제화(i18n) 지원을 통한 다국어 에러 메시지</li>
 *   <li>RFC 7807 Problem Details 형식의 Type URI 생성</li>
 *   <li>에러별 상세 정보 및 문서 링크 제공</li>
 * </ul>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>UPLOAD_SESSION_NOT_FOUND → 404 Not Found</li>
 *   <li>INVALID_SESSION_STATE → 409 Conflict</li>
 *   <li>UPLOAD_ALREADY_COMPLETED → 409 Conflict</li>
 *   <li>UPLOAD_SESSION_EXPIRED → 410 Gone</li>
 *   <li>UPLOAD_TYPE_MISMATCH → 400 Bad Request</li>
 *   <li>DUPLICATE_PART_NUMBER → 409 Conflict</li>
 *   <li>PART_NUMBER_OUT_OF_RANGE → 400 Bad Request</li>
 *   <li>INCOMPLETE_MULTIPART_UPLOAD → 400 Bad Request</li>
 *   <li>MULTIPART_NOT_INITIALIZED → 400 Bad Request</li>
 *   <li>FILE_SIZE_LIMIT_EXCEEDED → 413 Payload Too Large</li>
 *   <li>INVALID_FILE_NAME → 400 Bad Request</li>
 *   <li>FILE_NAME_TOO_LONG → 400 Bad Request</li>
 *   <li>INVALID_MIME_TYPE → 400 Bad Request</li>
 *   <li>MISSING_STORAGE_CONTEXT → 400 Bad Request</li>
 *   <li>INVALID_UPLOAD_REQUEST → 400 Bad Request</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadApiErrorMapper implements ErrorMapper {

    private static final String PREFIX = "UPLOAD-";

    private final MessageSource messageSource;
    private final ApiErrorProperties errorProperties;

    /**
     * UploadApiErrorMapper 생성자
     *
     * @param messageSource 국제화 메시지 소스
     * @param errorProperties API 에러 설정
     */
    public UploadApiErrorMapper(MessageSource messageSource, ApiErrorProperties errorProperties) {
        this.messageSource = messageSource;
        this.errorProperties = errorProperties;
    }

    /**
     * Upload 도메인 에러 코드를 지원하는지 확인
     *
     * <p>에러 코드가 "UPLOAD-"로 시작하면 이 매퍼가 처리합니다.</p>
     *
     * @param code 에러 코드
     * @return true if 에러 코드가 "UPLOAD-"로 시작
     */
    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    /**
     * Upload 도메인 예외를 HTTP 응답으로 매핑
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

        // UploadErrorCode enum에 정의된 코드인지 확인
        UploadErrorCode errorCode = findErrorCode(code);

        if (errorCode == null) {
            // 정의되지 않은 에러 코드는 기본 매핑
            return createDefaultMapping(ex, locale);
        }

        return switch (errorCode) {
            case UPLOAD_SESSION_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.upload.session_not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("upload-session-not-found"))
            );

            case INVALID_SESSION_STATE -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.upload.invalid_session_state", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("invalid-session-state"))
            );

            case UPLOAD_ALREADY_COMPLETED -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.upload.already_completed", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("upload-already-completed"))
            );

            case UPLOAD_SESSION_EXPIRED -> new MappedError(
                HttpStatus.GONE,
                "Gone",
                getLocalizedMessage("error.upload.session_expired", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("upload-session-expired"))
            );

            case UPLOAD_TYPE_MISMATCH -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.type_mismatch", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("upload-type-mismatch"))
            );

            case DUPLICATE_PART_NUMBER -> new MappedError(
                HttpStatus.CONFLICT,
                "Conflict",
                getLocalizedMessage("error.upload.duplicate_part_number", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("duplicate-part-number"))
            );

            case PART_NUMBER_OUT_OF_RANGE -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.part_number_out_of_range", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("part-number-out-of-range"))
            );

            case INCOMPLETE_MULTIPART_UPLOAD -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.incomplete_multipart_upload", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("incomplete-multipart-upload"))
            );

            case MULTIPART_NOT_INITIALIZED -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.multipart_not_initialized", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("multipart-not-initialized"))
            );

            case FILE_SIZE_LIMIT_EXCEEDED -> new MappedError(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Payload Too Large",
                getLocalizedMessage("error.upload.file_size_limit_exceeded", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-size-limit-exceeded"))
            );

            case INVALID_FILE_NAME -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.invalid_file_name", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("invalid-file-name"))
            );

            case FILE_NAME_TOO_LONG -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.file_name_too_long", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("file-name-too-long"))
            );

            case INVALID_MIME_TYPE -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.invalid_mime_type", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("invalid-mime-type"))
            );

            case MISSING_STORAGE_CONTEXT -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.missing_storage_context", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("missing-storage-context"))
            );

            case INVALID_UPLOAD_REQUEST -> new MappedError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                getLocalizedMessage("error.upload.invalid_upload_request", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("invalid-upload-request"))
            );
        };
    }

    /**
     * 에러 코드 문자열을 UploadErrorCode enum으로 변환
     *
     * @param code 에러 코드 문자열 (예: "UPLOAD-001")
     * @return UploadErrorCode enum 또는 null
     */
    private UploadErrorCode findErrorCode(String code) {
        try {
            // "UPLOAD-001" → "UPLOAD_SESSION_NOT_FOUND"
            return switch (code) {
                case "UPLOAD-001" -> UploadErrorCode.UPLOAD_SESSION_NOT_FOUND;
                case "UPLOAD-002" -> UploadErrorCode.INVALID_SESSION_STATE;
                case "UPLOAD-003" -> UploadErrorCode.UPLOAD_ALREADY_COMPLETED;
                case "UPLOAD-004" -> UploadErrorCode.UPLOAD_SESSION_EXPIRED;
                case "UPLOAD-011" -> UploadErrorCode.UPLOAD_TYPE_MISMATCH;
                case "UPLOAD-012" -> UploadErrorCode.DUPLICATE_PART_NUMBER;
                case "UPLOAD-013" -> UploadErrorCode.PART_NUMBER_OUT_OF_RANGE;
                case "UPLOAD-014" -> UploadErrorCode.INCOMPLETE_MULTIPART_UPLOAD;
                case "UPLOAD-015" -> UploadErrorCode.MULTIPART_NOT_INITIALIZED;
                case "UPLOAD-021" -> UploadErrorCode.FILE_SIZE_LIMIT_EXCEEDED;
                case "UPLOAD-022" -> UploadErrorCode.INVALID_FILE_NAME;
                case "UPLOAD-023" -> UploadErrorCode.FILE_NAME_TOO_LONG;
                case "UPLOAD-024" -> UploadErrorCode.INVALID_MIME_TYPE;
                case "UPLOAD-031" -> UploadErrorCode.MISSING_STORAGE_CONTEXT;
                case "UPLOAD-032" -> UploadErrorCode.INVALID_UPLOAD_REQUEST;
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
     * @param key 메시지 키 (예: "error.upload.session_not_found")
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
