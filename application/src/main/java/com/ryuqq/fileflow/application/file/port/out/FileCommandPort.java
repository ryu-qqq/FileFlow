package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.asset.FileAsset;

/**
 * File Command Port (CQRS Command Side)
 *
 * <p>파일 상태 변경 전용 Port - Persistence Layer 구현</p>
 *
 * <p><strong>CQRS 설계 원칙</strong>:</p>
 * <ul>
 *   <li>쓰기 전용 - Domain 상태 변경</li>
 *   <li>트랜잭션 보장 - @Transactional 필수</li>
 *   <li>Domain 메서드 호출 - Tell, Don't Ask</li>
 *   <li>변경 추적 - Dirty Checking 활용</li>
 * </ul>
 *
 * <p><strong>구현 가이드</strong>:</p>
 * <ul>
 *   <li>JPA Repository.save() 호출</li>
 *   <li>Mapper로 Domain → Entity 변환</li>
 *   <li>트랜잭션 커밋 시 Dirty Checking으로 자동 UPDATE</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface FileCommandPort {

    /**
     * 파일 저장 (신규 또는 업데이트)
     *
     * <p><strong>신규 저장</strong>:</p>
     * <ul>
     *   <li>FileAsset.getId() == null</li>
     *   <li>JPA AUTO_INCREMENT로 ID 생성</li>
     * </ul>
     *
     * <p><strong>업데이트</strong>:</p>
     * <ul>
     *   <li>FileAsset.getId() != null</li>
     *   <li>Dirty Checking으로 변경된 필드만 UPDATE</li>
     * </ul>
     *
     * @param fileAsset Domain FileAsset
     * @return 저장된 FileAsset (ID 포함)
     */
    FileAsset save(FileAsset fileAsset);

    /**
     * 파일 삭제 (물리 삭제)
     *
     * <p><strong>주의</strong>: 일반적으로 Soft Delete 사용 권장</p>
     * <p>물리 삭제는 Batch Job에서만 사용</p>
     *
     * @param id FileAsset ID
     */
    void delete(Long id);
}
