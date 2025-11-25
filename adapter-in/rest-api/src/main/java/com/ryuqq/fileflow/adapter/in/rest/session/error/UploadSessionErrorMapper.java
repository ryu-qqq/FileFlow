package com.ryuqq.fileflow.adapter.in.rest.session.error;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Upload Session 도메인 예외를 HTTP 응답으로 매핑하는 ErrorMapper.
 *
 * <p>RFC 7807 Problem Details 표준을 준수하여 에러 응답을 생성합니다.
 *
 * <p><strong>지원하는 에러 코드:</strong>
 *
 * <ul>
 *   <li>FILE-SIZE-EXCEEDED (400)
 *   <li>UNSUPPORTED-FILE-TYPE (400)
 *   <li>INVALID-SESSION-STATUS (409)
 *   <li>SESSION-EXPIRED (410)
 *   <li>DUPLICATE-PART-NUMBER (409)
 *   <li>INVALID-PART-NUMBER (400)
 *   <li>INCOMPLETE-PARTS (412)
 *   <li>SESSION-NOT-FOUND (404)
 *   <li>PART-NOT-FOUND (404)
 *   <li>ETAG-MISMATCH (409)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UploadSessionErrorMapper implements ErrorMapper {

    private static final String TYPE_URI_PREFIX = "https://api.fileflow.com/errors/session/";
    private static final String MESSAGE_PREFIX = "error.session.";

    private final MessageSource messageSource;

    private final Set<String> supportedCodes;
    private final Map<String, HttpStatus> statusMap;

    /**
     * UploadSessionErrorMapper 생성자
     *
     * @param messageSource 메시지 소스 (I18N)
     */
    public UploadSessionErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.supportedCodes =
                Stream.of(SessionErrorCode.values())
                        .map(SessionErrorCode::getCode)
                        .collect(Collectors.toSet());
        this.statusMap =
                Stream.of(SessionErrorCode.values())
                        .collect(
                                Collectors.toMap(
                                        SessionErrorCode::getCode,
                                        code -> HttpStatus.valueOf(code.getHttpStatus())));
    }

    /**
     * 주어진 에러 코드를 이 매퍼가 지원하는지 확인합니다.
     *
     * @param code 에러 코드
     * @return 지원 여부
     */
    @Override
    public boolean supports(String code) {
        return supportedCodes.contains(code);
    }

    /**
     * DomainException을 MappedError로 변환합니다.
     *
     * @param ex 도메인 예외
     * @param locale 로케일 (I18N)
     * @return 매핑된 에러 정보
     */
    @Override
    public MappedError map(DomainException ex, Locale locale) {
        String code = ex.code();
        HttpStatus status = statusMap.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR);

        String title = resolveTitle(code, locale);
        String detail = resolveDetail(code, ex.getMessage(), locale);
        URI type = buildTypeUri(code);

        return new MappedError(status, title, detail, type);
    }

    /**
     * 에러 코드에 해당하는 제목을 조회합니다.
     *
     * @param code 에러 코드
     * @param locale 로케일
     * @return 에러 제목
     */
    private String resolveTitle(String code, Locale locale) {
        String messageKey = MESSAGE_PREFIX + normalizeCode(code) + ".title";
        return messageSource.getMessage(messageKey, null, getDefaultTitle(code), locale);
    }

    /**
     * 에러 코드에 해당하는 상세 메시지를 조회합니다.
     *
     * @param code 에러 코드
     * @param defaultMessage 기본 메시지
     * @param locale 로케일
     * @return 에러 상세 메시지
     */
    private String resolveDetail(String code, String defaultMessage, Locale locale) {
        String messageKey = MESSAGE_PREFIX + normalizeCode(code) + ".detail";
        return messageSource.getMessage(messageKey, null, defaultMessage, locale);
    }

    /**
     * RFC 7807 Type URI를 생성합니다.
     *
     * @param code 에러 코드
     * @return Type URI
     */
    private URI buildTypeUri(String code) {
        return URI.create(TYPE_URI_PREFIX + normalizeCode(code));
    }

    /**
     * 에러 코드를 URI에 사용할 수 있는 형태로 정규화합니다.
     *
     * @param code 에러 코드 (예: "FILE-SIZE-EXCEEDED")
     * @return 정규화된 코드 (예: "file-size-exceeded")
     */
    private String normalizeCode(String code) {
        return code.toLowerCase();
    }

    /**
     * 기본 제목을 반환합니다.
     *
     * @param code 에러 코드
     * @return 기본 제목
     */
    private String getDefaultTitle(String code) {
        for (SessionErrorCode errorCode : SessionErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return "Session Error";
    }
}
