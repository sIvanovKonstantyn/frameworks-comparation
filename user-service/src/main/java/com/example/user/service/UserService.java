package com.example.user.service;

import com.example.user.model.User;
import com.example.user.model.UserTask;
import com.example.user.repository.UserRepository;
import com.example.user.repository.entities.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userEntity -> new User(userEntity.getId(), userEntity.getName(), userEntity.getTaskIds()))
                .toList();
    }

    public List<UserTask> getAllUsersTasks() {
        return List.of();
    }

    public User save(User user) {
        UserEntity savedUser = userRepository.save(new UserEntity(user.id(), user.name(), user.taskIds()));
        return new User(savedUser.getId(), savedUser.getName(), savedUser.getTaskIds());
    }

    public void updateTaskList(String userId, String taskId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        userEntity.getTaskIds().add(Long.parseLong(taskId));
        userRepository.update(userEntity);
    }
}
