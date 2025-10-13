package com.ryuqq.fileflow.adapter.image.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * EXIF 메타데이터 유지 전략
 *
 * 역할:
 * - 저작권, 카메라 정보 등 유용한 EXIF 데이터는 유지
 * - GPS 위치 정보 등 개인정보는 제거
 * - 이미지 방향(Orientation) 정보는 유지하여 올바른 표시 보장
 *
 * 유지 항목:
 * - exif_make: 카메라 제조사
 * - exif_model: 카메라 모델
 * - exif_datetime: 촬영 일시
 * - exif_software: 소프트웨어 정보
 * - exif_orientation: 이미지 방향
 * - exif_copyright: 저작권 정보
 *
 * 제거 항목:
 * - GPS 위치 정보 (위도, 경도, 고도)
 *
 * 주의사항:
 * - WebP 포맷은 EXIF 메타데이터 저장을 제한적으로 지원
 * - webp-imageio 라이브러리는 메타데이터 쓰기를 지원하지 않음
 * - 따라서 현재 구현에서는 메타데이터를 DB에만 저장하고 이미지에는 포함하지 않음
 *
 * @author sangwon-ryu
 */
@Component
public class PreserveExifMetadataStrategy implements ExifMetadataStrategy {

    /**
     * EXIF 메타데이터를 선택적으로 유지합니다.
     *
     * 현재 구현:
     * - WebP는 메타데이터 쓰기를 지원하지 않으므로
     * - 메타데이터는 DB(file_metadata 테이블)에만 저장
     * - 이미지 파일 자체에는 메타데이터가 포함되지 않음
     *
     * 향후 개선:
     * - JPEG, PNG 등 메타데이터를 지원하는 포맷의 경우
     * - Apache Commons Imaging 등을 사용하여 메타데이터 쓰기 지원 가능
     *
     * @param sourceImage 원본 이미지
     * @param sourceImageBytes 원본 이미지 바이트 배열 (메타데이터 추출용)
     * @return 메타데이터가 처리된 이미지
     * @throws IOException 처리 중 오류 발생 시
     */
    @Override
    public BufferedImage processMetadata(BufferedImage sourceImage, byte[] sourceImageBytes) throws IOException {
        // GPS 정보 제거 로깅
        if (sourceImageBytes != null && sourceImageBytes.length > 0) {
            logMetadataProcessing(sourceImageBytes);
        }

        // 현재는 BufferedImage를 그대로 반환
        // WebP 변환 시 메타데이터는 자동으로 제거되고 DB에만 저장됨
        return sourceImage;
    }

    /**
     * 메타데이터를 부분적으로 보존합니다.
     * (실제로는 DB에만 저장하고 이미지에는 포함하지 않음)
     *
     * @return true (의도적으로 메타데이터 유지를 시도함)
     */
    @Override
    public boolean preservesMetadata() {
        return true;
    }

    /**
     * 메타데이터 처리 과정을 로깅합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     */
    private void logMetadataProcessing(byte[] imageBytes) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));

            // GPS 정보는 제거됨
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                System.out.println("GPS metadata will be removed for privacy");
            }

            // 카메라 정보는 유지 (DB에 저장됨)
            ExifIFD0Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifDirectory != null) {
                String make = exifDirectory.getString(ExifIFD0Directory.TAG_MAKE);
                String model = exifDirectory.getString(ExifIFD0Directory.TAG_MODEL);
                if (make != null || model != null) {
                    System.out.println("Camera metadata will be preserved in database: " + make + " " + model);
                }
            }
        } catch (ImageProcessingException | IOException e) {
            // 메타데이터 읽기 실패는 무시
        }
    }
}
