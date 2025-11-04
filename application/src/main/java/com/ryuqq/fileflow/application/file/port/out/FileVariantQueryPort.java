package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.variant.FileVariant;

import java.util.List;

/**
 * FileVariant Query Port (CQRS Query)
 *
 * <p><strong>역할</strong>: FileVariant 조회 Port 인터페이스</p>
 * <p><strong>위치</strong>: application/file/port/out/</p>
 * <p><strong>구현</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 *
 * <h3>헥사고날 아키텍처 패턴</h3>
 * <ul>
 *   <li>✅ Outbound Port - Application → Adapter 의존성</li>
 *   <li>✅ CQRS Query Side - 읽기 전용 조회</li>
 *   <li>❌ Command Side 없음 (SaveFileVariantPort 사용)</li>
 * </ul>
 *
 * <h3>사용 시나리오</h3>
 * <ul>
 *   <li>파일 상세 조회 시 Variants 목록 조회</li>
 *   <li>특정 VariantType 조회 (THUMBNAIL, OPTIMIZED 등)</li>
 *   <li>썸네일 URL 응답 생성</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface FileVariantQueryPort {

    /**
     * fileId로 모든 FileVariant 조회
     *
     * <p>특정 FileAsset에 속한 모든 Variant를 조회합니다.</p>
     *
     * @param fileId FileAsset의 ID (Long FK)
     * @return FileVariant 목록 (빈 리스트 가능)
     */
    List<FileVariant> findAllByFileId(Long fileId);
}
