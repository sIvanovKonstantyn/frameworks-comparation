package com.example.task;

import com.example.task.event.Event;
import com.example.task.event.EventPublisher;
import org.pragmatica.jdbc.JdbcOperations;
import org.pragmatica.json.JsonMapper;
import org.pragmatica.lang.Promise;
import org.pragmatica.lang.Result;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public interface TaskSlice {
    record CreateTaskRequest(String description, String userId) {}

    record Task(String id, String description, String userId) {}

    Promise<Result<Task>> createTask(CreateTaskRequest request);
    
    Promise<Result<List<Task>>> getAllTasks();

    static TaskSlice taskSlice(JdbcOperations jdbc, EventPublisher eventPublisher) {
        var jsonMapper = JsonMapper.defaultJsonMapper();
        
        return new TaskSlice() {
            @Override
            public Promise<Result<Task>> createTask(CreateTaskRequest request) {
                var id = generateId(request.description(), request.userId());
                return jdbc.update(
                    "INSERT INTO tasks (id, description, userId) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    id, request.description(), request.userId()
                ).map(i -> {
                    var task = new Task(id, request.description(), request.userId());
                    return Result.success(task);
                })
                .onSuccess(result -> {
                    result.onSuccess(this::publishTaskCreated);
                });
            }

            @Override
            public Promise<Result<List<Task>>> getAllTasks() {
                return jdbc.queryList(
                    "SELECT id, description, userId FROM tasks",
                    rs -> new Task(rs.getString("id"), rs.getString("description"), rs.getString("userId"))
                ).map(Result::success);
            }
            
            private void publishTaskCreated(Task task) {
                jsonMapper.writeAsString(task)
                    .flatMap(payload -> eventPublisher.publish(new Event(task.id(), payload)))
                    .onFailure(cause -> System.err.println("Failed to publish event: " + cause.message()));
            }
        };
    }

    private static String generateId(String description, String userId) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest((description + userId).getBytes(StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 64);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ID", e);
        }
    }
}
