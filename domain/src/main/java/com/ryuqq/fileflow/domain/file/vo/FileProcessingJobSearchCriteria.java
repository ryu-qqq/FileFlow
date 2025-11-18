package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.iam.vo.FileId;

/**
 * FileProcessingJob 검색 조건 Value Object
 * <p>
 * QueryPort의 findByCriteria, countByCriteria에서 사용하는 검색 조건입니다.
 * </p>
 */
public record FileProcessingJobSearchCriteria(
        FileId fileId,
        JobStatus jobStatus,
        String jobType
) {
    /**
     * 모든 조건을 만족하는 검색 조건 생성
     */
    public static FileProcessingJobSearchCriteria of(FileId fileId, JobStatus jobStatus, String jobType) {
        return new FileProcessingJobSearchCriteria(fileId, jobStatus, jobType);
    }

    /**
     * fileId로만 검색
     */
    public static FileProcessingJobSearchCriteria byFileId(FileId fileId) {
        return new FileProcessingJobSearchCriteria(fileId, null, null);
    }

    /**
     * jobStatus로만 검색
     */
    public static FileProcessingJobSearchCriteria byJobStatus(JobStatus jobStatus) {
        return new FileProcessingJobSearchCriteria(null, jobStatus, null);
    }

    /**
     * jobType으로만 검색
     */
    public static FileProcessingJobSearchCriteria byJobType(String jobType) {
        return new FileProcessingJobSearchCriteria(null, null, jobType);
    }
}
