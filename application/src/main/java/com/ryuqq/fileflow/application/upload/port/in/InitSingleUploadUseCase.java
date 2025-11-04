package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;

/**
 * Init Single Upload Use Case
 * 단일 업로드 초기화
 *
 * <p>책임:</p>
 * <ul>
 *   <li>100MB 미만 파일의 단일 업로드 세션 생성</li>
 *   <li>S3 Presigned URL 발급 (단일 PUT 업로드용)</li>
 *   <li>UploadSession 저장 (SINGLE_UPLOAD 타입)</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>파일 크기가 100MB 미만인 경우에만 사용</li>
 *   <li>단일 HTTP PUT 요청으로 업로드 가능</li>
 *   <li>Multipart Upload보다 효율적 (API 호출 1회)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface InitSingleUploadUseCase {

    /**
     * 단일 업로드 초기화
     *
     * @param command 단일 업로드 초기화 명령
     * @return 세션 키, Presigned URL, 저장 경로
     * @throws IllegalArgumentException 파일 크기가 100MB 이상인 경우
     */
    SingleUploadResponse execute(InitSingleUploadCommand command);
}
