package com.example.user.tracing;

import brave.Span;
import brave.Tracer;
import org.pragmatica.lang.Functions.Fn1;
import org.pragmatica.lang.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface ResultTracing {
    
    <T, R> Fn1<Result<R>, T> around(Fn1<Result<R>, T> fn);
    
    <R> Supplier<Result<R>> around(Supplier<Result<R>> supplier);
    
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
        
        public ResultTracing build() {
            return new SpanTracing(name, tracer, tags);
        }
    }
    
    record SpanTracing(String name, Tracer tracer, Map<String, String> tags) implements ResultTracing {
        @Override
        public <T, R> Fn1<Result<R>, T> around(Fn1<Result<R>, T> fn) {
            return input -> {
                var span = tracer.nextSpan().name(name).start();
                tags.forEach(span::tag);
                var result = fn.apply(input);
                finishSpan(span, result);
                return result;
            };
        }
        
        @Override
        public <R> Supplier<Result<R>> around(Supplier<Result<R>> supplier) {
            return () -> {
                var span = tracer.nextSpan().name(name).start();
                tags.forEach(span::tag);
                var result = supplier.get();
                finishSpan(span, result);
                return result;
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
