package com.ryuqq.fileflow.adapter.image.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.springframework.stereotype.Component;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * EXIF Orientation 태그 기반 이미지 회전 처리기
 *
 * 역할:
 * - EXIF Orientation 태그를 읽어 이미지를 올바른 방향으로 회전
 * - 회전 후 Orientation 태그는 1 (Normal)로 재설정
 * - 카메라에서 촬영한 이미지의 방향을 자동으로 보정
 *
 * EXIF Orientation 값:
 * - 1: Normal (회전 없음)
 * - 2: Flip horizontal
 * - 3: Rotate 180
 * - 4: Flip vertical
 * - 5: Flip horizontal + Rotate 90 CW
 * - 6: Rotate 90 CW
 * - 7: Flip horizontal + Rotate 270 CW
 * - 8: Rotate 270 CW
 *
 * @author sangwon-ryu
 */
@Component
public class ImageRotationHandler {

    /**
     * EXIF Orientation 태그를 기반으로 이미지를 회전합니다.
     *
     * @param sourceImage 원본 이미지
     * @param sourceImageBytes 원본 이미지 바이트 배열 (EXIF 읽기용)
     * @return 회전된 이미지
     * @throws IOException 처리 중 오류 발생 시
     */
    public BufferedImage rotateByExifOrientation(BufferedImage sourceImage, byte[] sourceImageBytes) throws IOException {
        Integer orientation = extractOrientation(sourceImageBytes);

        if (orientation == null || orientation == 1) {
            // Orientation이 없거나 Normal이면 회전 불필요
            return sourceImage;
        }

        return applyRotation(sourceImage, orientation);
    }

    /**
     * EXIF 메타데이터로부터 Orientation 태그를 추출합니다.
     *
     * @param imageBytes 이미지 바이트 배열
     * @return Orientation 값 (1-8), 없으면 null
     */
    private Integer extractOrientation(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));
            ExifIFD0Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (exifDirectory == null) {
                return null;
            }

            return exifDirectory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);

        } catch (ImageProcessingException | IOException e) {
            // EXIF 읽기 실패 시 null 반환 (회전 없이 원본 사용)
            return null;
        }
    }

    /**
     * Orientation 값에 따라 이미지를 회전합니다.
     *
     * @param sourceImage 원본 이미지
     * @param orientation Orientation 값 (1-8)
     * @return 회전된 이미지
     */
    private BufferedImage applyRotation(BufferedImage sourceImage, int orientation) {
        switch (orientation) {
            case 1:
                // Normal - 회전 불필요
                return sourceImage;

            case 2:
                // Flip horizontal
                return flipHorizontal(sourceImage);

            case 3:
                // Rotate 180
                return rotate(sourceImage, 180);

            case 4:
                // Flip vertical
                return flipVertical(sourceImage);

            case 5:
                // Flip horizontal + Rotate 90 CW
                return flipHorizontal(rotate(sourceImage, 90));

            case 6:
                // Rotate 90 CW
                return rotate(sourceImage, 90);

            case 7:
                // Flip horizontal + Rotate 270 CW
                return flipHorizontal(rotate(sourceImage, 270));

            case 8:
                // Rotate 270 CW (or 90 CCW)
                return rotate(sourceImage, 270);

            default:
                // 알 수 없는 Orientation 값은 원본 반환
                return sourceImage;
        }
    }

    /**
     * 이미지를 지정된 각도로 회전합니다.
     *
     * @param image 원본 이미지
     * @param degrees 회전 각도 (90, 180, 270)
     * @return 회전된 이미지
     */
    private BufferedImage rotate(BufferedImage image, int degrees) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 90도 또는 270도 회전 시 width와 height가 바뀜
        int newWidth = (degrees == 90 || degrees == 270) ? height : width;
        int newHeight = (degrees == 90 || degrees == 270) ? width : height;

        BufferedImage rotatedImage = new BufferedImage(
                newWidth,
                newHeight,
                image.getType()
        );

        Graphics2D g2d = rotatedImage.createGraphics();

        // 회전 변환 적용
        AffineTransform transform = new AffineTransform();

        if (degrees == 90) {
            transform.translate(height, 0);
            transform.rotate(Math.toRadians(90));
        } else if (degrees == 180) {
            transform.translate(width, height);
            transform.rotate(Math.toRadians(180));
        } else if (degrees == 270) {
            transform.translate(0, width);
            transform.rotate(Math.toRadians(270));
        }

        g2d.drawImage(image, transform, null);
        g2d.dispose();

        return rotatedImage;
    }

    /**
     * 이미지를 수평으로 반전합니다.
     *
     * @param image 원본 이미지
     * @return 반전된 이미지
     */
    private BufferedImage flipHorizontal(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage flippedImage = new BufferedImage(
                width,
                height,
                image.getType()
        );

        Graphics2D g2d = flippedImage.createGraphics();

        // 수평 반전 변환
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-width, 0);

        g2d.drawImage(image, transform, null);
        g2d.dispose();

        return flippedImage;
    }

    /**
     * 이미지를 수직으로 반전합니다.
     *
     * @param image 원본 이미지
     * @return 반전된 이미지
     */
    private BufferedImage flipVertical(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage flippedImage = new BufferedImage(
                width,
                height,
                image.getType()
        );

        Graphics2D g2d = flippedImage.createGraphics();

        // 수직 반전 변환
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -height);

        g2d.drawImage(image, transform, null);
        g2d.dispose();

        return flippedImage;
    }
}
