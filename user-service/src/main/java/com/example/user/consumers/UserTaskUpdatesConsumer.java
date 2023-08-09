package com.example.user.consumers;

import brave.Span;
import brave.Tracer;
import com.example.user.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Singleton
@Slf4j
public class UserTaskUpdatesConsumer implements Managed {
    private final Consumer<String, String> consumer;
    private final UserService userService;
    private final ObjectMapper mapper;
    private final Tracer tracer;

    private volatile boolean running;

    public UserTaskUpdatesConsumer(Consumer<String, String> consumer, UserService userService, ObjectMapper mapper, Tracer tracer) {
        this.consumer = consumer;
        this.userService = userService;
        this.mapper = mapper;
        this.tracer = tracer;
    }

    @Override
    public void start() {
        consumer.subscribe(List.of("task-updates"));
        running = true;
        new Thread(
                () -> {
                    while (running) {
                        if (userService == null) {
                            continue;
                        }
                        ConsumerRecords<String, String> records =
                                consumer.poll(Duration.ofMillis(100));

                        for (ConsumerRecord<String, String> record : records) {

                            try {
                                Span trace = tracer.nextSpan().name("UserTaskUpdatesConsumer.consumeOne()").start();
                                Map<String, Object> messageValues = mapper.readValue(record.value(), Map.class);
                                userService.updateTaskList((String)messageValues.get("userId"), (Integer)messageValues.get("id"));
                                trace.finish();

                            } catch (JsonProcessingException e) {
                                log.warn("Failed to parse message: {}", record.value());
                            }
                        }
                    }
                }
        ).start();
    }
}
