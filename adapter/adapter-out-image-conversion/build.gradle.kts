// ========================================
// Adapter-Out Image Conversion (Outbound Adapter)
// ========================================
// Purpose: 이미지 변환 및 최적화 어댑터
// - WebP 변환
// - Thumbnail 생성
// - Image Resizing
// - Format Conversion
//
// Dependencies:
// - application (Image Processing Port 구현)
// - domain
// - Thumbnailator
// - imageio-webp (WebP support)
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

    // ========================================
    // Image Processing
    // ========================================
    implementation(rootProject.libs.thumbnailator)

    // ========================================
    // WebP Support
    // ========================================
    implementation(rootProject.libs.imageio.webp)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
