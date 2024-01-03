package com.example.task.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
@NamedQuery(
    name = "com.example.task.repositories.entities.TaskEntity.findAll",
    query = "SELECT t FROM TaskEntity t")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String description;
    @Column
    private String userId;
}