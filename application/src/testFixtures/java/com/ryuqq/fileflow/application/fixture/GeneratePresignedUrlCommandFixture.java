package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.domain.vo.FileCategory;
import com.ryuqq.fileflow.domain.vo.FileName;
import com.ryuqq.fileflow.domain.vo.FileSize;
import com.ryuqq.fileflow.domain.vo.MimeType;
import com.ryuqq.fileflow.domain.vo.Tags;
import com.ryuqq.fileflow.domain.vo.UploaderId;

import java.util.List;

/**
 * GeneratePresignedUrlCommand TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aCommand(), create()
 * - Domain VO 사용 (primitive type 금지)
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
                FileName.of("profile.jpg"),
                FileSize.of(1024L),
                MimeType.of("image/jpeg"),
                UploaderId.of(1L),
                FileCategory.of("기타"),
                Tags.of(List.of("profile", "avatar"))
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
                FileName.of(fileName),
                FileSize.of(1024L),
                MimeType.of("image/jpeg"),
                UploaderId.of(1L),
                FileCategory.of("기타"),
                Tags.of(List.of("profile", "avatar"))
        );
    }

    /**
     * 커스텀 파일 크기로 Command 생성
     */
    public static GeneratePresignedUrlCommand withFileSize(Long fileSize) {
        return new GeneratePresignedUrlCommand(
                FileName.of("profile.jpg"),
                FileSize.of(fileSize),
                MimeType.of("image/jpeg"),
                UploaderId.of(1L),
                FileCategory.of("기타"),
                Tags.of(List.of("profile", "avatar"))
        );
    }

    /**
     * 커스텀 MIME 타입으로 Command 생성
     */
    public static GeneratePresignedUrlCommand withMimeType(String mimeType) {
        return new GeneratePresignedUrlCommand(
                FileName.of("document.pdf"),
                FileSize.of(2048L),
                MimeType.of(mimeType),
                UploaderId.of(1L),
                FileCategory.of("문서"),
                Tags.of(List.of("doc", "pdf"))
        );
    }

    /**
     * 커스텀 업로더 ID로 Command 생성
     */
    public static GeneratePresignedUrlCommand withUploaderId(Long uploaderId) {
        return new GeneratePresignedUrlCommand(
                FileName.of("profile.jpg"),
                FileSize.of(1024L),
                MimeType.of("image/jpeg"),
                UploaderId.of(uploaderId),
                FileCategory.of("기타"),
                Tags.of(List.of("profile", "avatar"))
        );
    }

    /**
     * 커스텀 카테고리로 Command 생성
     */
    public static GeneratePresignedUrlCommand withCategory(String category) {
        return new GeneratePresignedUrlCommand(
                FileName.of("file.bin"),
                FileSize.of(512L),
                MimeType.of("application/octet-stream"),
                UploaderId.of(1L),
                FileCategory.of(category),
                Tags.of(List.of("file"))
        );
    }

    /**
     * 커스텀 태그로 Command 생성
     */
    public static GeneratePresignedUrlCommand withTags(List<String> tags) {
        return new GeneratePresignedUrlCommand(
                FileName.of("profile.jpg"),
                FileSize.of(1024L),
                MimeType.of("image/jpeg"),
                UploaderId.of(1L),
                FileCategory.of("기타"),
                Tags.of(tags)
        );
    }

    /**
     * 문서 파일 업로드 Command
     */
    public static GeneratePresignedUrlCommand documentCommand() {
        return new GeneratePresignedUrlCommand(
                FileName.of("report.pdf"),
                FileSize.of(5120L),
                MimeType.of("application/pdf"),
                UploaderId.of(2L),
                FileCategory.of("문서"),
                Tags.of(List.of("report", "pdf", "official"))
        );
    }

    /**
     * 비디오 파일 업로드 Command
     */
    public static GeneratePresignedUrlCommand videoCommand() {
        return new GeneratePresignedUrlCommand(
                FileName.of("demo.mp4"),
                FileSize.of(10485760L), // 10MB
                MimeType.of("video/mp4"),
                UploaderId.of(3L),
                FileCategory.of("전시영역"),
                Tags.of(List.of("demo", "video", "tutorial"))
        );
    }
}
