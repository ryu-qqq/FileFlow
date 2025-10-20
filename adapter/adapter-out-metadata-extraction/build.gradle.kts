// ========================================
// Adapter-Out Metadata Extraction (Outbound Adapter)
// ========================================
// Purpose: 파일 메타데이터 추출 어댑터
// - Image EXIF (GPS, Camera, Date)
// - PDF Metadata
// - Video Metadata (duration, codec, etc.)
//
// Dependencies:
// - application (Metadata Port 구현)
// - domain
// - metadata-extractor library
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
    // Metadata Extraction
    // ========================================
    implementation(rootProject.libs.metadata.extractor)

    // ========================================
    // Apache Tika (Optional - for more file types)
    // ========================================
    // implementation("org.apache.tika:tika-core:2.9.1")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
