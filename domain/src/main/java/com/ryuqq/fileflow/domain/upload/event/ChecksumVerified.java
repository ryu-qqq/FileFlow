package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.upload.vo.CheckSum;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 체크섬 검증 완료 이벤트
 *
 * 업로드된 파일의 체크섬이 성공적으로 검증되었을 때 발생하는 도메인 이벤트입니다.
 *
 * 용도:
 * - 파일 무결성 검증 완료 기록
 * - 다음 처리 단계(메타데이터 추출) 트리거
 * - 감사(Audit) 목적의 검증 기록
 */
public record ChecksumVerified(
        String sessionId,
        CheckSum expectedChecksum,
        CheckSum actualChecksum,
        boolean matched,
        LocalDateTime verifiedAt
) {

    /**
     * ChecksumVerified 이벤트를 생성합니다.
     *
     * @param sessionId 업로드 세션 ID
     * @param expectedChecksum 예상된 체크섬 (클라이언트 제공)
     * @param actualChecksum 실제 체크섬 (서버 계산)
     * @param matched 체크섬 일치 여부
     * @param verifiedAt 검증 완료 시간
     * @return ChecksumVerified 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ChecksumVerified of(
            String sessionId,
            CheckSum expectedChecksum,
            CheckSum actualChecksum,
            boolean matched,
            LocalDateTime verifiedAt
    ) {
        return of(sessionId, expectedChecksum, actualChecksum, matched, verifiedAt, Clock.systemDefaultZone());
    }

    /**
     * ChecksumVerified 이벤트를 생성합니다 (테스트용 Clock 주입).
     *
     * @param sessionId 업로드 세션 ID
     * @param expectedChecksum 예상된 체크섬 (클라이언트 제공)
     * @param actualChecksum 실제 체크섬 (서버 계산)
     * @param matched 체크섬 일치 여부
     * @param verifiedAt 검증 완료 시간
     * @param clock 시간 검증용 Clock
     * @return ChecksumVerified 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ChecksumVerified of(
            String sessionId,
            CheckSum expectedChecksum,
            CheckSum actualChecksum,
            boolean matched,
            LocalDateTime verifiedAt,
            Clock clock
    ) {
        validateSessionId(sessionId);
        validateExpectedChecksum(expectedChecksum);
        validateActualChecksum(actualChecksum);
        validateVerifiedAt(verifiedAt, clock);
        validateChecksumMatch(expectedChecksum, actualChecksum, matched);
        return new ChecksumVerified(sessionId, expectedChecksum, actualChecksum, matched, verifiedAt);
    }

    /**
     * 검증 성공 이벤트를 생성합니다.
     *
     * @param sessionId 업로드 세션 ID
     * @param checksum 검증된 체크섬
     * @param verifiedAt 검증 완료 시간
     * @return ChecksumVerified 인스턴스
     */
    public static ChecksumVerified success(
            String sessionId,
            CheckSum checksum,
            LocalDateTime verifiedAt
    ) {
        return of(sessionId, checksum, checksum, true, verifiedAt);
    }

    /**
     * 검증 성공 이벤트를 생성합니다 (테스트용 Clock 주입).
     *
     * @param sessionId 업로드 세션 ID
     * @param checksum 검증된 체크섬
     * @param verifiedAt 검증 완료 시간
     * @param clock 시간 검증용 Clock
     * @return ChecksumVerified 인스턴스
     */
    public static ChecksumVerified success(
            String sessionId,
            CheckSum checksum,
            LocalDateTime verifiedAt,
            Clock clock
    ) {
        return of(sessionId, checksum, checksum, true, verifiedAt, clock);
    }

    /**
     * 검증 실패 이벤트를 생성합니다.
     *
     * @param sessionId 업로드 세션 ID
     * @param expectedChecksum 예상된 체크섬
     * @param actualChecksum 실제 체크섬
     * @param verifiedAt 검증 완료 시간
     * @return ChecksumVerified 인스턴스
     */
    public static ChecksumVerified failure(
            String sessionId,
            CheckSum expectedChecksum,
            CheckSum actualChecksum,
            LocalDateTime verifiedAt
    ) {
        return of(sessionId, expectedChecksum, actualChecksum, false, verifiedAt);
    }

    /**
     * 검증 실패 이벤트를 생성합니다 (테스트용 Clock 주입).
     *
     * @param sessionId 업로드 세션 ID
     * @param expectedChecksum 예상된 체크섬
     * @param actualChecksum 실제 체크섬
     * @param verifiedAt 검증 완료 시간
     * @param clock 시간 검증용 Clock
     * @return ChecksumVerified 인스턴스
     */
    public static ChecksumVerified failure(
            String sessionId,
            CheckSum expectedChecksum,
            CheckSum actualChecksum,
            LocalDateTime verifiedAt,
            Clock clock
    ) {
        return of(sessionId, expectedChecksum, actualChecksum, false, verifiedAt, clock);
    }

    /**
     * Compact constructor로 검증 로직 수행
     */
    public ChecksumVerified {
        // Note: Validation은 factory method에서 수행
        // Record는 불변이므로 생성 후 검증 불필요
    }

    // ========== Validation Methods ==========

    private static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
    }

    private static void validateExpectedChecksum(CheckSum expectedChecksum) {
        if (expectedChecksum == null) {
            throw new IllegalArgumentException("ExpectedChecksum cannot be null");
        }
    }

    private static void validateActualChecksum(CheckSum actualChecksum) {
        if (actualChecksum == null) {
            throw new IllegalArgumentException("ActualChecksum cannot be null");
        }
    }

    private static void validateVerifiedAt(LocalDateTime verifiedAt, Clock clock) {
        if (verifiedAt == null) {
            throw new IllegalArgumentException("VerifiedAt cannot be null");
        }
        if (verifiedAt.isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("VerifiedAt cannot be in the future");
        }
    }

    private static void validateChecksumMatch(
            CheckSum expectedChecksum,
            CheckSum actualChecksum,
            boolean matched
    ) {
        boolean actualMatch = expectedChecksum.matches(actualChecksum);
        if (matched != actualMatch) {
            throw new IllegalArgumentException(
                    "Matched flag does not reflect actual checksum comparison. " +
                    "Expected: " + actualMatch + ", but got: " + matched
            );
        }
    }

    /**
     * 검증이 성공했는지 확인합니다.
     *
     * @return 성공 여부
     */
    public boolean isSuccess() {
        return matched;
    }

    /**
     * 검증이 실패했는지 확인합니다.
     *
     * @return 실패 여부
     */
    public boolean isFailure() {
        return !matched;
    }
}
