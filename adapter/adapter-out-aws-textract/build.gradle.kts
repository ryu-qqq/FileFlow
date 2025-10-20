// ========================================
// Adapter-Out AWS Textract (Outbound Adapter)
// ========================================
// Purpose: Textract OCR 어댑터
// - PDF Text Extraction
// - HTML OCR (image → text)
// - Table Detection
//
// Dependencies:
// - application (OCR Port 구현)
// - domain
// - AWS SDK Textract
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
    // AWS SDK Textract
    // ========================================
    implementation(platform(rootProject.libs.aws.sdk.bom))
    implementation("software.amazon.awssdk:textract")
    implementation("software.amazon.awssdk:s3") // Textract requires S3 integration

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
