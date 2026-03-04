package com.example.user.db;

import brave.Tracer;
import org.bson.Document;
import org.pragmatica.lang.Functions.ThrowingFn1;
import org.pragmatica.lang.Promise;

import java.util.List;

record TracedMongoOperations(MongoOperations delegate, Tracer tracer) implements MongoOperations {

    @Override
    public <T> Promise<List<T>> findAll(String collection, ThrowingFn1<T, Document> mapper) {
        var span = tracer.nextSpan().name("mongo.findAll").tag("collection", collection).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.findAll(collection, mapper)
                           .onResult(result -> span.finish());
        }
    }

    @Override
    public Promise<Void> save(String collection, Document document) {
        var span = tracer.nextSpan().name("mongo.save").tag("collection", collection).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.save(collection, document)
                           .onResult(result -> span.finish());
        }
    }

    @Override
    public Promise<Void> updateOne(String collection, Document filter, Document update) {
        var span = tracer.nextSpan().name("mongo.updateOne").tag("collection", collection).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.updateOne(collection, filter, update)
                           .onResult(result -> span.finish());
        }
    }
}
