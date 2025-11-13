package com.ryuqq.fileflow.domain.upload.exception;

/**
 * Upload 도메인 에러 코드 Enum
 *
 * <p>Upload 바운디드 컨텍스트에서 발생하는 모든 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 구조:</strong></p>
 * <ul>
 *   <li>접두사: UPLOAD-</li>
 *   <li>범위: 001 ~ 099</li>
 *   <li>HTTP Status 매핑: ErrorMapper에서 처리</li>
 * </ul>
 *
 * <p><strong>에러 코드 분류:</strong></p>
 * <ul>
 *   <li>001-010: Session 관련 에러 (Not Found, State)</li>
 *   <li>011-020: Multipart 관련 에러 (Part, Type)</li>
 *   <li>021-030: 파일 검증 에러 (Size, Name, MIME)</li>
 *   <li>031-040: 비즈니스 로직 에러 (Storage, Context)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum UploadErrorCode {

    // ===== Session 관련 에러 (001-010) =====

    /**
     * Upload Session을 찾을 수 없음
     * HTTP: 404 Not Found
     */
    UPLOAD_SESSION_NOT_FOUND("UPLOAD-001", "Upload session not found"),

    /**
     * Upload Session 상태가 유효하지 않음
     * HTTP: 409 Conflict
     * 예: PENDING 상태가 아닌데 start() 호출
     */
    INVALID_SESSION_STATE("UPLOAD-002", "Invalid session state"),

    /**
     * Upload Session이 이미 완료됨
     * HTTP: 409 Conflict
     */
    UPLOAD_ALREADY_COMPLETED("UPLOAD-003", "Upload already completed"),

    /**
     * Upload Session이 만료됨
     * HTTP: 410 Gone
     */
    UPLOAD_SESSION_EXPIRED("UPLOAD-004", "Upload session expired"),

    // ===== Multipart 관련 에러 (011-020) =====

    /**
     * Upload 타입 불일치
     * HTTP: 400 Bad Request
     * 예: SINGLE 타입인데 Multipart 메서드 호출
     */
    UPLOAD_TYPE_MISMATCH("UPLOAD-011", "Upload type mismatch"),

    /**
     * 중복된 파트 번호
     * HTTP: 409 Conflict
     */
    DUPLICATE_PART_NUMBER("UPLOAD-012", "Duplicate part number"),

    /**
     * 파트 번호 범위 초과
     * HTTP: 400 Bad Request
     * 범위: 1 ~ 10,000
     */
    PART_NUMBER_OUT_OF_RANGE("UPLOAD-013", "Part number out of range"),

    /**
     * Multipart 업로드 미완료
     * HTTP: 400 Bad Request
     * 예: 모든 파트가 업로드되지 않았는데 complete() 호출
     */
    INCOMPLETE_MULTIPART_UPLOAD("UPLOAD-014", "Incomplete multipart upload"),

    /**
     * Multipart가 초기화되지 않음
     * HTTP: 400 Bad Request
     */
    MULTIPART_NOT_INITIALIZED("UPLOAD-015", "Multipart not initialized"),

    // ===== 파일 검증 에러 (021-030) =====

    /**
     * 파일 크기 제한 초과
     * HTTP: 413 Payload Too Large
     * 단일 업로드: 100MB, Multipart: 5TB
     */
    FILE_SIZE_LIMIT_EXCEEDED("UPLOAD-021", "File size limit exceeded"),

    /**
     * 유효하지 않은 파일명
     * HTTP: 400 Bad Request
     */
    INVALID_FILE_NAME("UPLOAD-022", "Invalid file name"),

    /**
     * 파일명 길이 초과
     * HTTP: 400 Bad Request
     * 최대 길이: 255자
     */
    FILE_NAME_TOO_LONG("UPLOAD-023", "File name too long"),

    /**
     * 유효하지 않은 MIME 타입
     * HTTP: 400 Bad Request
     */
    INVALID_MIME_TYPE("UPLOAD-024", "Invalid MIME type"),

    // ===== 비즈니스 로직 에러 (031-040) =====

    /**
     * Storage Context 누락
     * HTTP: 400 Bad Request
     */
    MISSING_STORAGE_CONTEXT("UPLOAD-031", "Missing storage context"),

    /**
     * 유효하지 않은 업로드 요청
     * HTTP: 400 Bad Request
     * 예: 필수 파라미터 누락
     */
    INVALID_UPLOAD_REQUEST("UPLOAD-032", "Invalid upload request");

    // ===== 필드 =====

    private final String code;
    private final String message;

    // ===== 생성자 =====

    /**
     * UploadErrorCode 생성자
     *
     * @param code 에러 코드 (예: "UPLOAD-001")
     * @param message 에러 메시지
     */
    UploadErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // ===== Getter =====

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드
     */
    public String code() {
        return code;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지
     */
    public String message() {
        return message;
    }
}
