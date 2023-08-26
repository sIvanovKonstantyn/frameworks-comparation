package com.example.user.repository.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private String id;
    private String name;
    private List<Long> taskIds;

    public List<Long> getTaskIds() {
        if (taskIds == null) {
            taskIds =  new ArrayList<>();
        }

        return taskIds;
    }
}
