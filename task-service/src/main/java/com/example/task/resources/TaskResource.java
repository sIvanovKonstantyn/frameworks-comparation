package com.example.task.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.task.configuration.TaskServiceConfiguration;
import com.example.task.event.TaskUpdatesProducer;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.entities.TaskEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import brave.ScopedSpan;
import brave.Tracer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TaskResource {

    private static final Logger log = LoggerFactory.getLogger(TaskResource.class.getName());

    private final Tracer tracer = TaskServiceConfiguration.tracer();
    @Inject
    private TaskRepository taskRepository;
    @Inject
    private TaskUpdatesProducer taskUpdatesProducer;

    private ObjectMapper mapper = new ObjectMapper();

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
        updateUserTasks(taskEntity);
        scopedSpan.finish();

        return Response.created(null)
            .entity(new Task(taskEntity.getId(), taskEntity.getDescription(), taskEntity.getUserId()))
            .build();
    }

    private void updateUserTasks(TaskEntity savedEntity) {
        if (savedEntity.getUserId() != null) {
            Map<String, String> message = new HashMap<>();
            message.put("userId", savedEntity.getUserId());
            message.put("id", savedEntity.getId()
                .toString());

            try {
                taskUpdatesProducer.send(mapper.writeValueAsString(message));
            } catch (Exception e) {
                log.error("Error while sending message to Kafka", e);
            }
        }
    }
}