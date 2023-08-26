package com.example.user.controller;

import brave.ScopedSpan;
import brave.Tracer;
import com.example.user.model.User;
import com.example.user.model.UserTask;
import com.example.user.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@RequestScoped
public class UserController {

    @Inject
    private UserService userService;

    @Inject
    private Tracer tracer;

    @GET
    public List<User> getAll() {
        ScopedSpan span = tracer.startScopedSpan("user-controller getAll");
        List<User> users = userService.getAll();
        span.finish();
        return users;
    }

    @GET
    public List<UserTask> getAllUsersTasks() {
        ScopedSpan span = tracer.startScopedSpan("user-controller getAllUsersTasks");
        List<UserTask> userTasks = List.of();
        span.finish();
        return userTasks;
    }

    // Create a new user
    @POST
    public Response save(User user) {
        ScopedSpan span = tracer.startScopedSpan("user-controller save");
        User savedUser = userService.save(user);
        span.finish();
        return Response
                .created(null)
                .entity(savedUser)
                .build();
    }
}
