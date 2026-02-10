package com.ryuqq.fileflow.domain.common.exception;

public class ErrorCodeFixture {

    public static TestErrorCode aTestError() {
        return TestErrorCode.TEST_ERROR;
    }

    public static TestErrorCode aNotFoundError() {
        return TestErrorCode.NOT_FOUND;
    }

    public enum TestErrorCode implements ErrorCode {
        TEST_ERROR("TEST-001", 400, "테스트 에러"),
        NOT_FOUND("TEST-002", 404, "리소스를 찾을 수 없습니다"),
        INTERNAL_ERROR("TEST-003", 500, "내부 서버 오류");

        private final String code;
        private final int httpStatus;
        private final String message;

        TestErrorCode(String code, int httpStatus, String message) {
            this.code = code;
            this.httpStatus = httpStatus;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public int getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
