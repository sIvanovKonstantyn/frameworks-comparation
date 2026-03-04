package com.example.user.db;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.pragmatica.lang.Functions.ThrowingFn1;
import org.pragmatica.lang.Promise;
import org.pragmatica.lang.utils.Causes;

import java.util.ArrayList;
import java.util.List;

public interface MongoOperations {

    <T> Promise<List<T>> findAll(String collection, ThrowingFn1<T, Document> mapper);

    Promise<Void> save(String collection, Document document);

    Promise<Void> updateOne(String collection, Document filter, Document update);

    static MongoOperations mongoOperations(MongoDatabase database) {
        return new MongoOperations() {
            @Override
            public <T> Promise<List<T>> findAll(String collection, ThrowingFn1<T, Document> mapper) {
                return Promise.promise(promise -> {
                    try {
                        var result = new ArrayList<T>();
                        for (var doc : database.getCollection(collection).find()) {
                            result.add(mapper.apply(doc));
                        }
                        promise.succeed(result);
                    } catch (Throwable e) {
                        promise.fail(Causes.fromThrowable(e));
                    }
                });
            }

            @Override
            public Promise<Void> save(String collection, Document document) {
                return Promise.promise(promise -> {
                    try {
                        database.getCollection(collection).insertOne(document);
                        promise.succeed(null);
                    } catch (Throwable e) {
                        promise.fail(Causes.fromThrowable(e));
                    }
                });
            }

            @Override
            public Promise<Void> updateOne(String collection, Document filter, Document update) {
                return Promise.promise(promise -> {
                    try {
                        database.getCollection(collection).updateOne(filter, update);
                        promise.succeed(null);
                    } catch (Throwable e) {
                        promise.fail(Causes.fromThrowable(e));
                    }
                });
            }
        };
    }
}
