-- callback_outbox 테이블에 max_retries 컬럼 추가
ALTER TABLE callback_outbox
    ADD COLUMN max_retries INT NOT NULL DEFAULT 5 AFTER retry_count;
