package com.example.user.messaging.consumer;

import com.example.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.Map;
import java.util.NoSuchElementException;

@ApplicationScoped
@Slf4j
public class UserTasksConsumer {
    @Inject
    private UserService userService;

    @Inject
    private ObjectMapper mapper;

    @Incoming("task-updates")
    public void consume(String message) throws JsonProcessingException {
        log.info("Received message: {}", message);
        Map<String, String> messageValues = mapper.readValue(message, Map.class);
        try {
            userService.updateTaskList(messageValues.get("userId"), messageValues.get("id"));
        } catch (NoSuchElementException e) {
            log.warn("User with id {} not found", messageValues.get("userId"));
        }
    }
}
