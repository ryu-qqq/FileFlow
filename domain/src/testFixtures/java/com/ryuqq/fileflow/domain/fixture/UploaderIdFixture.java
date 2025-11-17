package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.vo.UploaderId;

/**
 * UploaderId Test Fixture
 * <p>
 * 테스트에서 사용할 UploaderId 생성 헬퍼 메서드를 제공합니다.
 * </p>
 */
public class UploaderIdFixture {

    /**
     * 기본 UploaderId 생성
     * <p>
     * Long 타입의 기본 ID를 반환합니다.
     * </p>
     *
     * @return UploaderId 인스턴스
     */
    public static UploaderId anUploaderId() {
        return UploaderId.of(12345L);
    }

    /**
     * 유효한 UploaderId 생성
     * <p>
     * anUploaderId()와 동일하지만, 명시적으로 "유효한" ID임을 나타냅니다.
     * </p>
     *
     * @return 유효한 UploaderId 인스턴스
     */
    public static UploaderId aValidUploaderId() {
        return anUploaderId();
    }

    /**
     * 커스텀 값으로 UploaderId 생성
     *
     * @param value ID 값 (Long)
     * @return UploaderId 인스턴스
     */
    public static UploaderId anUploaderIdWith(Long value) {
        return UploaderId.of(value);
    }
}
