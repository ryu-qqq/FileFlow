package com.ryuqq.fileflow.application.image.port.in;

import com.ryuqq.fileflow.application.image.dto.BatchThumbnailCommand;
import com.ryuqq.fileflow.application.image.dto.BatchThumbnailResult;

/**
 * Batch Thumbnail 생성 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 원본 이미지에 대해 여러 크기의 썸네일을 일괄 생성하는 비즈니스 로직을 정의합니다.
 *
 * 주요 기능:
 * - 원본 이미지 1회 로드로 여러 썸네일 생성 (메모리 효율)
 * - 병렬 처리를 통한 썸네일 생성 및 S3 업로드 최적화
 * - FileAsset 및 FileRelationship 메타데이터 저장
 * - 트랜잭션 관리를 통한 데이터 일관성 보장
 *
 * 사용 시나리오:
 * - 업로드 완료 후 자동 썸네일 생성
 * - 기존 이미지에 대한 썸네일 재생성
 * - 새로운 썸네일 크기 정책 적용
 *
 * @author sangwon-ryu
 */
public interface GenerateBatchThumbnailsUseCase {

    /**
     * 원본 이미지에 대해 여러 크기의 썸네일을 일괄 생성합니다.
     *
     * 처리 흐름:
     * 1. 원본 이미지 로드 (1회)
     * 2. 각 크기별 썸네일 생성 및 S3 업로드 (병렬 처리)
     * 3. 각 썸네일의 FileAsset 저장
     * 4. 원본-썸네일 FileRelationship 저장
     * 5. 결과 반환
     *
     * @param command 배치 썸네일 생성 Command
     * @return 배치 썸네일 생성 결과
     * @throws IllegalArgumentException command가 유효하지 않은 경우
     */
    BatchThumbnailResult generateBatchThumbnails(BatchThumbnailCommand command);
}
