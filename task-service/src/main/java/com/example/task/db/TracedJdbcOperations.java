package com.example.task.db;

import brave.Tracer;
import org.pragmatica.jdbc.JdbcOperations;
import org.pragmatica.lang.Functions.ThrowingFn1;
import org.pragmatica.lang.Option;
import org.pragmatica.lang.Promise;

import java.sql.ResultSet;
import java.util.List;

record TracedJdbcOperations(JdbcOperations delegate, Tracer tracer) implements JdbcOperations {
    
    @Override
    public <T> Promise<T> queryOne(String sql, ThrowingFn1<T, ResultSet> mapper, Object... params) {
        var span = tracer.nextSpan().name("jdbc.queryOne").tag("sql", sql).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.queryOne(sql, mapper, params)
                .onResult(result -> span.finish());
        }
    }

    @Override
    public <T> Promise<Option<T>> queryOptional(String sql, ThrowingFn1<T, ResultSet> mapper, Object... params) {
        var span = tracer.nextSpan().name("jdbc.queryOptional").tag("sql", sql).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.queryOptional(sql, mapper, params)
                .onResult(result -> span.finish());
        }
    }

    @Override
    public <T> Promise<List<T>> queryList(String sql, ThrowingFn1<T, ResultSet> rowMapper, Object... params) {
        var span = tracer.nextSpan().name("jdbc.queryList").tag("sql", sql).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.queryList(sql, rowMapper, params)
                .onResult(result -> span.finish());
        }
    }

    @Override
    public Promise<Integer> update(String sql, Object... params) {
        var span = tracer.nextSpan().name("jdbc.update").tag("sql", sql).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.update(sql, params)
                .onResult(result -> span.finish());
        }
    }

    @Override
    public Promise<int[]> batch(String sql, List<Object[]> paramsList) {
        var span = tracer.nextSpan().name("jdbc.batch").tag("sql", sql).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return delegate.batch(sql, paramsList)
                .onResult(result -> span.finish());
        }
    }
}
