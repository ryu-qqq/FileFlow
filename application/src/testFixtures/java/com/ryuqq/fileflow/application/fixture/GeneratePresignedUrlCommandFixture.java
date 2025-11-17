package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;

import java.util.List;

/**
 * GeneratePresignedUrlCommand TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aCommand(), create()
 * </p>
 */
public class GeneratePresignedUrlCommandFixture {

    /**
     * 기본 GeneratePresignedUrlCommand 생성
     * <p>
     * 일반적인 이미지 파일 업로드 시나리오
     * </p>
     */
    public static GeneratePresignedUrlCommand aCommand() {
        return new GeneratePresignedUrlCommand(
                "profile.jpg",
                1024L,
                "image/jpeg",
                1L,
                "PROFILE",
                List.of("profile", "avatar")
        );
    }

    /**
     * 기본 GeneratePresignedUrlCommand 생성 (alias)
     */
    public static GeneratePresignedUrlCommand create() {
        return aCommand();
    }

    /**
     * 커스텀 파일명으로 Command 생성
     */
    public static GeneratePresignedUrlCommand withFileName(String fileName) {
        return new GeneratePresignedUrlCommand(
                fileName,
                1024L,
                "image/jpeg",
                1L,
                "PROFILE",
                List.of("profile", "avatar")
        );
    }

    /**
     * 커스텀 파일 크기로 Command 생성
     */
    public static GeneratePresignedUrlCommand withFileSize(Long fileSize) {
        return new GeneratePresignedUrlCommand(
                "profile.jpg",
                fileSize,
                "image/jpeg",
                1L,
                "PROFILE",
                List.of("profile", "avatar")
        );
    }

    /**
     * 커스텀 MIME 타입으로 Command 생성
     */
    public static GeneratePresignedUrlCommand withMimeType(String mimeType) {
        return new GeneratePresignedUrlCommand(
                "document.pdf",
                2048L,
                mimeType,
                1L,
                "DOCUMENT",
                List.of("doc", "pdf")
        );
    }

    /**
     * 커스텀 업로더 ID로 Command 생성
     */
    public static GeneratePresignedUrlCommand withUploaderId(Long uploaderId) {
        return new GeneratePresignedUrlCommand(
                "profile.jpg",
                1024L,
                "image/jpeg",
                uploaderId,
                "PROFILE",
                List.of("profile", "avatar")
        );
    }

    /**
     * 커스텀 카테고리로 Command 생성
     */
    public static GeneratePresignedUrlCommand withCategory(String category) {
        return new GeneratePresignedUrlCommand(
                "file.bin",
                512L,
                "application/octet-stream",
                1L,
                category,
                List.of("file")
        );
    }

    /**
     * 커스텀 태그로 Command 생성
     */
    public static GeneratePresignedUrlCommand withTags(List<String> tags) {
        return new GeneratePresignedUrlCommand(
                "profile.jpg",
                1024L,
                "image/jpeg",
                1L,
                "PROFILE",
                tags
        );
    }

    /**
     * 문서 파일 업로드 Command
     */
    public static GeneratePresignedUrlCommand documentCommand() {
        return new GeneratePresignedUrlCommand(
                "report.pdf",
                5120L,
                "application/pdf",
                2L,
                "DOCUMENT",
                List.of("report", "pdf", "official")
        );
    }

    /**
     * 비디오 파일 업로드 Command
     */
    public static GeneratePresignedUrlCommand videoCommand() {
        return new GeneratePresignedUrlCommand(
                "demo.mp4",
                10485760L, // 10MB
                "video/mp4",
                3L,
                "VIDEO",
                List.of("demo", "video", "tutorial")
        );
    }
}
