package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Configuration for asynchronous task execution and scheduled tasks using Java 21 Virtual Threads
 * Enables @Async annotation for non-blocking operations like email sending
 * Enables @Scheduled annotation for periodic tasks like SSE heartbeats
 *
 * Virtual Threads provide lightweight, scalable concurrency without the overhead of traditional thread pools
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Virtual thread executor for async email sending
     * Uses Java 21 Virtual Threads - extremely lightweight, millions can be created
     * No need for pool sizes or queue capacities
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadFactory factory = Thread.ofVirtual()
                .name("email-virtual-", 0)
                .factory();

        Executor executor = Executors.newThreadPerTaskExecutor(factory);

        log.info("Email task executor initialized with Virtual Threads (unlimited scalability)");
        return executor;
    }

    /**
     * Virtual thread executor for general async tasks
     * Uses Java 21 Virtual Threads - extremely lightweight, millions can be created
     * No need for pool sizes or queue capacities
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadFactory factory = Thread.ofVirtual()
                .name("async-virtual-", 0)
                .factory();

        Executor executor = Executors.newThreadPerTaskExecutor(factory);

        log.info("General task executor initialized with Virtual Threads (unlimited scalability)");
        return executor;
    }
}
