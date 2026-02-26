package com.ryuqq.fileflow.application.common.metric;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class FileFlowMetrics {

    private static final String PREFIX = "fileflow.";

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public FileFlowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        validateTags(tags);
        Timer timer =
                timerCache.computeIfAbsent(
                        createCacheKey(name, tags),
                        k ->
                                Timer.builder(PREFIX + name)
                                        .tags(tags)
                                        .publishPercentileHistogram()
                                        .register(meterRegistry));
        sample.stop(timer);
    }

    public void incrementCounter(String name, String... tags) {
        validateTags(tags);
        getOrCreateCounter(name, tags).increment();
    }

    public void recordBatchResult(
            String name, String category, SchedulerBatchProcessingResult result) {
        getOrCreateCounter(name + "_items_total", "category", category, "status", "total")
                .increment(result.total());
        getOrCreateCounter(name + "_items_total", "category", category, "status", "success")
                .increment(result.success());
        getOrCreateCounter(name + "_items_total", "category", category, "status", "failed")
                .increment(result.failed());
    }

    public void recordDuration(String name, Duration duration, String... tags) {
        validateTags(tags);
        Timer timer =
                timerCache.computeIfAbsent(
                        createCacheKey(name, tags),
                        k -> Timer.builder(PREFIX + name).tags(tags).register(meterRegistry));
        timer.record(duration);
    }

    private Counter getOrCreateCounter(String name, String... tags) {
        return counterCache.computeIfAbsent(
                createCacheKey(name, tags),
                k -> Counter.builder(PREFIX + name).tags(tags).register(meterRegistry));
    }

    private String createCacheKey(String name, String... tags) {
        return name + Arrays.toString(tags);
    }

    private void validateTags(String... tags) {
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Tags must be key-value pairs (even count), but got "
                            + tags.length
                            + " elements");
        }
    }
}
