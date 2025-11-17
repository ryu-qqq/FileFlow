package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;

/**
 * CompleteUploadCommand TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aCommand(), create()
 * </p>
 */
public class CompleteUploadCommandFixture {

    /**
     * 기본 CompleteUploadCommand 생성
     */
    public static CompleteUploadCommand aCommand() {
        return new CompleteUploadCommand(1L);
    }

    /**
     * 기본 CompleteUploadCommand 생성 (alias)
     */
    public static CompleteUploadCommand create() {
        return aCommand();
    }

    /**
     * 커스텀 파일 ID로 Command 생성
     */
    public static CompleteUploadCommand withFileId(Long fileId) {
        return new CompleteUploadCommand(fileId);
    }

    /**
     * 특정 사용자의 파일 업로드 완료 Command (예시)
     */
    public static CompleteUploadCommand forUser(Long userId) {
        // userId를 기반으로 fileId 생성 (예시: userId * 100)
        return new CompleteUploadCommand(userId * 100);
    }
}
