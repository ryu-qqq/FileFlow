# Image Processor Adapter

이미지 리사이징 및 메타데이터 추출을 담당하는 Outbound Adapter 모듈.

## 기술 선택: Scrimage

### 선택 이유

| 라이브러리 | WebP 지원 | Thread-safe | 메타데이터 추출 | 유지보수 | 선택 |
|-----------|----------|-------------|----------------|---------|------|
| **Scrimage** | **Native** | **Immutable API** | **내장** | **활발** | **선택** |
| Thumbnailator | 미지원 | O | 미지원 | 보통 | X |
| ImageIO (JDK) | 플러그인 필요 | O | 제한적 | 표준 | X |
| imgscalr | 미지원 | O | 미지원 | 미유지 | X |
| TwelveMonkeys | 플러그인 | O | O | 보통 | X |
| libvips (JNI) | O | O | O | 활발 | X (복잡성) |

### 상세 비교

#### 1. WebP 네이티브 지원

```java
// Scrimage - 네이티브 WebP 지원
new WebpWriter(80).write(outputStream);

// 다른 라이브러리 - 추가 의존성 필요 (ImageIO 플러그인 등)
```

**왜 중요한가?**
- WebP는 JPEG 대비 25-35% 작은 파일 크기
- 최신 브라우저에서 모두 지원
- CDN 트래픽 비용 절감

#### 2. Immutable API (Thread-safe)

```java
// Scrimage - Immutable (Thread-safe)
ImmutableImage image = ImmutableImage.loader().fromStream(input);
ImmutableImage resized = image.bound(600, 600);  // 새 객체 반환

// 다른 라이브러리 - Mutable (Thread 주의 필요)
BufferedImage image = ImageIO.read(input);
image.getGraphics().drawImage(...);  // 원본 수정
```

**왜 중요한가?**
- 멀티스레드 환경에서 안전
- 함수형 프로그래밍 스타일
- 버그 발생 가능성 감소

#### 3. 메타데이터 추출 내장

```java
// Scrimage - 메타데이터 추출 내장
ImageMetadata metadata = image.metadata();
metadata.tags().stream()
    .filter(tag -> tag.name().contains("color"))
    .findFirst();

// 다른 라이브러리 - 별도 라이브러리 필요 (metadata-extractor 등)
```

#### 4. 활발한 유지보수

- GitHub Stars: 1.5K+
- 최신 버전: 4.1.3 (2024)
- Kotlin/Scala 기반 현대적 설계
- 지속적인 버그 수정 및 기능 추가

### 제외된 라이브러리들

#### Thumbnailator
- **장점**: 간단한 API, 가벼움
- **단점**: WebP 미지원, 메타데이터 추출 불가
- **제외 이유**: WebP가 필수 요구사항

#### ImageIO (JDK 표준)
- **장점**: 추가 의존성 없음
- **단점**: WebP 지원에 별도 플러그인 필요, 저수준 API
- **제외 이유**: 생산성 저하, WebP 플러그인 관리 부담

#### imgscalr
- **장점**: 빠른 리사이징
- **단점**: 2016년 이후 유지보수 중단, WebP 미지원
- **제외 이유**: 보안 업데이트 없음

#### TwelveMonkeys
- **장점**: 다양한 포맷 지원
- **단점**: ImageIO 플러그인 방식, 설정 복잡
- **제외 이유**: Scrimage 대비 생산성 저하

#### libvips (JNI)
- **장점**: 가장 빠른 성능, 메모리 효율
- **단점**: JNI 기반으로 배포 복잡, 네이티브 라이브러리 관리 필요
- **제외 이유**: 운영 복잡성 증가

## 아키텍처

```
application/
└── port/out/client/
    └── ImageProcessingPort.java  (인터페이스)

adapter-out/image-processor/
├── ScrimageImageProcessor.java   (구현체)
└── ImageProcessingException.java (예외)
```

## 사용법

### 이미지 리사이징

```java
@Component
public class ImageResizingService {
    private final ImageProcessingPort imageProcessingPort;

    public byte[] resizeToThumbnail(byte[] originalImage) {
        ImageProcessingResultResponse result = imageProcessingPort.resize(
            originalImage,
            ImageVariant.THUMBNAIL,  // 200x200
            ImageFormat.WEBP
        );
        return result.data();
    }
}
```

### 메타데이터 추출

```java
@Component
public class ImageMetadataService {
    private final ImageProcessingPort imageProcessingPort;

    public ImageMetadataResponse extractMetadata(byte[] imageData) {
        return imageProcessingPort.extractMetadata(imageData);
        // width, height, format, colorSpace 반환
    }
}
```

## 지원 포맷

### 입력 포맷
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)
- BMP (.bmp)

### 출력 포맷
- WebP (권장, 압축률 우수)
- JPEG (사진에 적합)
- PNG (투명도 필요 시)

## 변형 타입

| 타입 | 최대 크기 | 용도 |
|-----|----------|------|
| ORIGINAL | 원본 유지 | 원본 저장 |
| LARGE | 1200x1200 | 상세 보기 |
| MEDIUM | 600x600 | 목록 보기 |
| THUMBNAIL | 200x200 | 미리보기 |

## 품질 설정

| 포맷 | 기본 품질 | 설명 |
|-----|----------|------|
| JPEG | 85 | 시각적 품질과 파일 크기 균형 |
| WebP | 80 | JPEG 대비 동일 품질에 작은 크기 |
| PNG | MaxCompression | 무손실, 최대 압축 |

## 의존성

```gradle
dependencies {
    implementation libs.scrimage.core
    implementation libs.scrimage.webp
    implementation libs.scrimage.formats.extra
}
```

## 테스트

```bash
./gradlew :adapter-out:image-processor:test
```

## 성능 고려사항

1. **메모리**: 큰 이미지 처리 시 힙 메모리 주의
2. **스레드 안전성**: Immutable API로 동시 처리 안전
3. **품질 vs 크기**: 용도에 맞는 품질 설정 권장
