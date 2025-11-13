// ========================================
// Adapter-Out Image Processor (Outbound Adapter)
// ========================================
// Purpose: 이미지 처리 어댑터 (Driven Adapter)
// - 썸네일 생성 (리사이징 + 압축)
// - S3 업로드/다운로드
//
// Dependencies:
// - application (ThumbnailPort 구현)
// - domain (Domain Value Objects)
// - adapter-out:aws-s3 (S3 Client)
// - Thumbnailator (이미지 리사이징)
//
// Policy:
// - Thumbnailator 사용 (간단한 API, 빠른 성능)
// - 썸네일 크기: 300x300 (정사각형)
// - JPEG 포맷, 85% 품질
// - Infrastructure 레이어 (Domain과 분리)
// ========================================

plugins {
    java
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapter-out:aws-s3"))

    // ========================================
    // Spring Boot
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")

    // ========================================
    // AWS SDK for Java v2
    // ========================================
    implementation(platform("software.amazon.awssdk:bom:2.20.26"))
    implementation("software.amazon.awssdk:s3")

    // ========================================
    // Thumbnailator - 이미지 리사이징
    // ========================================
    // https://github.com/coobird/thumbnailator
    // 장점: 간단한 API, 빠른 성능, 다양한 포맷 지원
    implementation("net.coobird:thumbnailator:0.4.20")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
