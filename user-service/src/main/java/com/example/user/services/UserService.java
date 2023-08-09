package com.example.user.services;

import com.example.user.client.TaskServiceClient;
import com.example.user.client.entities.Task;
import com.example.user.model.User;
import com.example.user.model.UserTask;
import com.example.user.repositories.UserRepository;
import com.example.user.repositories.entities.UserEntity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class UserService {
    private final UserRepository userRepository;
    private final TaskServiceClient taskServiceClient;

    @Inject
    public UserService(UserRepository userRepository, TaskServiceClient taskServiceClient) {
        this.userRepository = userRepository;
        this.taskServiceClient = taskServiceClient;
    }

    public List<User> getAll() {
        return userRepository.findAll().stream()
                .map(e -> new User(e.getId(), e.getName(), e.getTaskIds()))
                .toList();
    }

    public User save(User user) {
        UserEntity savedUserEntity = new UserEntity(null, user.name(), user.taskIds());
        savedUserEntity = userRepository.save(savedUserEntity);

        return new User(
                savedUserEntity.getId(),
                savedUserEntity.getName(),
                savedUserEntity.getTaskIds()
        );
    }

    public void updateTaskList(String userId, Integer taskId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        userEntity.getTaskIds().add(taskId.longValue());
        userRepository.update(userEntity);
    }

    public List<UserTask> getAllUsersTasks() {
        List<User> users = getAll();
        List<Task> tasks = taskServiceClient.getAll();

        return users.stream()
                .map(user -> new UserTask(
                                user.id(),
                                user.name(),
                                tasks.stream()
                                        .filter(task -> user.taskIds().contains(task.id()))
                                        .map(Task::description)
                                        .toList()
                        )
                )
                .toList();
    }
}
