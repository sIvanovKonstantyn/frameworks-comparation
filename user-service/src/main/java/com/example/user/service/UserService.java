package com.example.user.service;

import com.example.user.client.TasksServiceClient;
import com.example.user.client.dto.Task;
import com.example.user.model.User;
import com.example.user.model.UserTask;
import com.example.user.repository.UserRepository;
import com.example.user.repository.entities.UserEntity;
import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Observed(name = "userService")
public class UserService {

    private final UserRepository userRepository;
    private final TasksServiceClient tasksServiceClient;


    @Autowired
    public UserService(UserRepository userRepository, TasksServiceClient tasksServiceClient) {
        this.userRepository = userRepository;
        this.tasksServiceClient = tasksServiceClient;
    }

    public List<User> getAll() {
        return userRepository.findAll().stream()
                .map(e -> new User(e.getId(), e.getName(), e.getTaskIds()))
                .toList();
    }

    public User save(User user) {
        UserEntity savedUserEntity = new UserEntity();
        savedUserEntity.setName(user.name());
        savedUserEntity.setTaskIds(user.taskIds());

        savedUserEntity = userRepository.save(savedUserEntity);

        return new User(
                savedUserEntity.getId(),
                savedUserEntity.getName(),
                savedUserEntity.getTaskIds()
        );
    }

    public void updateTaskList(String userId, String taskId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        userEntity.getTaskIds().add(Long.parseLong(taskId));
        userRepository.save(userEntity);
    }

    public List<UserTask> getAllUsersTasks() {
        List<User> users = getAll();
        List<Task> tasks = tasksServiceClient.getAll();

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
