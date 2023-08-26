package com.example.user.configuration;

import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.sampler.Sampler;
import com.example.user.client.TaskServiceClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Dependent
public class UserServiceConfiguration {

    @ConfigProperty(name = "app.zipkin.url")
    private String zipkinUrl;

    @ConfigProperty(name = "app.task-service.url")
    private String taskServiceUrl;

    @Produces
    public TaskServiceClient taskServiceClient() {
        return Feign.builder()
                .decoder(new GsonDecoder())
                .target(TaskServiceClient.class, taskServiceUrl);
    }

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
