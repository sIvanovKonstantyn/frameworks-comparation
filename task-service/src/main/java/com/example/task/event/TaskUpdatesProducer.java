package com.example.task.event;

//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//
//import org.eclipse.microprofile.reactive.messaging.Outgoing;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.smallrye.reactive.messaging.kafka.KafkaMessage;
//import jakarta.enterprise.context.ApplicationScoped;

import static com.example.task.configuration.TaskServiceConfiguration.kafkaConnectionProperties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

@ApplicationScoped
public class TaskUpdatesProducer {

    @SneakyThrows
    public void send(String message) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaConnectionProperties())) {
            producer.send(new ProducerRecord<>("task-updates", message))
                .get();
        }
    }
}
