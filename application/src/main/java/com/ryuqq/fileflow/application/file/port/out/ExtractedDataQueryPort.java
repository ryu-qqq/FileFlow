package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;

import java.util.List;

/**
 * ExtractedData Query Port (CQRS Query)
 *
 * <p><strong>역할</strong>: ExtractedData 조회 Port 인터페이스</p>
 * <p><strong>위치</strong>: application/file/port/out/</p>
 * <p><strong>구현</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 *
 * <h3>헥사고날 아키텍처 패턴</h3>
 * <ul>
 *   <li>✅ Outbound Port - Application → Adapter 의존성</li>
 *   <li>✅ CQRS Query Side - 읽기 전용 조회</li>
 *   <li>❌ Command Side 없음 (SaveExtractedDataPort 사용)</li>
 * </ul>
 *
 * <h3>사용 시나리오</h3>
 * <ul>
 *   <li>파일 메타데이터 조회 시 ExtractedData 포함</li>
 *   <li>EXIF, OCR, Face Detection 데이터 조회</li>
 *   <li>메타데이터 Map 형태로 API 응답 변환</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ExtractedDataQueryPort {

    /**
     * fileId로 모든 ExtractedData 조회
     *
     * <p>특정 FileAsset에서 추출된 모든 메타데이터를 조회합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return ExtractedData 목록 (빈 리스트 가능)
     */
    List<ExtractedData> findAllByFileId(Long fileId);
}
