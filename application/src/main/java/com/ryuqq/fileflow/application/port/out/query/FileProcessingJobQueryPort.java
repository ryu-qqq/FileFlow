package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * FileProcessingJob Query Port (Outbound Port)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *QueryPort
 * - 패키지: ..application..port.out.query..
 * - 필수 메서드 4개: findById, existsById, findByCriteria, countByCriteria
 * - Value Object 파라미터: FileProcessingJobId, FileProcessingJobSearchCriteria
 * - Domain 반환: FileProcessingJob Aggregate (DTO/Entity 반환 금지)
 * </p>
 * <p>
 * Application Layer에서 Persistence Layer로 FileProcessingJob Aggregate 조회 요청을 위한 Port입니다.
 * CQRS 분리: 조회 전용 Port (저장/수정/삭제 메서드 금지)
 * </p>
 */
public interface FileProcessingJobQueryPort {

    /**
     * ID로 파일 처리 작업 조회
     *
     * @param id 파일 처리 작업 ID (FileProcessingJobId VO)
     * @return 조회된 FileProcessingJob Aggregate (Optional)
     */
    Optional<FileProcessingJob> findById(FileProcessingJobId id);

    /**
     * ID로 파일 처리 작업 존재 여부 확인
     *
     * @param id 파일 처리 작업 ID (FileProcessingJobId VO)
     * @return 존재 여부 (boolean)
     */
    boolean existsById(FileProcessingJobId id);

    /**
     * 검색 조건으로 파일 처리 작업 목록 조회
     *
     * @param criteria 검색 조건 (FileProcessingJobSearchCriteria VO)
     * @return 조회된 FileProcessingJob Aggregate 목록
     */
    List<FileProcessingJob> findByCriteria(FileProcessingJobSearchCriteria criteria);

    /**
     * 검색 조건으로 파일 처리 작업 개수 조회
     *
     * @param criteria 검색 조건 (FileProcessingJobSearchCriteria VO)
     * @return 파일 처리 작업 개수
     */
    long countByCriteria(FileProcessingJobSearchCriteria criteria);
}
