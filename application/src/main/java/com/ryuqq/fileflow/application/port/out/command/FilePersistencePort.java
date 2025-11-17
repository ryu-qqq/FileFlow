package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;

/**
 * File Persistence Port (Outbound Port)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *PersistencePort
 * - 메서드: persist() 하나만
 * - 반환 타입: FileId (Value Object)
 * - 파라미터: File (Domain Aggregate)
 * </p>
 * <p>
 * Application Layer에서 Persistence Layer로 File Aggregate 영속화 요청을 위한 Port입니다.
 * persist() 메서드는 신규 생성과 수정을 모두 처리합니다.
 * </p>
 */
public interface FilePersistencePort {

    /**
     * File Aggregate 영속화
     * <p>
     * 신규 생성과 수정을 통합 처리합니다.
     * - 신규: fileId가 null → 저장 후 생성된 FileId 반환
     * - 수정: fileId가 존재 → 수정 후 동일한 FileId 반환
     * </p>
     *
     * @param file 영속화할 File Aggregate
     * @return 영속화된 File의 FileId (Value Object)
     */
    FileId persist(File file);
}
