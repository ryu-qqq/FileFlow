// ========================================
// Adapter-Out Metadata Extractor (Outbound Adapter)
// ========================================
// Purpose: 메타데이터 추출 어댑터 (Driven Adapter)
// - 파일 메타데이터 추출 (이미지, 비디오, 문서)
// - S3 다운로드
//
// Dependencies:
// - application (MetadataPort 구현)
// - domain (Domain Value Objects)
// - adapter-out:aws-s3 (S3 Client)
// - Apache Tika (범용 메타데이터 추출)
//
// Policy:
// - Apache Tika 사용 (1000+ 파일 타입 지원)
// - 이미지: EXIF (촬영 날짜, GPS, 카메라)
// - 비디오: Duration, Resolution, Codec
// - 문서: 작성자, 생성일, 페이지 수
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
    // Apache Tika - 메타데이터 추출
    // ========================================
    // https://tika.apache.org/
    // 장점: 범용 메타데이터 추출, 모든 파일 타입 지원, 자동 포맷 감지
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
