# FILE-ASSET-PROCESSING Infrastructure Layer TDD Plan

> **Jira Issue**: [KAN-341](https://ryuqqq.atlassian.net/browse/KAN-341)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)

## Overview
- **PRD**: `docs/prd/file-asset-processing.md`
- **Layer**: Infrastructure (External Systems Integration)
- **Estimated Time**: 180 minutes (3 hours)
- **Total Cycles**: 12 TDD cycles

---

## Infrastructure Components

### 구현 대상
1. **ThumbnailatorImageProcessor** - ImageProcessingPort 구현 (이미지 리사이징)
2. **JsoupHtmlImageExtractor** - HtmlProcessingPort 구현 (HTML 이미지 추출/교체)
3. **ExternalImageDownloader** - 외부 URL 이미지 다운로드
4. **SqsMessageSender** - SqsMessagePort 구현 (SQS 메시지 발송)
5. **FileProcessingQueueListener** - SQS 메시지 수신 (Worker)

### 사용 라이브러리
- **이미지 처리**: Thumbnailator + webp-imageio
- **HTML 파싱**: Jsoup
- **SQS**: AWS SDK for Java v2
- **HTTP**: RestTemplate / WebClient

---

## Zero-Tolerance Rules (Infrastructure Layer)

### Must Follow
- [x] **Lombok 금지** - Plain Java 사용
- [x] **Port 인터페이스 구현** - Application Layer Port 구현체
- [x] **외부 시스템 격리** - 실제 외부 호출은 Infrastructure 내부에만
- [x] **Timeout 설정 필수** - 외부 호출 시 Connection/Read Timeout
- [x] **예외 변환** - Infrastructure 예외 → Domain 예외로 변환

### Test Requirements
- 단위 테스트: Mock 사용 (외부 시스템 격리)
- 통합 테스트: TestContainers (LocalStack) 또는 실제 파일 사용
- 이미지 처리: 테스트용 이미지 파일 준비

---

## TDD Cycles

### Cycle 1: ImageProcessingResult DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/image/ImageProcessingResultTest.java
[ ] 테스트: `생성_모든필드설정()` - bytes, width, height, size 검증
[ ] 테스트: `생성_빈bytes_예외()` - null/empty bytes 검증
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ImageProcessingResult DTO 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/image/ImageProcessingResult.java
[ ] record 사용 (Lombok 금지!)
[ ] 필드: byte[] bytes, int width, int height, long size
[ ] Compact Constructor에서 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ImageProcessingResult DTO 구현"
```

---

### Cycle 2: ThumbnailatorImageProcessor - resize 기본 (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/image/ThumbnailatorImageProcessorTest.java
[ ] 테스트용 이미지 준비: src/test/resources/images/test-image-1000x800.jpg
[ ] 테스트: `resize_가로이미지_LARGE_1200px()` - 가로가 긴 이미지
  - input: 1000x800, variant: LARGE(1200)
  - expected: width <= 1200, 비율 유지
[ ] 테스트: `resize_세로이미지_MEDIUM_600px()` - 세로가 긴 이미지
  - input: 600x1000, variant: MEDIUM(600)
  - expected: height <= 600, 비율 유지
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ThumbnailatorImageProcessor 리사이징 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 클래스 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/image/ThumbnailatorImageProcessor.java
[ ] @Component 어노테이션
[ ] ImageProcessingPort 구현
[ ] resize(byte[] sourceBytes, ImageVariant variant, ImageFormat format) 메서드
  - BufferedImage original = ImageIO.read(...)
  - 가로/세로 비율 계산
  - Thumbnails.of(original).size(...).outputFormat(...).toOutputStream(...)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ThumbnailatorImageProcessor 기본 리사이징 구현"
```

---

### Cycle 3: ThumbnailatorImageProcessor - JPEG/PNG 포맷 (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `resize_JPEG포맷_확장자jpg()` - JPEG 출력
[ ] 테스트: `resize_PNG포맷_확장자png()` - PNG 출력 (투명도 유지)
[ ] 테스트: `resize_ORIGINAL_variant_리사이징안함()` - ORIGINAL은 리사이징 없이 반환
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: ThumbnailatorImageProcessor 포맷 변환 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] ImageFormat에 따른 outputFormat 분기
[ ] ORIGINAL variant 처리 (리사이징 스킵)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ThumbnailatorImageProcessor 포맷 변환 구현"
```

---

### Cycle 4: ThumbnailatorImageProcessor - WebP 포맷 (20분)

**Red Phase** - `test:`
```
[ ] 테스트: `resize_WEBP포맷_webp출력()` - WebP 출력
[ ] 테스트: `resize_WEBP_품질설정()` - 품질 80% 적용 확인
[ ] 테스트 실행 → 실패 확인 (webp-imageio 라이브러리 필요)
[ ] Commit: "test: ThumbnailatorImageProcessor WebP 포맷 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] build.gradle에 webp-imageio 의존성 추가
  - implementation 'org.sejda.imageio:webp-imageio:0.1.6'
