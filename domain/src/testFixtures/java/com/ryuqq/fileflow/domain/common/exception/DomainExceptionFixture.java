package com.ryuqq.fileflow.domain.common.exception;

import java.util.Map;

public class DomainExceptionFixture {

    public static TestDomainException aDomainException() {
        return new TestDomainException(ErrorCodeFixture.TestErrorCode.TEST_ERROR);
    }

    public static TestDomainException aDomainException(ErrorCode errorCode) {
        return new TestDomainException(errorCode);
    }

    public static TestDomainException aDomainException(ErrorCode errorCode, String message) {
        return new TestDomainException(errorCode, message);
    }

    public static TestDomainException aDomainException(
            ErrorCode errorCode, String message, Map<String, Object> args) {
        return new TestDomainException(errorCode, message, args);
    }

    public static class TestDomainException extends DomainException {

        public TestDomainException(ErrorCode errorCode) {
            super(errorCode);
        }

        public TestDomainException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }

        public TestDomainException(ErrorCode errorCode, String message, Map<String, Object> args) {
            super(errorCode, message, args);
        }
    }
}
