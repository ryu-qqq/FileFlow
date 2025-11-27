package com.ryuqq.fileflow.domain.session.vo;

import java.util.UUID;

/**
 * 업로드 세션 ID Value Object.
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code generate()} - UUID 기반 신규 ID 생성
 *   <li>{@code of(UUID value)} - 값 기반 생성
 * </ul>
 *
 * @param value UUID 값
 */
public record UploadSessionId(UUID value) {

    /** Compact Constructor (검증 로직). */
    public UploadSessionId {
        if (value == null) {
            throw new IllegalArgumentException("UploadSessionId는 null일 수 없습니다.");
        }
    }

    /**
     * 신규 세션 ID 생성 (UUID 랜덤 생성).
     *
     * @return 신규 UploadSessionId
     */
    public static UploadSessionId forNew() {
        return new UploadSessionId(UUID.randomUUID());
    }

    /**
     * 값 기반 생성.
     *
     * @param value UUID 값 (null 불가)
     * @return UploadSessionId
     * @throws IllegalArgumentException value가 null인 경우
     */
    public static UploadSessionId of(UUID value) {
        return new UploadSessionId(value);
    }

    public static UploadSessionId of(String value) {
        UUID uuid = UUID.fromString(value);
        return new UploadSessionId(uuid);
    }

    /**
     * ID가 신규인지 확인 (항상 false, 생성 시 값이 필수).
     *
     * @return 항상 false (null 허용하지 않음)
     */
    public boolean isNew() {
        return false; // Record 생성 시 null 검증으로 항상 값이 존재
    }

    public String getValue() {
        return value.toString();
    }
}
