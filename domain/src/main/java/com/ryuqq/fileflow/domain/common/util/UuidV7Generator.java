package com.ryuqq.fileflow.domain.common.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UUIDv7 생성 및 검증 유틸리티.
 *
 * <p>UUIDv7은 시간 기반 UUID로, 다음 특성을 가진다:
 *
 * <ul>
 *   <li>첫 48비트: Unix timestamp (밀리초)
 *   <li>4비트: 버전 (7)
 *   <li>12비트: 랜덤
 *   <li>2비트: variant (RFC 4122)
 *   <li>62비트: 랜덤
 * </ul>
 *
 * <p><strong>장점</strong>:
 *
 * <ul>
 *   <li>시간순 정렬 가능 (DB 인덱스 성능 향상)
 *   <li>분산 시스템에서 충돌 없는 고유 ID
 *   <li>생성 시간 추출 가능
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * UUIDv7 형식 정규식.
     *
     * <p>형식: xxxxxxxx-xxxx-7xxx-yxxx-xxxxxxxxxxxx (y는 8, 9, a, b 중 하나)
     */
    private static final Pattern UUID_V7_PATTERN =
            Pattern.compile(
                    "^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);

    /** UUIDv7 총 길이 (하이픈 포함). */
    private static final int UUID_STRING_LENGTH = 36;

    private UuidV7Generator() {
        // 유틸리티 클래스는 인스턴스화 금지
    }

    /**
     * 새로운 UUIDv7을 생성한다.
     *
     * @return UUIDv7 문자열 (소문자, 하이픈 포함)
     */
    public static String generate() {
        return generate(Instant.now());
    }

    /**
     * 지정된 시간 기반으로 UUIDv7을 생성한다.
     *
     * <p>주로 테스트용으로 사용.
     *
     * @param timestamp 기준 시간
     * @return UUIDv7 문자열
     */
    public static String generate(Instant timestamp) {
        long milliseconds = timestamp.toEpochMilli();

        // 48비트 타임스탬프 + 4비트 버전(7) + 12비트 랜덤
        long mostSigBits = (milliseconds << 16) | (7L << 12) | (RANDOM.nextLong() & 0x0FFFL);

        // 2비트 variant (10) + 62비트 랜덤
        long leastSigBits = (2L << 62) | (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL);

        UUID uuid = new UUID(mostSigBits, leastSigBits);
        return uuid.toString();
    }

    /**
     * UUIDv7 형식인지 검증한다.
     *
     * @param value 검증할 문자열
     * @return 유효한 UUIDv7이면 true
     */
    public static boolean isValid(String value) {
        if (value == null || value.length() != UUID_STRING_LENGTH) {
            return false;
        }
        return UUID_V7_PATTERN.matcher(value).matches();
    }

    /**
     * UUIDv7 형식을 검증하고 유효하지 않으면 예외를 던진다.
     *
     * @param value 검증할 문자열
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 유효하지 않은 형식인 경우
     */
    public static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "는 null이거나 빈 문자열일 수 없습니다.");
        }
        if (!isValid(value)) {
            throw new IllegalArgumentException(fieldName + "는 유효한 UUIDv7 형식이어야 합니다: " + value);
        }
    }

    /**
     * UUIDv7에서 생성 시간을 추출한다.
     *
     * @param uuidV7 UUIDv7 문자열
     * @return 생성 시간
     * @throws IllegalArgumentException 유효하지 않은 UUIDv7인 경우
     */
    public static Instant extractTimestamp(String uuidV7) {
        validate(uuidV7, "uuidV7");

        UUID uuid = UUID.fromString(uuidV7);
        long mostSigBits = uuid.getMostSignificantBits();

        // 상위 48비트 추출 (타임스탬프)
        long milliseconds = mostSigBits >>> 16;
        return Instant.ofEpochMilli(milliseconds);
    }
}
