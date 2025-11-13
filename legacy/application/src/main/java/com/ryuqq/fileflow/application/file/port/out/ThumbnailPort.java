package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.thumbnail.ThumbnailInfo;

/**
 * Thumbnail Generation Port (출력 포트)
 *
 * <p>파일의 썸네일을 생성하는 출력 포트입니다.</p>
 *
 * <p><strong>Port의 역할:</strong></p>
 * <ul>
 *   <li>헥사고날 아키텍처의 출력 포트 (Application → Infrastructure)</li>
 *   <li>비즈니스 로직이 원하는 "무엇을" 정의 (인터페이스)</li>
 *   <li>Adapter가 기술적으로 "어떻게" 구현 (구현체)</li>
 * </ul>
 *
 * <p><strong>구현체 위치:</strong></p>
 * <ul>
 *   <li>adapter-out/image-processor/</li>
 *   <li>ThumbnailAdapter implements ThumbnailPort</li>
 * </ul>
 *
 * <p><strong>썸네일 생성 과정:</strong></p>
 * <ol>
 *   <li>원본 파일을 S3에서 다운로드 (Stream)</li>
 *   <li>이미지 리사이징 (예: 4000x3000 → 300x300)</li>
 *   <li>압축 (예: 5MB PNG → 50KB JPEG, 품질 85%)</li>
 *   <li>S3에 썸네일 업로드 (thumbnails/ 경로)</li>
 *   <li>ThumbnailInfo 생성 및 반환</li>
 * </ol>
 *
 * <p><strong>사용 위치:</strong></p>
 * <ul>
 *   <li>PipelineWorker - 비동기 Pipeline 처리</li>
 *   <li>FileQueryService - 썸네일 조회 (미래 확장)</li>
 * </ul>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>파일이 이미지가 아닌 경우: IllegalArgumentException</li>
 *   <li>S3 다운로드 실패: 구현체에서 처리</li>
 *   <li>리사이징 실패: 구현체에서 처리</li>
 *   <li>S3 업로드 실패: 구현체에서 처리</li>
 * </ul>
 *
 * <p><strong>트랜잭션 고려사항:</strong></p>
 * <ul>
 *   <li>⚠️ 외부 API 호출 (S3)이므로 트랜잭션 밖에서 호출 필수</li>
 *   <li>✅ PipelineWorker에서 @Async로 호출 (트랜잭션 분리)</li>
 *   <li>✅ 실패 시 PipelineOutbox에서 재시도</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ThumbnailPort {

    /**
     * 썸네일 생성 및 S3 업로드
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>파일 타입 검증 (이미지만 처리)</li>
     *   <li>S3에서 원본 파일 다운로드 (Stream)</li>
     *   <li>이미지 리사이징 (비율 유지 또는 정사각형)</li>
     *   <li>압축 (JPEG 품질 조정)</li>
     *   <li>S3에 썸네일 업로드 (thumbnails/ 경로)</li>
     *   <li>ThumbnailInfo 생성 및 반환</li>
     * </ol>
     *
     * <p><strong>썸네일 저장 경로:</strong></p>
     * <pre>
     * 원본: uploads/2025/01/uuid.jpg
     * 썸네일: thumbnails/2025/01/uuid_300x300.jpg
     * </pre>
     *
     * <p><strong>리사이징 옵션:</strong></p>
     * <ul>
     *   <li>기본: 300x300 (정사각형, 비율 유지하며 크롭)</li>
     *   <li>확장: 여러 크기 지원 (small: 150x150, large: 800x600)</li>
     * </ul>
     *
     * <p><strong>압축 옵션:</strong></p>
     * <ul>
     *   <li>포맷: JPEG (PNG보다 작음)</li>
     *   <li>품질: 85% (시각적 품질과 파일 크기 균형)</li>
     * </ul>
     *
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>S3 다운로드: Stream 사용 (메모리 효율)</li>
     *   <li>리사이징: Thumbnailator (빠르고 간단)</li>
     *   <li>S3 업로드: Multipart Upload (대용량 파일 대비)</li>
     * </ul>
     *
     * @param fileAsset 원본 파일 (Domain Entity)
     * @return 생성된 썸네일 정보
     * @throws IllegalArgumentException 파일이 이미지가 아닌 경우
     * @throws RuntimeException         S3 작업 실패 (구현체에서 처리)
     */
    ThumbnailInfo generateThumbnail(FileAsset fileAsset);
}
