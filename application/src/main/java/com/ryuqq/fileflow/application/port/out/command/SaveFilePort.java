package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;

/**
 * File 저장 Port (Outbound Port)
 * <p>
 * Outbound Port 규칙:
 * - 인터페이스명: *Port (동작 중심)
 * - 패키지: ..application..port.out.command..
 * - 메서드: 명확한 동작 표현 (save, persist, update 등)
 * - Domain Aggregate 입력, ID 반환
 * </p>
 * <p>
 * File Aggregate를 영속화하는 Port입니다.
 * </p>
 */
public interface SaveFilePort {

    /**
     * File Aggregate 저장
     * <p>
     * File 메타데이터를 영속화합니다.
     * 외부 URL 다운로드의 경우 PENDING 상태로 저장됩니다.
     * </p>
     *
     * @param file 저장할 File Aggregate
     * @return 생성된 FileId
     */
    FileId save(File file);
}
