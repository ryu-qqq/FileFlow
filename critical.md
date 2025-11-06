Critical 이슈: ExternalDownloadManager - Checksum/MimeType 비동기 처리 미구현
문제 상황
위치: application/src/main/java/com/ryuqq/fileflow/application/download/manager/ExternalDownloadManager.java:274-279
현재 코드:
// FileAsset Aggregate 생성 ⭐ Factory Method 사용FileAsset fileAsset = FileAsset.fromCompletedUpload(    session,    result.storageKey(),    FileSize.of(fileSize));
문제점:
FileAsset.fromCompletedUpload() 호출 시:
Checksum: "pending"으로 고정 (Line 390)
MimeType: "application/octet-stream" 기본값 고정 (Line 388)
StorageUploadFacade.calculateChecksum() 구현 없음:
public String calculateChecksum(StorageKey storageKey) {       // TODO: S3StoragePort에 calculateChecksum 메서드 추가 필요       log.warn("calculateChecksum not implemented yet: key={}", storageKey.value());       return "pending";  // ❌ 항상 "pending" 반환   }
ExternalDownloadWorker에서 체크섬 계산 시도:
String checksum = storageUploadFacade.calculateChecksum(storageKey);   // 하지만 실제로는 "pending" 반환
결과:
FileAsset이 "pending" Checksum으로 저장됨
MimeType이 "application/octet-stream"으로 고정됨
FileAsset이 완전한 상태(AVAILABLE)로 전환되지 못함
필요한 작업
StorageUploadFacade.calculateChecksum() 구현
S3StoragePort에 S3 객체의 ETag/MD5 조회 메서드 추가
실제 체크섬 계산 로직 구현
MimeType 분석 로직 추가
HTTP 응답의 Content-Type 사용 (이미 httpResult.contentType() 있음)
또는 파일 확장자 기반 분석
FileAsset 업데이트 로직
Checksum 계산 후 FileAsset 업데이트
MimeType 설정 후 FileAsset 업데이트
상태를 AVAILABLE로 전환
영향도
Critical: 데이터 무결성 문제
FileAsset이 "pending" Checksum으로 남아있음
MimeType이 정확하지 않음
파일 무결성 검증 불가능
이 이슈는 별도 작업으로 분리하여 구현해야 합니다. 현재 PR에서는 이 부분을 수정하지 않았습니다.
