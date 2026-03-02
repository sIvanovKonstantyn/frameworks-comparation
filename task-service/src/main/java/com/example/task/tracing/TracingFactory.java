package com.example.task.tracing;

import brave.Tracing;
import brave.Tracer;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

public class TracingFactory {
    
    public static Tracer create(String serviceName, String zipkinUrl) {
        var sender = URLConnectionSender.create(zipkinUrl);
        var spanHandler = AsyncZipkinSpanHandler.create(sender);
        var tracing = Tracing.newBuilder()
            .localServiceName(serviceName)
            .addSpanHandler(spanHandler)
            .build();
        return tracing.tracer();
    }
}
