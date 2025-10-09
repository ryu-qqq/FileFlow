package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;

/**
 * 업로드 완료 확인 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 클라이언트가 S3에 파일 업로드를 완료한 후 서버에 알리는 비즈니스 로직을 정의합니다.
 *
 * 흐름:
 * 1. 클라이언트가 S3 Presigned URL로 직접 업로드
 * 2. 업로드 완료 후 이 UseCase를 호출하여 서버에 알림
 * 3. S3 Event는 비동기적으로 나중에 도착 가능
 *
 * 주요 책임:
 * - S3에 파일이 실제로 존재하는지 확인
 * - ETag 검증 (제공된 경우)
 * - 세션 상태를 COMPLETED로 업데이트
 * - 메타데이터 추출 프로세스 시작
 *
 * @author sangwon-ryu
 */
public interface ConfirmUploadUseCase {

    /**
     * 클라이언트의 업로드 완료를 확인하고 세션을 완료 처리합니다.
     *
     * @param command 업로드 완료 확인 Command
     * @return 확인 결과 응답
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 이미 처리된 세션이거나 유효하지 않은 상태인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception S3에 파일이 없는 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException ETag 불일치 시
     */
    ConfirmUploadResponse confirm(ConfirmUploadCommand command);
}
