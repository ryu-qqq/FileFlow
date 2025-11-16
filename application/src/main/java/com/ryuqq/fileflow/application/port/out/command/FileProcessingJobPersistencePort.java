package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;

/**
 * FileProcessingJob Persistence Port (Outbound Port)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *PersistencePort
 * - 패키지: ..application..port.out.command..
 * - 메서드: persist() 하나만
 * - 반환 타입: FileProcessingJobId (Value Object)
 * - 파라미터: FileProcessingJob (Domain Aggregate)
 * </p>
 * <p>
 * Application Layer에서 Persistence Layer로 FileProcessingJob Aggregate 영속화 요청을 위한 Port입니다.
 * persist() 메서드는 신규 생성과 수정을 모두 처리합니다.
 * </p>
 */
public interface FileProcessingJobPersistencePort {

    /**
     * FileProcessingJob Aggregate 영속화
     * <p>
     * 신규 생성과 수정을 통합 처리합니다.
     * - 신규: fileProcessingJobId가 null → 저장 후 생성된 FileProcessingJobId 반환
     * - 수정: fileProcessingJobId가 존재 → 수정 후 동일한 FileProcessingJobId 반환
     * </p>
     *
     * @param job 영속화할 FileProcessingJob Aggregate
     * @return 영속화된 FileProcessingJob의 FileProcessingJobId (Value Object)
     */
    FileProcessingJobId persist(FileProcessingJob job);
}
