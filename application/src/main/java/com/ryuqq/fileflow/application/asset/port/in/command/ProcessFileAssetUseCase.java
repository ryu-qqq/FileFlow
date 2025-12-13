package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.ProcessFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.ProcessFileAssetResponse;

/**
 * FileAsset 처리 UseCase.
 *
 * <p>CQRS Command Side - 이미지 리사이징 및 포맷 변환 처리
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>이미지 리사이징 (ORIGINAL, THUMBNAIL, MEDIUM, LARGE)
 *   <li>포맷 변환 (WebP 등)
 *   <li>메타데이터 추출 (width, height)
 *   <li>S3 업로드 및 ProcessedFileAsset 저장
 * </ul>
 *
 * <p><strong>처리 흐름:</strong>
 *
 * <ol>
 *   <li>FileAsset 조회 및 상태 검증
 *   <li>상태 변경 (PENDING → PROCESSING)
 *   <li>원본 이미지 다운로드 (S3)
 *   <li>이미지 리사이징 및 포맷 변환
 *   <li>처리된 이미지 S3 업로드
 *   <li>ProcessedFileAsset 저장
 *   <li>상태 변경 (PROCESSING → RESIZED)
 *   <li>Outbox 이벤트 발행
 * </ol>
 *
 * <p><strong>트랜잭션 경계:</strong>
 *
 * <ul>
 *   <li>외부 I/O (S3, 이미지 처리)는 트랜잭션 밖에서 수행
 *   <li>DB 저장은 Facade를 통해 트랜잭션 내에서 수행
 * </ul>
 */
public interface ProcessFileAssetUseCase {

    /**
     * FileAsset 처리 실행.
     *
     * @param command 처리 요청 Command
     * @return 처리 결과 응답
     */
    ProcessFileAssetResponse execute(ProcessFileAssetCommand command);
}
