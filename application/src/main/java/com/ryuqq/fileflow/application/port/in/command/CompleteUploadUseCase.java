package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;

/**
 * 업로드 완료 UseCase (Inbound Port - Command)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *UseCase (Port In Command)
 * - 패키지: ..application..port.in.command..
 * - 메서드: execute() 하나만
 * - Command 입력, Response 반환 (void 가능)
 * </p>
 * <p>
 * 파일 업로드 완료 시 호출됩니다.
 * 클라이언트가 Presigned URL로 업로드 완료 후, 백엔드에 완료를 통지할 때 사용합니다.
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CompleteUploadUseCase {

    /**
     * 업로드 완료 UseCase 실행
     * <p>
     * 워크플로우:
     * 1. File 조회 (@Transactional)
     * 2. 상태 검증 (PENDING/UPLOADING만 허용)
     * 3. S3 Object 존재 확인 (트랜잭션 밖)
     * 4. File 상태 업데이트 (COMPLETED) (@Transactional)
     * 5. MessageOutbox 생성 (FILE_UPLOADED 이벤트)
     * </p>
     *
     * @param command 업로드 완료 Command
     */
    void execute(CompleteUploadCommand command);
}
