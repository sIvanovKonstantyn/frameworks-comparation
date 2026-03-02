package com.example.task;

import org.pragmatica.config.toml.TomlParser;
import org.pragmatica.lang.Result;

import java.nio.file.Path;

import static org.pragmatica.lang.Result.success;

public record ApplicationConfiguration(String dbUrl, String dbUser, String dbPassword, String serviceName, String zipkinUrl, String kafkaBootstrapServers, String kafkaTopic) {
    
    record ConfigError(String message) implements org.pragmatica.lang.Cause {}
    
    static Result<ApplicationConfiguration> load(String[] args) {
        if (args.length == 0) {
            return Result.failure(new ConfigError("Usage: java -jar task-service.jar <config-path>"));
        }
        
        return TomlParser.parseFile(Path.of(args[0]))
            .flatMap(toml -> {
                var url = toml.getString("database", "url").or("");
                var user = toml.getString("database", "user").or("");
                var password = toml.getString("database", "password").or("");
                var serviceName = toml.getString("tracing", "serviceName").or("task-service");
                var zipkinUrl = toml.getString("tracing", "zipkinUrl").or("http://localhost:9411/api/v2/spans");
                var kafkaBootstrapServers = toml.getString("kafka", "bootstrapServers").or("localhost:9092");
                var kafkaTopic = toml.getString("kafka", "topic").or("task-events");
                
                if (url.isEmpty()) {
                    return Result.failure(new ConfigError("Missing database.url in config"));
                }
                
                return success(new ApplicationConfiguration(url, user, password, serviceName, zipkinUrl, kafkaBootstrapServers, kafkaTopic));
            });
    }
}
