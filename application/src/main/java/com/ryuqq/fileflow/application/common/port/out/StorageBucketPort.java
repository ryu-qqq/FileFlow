package com.ryuqq.fileflow.application.common.port.out;

/**
 * 저장소 버킷 정보 제공 포트.
 *
 * <p>S3 버킷명을 추상화하여 모든 모듈에서 공통으로 사용합니다.
 */
public interface StorageBucketPort {

    /**
     * 저장소 버킷명을 반환합니다.
     *
     * @return 버킷명
     */
    String getBucket();
}
