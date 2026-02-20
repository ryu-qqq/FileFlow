package com.ryuqq.fileflow.adapter.in.scheduler.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

@Tag("unit")
@DisplayName("SchedulerLoggingAspect 단위 테스트")
class SchedulerLoggingAspectTest {

    private MeterRegistry meterRegistry;
    private SchedulerLoggingAspect sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new SchedulerLoggingAspect(meterRegistry);
    }

    @Nested
    @DisplayName("around 메서드")
    class Around {

        @Test
        @DisplayName("성공: 배치 결과를 반환하면 정상 완료한다")
        void shouldReturnBatchResult() throws Throwable {
            // given
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");

            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(10, 8, 2);
            given(joinPoint.proceed()).willReturn(expected);

            // when
            Object result = sut.around(joinPoint, schedulerJob);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("성공: 처리 대상이 없으면 empty 결과를 반환한다")
        void shouldHandleEmptyResult() throws Throwable {
            // given
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");

            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.empty();
            given(joinPoint.proceed()).willReturn(expected);

            // when
            Object result = sut.around(joinPoint, schedulerJob);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("성공: 작업 완료 후 MDC에서 traceId를 제거한다")
        void shouldRemoveTraceIdFromMdcAfterCompletion() throws Throwable {
            // given
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");
            given(joinPoint.proceed()).willReturn(SchedulerBatchProcessingResult.empty());

            // when
            sut.around(joinPoint, schedulerJob);

            // then
            assertThat(MDC.get("traceId")).isNull();
        }

        @Test
        @DisplayName("실패: 예외 발생 시에도 MDC에서 traceId를 제거한다")
        void shouldRemoveTraceIdFromMdcOnException() throws Throwable {
            // given
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");
            given(joinPoint.proceed()).willThrow(new RuntimeException("test error"));

            // when & then
            assertThatThrownBy(() -> sut.around(joinPoint, schedulerJob))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("test error");

            assertThat(MDC.get("traceId")).isNull();
        }
    }

    @Nested
    @DisplayName("메트릭 기록")
    class MetricsRecording {

        @Test
        @DisplayName("성공: 작업 완료 시 duration Timer를 기록한다")
        void shouldRecordDurationTimer() throws Throwable {
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");
            given(joinPoint.proceed()).willReturn(SchedulerBatchProcessingResult.empty());

            sut.around(joinPoint, schedulerJob);

            assertThat(
                            meterRegistry
                                    .find("scheduler.job.duration")
                                    .tag("job_name", "TestJob")
                                    .timer())
                    .isNotNull();
        }

        @Test
        @DisplayName("성공: 배치 결과가 있으면 items Counter를 기록한다")
        void shouldRecordItemsCounter() throws Throwable {
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");
            given(joinPoint.proceed()).willReturn(SchedulerBatchProcessingResult.of(10, 8, 2));

            sut.around(joinPoint, schedulerJob);

            assertThat(
                            meterRegistry
                                    .find("scheduler.job.items")
                                    .tag("job_name", "TestJob")
                                    .tag("result", "success")
                                    .counter()
                                    .count())
                    .isEqualTo(8.0);
            assertThat(
                            meterRegistry
                                    .find("scheduler.job.items")
                                    .tag("job_name", "TestJob")
                                    .tag("result", "failed")
                                    .counter()
                                    .count())
                    .isEqualTo(2.0);
        }

        @Test
        @DisplayName("성공: 예외 발생 시에도 duration Timer를 기록한다")
        void shouldRecordDurationTimerOnException() throws Throwable {
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");
            given(joinPoint.proceed()).willThrow(new RuntimeException("test error"));

            assertThatThrownBy(() -> sut.around(joinPoint, schedulerJob))
                    .isInstanceOf(RuntimeException.class);

            assertThat(
                            meterRegistry
                                    .find("scheduler.job.duration")
                                    .tag("job_name", "TestJob")
                                    .timer())
                    .isNotNull();
        }

        @Test
        @DisplayName("성공: 처리 대상이 없으면 items Counter를 기록하지 않는다")
        void shouldNotRecordItemsCounterForEmptyResult() throws Throwable {
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            SchedulerJob schedulerJob = mock(SchedulerJob.class);
            given(schedulerJob.value()).willReturn("TestJob");
            given(joinPoint.proceed()).willReturn(SchedulerBatchProcessingResult.empty());

            sut.around(joinPoint, schedulerJob);

            assertThat(meterRegistry.find("scheduler.job.items").counter()).isNull();
        }
    }
}
