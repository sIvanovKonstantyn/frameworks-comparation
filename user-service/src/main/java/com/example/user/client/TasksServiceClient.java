package com.example.user.client;

import com.example.user.client.dto.Task;
import feign.Headers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@FeignClient(value = "taskServiceClient",
        url = "${app.clients.task-service.url}",
        fallback = TasksServiceClient.TasksServiceFallbackImpl.class
)
@Headers("Accept: application/json")
public interface TasksServiceClient {

    @GetMapping("/tasks")
    List<Task> getAll();

    @Slf4j
    @Component
    class TasksServiceFallbackImpl implements TasksServiceClient {
        @Override
        public List<Task> getAll() {
            log.warn("Fallback method for getExchangeRates() is being used.");
            return Collections.emptyList();
        }
    }

}
