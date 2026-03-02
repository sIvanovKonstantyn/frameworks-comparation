package com.example.task.db;

import com.example.task.ApplicationConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.pragmatica.jdbc.JdbcOperations;

public class JdbcFactory {
    
    public static JdbcOperations create(ApplicationConfiguration config, brave.Tracer tracer) {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.dbUrl());
        hikariConfig.setUsername(config.dbUser());
        hikariConfig.setPassword(config.dbPassword());
        var dataSource = new HikariDataSource(hikariConfig);

        return new com.example.task.db.TracedJdbcOperations(JdbcOperations.jdbcOperations(dataSource), tracer);
    }
}
