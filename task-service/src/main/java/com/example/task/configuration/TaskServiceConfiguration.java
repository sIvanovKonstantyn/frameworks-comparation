package com.example.task.configuration;

import java.io.IOException;
import java.util.Properties;

import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.sampler.Sampler;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class TaskServiceConfiguration {

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(TaskServiceConfiguration.class.getClassLoader()
                .getResourceAsStream("application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Tracer tracer() {
        OkHttpSender sender = OkHttpSender.create(PROPERTIES.getProperty("tracer-url"));
        SpanHandler handler = AsyncZipkinSpanHandler.create(sender);

        // Initialize Brave tracing
        Tracing tracing = Tracing.newBuilder()
            .localServiceName("task-service")
            .sampler(Sampler.ALWAYS_SAMPLE)
            .propagationFactory(B3Propagation.FACTORY)
            .addSpanHandler(handler)
            .build();

        return tracing.tracer();
    }

    public static Properties kafkaConnectionProperties() {
        return PROPERTIES;
    }
}
