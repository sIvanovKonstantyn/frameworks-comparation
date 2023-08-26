package com.example.user.repository;

import brave.Span;
import brave.Tracer;
import com.example.user.repository.entities.UserEntity;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {
    @ConfigProperty(name = "app.mongo.url")
    private String mongoUrl;
    @Inject
    private Tracer tracer;

    public UserEntity save(UserEntity userEntity) {
        Span trace = tracer.nextSpan().name("UserRepository.save()").start();
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> collection = getCollection(mongoClient);
            Document document = new Document();
            document.append("name", userEntity.getName());
            document.append("taskIds", userEntity.getTaskIds());

            InsertOneResult result = collection.insertOne(document);

            BsonValue id = result.getInsertedId();
            if (id != null) {
                userEntity.setId(((BsonObjectId) id).getValue().toString());
            }
        }

        trace.finish();

        return userEntity;
    }

    public UserEntity update(UserEntity userEntity) {
        Span trace = tracer.nextSpan().name("UserRepository.update()").start();
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> collection = getCollection(mongoClient);
            Document document = new Document();
            document.append("name", userEntity.getName());
            document.append("taskIds", userEntity.getTaskIds());

            collection.replaceOne(filterById(userEntity.getId()), document);
        }

        trace.finish();

        return userEntity;
    }

    public List<UserEntity> findAll() {
        Span trace = tracer.nextSpan().name("UserRepository.findAll()").start();
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> collection = getCollection(mongoClient);
            List<Document> result = new ArrayList<>();
            collection.find().into(result);

            List<UserEntity> userEntities = result.stream()
                    .map(d -> new UserEntity(
                            d.getObjectId("_id").toString(),
                            d.getString("name"),
                            d.getList("taskIds", Long.class)
                    ))
                    .toList();
            trace.finish();
            return userEntities;
        }
    }

    private MongoClient getConnection() {
        return MongoClients.create(mongoUrl);
    }

    private MongoCollection<Document> getCollection(MongoClient mongoClient) {
        MongoDatabase users = mongoClient.getDatabase("users");
        return users.getCollection("users");
    }

    public Optional<UserEntity> findById(String userId) {
        Span trace = tracer.nextSpan().name("UserRepository.findById()").start();
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> collection = getCollection(mongoClient);
            List<Document> result = new ArrayList<>();
            Bson filter = filterById(userId);
            collection.find(filter).into(result);

            Optional<UserEntity> userEntity = result.stream()
                    .map(d -> new UserEntity(
                            d.getObjectId("_id").toString(),
                            d.getString("name"),
                            d.getList("taskIds", Long.class)
                    ))
                    .findAny();
            trace.finish();
            return userEntity;
        }
    }

    private Bson filterById(String userId) {
        Bson filter = Filters.eq("_id", new ObjectId(userId));
        return filter;
    }
}
