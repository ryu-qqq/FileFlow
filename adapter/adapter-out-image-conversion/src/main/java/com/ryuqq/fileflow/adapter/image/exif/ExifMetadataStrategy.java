package com.ryuqq.fileflow.adapter.image.exif;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * EXIF 메타데이터 처리 전략 인터페이스
 *
 * 역할:
 * - 이미지 변환/압축 시 EXIF 메타데이터 처리 방법을 정의
 * - Strategy 패턴으로 다양한 메타데이터 처리 전략 지원
 *
 * 구현체:
 * - RemoveExifMetadataStrategy: GPS 등 개인정보 제거
 * - PreserveExifMetadataStrategy: 필요한 메타데이터만 유지
 *
 * @author sangwon-ryu
 */
public interface ExifMetadataStrategy {

    /**
     * 이미지 변환 시 EXIF 메타데이터를 처리합니다.
     *
     * @param sourceImage 원본 이미지
     * @param sourceImageBytes 원본 이미지 바이트 배열
     * @return 처리된 이미지 (메타데이터 처리 완료)
     * @throws IOException 처리 중 오류 발생 시
     */
    BufferedImage processMetadata(BufferedImage sourceImage, byte[] sourceImageBytes) throws IOException;

    /**
     * 이 전략이 메타데이터를 보존하는지 여부를 반환합니다.
     *
     * @return 메타데이터 보존 여부
     */
    boolean preservesMetadata();
}
