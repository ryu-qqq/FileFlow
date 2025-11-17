package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.UploadFromExternalUrlCommand;

/**
 * 외부 URL 업로드 UseCase (Inbound Port - Command)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *UseCase (Port In Command)
 * - 패키지: ..application..port.in.command..
 * - 메서드: execute() 하나만
 * - Command 입력, Response 반환 (void 가능)
 * </p>
 * <p>
 * 외부 URL에서 파일을 다운로드하여 S3에 업로드하는 UseCase입니다.
 * 비동기 처리를 위해 MessageOutbox를 사용합니다.
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UploadFromExternalUrlUseCase {

    /**
     * 외부 URL 업로드 UseCase 실행
     * <p>
     * 워크플로우:
     * 1. URL 검증 (HTTPS만 허용)
     * 2. File 메타데이터 생성 (PENDING 상태)
     * 3. MessageOutbox 생성 (FILE_DOWNLOAD_REQUESTED 이벤트)
     * </p>
     *
     * @param command 외부 URL 업로드 Command
     */
    void execute(UploadFromExternalUrlCommand command);
}
