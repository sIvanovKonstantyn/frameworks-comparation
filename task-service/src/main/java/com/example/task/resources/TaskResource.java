package com.example.task.resources;

import java.util.List;
import java.util.stream.Collectors;

import com.example.task.configuration.TaskServiceConfiguration;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.entities.TaskEntity;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.sampler.Sampler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TaskResource {

    private final Tracer tracer = TaskServiceConfiguration.tracer();
    @Inject
    private TaskRepository taskRepository;


    // Get all tasks
    @GET
    public List<Task> getAll() {
        ScopedSpan scopedSpan = tracer.startScopedSpan("task-resource getAll");
        List<Task> result = taskRepository.findAll()
            .stream()
            .map(t -> new Task(t.getId(), t.getDescription(), t.getUserId()))
            .collect(Collectors.toList());
        scopedSpan.finish();
        return result;
    }

    // Create a new task
    @POST
    public Response save(Task task) {
        ScopedSpan scopedSpan = tracer.startScopedSpan("task-resource save");
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setDescription(task.description());
        taskEntity.setUserId(task.userId());
        taskEntity = taskRepository.save(taskEntity);
        scopedSpan.finish();

        return Response.created(null)
            .entity(new Task(taskEntity.getId(), taskEntity.getDescription(), taskEntity.getUserId()))
            .build();
    }
}