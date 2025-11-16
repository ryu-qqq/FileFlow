package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.command.ProcessFileCommand;

import java.util.List;

/**
 * ProcessFileCommand TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aCommand(), create()
 * </p>
 */
public class ProcessFileCommandFixture {

    /**
     * 기본 ProcessFileCommand 생성
     */
    public static ProcessFileCommand aCommand() {
        return new ProcessFileCommand(
                1L,
                List.of("THUMBNAIL", "METADATA")
        );
    }

    /**
     * 기본 ProcessFileCommand 생성 (alias)
     */
    public static ProcessFileCommand create() {
        return aCommand();
    }

    /**
     * 커스텀 파일 ID로 Command 생성
     */
    public static ProcessFileCommand withFileId(Long fileId) {
        return new ProcessFileCommand(
                fileId,
                List.of("THUMBNAIL", "METADATA")
        );
    }

    /**
     * 커스텀 작업 유형으로 Command 생성
     */
    public static ProcessFileCommand withJobTypes(List<String> jobTypes) {
        return new ProcessFileCommand(
                1L,
                jobTypes
        );
    }

    /**
     * 썸네일 생성 작업만 포함 (시나리오)
     */
    public static ProcessFileCommand thumbnailOnly() {
        return new ProcessFileCommand(
                2L,
                List.of("THUMBNAIL")
        );
    }

    /**
     * 메타데이터 추출 작업만 포함 (시나리오)
     */
    public static ProcessFileCommand metadataOnly() {
        return new ProcessFileCommand(
                3L,
                List.of("METADATA")
        );
    }

    /**
     * 압축 작업만 포함 (시나리오)
     */
    public static ProcessFileCommand compressOnly() {
        return new ProcessFileCommand(
                4L,
                List.of("COMPRESS")
        );
    }

    /**
     * 모든 처리 작업 포함 (시나리오)
     */
    public static ProcessFileCommand allJobs() {
        return new ProcessFileCommand(
                5L,
                List.of("THUMBNAIL", "METADATA", "COMPRESS", "WATERMARK")
        );
    }
}
