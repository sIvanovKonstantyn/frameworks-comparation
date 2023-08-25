package com.example.task.controller;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracer;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.entities.TaskEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@RequestScoped
public class TaskController {

    @Inject
    private TaskRepository taskRepository;

    @Inject
    @Channel("task-updates")
    private Emitter<String> taskUpdatesEmitter;

    @Inject
    private ObjectMapper mapper;

    @Inject
    private Tracer tracer;

    // Get all tasks
    @GET
    public List<Task> getAll() {

        ScopedSpan span = tracer.startScopedSpan("task-controller getAll");

        Span repoSpan = tracer.newChild(span.context()).name("task-repository findAll").start();
        List<TaskEntity> entities = taskRepository.findAll();
        repoSpan.finish();

        var tasks = entities.stream()
                .map(taskEntity -> new Task(taskEntity.getId(), taskEntity.getDescription(), taskEntity.getUserId()))
                .toList();

        span.finish();

        return tasks;
    }

    // Create a new task
    @POST
    public Response save(Task task) {
        ScopedSpan span = tracer.startScopedSpan("task-controller save");

        var taskEntity = new TaskEntity();
        taskEntity.setDescription(task.description());
        taskEntity.setUserId(task.userId());
        Span repoSpan = tracer.newChild(span.context()).name("task-repository save").start();
        var savedEntity = taskRepository.save(taskEntity);
        repoSpan.finish();

        updateUserTasks(savedEntity);

        span.finish();

        return Response
                .created(null)
                .entity(new Task(savedEntity.getId(), savedEntity.getDescription(), savedEntity.getUserId()))
                .build();
    }

    private void updateUserTasks(TaskEntity savedEntity) {
        if (savedEntity.getUserId() != null) {
            Map<String, String> message = new HashMap<>();
            message.put("userId", savedEntity.getUserId());
            message.put("id", savedEntity.getId().toString());

            try {
                taskUpdatesEmitter.send(mapper.writeValueAsString(message))
                        .thenAccept(unused -> log.info("task message sent to Kafka"));
            } catch (JsonProcessingException e) {
                log.error("Error while sending message to Kafka", e);
            }
        }
    }
}
