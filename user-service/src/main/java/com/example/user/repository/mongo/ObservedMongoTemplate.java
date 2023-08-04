package com.example.user.repository.mongo;

import com.mongodb.client.MongoClient;
import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mongoTemplate")
@Observed(name = "mongo")
public class ObservedMongoTemplate extends MongoTemplate {

    @Autowired
    public ObservedMongoTemplate(
            MongoClient mongoClient,
            @Value(value = "${app.mongo.database}") String databaseName) {
        super(mongoClient, databaseName);
    }

    @Override
    public <T> T insert(T objectToSave, String collectionName) {
        return super.insert(objectToSave, collectionName);
    }

    @Override
    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
        return super.find(query, entityClass, collectionName);
    }
}
