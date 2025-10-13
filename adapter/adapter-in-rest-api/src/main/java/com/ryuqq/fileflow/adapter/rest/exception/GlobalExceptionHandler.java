package com.ryuqq.fileflow.adapter.rest.exception;

import com.ryuqq.fileflow.adapter.rest.dto.response.ErrorResponse;
import com.ryuqq.fileflow.adapter.rest.dto.response.PolicyViolationErrorResponse;
import com.ryuqq.fileflow.domain.policy.exception.InvalidPolicyException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 *
 * REST API 전역 예외 처리를 담당합니다.
 * NO Inner Class 규칙 준수
 *
 * @author sangwon-ryu
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * PolicyNotFoundException 처리
     *
     * @param ex PolicyNotFoundException
     * @param request HttpServletRequest
     * @return 404 NOT_FOUND 응답
     */
    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePolicyNotFoundException(
            PolicyNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Policy Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * InvalidPolicyException 처리
     *
     * @param ex InvalidPolicyException
     * @param request HttpServletRequest
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(InvalidPolicyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPolicyException(
            InvalidPolicyException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Policy",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * UploadSessionNotFoundException 처리
     *
     * @param ex UploadSessionNotFoundException
     * @param request HttpServletRequest
     * @return 404 NOT_FOUND 응답
     */
    @ExceptionHandler(UploadSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUploadSessionNotFoundException(
            UploadSessionNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Upload Session Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * FileNotFoundInS3Exception 처리
     *
     * @param ex FileNotFoundInS3Exception
     * @param request HttpServletRequest
     * @return 404 NOT_FOUND 응답
     */
    @ExceptionHandler(FileNotFoundInS3Exception.class)
    public ResponseEntity<ErrorResponse> handleFileNotFoundInS3Exception(
            FileNotFoundInS3Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "File Not Found in S3",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * ChecksumMismatchException 처리
     *
     * @param ex ChecksumMismatchException
     * @param request HttpServletRequest
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(ChecksumMismatchException.class)
    public ResponseEntity<ErrorResponse> handleChecksumMismatchException(
            ChecksumMismatchException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Checksum Mismatch",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * PolicyViolationException 처리
     *
     * 표준화된 정책 위반 에러 응답을 반환합니다.
     * 에러 타입, 사용자 친화적 메시지, 상세 정보를 포함합니다.
     *
     * @param ex PolicyViolationException
     * @param request HttpServletRequest
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(PolicyViolationException.class)
    public ResponseEntity<PolicyViolationErrorResponse> handlePolicyViolationException(
            PolicyViolationException ex,
            HttpServletRequest request
    ) {
        PolicyViolationErrorResponse errorResponse = PolicyViolationErrorResponse.from(
                ex,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * MissingHeaderException 처리
     *
     * @param ex MissingHeaderException
     * @param request HttpServletRequest
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeaderException(
            MissingHeaderException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Missing Required Header",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리
     *
     * @param ex IllegalArgumentException
     * @param request HttpServletRequest
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * IllegalStateException 처리
     *
     * @param ex IllegalStateException
     * @param request HttpServletRequest
     * @return 409 CONFLICT 응답
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Validation 실패 처리
     *
     * @param ex MethodArgumentNotValidException
     * @param request HttpServletRequest
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errorMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 그 외 모든 예외 처리
     *
     * @param ex Exception
     * @param request HttpServletRequest
     * @return 500 INTERNAL_SERVER_ERROR 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * FieldError를 포맷팅합니다.
     * NO Inner Class 규칙에 따라 private 메서드로 구현
     *
     * @param fieldError FieldError
     * @return 포맷팅된 에러 메시지
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + Objects.requireNonNullElse(
                fieldError.getDefaultMessage(),
                "Invalid value"
        );
    }
}
