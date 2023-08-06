package com.example.task.resources;

import com.example.task.model.Task;
import com.example.task.repositories.TaskRepository;
import com.example.task.repositories.entities.TaskEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Singleton
@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class TaskResource {

    private final TaskRepository taskRepository;
    private final Producer<String, String> producer;
    private final ObjectMapper objectMapper;

    @Inject
    public TaskResource(TaskRepository taskRepository, Producer producer, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    @POST
    @UnitOfWork
    public Task save(@Valid Task task) {
        TaskEntity savedTaskEntity = new TaskEntity();
        savedTaskEntity.setDescription(task.description());
        savedTaskEntity.setUserId(task.userId());

        savedTaskEntity = taskRepository.save(savedTaskEntity);
        sendUpdateMessage(savedTaskEntity);

        return new Task(savedTaskEntity.getId(), savedTaskEntity.getDescription(), savedTaskEntity.getUserId());
    }

    private void sendUpdateMessage(TaskEntity taskEntity) {
        if (taskEntity.getUserId() == null) {
            return;
        }
        Map<String, Object> message = Map.of(
                "userId", taskEntity.getUserId(),
                "id", taskEntity.getId()
        );

        try {
            producer.send(
                    new ProducerRecord<>(
                            "task-updates",
                            objectMapper.writeValueAsString(message)
                    )
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to send message to Kafka", e);
        }
    }

    @GET
    @UnitOfWork
    public List<Task> getAll() {
        return taskRepository.findAll().stream()
                .map(taskEntity -> new Task(taskEntity.getId(), taskEntity.getDescription(), taskEntity.getUserId()))
                .toList();
    }
}
