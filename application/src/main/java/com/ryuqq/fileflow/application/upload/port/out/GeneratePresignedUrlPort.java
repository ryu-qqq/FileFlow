package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PresignedUrlInfo;

/**
 * Presigned URL 생성을 위한 Output Port
 * S3 등의 스토리지 서비스에서 임시 업로드 URL을 생성하는 기능을 정의합니다.
 *
 * Hexagonal Architecture:
 * - Application Layer의 Port (인터페이스)
 * - Adapter Layer에서 구현 (S3PresignedUrlAdapter)
 * - Domain 모델과 Application 레이어를 외부 기술로부터 격리
 */
public interface GeneratePresignedUrlPort {

    /**
     * 단일 파일 업로드를 위한 Presigned URL을 생성합니다.
     * 파일 크기가 100MB 미만인 경우 사용합니다.
     *
     * @param command 파일 업로드 명령
     * @return Presigned URL 정보
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws RuntimeException URL 생성 실패 시
     */
    PresignedUrlInfo generate(FileUploadCommand command);

    /**
     * 멀티파트 업로드를 시작하고 파트별 Presigned URL을 생성합니다.
     * 파일 크기가 100MB 이상인 경우 사용합니다.
     *
     * @param command 파일 업로드 명령
     * @return 멀티파트 업로드 정보 (uploadId와 파트별 Presigned URL 포함)
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws RuntimeException 멀티파트 업로드 시작 실패 시
     */
    MultipartUploadInfo initiateMultipartUpload(FileUploadCommand command);
}
