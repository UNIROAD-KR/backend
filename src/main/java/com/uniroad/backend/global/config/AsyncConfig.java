package com.uniroad.backend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 푸시 발송처럼 응답을 기다릴 필요가 없는 작업을 요청 스레드에서 떼어낸다.
 *
 * FCM 발송은 구글로 나가는 블로킹 HTTP 호출이라, 요청 트랜잭션 안에서 돌리면
 * 그동안 DB 커넥션이 잡혀 있는다. 수신자와 기기 수만큼 왕복이 쌓이면
 * 커넥션 풀이 먼저 마른다.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String PUSH_EXECUTOR = "pushExecutor";

    @Bean(name = PUSH_EXECUTOR)
    public Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("push-");
        // 큐가 가득 차면 버리지 않고 호출한 스레드가 직접 처리한다.
        // 알림을 소리 없이 잃는 것보다 잠깐 느려지는 편이 낫다.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
}
