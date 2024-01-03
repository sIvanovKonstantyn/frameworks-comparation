package com.example.task.repository;

import java.util.List;

import com.example.task.repository.entities.TaskEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class TaskRepository {

    private EntityManagerFactory factory;

    public TaskRepository() {
        factory = Persistence.createEntityManagerFactory("defaultPU");
    }

    public TaskEntity save(TaskEntity taskEntity) {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = factory.createEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(taskEntity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }

        return taskEntity;
    }

    public List<TaskEntity> findAll() {
        try (EntityManager entityManager = factory.createEntityManager()) {
            TypedQuery<TaskEntity> query = entityManager
                .createNamedQuery("com.example.task.repositories.entities.TaskEntity.findAll",
                    TaskEntity.class);
            return query.getResultList();
        }
    }
}
