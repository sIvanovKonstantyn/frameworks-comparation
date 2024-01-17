package com.example.user.repositories.entities;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class UserEntity {
    private String id;
    private String name;
    private List<Long> taskIds;

    public List<Long> getTaskIds() {
        if (taskIds == null) {
            taskIds = new ArrayList<>();
        }
        return taskIds;
    }
}