package com.fundquest.auth.audit_trail.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for audit trail system
 * Enables AOP and async processing for audit logging
 */
@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@Slf4j
public class AuditConfig {

    /**
     * Dedicated thread pool for audit logging
     * Separate from main application thread pool to avoid blocking business operations
     */
    @Bean("auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum number of threads
        executor.setCorePoolSize(2);

        // Maximum pool size - maximum number of threads
        executor.setMaxPoolSize(5);

        // Queue capacity - number of tasks to queue when all threads are busy
        executor.setQueueCapacity(100);

        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("Audit-");

        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);

        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Rejection policy - what to do when queue is full
        executor.setRejectedExecutionHandler((runnable, threadPoolExecutor) -> {
            log.warn("Audit task rejected, queue is full. Task: {}", runnable);
            // Run in calling thread as fallback
            if (!threadPoolExecutor.isShutdown()) {
                runnable.run();
            }
        });

        executor.initialize();

        log.info("Audit executor configured with core size: {}, max size: {}, queue capacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