[ ] WebP 포맷 처리 분기
[ ] 품질 설정 적용 (default: 80)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ThumbnailatorImageProcessor WebP 포맷 구현"
```

---

### Cycle 5: ThumbnailatorImageProcessor - 예외 처리 (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `resize_손상된이미지_예외발생()` - ImageProcessingException
[ ] 테스트: `resize_지원안되는포맷_예외발생()` - UnsupportedFormatException
[ ] 테스트: `resize_null입력_예외발생()` - IllegalArgumentException
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: ThumbnailatorImageProcessor 예외 처리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 예외 클래스 생성: ImageProcessingException, UnsupportedFormatException
[ ] try-catch로 예외 변환
[ ] null/empty 입력 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ThumbnailatorImageProcessor 예외 처리 구현"
```

---

### Cycle 6: ExtractedImage DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/html/ExtractedImageTest.java
[ ] 테스트: `생성_URL과소스타입설정()` - url, sourceType 검증
[ ] 테스트: `ImageSourceType_IMG_SRC()` - img 태그 src
[ ] 테스트: `ImageSourceType_INLINE_STYLE()` - style 속성 내 url()
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ExtractedImage DTO 및 ImageSourceType 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Enum 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/html/ImageSourceType.java
  - IMG_SRC, INLINE_STYLE
[ ] DTO 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/html/ExtractedImage.java
  - record(String url, ImageSourceType sourceType)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ExtractedImage DTO 및 ImageSourceType 구현"
```

---

### Cycle 7: JsoupHtmlImageExtractor - extractImages (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/html/JsoupHtmlImageExtractorTest.java
[ ] 테스트용 HTML 준비: src/test/resources/html/test-with-images.html
[ ] 테스트: `extractImages_img태그_src추출()` - <img src="...">
[ ] 테스트: `extractImages_복수이미지_모두추출()` - 여러 img 태그
[ ] 테스트: `extractImages_상대경로URL_추출()` - ./images/test.jpg
[ ] 테스트: `extractImages_이미지없음_빈리스트()` - 이미지 없는 HTML
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: JsoupHtmlImageExtractor img 태그 추출 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 클래스 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/html/JsoupHtmlImageExtractor.java
[ ] @Component 어노테이션
[ ] HtmlProcessingPort 구현
[ ] extractImages(String htmlContent) 메서드
  - Document doc = Jsoup.parse(htmlContent)
  - doc.select("img[src]").forEach(...)
  - ExtractedImage(url, ImageSourceType.IMG_SRC) 생성
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: JsoupHtmlImageExtractor img 태그 추출 구현"
```

---

### Cycle 8: JsoupHtmlImageExtractor - inline style 추출 (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `extractImages_inlineStyle_backgroundUrl추출()` - style="background: url(...)"
[ ] 테스트: `extractImages_inlineStyle_backgroundImageUrl추출()` - style="background-image: url(...)"
[ ] 테스트: `extractImages_혼합_img와style모두추출()` - img + style URL 모두
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: JsoupHtmlImageExtractor inline style URL 추출 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] extractUrlsFromStyle(String style) private 메서드 추가
  - 정규식: url\(['"]?([^'")]+)['"]?\)
