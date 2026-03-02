package com.example.task.event;

import brave.Tracer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.pragmatica.lang.Result;

public class KafkaEventPublisher implements EventPublisher {
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final Tracer tracer;

    public KafkaEventPublisher(KafkaProducer<String, String> producer, String topic, Tracer tracer) {
        this.producer = producer;
        this.topic = topic;
        this.tracer = tracer;
    }

    @Override
    public Result<Void> publish(Event event) {
        var span = tracer.nextSpan().name("kafka-publish").tag("topic", topic).start();
        try (var scope = tracer.withSpanInScope(span)) {
            producer.send(new ProducerRecord<>(topic, event.key(), event.payload()));
            return Result.success(null);
        } catch (Exception e) {
            span.error(e);
            return Result.failure(new KafkaPublishError(e.getMessage()));
        } finally {
            span.finish();
        }
    }

    record KafkaPublishError(String message) implements org.pragmatica.lang.Cause {}

    public void close() {
        producer.close();
    }
}
