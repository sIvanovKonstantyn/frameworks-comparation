package com.example.user.controller;

import com.example.user.model.User;
import com.example.user.model.UserTask;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users
    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/tasks")
    public List<UserTask> getAllUsersTasks() {
        return userService.getAllUsersTasks();
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<User> save(@RequestBody User user) {
        return new ResponseEntity<>(
                userService.save(user),
                HttpStatus.CREATED
        );
    }
}
