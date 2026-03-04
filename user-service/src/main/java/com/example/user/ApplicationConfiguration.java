package com.example.user;

import org.pragmatica.config.toml.TomlParser;
import org.pragmatica.lang.Result;

import java.nio.file.Path;

import static org.pragmatica.lang.Result.success;

public record ApplicationConfiguration(String serviceName, String zipkinUrl, String mongoUri, String mongoDatabase,
                                       String kafkaBootstrapServers, String kafkaTopic, String taskServiceUrl) {

    record ConfigError(String message) implements org.pragmatica.lang.Cause {}

    static Result<ApplicationConfiguration> load(String[] args) {
        if (args.length == 0) {
            return Result.failure(new ConfigError("Usage: java -jar user-service.jar <config-path>"));
        }

        return TomlParser.parseFile(Path.of(args[0]))
            .flatMap(toml -> {
                var serviceName = toml.getString("tracing", "serviceName").or("user-service");
                var zipkinUrl = toml.getString("tracing", "zipkinUrl").or("http://localhost:9411/api/v2/spans");
                var mongoUri = toml.getString("mongo", "uri").or("mongodb://localhost:27017");
                var mongoDatabase = toml.getString("mongo", "database").or("users");
                var kafkaBootstrapServers = toml.getString("kafka", "bootstrapServers").or("localhost:9092");
                var kafkaTopic = toml.getString("kafka", "topic").or("task-events");
                var taskServiceUrl = toml.getString("taskService", "url").or("http://localhost:8080");

                return success(new ApplicationConfiguration(serviceName, zipkinUrl, mongoUri, mongoDatabase,
                    kafkaBootstrapServers, kafkaTopic, taskServiceUrl));
            });
    }
}
