package com.example.task.repository.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class TaskEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private String userId;
}
