package com.example.task.controller;

import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.entities.TaskEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@Observed(name = "taskController")
public class TaskController {

    private final TaskRepository taskRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public TaskController(TaskRepository taskRepository, KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Get all tasks
    @GetMapping
    public List<Task> getAll() {
        return taskRepository.findAll().stream()
                .map(taskEntity -> new Task(taskEntity.getId(), taskEntity.getDescription(), taskEntity.getUserId()))
                .toList();
    }

    // Create a new task
    @PostMapping
    public ResponseEntity<Task> save(@RequestBody Task task) {
        TaskEntity savedTaskEntity = new TaskEntity();
        savedTaskEntity.setDescription(task.description());
        savedTaskEntity.setUserId(task.userId());

        savedTaskEntity = taskRepository.save(savedTaskEntity);
        pushUpdateEvent(savedTaskEntity);
        return new ResponseEntity<>(
                new Task(
                        savedTaskEntity.getId(),
                        savedTaskEntity.getDescription(),
                        savedTaskEntity.getUserId()
                ),
                HttpStatus.CREATED
        );
    }

    private void pushUpdateEvent(TaskEntity task) {
        if (task.getUserId() == null) {
            return;
        }
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                "task-updates",
                null,
                null,
                null,
                prepareMessage(task)
        );
        //add traceability
        kafkaTemplate.send(producerRecord);
    }

    private String prepareMessage(TaskEntity savedTask) {
        HashMap<String, String> message = new HashMap<>();
        message.put("id", savedTask.getId().toString());
        message.put("userId", savedTask.getUserId());
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
