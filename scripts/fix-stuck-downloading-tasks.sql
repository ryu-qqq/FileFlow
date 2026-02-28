-- Stage 환경: DOWNLOADING 상태로 stuck된 다운로드 태스크 FAILED 처리
-- 원인: 공백 포함 URL로 인한 URI.create() 실패 → DOWNLOADING 상태 영구 고착
-- 실행 전 확인: SELECT count(*) FROM download_task WHERE status = 'DOWNLOADING';

UPDATE download_task
SET status     = 'FAILED',
    last_error = 'URL에 공백 포함으로 인한 URI 파싱 실패 (hotfix 적용 전 stuck 건)',
    updated_at = NOW(6)
WHERE status = 'DOWNLOADING';
