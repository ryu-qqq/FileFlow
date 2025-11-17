package com.ryuqq.fileflow.domain.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * UUID v7 생성 유틸리티
 * <p>
 * RFC 9562 표준을 따르는 시간 순서 기반 UUID를 생성합니다.
 * UUID v7은 타임스탬프를 기반으로 하여 자연스럽게 시간 순서대로 정렬됩니다.
 * </p>
 */
public class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7Generator() {
        // Utility class - 인스턴스화 방지
    }

    /**
     * UUID v7을 생성합니다.
     * <p>
     * 구조:
     * - 48비트: 타임스탬프 (밀리초)
     * - 4비트: 버전 (0111 = 7)
     * - 12비트: 랜덤 데이터
     * - 2비트: Variant (10)
     * - 62비트: 랜덤 데이터
     * </p>
     *
     * @return UUID v7 문자열 (36자, 하이픈 포함)
     */
    public static String generate() {
        // 1. 현재 타임스탬프 (밀리초)
        long timestamp = Instant.now().toEpochMilli();

        // 2. 랜덤 바이트 생성 (10 바이트)
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        // 3. UUID v7 구조 생성
        // timestamp (48비트) | version (4비트) | random (12비트)
        long mostSigBits = (timestamp << 16) | ((long) (randomBytes[0] & 0x0F) << 8) | (randomBytes[1] & 0xFF);
        mostSigBits |= 0x7000; // 버전 7 설정

        // random (2비트 variant + 62비트 random)
        long leastSigBits = 0;
        for (int i = 2; i < 10; i++) {
            leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xFF);
        }
        leastSigBits &= 0x3FFFFFFFFFFFFFFFL; // Variant 비트 클리어
        leastSigBits |= 0x8000000000000000L; // Variant 10 설정

        // 4. UUID 생성 및 문자열 변환
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        return uuid.toString();
    }
}
