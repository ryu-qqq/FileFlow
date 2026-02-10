package com.ryuqq.fileflow.adapter.out.persistence.redis.session.dto;

import java.time.Duration;

public record SessionExpirationRedisData(String key, String value, Duration ttl) {}
