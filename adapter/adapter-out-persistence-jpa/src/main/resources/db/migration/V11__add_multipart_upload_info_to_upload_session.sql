-- ========================================
-- V11: Add multipart_upload_info to upload_session
-- ========================================
-- 멀티파트 업로드 정보를 JSON 형태로 저장하는 컬럼 추가
-- 대용량 파일 업로드 시 파트 정보와 진행률 추적을 위한 데이터
-- ========================================

ALTER TABLE upload_session
ADD COLUMN multipart_upload_info JSON NULL COMMENT '멀티파트 업로드 정보 (uploadId, uploadPath, parts)';

-- JSON 구조 예시:
-- {
--   "uploadId": "s3-multipart-upload-id",
--   "uploadPath": "tenant-id/uploads/2024/01/15/file.mp4",
--   "parts": [
--     {"partNumber": 1, "uploadUrl": "presigned-url-1", "eTag": null},
--     {"partNumber": 2, "uploadUrl": "presigned-url-2", "eTag": null},
--     ...
--   ]
-- }
