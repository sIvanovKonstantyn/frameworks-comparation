package com.example.task.tracing;

import brave.Span;
import brave.Tracer;
import org.pragmatica.lang.Functions.Fn1;
import org.pragmatica.lang.Promise;
import org.pragmatica.lang.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface PromiseTracing {
    
    <T, R> Fn1<Promise<R>, T> around(Fn1<Promise<R>, T> fn);
    
    <R> Supplier<Promise<R>> around(Supplier<Promise<R>> supplier);
    
    static SpanStageTracer span(String name) {
        return tracer -> new SpanStageTags(name, tracer);
    }
    
    interface SpanStageTracer {
        SpanStageTags tracer(Tracer tracer);
    }
    
    final class SpanStageTags {
        private final String name;
        private final Tracer tracer;
        private final Map<String, String> tags = new HashMap<>();
        
        private SpanStageTags(String name, Tracer tracer) {
            this.name = name;
            this.tracer = tracer;
        }
        
        public SpanStageTags tag(String key, String value) {
            tags.put(key, value);
            return this;
        }
        
        public PromiseTracing build() {
            return new SpanTracing(name, tracer, tags);
        }
    }
    
    record SpanTracing(String name, Tracer tracer, Map<String, String> tags) implements PromiseTracing {
        @Override
        public <T, R> Fn1<Promise<R>, T> around(Fn1<Promise<R>, T> fn) {
            return input -> {
                var span = tracer.nextSpan().name(name).start();
                tags.forEach(span::tag);
                return fn.apply(input)
                         .onResult(result -> finishSpan(span, result));
            };
        }
        
        @Override
        public <R> Supplier<Promise<R>> around(Supplier<Promise<R>> supplier) {
            return () -> {
                var span = tracer.nextSpan().name(name).start();
                tags.forEach(span::tag);
                return supplier.get()
                               .onResult(result -> finishSpan(span, result));
            };
        }
        
        private <R> void finishSpan(Span span, Result<R> result) {
            switch (result) {
                case Result.Success<R> ignored -> span.tag("result", "success");
                case Result.Failure<R> failure -> {
                    span.tag("result", "failure");
                    span.tag("error", failure.cause().message());
                }
            }
            span.finish();
        }
    }
}
