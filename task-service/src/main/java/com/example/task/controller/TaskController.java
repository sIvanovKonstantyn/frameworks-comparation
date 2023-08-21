package com.example.task.controller;

import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.entities.TaskEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskController {

    private final TaskRepository taskRepository;

    @Inject
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Get all tasks
    @GET
    public List<Task> getAll() {
        return taskRepository.findAll().stream()
                .map(taskEntity -> new Task(taskEntity.getId(), taskEntity.getDescription(), taskEntity.getUserId()))
                .toList();
    }

    // Create a new task
    @POST
    public Response save(Task task) {
        var taskEntity = new TaskEntity();
        taskEntity.setDescription(task.description());
        taskEntity.setUserId(task.userId());
        var savedEntity = taskRepository.save(taskEntity);

        return Response
                .created(null)
                .entity(new Task(savedEntity.getId(), savedEntity.getDescription(), savedEntity.getUserId()))
                .build();
    }
}
