package com.ryuqq.fileflow.adapter.rest.exception;

/**
 * Missing Header Exception
 *
 * HTTP 헤더가 누락되었을 때 발생하는 예외입니다.
 * Interceptor에서 필수 헤더 검증 시 사용됩니다.
 *
 * 제약사항:
 * - NO Lombok
 *
 * @author sangwon-ryu
 */
public class MissingHeaderException extends RuntimeException {

    private final String headerName;

    /**
     * Constructor
     *
     * @param headerName 누락된 헤더 이름
     */
    public MissingHeaderException(String headerName) {
        super(String.format("Required header is missing: %s", headerName));
        this.headerName = headerName;
    }

    /**
     * 누락된 헤더 이름을 반환합니다.
     *
     * @return 헤더 이름
     */
    public String getHeaderName() {
        return headerName;
    }
}