[ ] doc.select("[style*=background]") 처리
[ ] ImageSourceType.INLINE_STYLE로 구분
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: JsoupHtmlImageExtractor inline style URL 추출 구현"
```

---

### Cycle 9: JsoupHtmlImageExtractor - replaceImageUrls (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `replaceImageUrls_img태그_src교체()` - 새 URL로 교체
[ ] 테스트: `replaceImageUrls_복수이미지_각각교체()` - 여러 이미지 개별 교체
[ ] 테스트: `replaceImageUrls_없는URL_무시()` - 매핑에 없는 URL은 그대로
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: JsoupHtmlImageExtractor URL 교체 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] replaceImageUrls(String htmlContent, Map<String, String> urlMappings) 메서드
  - doc.select("img[src]").forEach(img -> {...})
  - urlMappings.containsKey(oldUrl) 확인 후 교체
  - return doc.html()
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: JsoupHtmlImageExtractor URL 교체 구현"
```

---

### Cycle 10: ExternalImageDownloader (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/http/ExternalImageDownloaderTest.java
[ ] Mock: RestTemplate 모킹
[ ] 테스트: `download_성공_바이트반환()` - 200 OK + body
[ ] 테스트: `download_404응답_예외발생()` - ImageDownloadException
[ ] 테스트: `download_타임아웃_예외발생()` - ImageDownloadException
[ ] 테스트: `download_null_URL_예외발생()` - IllegalArgumentException
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ExternalImageDownloader 다운로드 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 클래스 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/http/ExternalImageDownloader.java
[ ] @Component 어노테이션
[ ] RestTemplate 의존성 주입 (생성자)
[ ] download(String imageUrl) 메서드
  - restTemplate.getForEntity(imageUrl, byte[].class)
  - 응답 코드 검증
  - 예외 변환 (ImageDownloadException)
[ ] 예외 클래스 생성: ImageDownloadException
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ExternalImageDownloader 구현"
```

---

### Cycle 11: SqsMessageSender (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/sqs/SqsMessageSenderTest.java
[ ] Mock: SqsClient 모킹
[ ] 테스트: `sendMessage_성공_messageId반환()` - SQS 전송 성공
[ ] 테스트: `sendMessage_실패_예외발생()` - SqsException → MessageSendException
[ ] 테스트: `sendMessage_null_payload_예외발생()` - IllegalArgumentException
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: SqsMessageSender 메시지 전송 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 클래스 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/sqs/SqsMessageSender.java
[ ] @Component 어노테이션
[ ] SqsMessagePort 구현
[ ] SqsClient 의존성 주입
[ ] sendMessage(String payload) 메서드
  - SendMessageRequest 생성
  - sqsClient.sendMessage(request)
  - return messageId
[ ] 예외 클래스 생성: MessageSendException
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: SqsMessageSender 구현"
```

---

### Cycle 12: FileProcessingQueueListener (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/sqs/FileProcessingQueueListenerTest.java
[ ] Mock: ProcessFileAssetUseCase 모킹
[ ] 테스트: `handleMessage_성공_UseCase호출()` - UseCase.execute() 호출 검증
[ ] 테스트: `handleMessage_잘못된메시지_예외()` - 파싱 실패 처리
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingQueueListener 메시지 처리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 클래스 생성: infrastructure/src/main/java/com/fileflow/fileasset/infrastructure/sqs/FileProcessingQueueListener.java
[ ] @Component 어노테이션
[ ] @SqsListener(value = "${sqs.file-processing-queue}")
[ ] ProcessFileAssetUseCase 의존성 주입
[ ] handleMessage(FileProcessingMessage message) 메서드
  - ProcessFileAssetCommand 생성
  - useCase.execute(command) 호출
[ ] DTO 생성: FileProcessingMessage (record)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingQueueListener 구현"
```

---

## Tidy Phase: Infrastructure 설정 클래스 (전체 완료 후)

**Refactor Phase** - `struct:`
```
[ ] RestTemplateConfig 생성
  - Connection Timeout: 5초
  - Read Timeout: 30초
  - @Bean RestTemplate 정의
