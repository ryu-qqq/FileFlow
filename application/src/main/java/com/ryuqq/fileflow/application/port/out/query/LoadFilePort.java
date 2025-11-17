package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;

import java.util.Optional;

/**
 * File 조회 Port (Outbound Port - Query)
 * <p>
 * Outbound Port 규칙:
 * - 인터페이스명: Load*Port (단건 조회)
 * - 패키지: ..application..port.out.query..
 * - Query 전용 (조회만 수행)
 * </p>
 * <p>
 * File Aggregate를 ID로 조회합니다.
 * </p>
 */
public interface LoadFilePort {

    /**
     * File ID로 File Aggregate 조회
     *
     * @param fileId 파일 ID
     * @return File Aggregate (Optional)
     */
    Optional<File> loadById(FileId fileId);
}
