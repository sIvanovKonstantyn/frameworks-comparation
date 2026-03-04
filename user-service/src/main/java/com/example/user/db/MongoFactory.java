package com.example.user.db;

import brave.Tracer;
import com.example.user.ApplicationConfiguration;
import com.mongodb.client.MongoClients;

public class MongoFactory {

    public static MongoOperations create(ApplicationConfiguration config, Tracer tracer) {
        var client = MongoClients.create(config.mongoUri());
        var database = client.getDatabase(config.mongoDatabase());
        return new TracedMongoOperations(MongoOperations.mongoOperations(database), tracer);
    }
}
