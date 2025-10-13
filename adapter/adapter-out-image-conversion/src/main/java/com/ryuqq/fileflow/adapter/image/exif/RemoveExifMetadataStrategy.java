package com.ryuqq.fileflow.adapter.image.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * EXIF 메타데이터 제거 전략
 *
 * 역할:
 * - 개인정보 보호를 위해 GPS 위치 정보 등 민감한 EXIF 데이터 제거
 * - 이미지 변환 시 메타데이터가 포함되지 않도록 처리
 *
 * 제거 대상:
 * - GPS 위치 정보 (위도, 경도, 고도)
 * - 기타 개인 식별 가능한 메타데이터
 *
 * 유지 항목:
 * - 이미지 기본 속성 (크기, 포맷 등)은 제거하지 않음
 *
 * @author sangwon-ryu
 */
@Component
public class RemoveExifMetadataStrategy implements ExifMetadataStrategy {

    /**
     * EXIF 메타데이터를 제거합니다.
     *
     * 처리 방법:
     * - BufferedImage 자체는 메타데이터를 포함하지 않음
     * - 이미지 변환 시 메타데이터를 포함하지 않으면 자동으로 제거됨
     * - 따라서 원본 이미지를 그대로 반환
     *
     * @param sourceImage 원본 이미지
     * @param sourceImageBytes 원본 이미지 바이트 배열 (메타데이터 추출용)
     * @return 메타데이터가 제거된 이미지
     * @throws IOException 처리 중 오류 발생 시
     */
    @Override
    public BufferedImage processMetadata(BufferedImage sourceImage, byte[] sourceImageBytes) throws IOException {
        // GPS 정보 로깅 (디버깅 및 모니터링용)
        logGpsMetadataIfPresent(sourceImageBytes);

        // BufferedImage는 메타데이터를 포함하지 않으므로
        // Thumbnailator로 변환 시 자동으로 메타데이터가 제거됨
        return sourceImage;
    }

    /**
     * 메타데이터를 보존하지 않습니다.
     *
     * @return false (메타데이터 제거)
     */
    @Override
    public boolean preservesMetadata() {
        return false;
    }

    /**
     * GPS 메타데이터가 있는 경우 로그를 출력합니다.
     * 개인정보 제거 작업을 모니터링하기 위한 목적입니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     */
    private void logGpsMetadataIfPresent(byte[] imageBytes) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                // GPS 정보가 있으면 제거됨을 로깅
                System.out.println("GPS metadata detected and will be removed: " +
                        "Lat=" + gpsDirectory.getGeoLocation().getLatitude() +
                        ", Lon=" + gpsDirectory.getGeoLocation().getLongitude());
            }
        } catch (ImageProcessingException | IOException e) {
            // 메타데이터 읽기 실패는 무시 (메타데이터 제거에는 영향 없음)
        }
    }
}
