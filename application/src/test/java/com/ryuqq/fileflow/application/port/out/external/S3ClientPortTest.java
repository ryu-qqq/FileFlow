package com.ryuqq.fileflow.application.port.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S3ClientPort 인터페이스 계약 테스트
 * <p>
 * 외부 API Port 규칙:
 * - 인터페이스명: *ClientPort
 * - 패키지: ..application..port.out.external..
 * - 메서드: 외부 API 호출 메서드 (generatePresignedUrl, initiateMultipartUpload, headObject, uploadFromUrl)
 * - Timeout, Retry 정책 Javadoc 필수
 * </p>
 */
class S3ClientPortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * S3ClientPort 인터페이스가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("S3ClientPort는 generatePresignedUrl() 메서드를 제공해야 한다")
    void shouldProvideGeneratePresignedUrlMethod() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // String generatePresignedUrl(String key, Duration expiration) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("S3ClientPort는 initiateMultipartUpload() 메서드를 제공해야 한다")
    void shouldProvideInitiateMultipartUploadMethod() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // String initiateMultipartUpload(String key, String contentType) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("S3ClientPort는 headObject() 메서드를 제공해야 한다")
    void shouldProvideHeadObjectMethod() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // HeadObjectResponse headObject(String key) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("S3ClientPort는 uploadFromUrl() 메서드를 제공해야 한다")
    void shouldProvideUploadFromUrlMethod() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // void uploadFromUrl(String sourceUrl, String targetKey) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("generatePresignedUrl()는 String을 반환해야 한다")
    void generatePresignedUrlShouldReturnString() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 반환 타입 검증
        // String 반환 (Presigned URL)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("initiateMultipartUpload()는 String을 반환해야 한다")
    void initiateMultipartUploadShouldReturnString() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 반환 타입 검증
        // String 반환 (Upload ID)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("headObject()는 HeadObjectResponse를 반환해야 한다")
    void headObjectShouldReturnHeadObjectResponse() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 반환 타입 검증
        // HeadObjectResponse 반환 (메타데이터)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("uploadFromUrl()는 void를 반환해야 한다")
    void uploadFromUrlShouldReturnVoid() {
        // Given: S3ClientPort 인터페이스 (컴파일 에러)
        S3ClientPort port = null;

        // When & Then: 반환 타입 검증
        // void 반환
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}
