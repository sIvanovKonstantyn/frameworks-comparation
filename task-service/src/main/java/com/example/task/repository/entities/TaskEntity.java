package com.example.task.repository.entities;

import lombok.Data;

@Data
public class TaskEntity {
    private Long id;
    private String description;
    private String userId;
}
