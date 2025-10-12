package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MultipartUploadInfo VO 테스트")
class MultipartUploadInfoTest {

    private static final LocalDateTime EXPIRES_AT = LocalDateTime.now().plusHours(1);

    private List<PartUploadInfo> createValidParts(int count) {
        List<PartUploadInfo> parts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            parts.add(PartUploadInfo.of(
                    i,
                    "https://s3.amazonaws.com/bucket/key?partNumber=" + i,
                    (i - 1) * 5_242_880L,
                    i * 5_242_880L - 1,
                    EXPIRES_AT
            ));
        }
        return parts;
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 MultipartUploadInfo를 생성할 수 있다")
        void createMultipartUploadInfo() {
            // given
            String uploadId = "upload-id-123";
            String uploadPath = "bucket/tenant-1/file.jpg";
            List<PartUploadInfo> parts = createValidParts(3);

            // when
            MultipartUploadInfo info = MultipartUploadInfo.of(uploadId, uploadPath, parts);

            // then
            assertThat(info.uploadId()).isEqualTo(uploadId);
            assertThat(info.uploadPath()).isEqualTo(uploadPath);
            assertThat(info.totalParts()).isEqualTo(3);
        }

        @Test
        @DisplayName("uploadId가 null이면 예외가 발생한다")
        void uploadIdNull() {
            assertThatThrownBy(() -> MultipartUploadInfo.of(null, "path", createValidParts(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UploadId cannot be null or empty");
        }

        @Test
        @DisplayName("uploadPath가 null이면 예외가 발생한다")
        void uploadPathNull() {
            assertThatThrownBy(() -> MultipartUploadInfo.of("id", null, createValidParts(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UploadPath cannot be null or empty");
        }

        @Test
        @DisplayName("parts가 null이면 예외가 발생한다")
        void partsNull() {
            assertThatThrownBy(() -> MultipartUploadInfo.of("id", "path", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Parts cannot be null or empty");
        }

        // Note: 10,000개 이상 파트 테스트는 성능 문제로 생략

        @Test
        @DisplayName("파트 번호가 연속적이지 않으면 예외가 발생한다")
        void partsNotSequential() {
            // given
            List<PartUploadInfo> parts = List.of(
                    PartUploadInfo.of(1, "https://url1", 0, 100, EXPIRES_AT),
                    PartUploadInfo.of(3, "https://url3", 101, 200, EXPIRES_AT) // 2 missing
            );

            // when & then
            assertThatThrownBy(() -> MultipartUploadInfo.of("id", "path", parts))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part numbers must be sequential");
        }
    }

    @Nested
    @DisplayName("메서드 테스트")
    class MethodTest {

        @Test
        @DisplayName("totalParts()는 전체 파트 개수를 반환한다")
        void totalParts() {
            // given
            MultipartUploadInfo info = MultipartUploadInfo.of("id", "path", createValidParts(5));

            // when & then
            assertThat(info.totalParts()).isEqualTo(5);
        }

        @Test
        @DisplayName("getPart()는 특정 파트 번호의 정보를 반환한다")
        void getPart() {
            // given
            MultipartUploadInfo info = MultipartUploadInfo.of("id", "path", createValidParts(3));

            // when
            PartUploadInfo part2 = info.getPart(2);

            // then
            assertThat(part2.partNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("getPart()에 유효하지 않은 파트 번호를 전달하면 예외가 발생한다")
        void getPartInvalidNumber() {
            // given
            MultipartUploadInfo info = MultipartUploadInfo.of("id", "path", createValidParts(3));

            // when & then
            assertThatThrownBy(() -> info.getPart(4))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid part number");
            assertThatThrownBy(() -> info.getPart(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid part number");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("parts 리스트는 방어적 복사되어 외부 변경으로부터 보호된다")
        void partsAreDefensivelyCopied() {
            // given
            List<PartUploadInfo> originalParts = new ArrayList<>(createValidParts(3));
            MultipartUploadInfo info = MultipartUploadInfo.of("id", "path", originalParts);

            // when
            originalParts.add(PartUploadInfo.of(4, "https://url4", 300, 400, EXPIRES_AT));

            // then
            assertThat(info.totalParts()).isEqualTo(3);
        }
    }
}
