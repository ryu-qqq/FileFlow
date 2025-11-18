package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.FileProcessingJobId;

/**
 * FileProcessingJobId Test Fixture
 * <p>
 * 테스트에서 사용할 FileProcessingJobId 생성 헬퍼 메서드를 제공합니다.
 * </p>
 */
public class FileProcessingJobIdFixture {

    /**
     * 기본 FileProcessingJobId 생성
     * <p>
     * UUID v7 형식의 기본 ID를 반환합니다.
     * </p>
     *
     * @return FileProcessingJobId 인스턴스
     */
    public static FileProcessingJobId aFileProcessingJobId() {
        return FileProcessingJobId.of("01JCQM5K3P9XYZ123456ABCD");
    }

    /**
     * 유효한 FileProcessingJobId 생성
     * <p>
     * aFileProcessingJobId()와 동일하지만, 명시적으로 "유효한" ID임을 나타냅니다.
     * </p>
     *
     * @return 유효한 FileProcessingJobId 인스턴스
     */
    public static FileProcessingJobId aValidFileProcessingJobId() {
        return aFileProcessingJobId();
    }

    /**
     * 커스텀 값으로 FileProcessingJobId 생성
     *
     * @param value ID 값
     * @return FileProcessingJobId 인스턴스
     */
    public static FileProcessingJobId aFileProcessingJobIdWith(String value) {
        return FileProcessingJobId.of(value);
    }
}
