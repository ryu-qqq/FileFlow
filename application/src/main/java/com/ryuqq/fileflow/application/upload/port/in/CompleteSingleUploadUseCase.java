package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteSingleUploadResponse;

/**
 * Single Upload 완료 UseCase (Port In)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3에 업로드된 파일의 존재 및 메타데이터 검증</li>
 *   <li>FileAsset Aggregate 생성</li>
 *   <li>UploadSession 완료 처리</li>
 * </ul>
 *
 * <p><strong>플로우:</strong></p>
 * <ol>
 *   <li>UploadSession 조회 및 검증 (PENDING 상태 확인)</li>
 *   <li>S3 HeadObject API로 파일 존재 확인</li>
 *   <li>FileAsset Aggregate 생성 및 저장</li>
 *   <li>UploadSession 완료 상태로 업데이트</li>
 * </ol>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴의 Port In 역할</li>
 *   <li>✅ Single Responsibility (업로드 완료 처리만)</li>
 *   <li>✅ Command/Query 분리 (Command)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface CompleteSingleUploadUseCase {

    /**
     * Single Upload 완료 처리
     *
     * <p>Client가 S3 Presigned URL로 업로드를 완료한 후 호출합니다.</p>
     *
     * @param command Complete Command (sessionKey)
     * @return 생성된 FileAsset 정보
     * @throws IllegalStateException UploadSession이 완료 가능한 상태가 아닌 경우
     * @throws com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException S3에 파일이 존재하지 않는 경우
     */
    CompleteSingleUploadResponse execute(CompleteSingleUploadCommand command);
}
