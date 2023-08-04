package com.example.user.repository.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
public class UserEntity {
    @Id
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
