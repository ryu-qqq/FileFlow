package com.ryuqq.fileflow.application.session.fixture;

import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.domain.file.vo.FileCategory;
import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.iam.vo.UploaderType;
import com.ryuqq.fileflow.domain.session.vo.SessionId;

/**
 * GeneratePresignedUrlCommand TestFixture (Object Mother 패턴)
 * <p>
 * MVP Scope: Single Presigned URL Upload
 * </p>
 */
public class GeneratePresignedUrlCommandFixture {

    public static GeneratePresignedUrlCommand aCommand() {
        return new GeneratePresignedUrlCommand(
                SessionId.generate(),
                FileName.of("example.jpg"),
                FileSize.of(1048576L), // 1MB
                MimeType.of("image/jpeg"),
                FileCategory.of("banner", UploaderType.ADMIN)
        );
    }

    public static GeneratePresignedUrlCommand create() {
        return aCommand();
    }

    public static GeneratePresignedUrlCommand withFileName(String fileName) {
        return new GeneratePresignedUrlCommand(
                SessionId.generate(),
                FileName.of(fileName),
                FileSize.of(1048576L),
                MimeType.of("image/jpeg"),
                FileCategory.of("banner", UploaderType.ADMIN)
        );
    }

    public static GeneratePresignedUrlCommand withFileSize(Long fileSize) {
        return new GeneratePresignedUrlCommand(
                SessionId.generate(),
                FileName.of("example.jpg"),
                FileSize.of(fileSize),
                MimeType.of("image/jpeg"),
                FileCategory.of("banner", UploaderType.ADMIN)
        );
    }

    public static GeneratePresignedUrlCommand withCategory(FileCategory category) {
        return new GeneratePresignedUrlCommand(
                SessionId.generate(),
                FileName.of("example.jpg"),
                FileSize.of(1048576L),
                MimeType.of("image/jpeg"),
                category
        );
    }

    public static GeneratePresignedUrlCommand withNullCategory() {
        return new GeneratePresignedUrlCommand(
                SessionId.generate(),
                FileName.of("example.jpg"),
                FileSize.of(1048576L),
                MimeType.of("image/jpeg"),
                null
        );
    }
}