[ ] SqsConfig 생성
  - SqsClient @Bean 정의
  - Queue URL 설정 (@Value)
[ ] ImageProcessingConfig 생성
  - WebP 품질 설정
  - JPEG 품질 설정
[ ] 테스트용 설정 분리 (test profile)
[ ] Commit: "struct: Infrastructure 설정 클래스 생성"
```

---

## TestFixture 생성 (Tidy)

**Refactor Phase** - `struct:`
```
[ ] TestImageFactory 생성
  - createTestImage(int width, int height, String format) - 테스트용 이미지 바이트 생성
  - loadTestImage(String resourcePath) - 리소스에서 로드
[ ] TestHtmlFactory 생성
  - createHtmlWithImages(List<String> imageUrls) - 테스트 HTML 생성
  - createHtmlWithInlineStyles(List<String> bgUrls) - inline style HTML
[ ] SqsMessageTestFixture 생성
  - createProcessingMessage(String fileAssetId)
[ ] 기존 테스트에 TestFixture 적용
[ ] Commit: "struct: Infrastructure TestFixture 생성 및 적용"
```

---

## 통합 테스트 (TestContainers)

### LocalStack 통합 테스트

**파일**: `infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/integration/SqsIntegrationTest.java`

```
[ ] @Testcontainers + LocalStackContainer(SQS)
[ ] 테스트: `실제SQS_메시지전송_수신()` - 실제 SQS 동작 검증
[ ] 테스트: `DLQ_처리실패시_이동()` - Dead Letter Queue 동작
[ ] Commit: "test: SQS LocalStack 통합 테스트 추가"
```

### 이미지 처리 통합 테스트

**파일**: `infrastructure/src/test/java/com/fileflow/fileasset/infrastructure/integration/ImageProcessingIntegrationTest.java`

```
[ ] 실제 이미지 파일 사용
[ ] 테스트: `전체흐름_JPEG_LARGE리사이징()` - 실제 JPEG 파일 → LARGE
[ ] 테스트: `전체흐름_PNG_WEBP변환()` - PNG → WebP 변환
[ ] 테스트: `전체흐름_대용량이미지_5MB()` - 5MB 이미지 처리 시간
[ ] Commit: "test: 이미지 처리 통합 테스트 추가"
```

---

## Summary

| Phase | Cycles | Estimated Time |
|-------|--------|----------------|
| DTO (1, 6) | 2 | 20분 |
| ImageProcessor (2-5) | 4 | 70분 |
| HtmlExtractor (7-9) | 3 | 50분 |
| HTTP/SQS (10-12) | 3 | 50분 |
| Config + TestFixture | 2 | 30분 |
| Integration Tests | 2 | 30분 |
| **Total** | **12+4** | **250분 (약 4시간)** |

---

## Dependencies (build.gradle)

```groovy
// 이미지 처리
implementation 'net.coobird:thumbnailator:0.4.20'
implementation 'org.sejda.imageio:webp-imageio:0.1.6'

// HTML 파싱
implementation 'org.jsoup:jsoup:1.17.2'

// AWS SQS
implementation 'software.amazon.awssdk:sqs:2.25.0'
implementation 'io.awspring.cloud:spring-cloud-aws-starter-sqs:3.1.0'

// 테스트
testImplementation 'org.testcontainers:localstack:1.19.7'
testImplementation 'org.testcontainers:junit-jupiter:1.19.7'
```

---

## Checklist Before Starting

- [ ] Domain Layer Plan 완료 확인
- [ ] Application Layer Plan 완료 확인
- [ ] Persistence Layer Plan 완료 확인
- [ ] Application Port 인터페이스 정의 완료 (ImageProcessingPort, HtmlProcessingPort, SqsMessagePort)
- [ ] AWS 자격 증명 설정 (LocalStack 또는 실제 AWS)
- [ ] 테스트용 이미지 파일 준비
- [ ] 테스트용 HTML 파일 준비
