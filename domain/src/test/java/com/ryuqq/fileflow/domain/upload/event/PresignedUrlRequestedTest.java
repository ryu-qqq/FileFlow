package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.policy.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PresignedUrlRequested Domain Event 테스트")
class PresignedUrlRequestedTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 PresignedUrlRequested 이벤트를 생성할 수 있다")
        void createPresignedUrlRequestedEvent() {
            // given
            String sessionId = "session-123";
            String uploaderId = "uploader-456";
            String fileName = "test-image.jpg";
            FileType fileType = FileType.IMAGE;
            long fileSizeBytes = 1024000L;
            String policyKey = "policy-789";

            // when
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    sessionId,
                    uploaderId,
                    fileName,
                    fileType,
                    fileSizeBytes,
                    policyKey
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.getSessionId()).isEqualTo(sessionId);
            assertThat(event.getUploaderId()).isEqualTo(uploaderId);
            assertThat(event.getFileName()).isEqualTo(fileName);
            assertThat(event.getFileType()).isEqualTo(fileType);
            assertThat(event.getFileSizeBytes()).isEqualTo(fileSizeBytes);
            assertThat(event.getPolicyKey()).isEqualTo(policyKey);
            assertThat(event.occurredOn()).isNotNull();
            assertThat(event.occurredOn()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("occurredOn은 현재 시간으로 자동 설정된다")
        void occurredOnIsSetToNow() {
            // given
            LocalDateTime beforeCreation = LocalDateTime.now();

            // when
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "file.jpg",
                    FileType.IMAGE,
                    1024L,
                    "policy-789"
            );

            // then
            LocalDateTime afterCreation = LocalDateTime.now();
            assertThat(event.occurredOn()).isAfterOrEqualTo(beforeCreation);
            assertThat(event.occurredOn()).isBeforeOrEqualTo(afterCreation);
        }

        @Test
        @DisplayName("다양한 FileType으로 이벤트를 생성할 수 있다")
        void createWithVariousFileTypes() {
            // given & when
            PresignedUrlRequested imageEvent = PresignedUrlRequested.of(
                    "session-1", "uploader-1", "image.jpg", FileType.IMAGE, 1024L, "policy-1"
            );
            PresignedUrlRequested htmlEvent = PresignedUrlRequested.of(
                    "session-2", "uploader-2", "page.html", FileType.HTML, 2048L, "policy-2"
            );
            PresignedUrlRequested pdfEvent = PresignedUrlRequested.of(
                    "session-3", "uploader-3", "doc.pdf", FileType.PDF, 512L, "policy-3"
            );
            PresignedUrlRequested excelEvent = PresignedUrlRequested.of(
                    "session-4", "uploader-4", "data.xlsx", FileType.EXCEL, 768L, "policy-4"
            );

            // then
            assertThat(imageEvent.getFileType()).isEqualTo(FileType.IMAGE);
            assertThat(htmlEvent.getFileType()).isEqualTo(FileType.HTML);
            assertThat(pdfEvent.getFileType()).isEqualTo(FileType.PDF);
            assertThat(excelEvent.getFileType()).isEqualTo(FileType.EXCEL);
        }
    }

    @Nested
    @DisplayName("eventType 테스트")
    class EventTypeTest {

        @Test
        @DisplayName("eventType은 'PresignedUrlRequested'를 반환한다")
        void eventTypeReturnsCorrectValue() {
            // given
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "file.jpg",
                    FileType.IMAGE,
                    1024L,
                    "policy-789"
            );

            // when
            String eventType = event.eventType();

            // then
            assertThat(eventType).isEqualTo("PresignedUrlRequested");
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 Getter가 올바른 값을 반환한다")
        void allGettersReturnCorrectValues() {
            // given
            String sessionId = "session-123";
            String uploaderId = "uploader-456";
            String fileName = "test.jpg";
            FileType fileType = FileType.IMAGE;
            long fileSizeBytes = 2048000L;
            String policyKey = "policy-789";

            PresignedUrlRequested event = PresignedUrlRequested.of(
                    sessionId, uploaderId, fileName, fileType, fileSizeBytes, policyKey
            );

            // when & then
            assertThat(event.getSessionId()).isEqualTo(sessionId);
            assertThat(event.getUploaderId()).isEqualTo(uploaderId);
            assertThat(event.getFileName()).isEqualTo(fileName);
            assertThat(event.getFileType()).isEqualTo(fileType);
            assertThat(event.getFileSizeBytes()).isEqualTo(fileSizeBytes);
            assertThat(event.getPolicyKey()).isEqualTo(policyKey);
        }

        @Test
        @DisplayName("큰 파일 크기도 정확히 저장된다")
        void largeFileSizeIsAccurate() {
            // given
            long largeSize = 5_000_000_000L; // 5GB

            // when
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "large-file.pdf",
                    FileType.PDF,
                    largeSize,
                    "policy-789"
            );

            // then
            assertThat(event.getFileSizeBytes()).isEqualTo(largeSize);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsWithSelf() {
            // given
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "file.jpg",
                    FileType.IMAGE,
                    1024L,
                    "policy-789"
            );

            // when & then
            assertThat(event).isEqualTo(event);
        }

        @Test
        @DisplayName("null과는 equals로 다르다")
        void notEqualsWithNull() {
            // given
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "file.jpg",
                    FileType.IMAGE,
                    1024L,
                    "policy-789"
            );

            // when & then
            assertThat(event).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와는 equals로 다르다")
        void notEqualsWithDifferentClass() {
            // given
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "file.jpg",
                    FileType.IMAGE,
                    1024L,
                    "policy-789"
            );

            // when & then
            assertThat(event).isNotEqualTo("different class");
        }

        @Test
        @DisplayName("다른 sessionId를 가진 이벤트는 equals로 다르다")
        void notEqualsWithDifferentSessionId() {
            // given
            PresignedUrlRequested event1 = PresignedUrlRequested.of(
                    "session-123", "uploader-1", "file.jpg", FileType.IMAGE, 1024L, "policy-1"
            );
            PresignedUrlRequested event2 = PresignedUrlRequested.of(
                    "session-456", "uploader-1", "file.jpg", FileType.IMAGE, 1024L, "policy-1"
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 fileSizeBytes를 가진 이벤트는 equals로 다르다")
        void notEqualsWithDifferentFileSize() {
            // given
            PresignedUrlRequested event1 = PresignedUrlRequested.of(
                    "session-123", "uploader-1", "file.jpg", FileType.IMAGE, 1024L, "policy-1"
            );
            PresignedUrlRequested event2 = PresignedUrlRequested.of(
                    "session-123", "uploader-1", "file.jpg", FileType.IMAGE, 2048L, "policy-1"
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드를 포함한다")
        void toStringContainsAllFields() {
            // given
            PresignedUrlRequested event = PresignedUrlRequested.of(
                    "session-123",
                    "uploader-456",
                    "test-file.jpg",
                    FileType.IMAGE,
                    1024000L,
                    "policy-789"
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("PresignedUrlRequested");
            assertThat(result).contains("session-123");
            assertThat(result).contains("uploader-456");
            assertThat(result).contains("test-file.jpg");
            assertThat(result).contains("IMAGE");
            assertThat(result).contains("1024000");
            assertThat(result).contains("policy-789");
        }
    }
}
