package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution
 * Enables @Async annotation for non-blocking operations like email sending
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Thread pool executor for async email sending
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-async-");
        executor.setRejectedExecutionHandler((r, executor1) ->
                log.warn("Email task rejected - queue full. Task: {}", r.toString())
        );
        executor.initialize();

        log.info("Email task executor initialized: corePoolSize=2, maxPoolSize=5, queueCapacity=100");
        return executor;
    }

    /**
     * Thread pool executor for general async tasks
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler((r, executor1) ->
                log.warn("Async task rejected - queue full. Task: {}", r.toString())
        );
        executor.initialize();

        log.info("General task executor initialized: corePoolSize=5, maxPoolSize=10, queueCapacity=200");
        return executor;
    }
}
