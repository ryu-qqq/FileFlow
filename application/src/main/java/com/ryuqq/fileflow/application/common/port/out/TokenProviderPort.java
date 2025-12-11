package com.ryuqq.fileflow.application.common.port.out;

/**
 * Token Provider Port
 *
 * <p>JWT 토큰 생성 및 검증을 위한 Port 인터페이스
 *
 * <p>역할:
 *
 * <ul>
 *   <li>Access Token / Refresh Token 생성
 *   <li>토큰 유효성 검증
 *   <li>토큰에서 회원 ID 추출
 * </ul>
 *
 * <p>구현체 위치:
 *
 * <ul>
 *   <li>adapter-out-security 또는 infrastructure 모듈
 *   <li>실제 JWT 라이브러리 사용 (jjwt, nimbus-jose-jwt 등)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface TokenProviderPort {

    /**
     * Access Token 생성
     *
     * @param memberId 회원 ID
     * @return 생성된 Access Token
     */
    String generateAccessToken(String memberId);

    /**
     * Refresh Token 생성
     *
     * @param memberId 회원 ID
     * @return 생성된 Refresh Token
     */
    String generateRefreshToken(String memberId);

    /**
     * Access Token 유효성 검증
     *
     * @param accessToken 검증할 Access Token
     * @return 유효 여부
     */
    boolean validateAccessToken(String accessToken);

    /**
     * Refresh Token 유효성 검증
     *
     * @param refreshToken 검증할 Refresh Token
     * @return 유효 여부
     */
    boolean validateRefreshToken(String refreshToken);

    /**
     * Access Token 만료 여부 확인
     *
     * <p>서명은 유효하지만 만료된 경우 true 반환
     *
     * @param accessToken 확인할 Access Token
     * @return 만료 여부
     */
    boolean isAccessTokenExpired(String accessToken);

    /**
     * Access Token에서 회원 ID 추출
     *
     * @param accessToken Access Token
     * @return 회원 ID
     */
    String extractMemberId(String accessToken);

    /**
     * Refresh Token에서 회원 ID 추출
     *
     * @param refreshToken Refresh Token
     * @return 회원 ID
     */
    String extractMemberIdFromRefreshToken(String refreshToken);

    /**
     * Access Token 만료 시간 조회 (초)
     *
     * @return 만료 시간 (초)
     */
    long getAccessTokenExpirationSeconds();

    /**
     * Refresh Token 만료 시간 조회 (초)
     *
     * @return 만료 시간 (초)
     */
    long getRefreshTokenExpirationSeconds();
}
