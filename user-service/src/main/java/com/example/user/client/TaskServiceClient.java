package com.example.user.client;

import com.example.user.client.entities.Task;
import feign.RequestLine;

import java.util.List;

public interface TaskServiceClient {
    @RequestLine("GET /tasks")
    List<Task> getAll();
}
