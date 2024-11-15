package hhplus.ecommerce.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

public class AsyncConfig {

    public static final String EVENT_ASYNC_TASK_EXECUTOR = "eventAsyncTaskExecutor";

    @Bean(name = EVENT_ASYNC_TASK_EXECUTOR)
    public TaskExecutor eventAsyncTaskExecutor() {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        float cpuUsage = 0.8f; // estimate
        float waitingTimeEfficiency = 0.5f; // estimate
        int corePoolSize = Math.round(numberOfCores * cpuUsage * (1 + waitingTimeEfficiency));
        int maxPoolSize = corePoolSize * 2;
        int awaitTerminationSeconds = 60;

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.initialize();
        return taskExecutor;
    }
}
