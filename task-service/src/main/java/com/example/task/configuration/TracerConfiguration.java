package com.example.task.configuration;

import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.sampler.Sampler;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Dependent
public class TracerConfiguration {

    @ConfigProperty(name = "app.zipkin.url")
    private String zipkinUrl;

    @Produces
    public Tracer tracer() {
        OkHttpSender sender = OkHttpSender.create(zipkinUrl);
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
}
