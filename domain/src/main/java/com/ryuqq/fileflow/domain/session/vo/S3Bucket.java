package com.ryuqq.fileflow.domain.session.vo;

import java.util.regex.Pattern;

/**
 * S3 버킷명 Value Object.
 *
 * <p><strong>AWS S3 버킷 네이밍 규칙</strong>:
 *
 * <ul>
 *   <li>3~63자 길이
 *   <li>소문자, 숫자, 하이픈(-), 마침표(.)만 사용
 *   <li>소문자 또는 숫자로 시작 및 종료
 *   <li>IP 주소 형식 불가 (예: 192.168.1.1)
 *   <li>xn--, sthree-, sthree-configurator로 시작 불가
 *   <li>-s3alias, --ol-s3로 끝나지 않음
 * </ul>
 *
 * @param bucketName S3 버킷명
 */
public record S3Bucket(String bucketName) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 63;

    // AWS S3 버킷명 정규식 (소문자, 숫자, 하이픈, 마침표만)
    private static final Pattern BUCKET_NAME_PATTERN =
            Pattern.compile("^[a-z0-9][a-z0-9.-]*[a-z0-9]$");

    // IP 주소 형식 체크
    private static final Pattern IP_ADDRESS_PATTERN =
            Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    /** Compact Constructor (검증 로직). */
    public S3Bucket {
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalArgumentException("S3 버킷명은 null이거나 빈 문자열일 수 없습니다.");
        }

        if (bucketName.length() < MIN_LENGTH || bucketName.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(
                            "S3 버킷명은 %d~%d자 사이여야 합니다: %s (%d자)",
                            MIN_LENGTH, MAX_LENGTH, bucketName, bucketName.length()));
        }

        if (!BUCKET_NAME_PATTERN.matcher(bucketName).matches()) {
            throw new IllegalArgumentException(
                    "S3 버킷명은 소문자, 숫자, 하이픈(-), 마침표(.)만 사용할 수 있으며, "
                            + "소문자 또는 숫자로 시작하고 끝나야 합니다: "
                            + bucketName);
        }

        if (IP_ADDRESS_PATTERN.matcher(bucketName).matches()) {
            throw new IllegalArgumentException("S3 버킷명은 IP 주소 형식을 사용할 수 없습니다: " + bucketName);
        }

        if (bucketName.startsWith("xn--")
                || bucketName.startsWith("sthree-")
                || bucketName.startsWith("sthree-configurator")) {
            throw new IllegalArgumentException(
                    "S3 버킷명은 'xn--', 'sthree-', 'sthree-configurator'로 시작할 수 없습니다: " + bucketName);
        }

        if (bucketName.endsWith("-s3alias") || bucketName.endsWith("--ol-s3")) {
            throw new IllegalArgumentException(
                    "S3 버킷명은 '-s3alias', '--ol-s3'로 끝날 수 없습니다: " + bucketName);
        }

        if (bucketName.contains("..")) {
            throw new IllegalArgumentException("S3 버킷명에 연속된 마침표(..)가 포함될 수 없습니다: " + bucketName);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param bucketName S3 버킷명 (AWS 네이밍 규칙 준수 필요)
     * @return S3Bucket
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static S3Bucket of(String bucketName) {
        return new S3Bucket(bucketName);
    }

    /**
     * 버킷명이 AWS 네이밍 규칙을 준수하는지 확인한다.
     *
     * @return 규칙 준수 시 true (생성 시 이미 검증되므로 항상 true)
     */
    public boolean isValid() {
        return true; // Compact Constructor에서 이미 검증됨
    }
}
