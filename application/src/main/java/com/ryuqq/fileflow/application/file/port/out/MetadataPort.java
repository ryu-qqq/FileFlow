package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.metadata.FileMetadata;

/**
 * Metadata Extraction Port (출력 포트)
 *
 * <p>파일의 메타데이터를 추출하는 출력 포트입니다.</p>
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
 *   <li>adapter-out/metadata-extractor/</li>
 *   <li>MetadataAdapter implements MetadataPort</li>
 * </ul>
 *
 * <p><strong>메타데이터 추출 과정:</strong></p>
 * <ol>
 *   <li>파일을 S3에서 다운로드 (전체 또는 부분)</li>
 *   <li>파일 타입 확인 (Content-Type)</li>
 *   <li>타입별 메타데이터 추출:
 *     <ul>
 *       <li>이미지: EXIF (촬영 날짜, GPS, 카메라 정보)</li>
 *       <li>비디오: Duration, Resolution, Codec</li>
 *       <li>문서: 작성자, 생성일, 페이지 수</li>
 *     </ul>
 *   </li>
 *   <li>메타데이터 정규화 (공통 포맷)</li>
 *   <li>FileMetadata 생성 및 반환</li>
 * </ol>
 *
 * <p><strong>지원 파일 타입:</strong></p>
 * <ul>
 *   <li>이미지: JPEG, PNG, GIF, BMP, TIFF, WebP</li>
 *   <li>비디오: MP4, AVI, MOV, MKV, WebM</li>
 *   <li>문서: PDF, DOCX, XLSX, PPTX</li>
 *   <li>기타: TXT, CSV (기본 정보만)</li>
 * </ul>
 *
 * <p><strong>사용 위치:</strong></p>
 * <ul>
 *   <li>PipelineWorker - 비동기 Pipeline 처리</li>
 *   <li>FileQueryService - 메타데이터 조회 (미래 확장)</li>
 * </ul>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>지원하지 않는 파일 타입: 빈 메타데이터 반환 (예외 없음)</li>
 *   <li>S3 다운로드 실패: 구현체에서 처리</li>
 *   <li>메타데이터 추출 실패: 부분 메타데이터 반환 (예외 없음)</li>
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
public interface MetadataPort {

    /**
     * 파일 메타데이터 추출
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>파일 타입 확인 (Content-Type)</li>
     *   <li>S3에서 파일 다운로드 (Stream 또는 전체)</li>
     *   <li>타입별 메타데이터 추출기 호출</li>
     *   <li>메타데이터 정규화 (Map&lt;String, Object&gt;)</li>
     *   <li>FileMetadata 생성 및 반환</li>
     * </ol>
     *
     * <p><strong>추출 가능한 메타데이터 (이미지):</strong></p>
     * <pre>
     * {
     *   "width": 4000,
     *   "height": 3000,
     *   "takenAt": LocalDateTime.of(2025, 1, 1, 10, 0),
     *   "gpsLatitude": 37.5665,
     *   "gpsLongitude": 126.9780,
     *   "cameraModel": "iPhone 15 Pro",
     *   "iso": 100,
     *   "fNumber": 1.8,
     *   "exposureTime": "1/1000"
     * }
     * </pre>
     *
     * <p><strong>추출 가능한 메타데이터 (비디오):</strong></p>
     * <pre>
     * {
     *   "duration": 120,          // 초
     *   "width": 1920,
     *   "height": 1080,
     *   "codec": "H.264",
     *   "frameRate": 30.0,        // FPS
     *   "bitrate": 5000000        // bps
     * }
     * </pre>
     *
     * <p><strong>추출 가능한 메타데이터 (문서):</strong></p>
     * <pre>
     * {
     *   "author": "John Doe",
     *   "createdAt": LocalDateTime.of(2025, 1, 1, 10, 0),
     *   "pageCount": 10,
     *   "title": "Report 2025"
     * }
     * </pre>
     *
     * <p><strong>기술 스택 (구현체):</strong></p>
     * <ul>
     *   <li>Apache Tika: 범용 메타데이터 추출 (모든 파일 타입 지원)</li>
     *   <li>metadata-extractor: 이미지 EXIF 전문 (더 상세한 정보)</li>
     * </ul>
     *
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>S3 다운로드: 부분 다운로드 (메타데이터만, 파일 전체 불필요)</li>
     *   <li>Tika: 빠른 파싱 (대부분 파일의 헤더만 읽음)</li>
     *   <li>캐싱: 추출된 메타데이터는 DB에 저장 (재추출 불필요)</li>
     * </ul>
     *
     * @param fileAsset 원본 파일 (Domain Entity)
     * @return 추출된 메타데이터 (없으면 빈 Map)
     * @throws RuntimeException S3 다운로드 실패 (구현체에서 처리)
     */
    FileMetadata extractMetadata(FileAsset fileAsset);
}
