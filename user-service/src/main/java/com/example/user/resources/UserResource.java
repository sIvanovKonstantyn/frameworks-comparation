package com.example.user.resources;

import com.example.user.model.User;
import com.example.user.model.UserTask;
import com.example.user.services.UserService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Singleton
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class UserResource {

    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    public User save(@Valid User user) {
        return userService.save(user);
    }

    @GET
    public List<User> getAll() {
        return userService.getAll();
    }


    @GET
    @Path("/tasks")
    public List<UserTask> getAllUsersTasks() {
        return userService.getAllUsersTasks();
    }
}
