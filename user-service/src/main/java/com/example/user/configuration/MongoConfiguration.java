package com.example.user.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.user.repository")
public class MongoConfiguration {

    @Value(value = "${app.mongo.database}")
    private String databaseName;

    @Value(value = "${app.mongo.uri}")
    private String databaseUrl;

    @Bean
    public MongoClient mongo() {
        ConnectionString connectionString = new ConnectionString(databaseUrl + "/" + databaseName);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }
}
