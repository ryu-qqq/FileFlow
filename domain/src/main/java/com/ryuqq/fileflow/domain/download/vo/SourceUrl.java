package com.ryuqq.fileflow.domain.download.vo;

import com.ryuqq.fileflow.domain.session.vo.FileName;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 외부 이미지 URL Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>HTTP 또는 HTTPS 프로토콜만 허용
 *   <li>null이거나 빈 문자열 불가
 *   <li>이미지 확장자 검증은 Content-Type으로 하므로 URL에서는 선택적
 * </ul>
 *
 * @param value URL 문자열
 */
public record SourceUrl(String value) {

    private static final Pattern HTTP_HTTPS_PATTERN =
            Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);

    /** Compact Constructor (검증 로직). */
    public SourceUrl {
        Objects.requireNonNull(value, "SourceUrl must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SourceUrl은 비어있을 수 없습니다.");
        }
        if (!HTTP_HTTPS_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "SourceUrl은 http:// 또는 https://로 시작해야 합니다: " + value);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param value URL 문자열
     * @return SourceUrl
     * @throws NullPointerException value가 null인 경우
     * @throws IllegalArgumentException 유효하지 않은 URL인 경우
     */
    public static SourceUrl of(String value) {
        return new SourceUrl(value);
    }

    /**
     * URL에서 파일명을 추출합니다.
     *
     * <p>추출 로직:
     *
     * <ol>
     *   <li>쿼리 파라미터 제거 (? 이후)
     *   <li>마지막 슬래시 이후 문자열 추출
     *   <li>추출 실패 시 기본값 반환: "external-download.{extension}"
     * </ol>
     *
     * @param extension 파일 확장자
     * @return FileName
     */
    public FileName extractFileName(String extension) {
        try {
            String path = this.value;

            // 쿼리 파라미터 제거
            int queryIndex = path.indexOf('?');
            if (queryIndex > 0) {
                path = path.substring(0, queryIndex);
            }

            // 마지막 슬래시 이후 파일명 추출
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                String name = path.substring(lastSlash + 1);
                if (!name.isBlank()) {
                    return FileName.of(name);
                }
            }
        } catch (Exception e) {
            // 추출 실패 시 기본값으로 fallback
        }

        // 기본값
        return FileName.of("external-download." + extension);
    }
}
