package com.example.user.event;

import brave.Tracer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public class KafkaEventConsumer {
    private final KafkaConsumer<String, String> consumer;
    private final Tracer tracer;
    private volatile boolean running = true;

    public KafkaEventConsumer(String bootstrapServers, String topic, String groupId, Tracer tracer) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(List.of(topic));
        this.tracer = tracer;
    }

    public void start(Consumer<String> handler) {
        Thread.ofVirtual().start(() -> {
            while (running) {
                var records = consumer.poll(Duration.ofMillis(100));
                for (var record : records) {
                    var span = tracer.nextSpan().name("kafka.consume").tag("topic", record.topic()).start();
                    try (var scope = tracer.withSpanInScope(span)) {
                        handler.accept(record.value());
                    } finally {
                        span.finish();
                    }
                }
            }
            consumer.close();
        });
    }

    public void stop() {
        running = false;
    }
}
