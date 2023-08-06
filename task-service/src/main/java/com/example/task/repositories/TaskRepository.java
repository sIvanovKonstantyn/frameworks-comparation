package com.example.task.repositories;

import brave.Span;
import brave.Tracer;
import com.example.task.repositories.entities.TaskEntity;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;


@Singleton
public class TaskRepository extends AbstractDAO<TaskEntity> {
    private final Tracer tracer;

    @Inject
    public TaskRepository(SessionFactory sessionFactory, Tracer tracer) {
        super(sessionFactory);
        this.tracer = tracer;
    }

    public TaskEntity save(TaskEntity taskEntity) {
        Span trace = tracer.nextSpan().name("TaskRepository.save()").start();
        TaskEntity persist = persist(taskEntity);
        trace.finish();
        return persist;
    }

    public List<TaskEntity> findAll() {
        Span trace = tracer.nextSpan().name("TaskRepository.findAll()").start();
        List<TaskEntity> list = list(namedTypedQuery("com.example.task.repositories.entities.TaskEntity.findAll"));
        trace.finish();
        return list;
    }
}
