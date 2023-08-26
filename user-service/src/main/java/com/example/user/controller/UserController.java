package com.example.user.controller;

import com.example.user.model.User;
import com.example.user.model.UserTask;
import jakarta.enterprise.context.RequestScoped;
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

    // Get all users
    @GET
    public List<User> getAll() {
        return List.of();
    }

    @GET
    public List<UserTask> getAllUsersTasks() {
        return List.of();
    }

    // Create a new user
    @POST
    public Response save(User user) {
        return Response
                .created(null)
                .entity(user)
                .build();
    }
}
