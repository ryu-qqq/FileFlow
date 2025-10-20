// ========================================
// Adapter-Out AWS S3 (Outbound Adapter)
// ========================================
// Purpose: S3 파일 저장소 어댑터
// - Presigned URL 생성
// - Multipart Upload
// - File Verification (checksum)
// - Lifecycle Management
//
// Dependencies:
// - application (Storage Port 구현)
// - domain
// - AWS SDK S3
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
    // AWS SDK S3
    // ========================================
    implementation(platform(rootProject.libs.aws.sdk.bom))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sts")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // LocalStack or S3Mock for testing
}
