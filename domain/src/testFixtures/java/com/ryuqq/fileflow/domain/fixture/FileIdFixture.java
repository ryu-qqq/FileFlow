package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.vo.FileId;

/**
 * FileId Test Fixture
 * <p>
 * 테스트에서 사용할 FileId 생성 헬퍼 메서드를 제공합니다.
 * </p>
 */
public class FileIdFixture {

    /**
     * 기본 FileId 생성
     * <p>
     * UUID v7 형식의 기본 ID를 반환합니다.
     * </p>
     *
     * @return FileId 인스턴스
     */
    public static FileId aFileId() {
        return FileId.of("01JCQM5K3P9XYZ123456ABCD");
    }

    /**
     * 유효한 FileId 생성
     * <p>
     * aFileId()와 동일하지만, 명시적으로 "유효한" ID임을 나타냅니다.
     * </p>
     *
     * @return 유효한 FileId 인스턴스
     */
    public static FileId aValidFileId() {
        return aFileId();
    }

    /**
     * 커스텀 값으로 FileId 생성
     *
     * @param value ID 값
     * @return FileId 인스턴스
     */
    public static FileId aFileIdWith(String value) {
        return FileId.of(value);
    }
}
