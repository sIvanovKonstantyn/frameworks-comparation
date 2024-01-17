package com.example.user.resources;

import java.util.List;

import com.example.user.model.User;
import com.example.user.repositories.UserRepository;
import com.example.user.repositories.entities.UserEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserResource {

    @Inject
    private UserRepository userRepository;

    @POST
    public User save(User user) {
        UserEntity savedUserEntity = new UserEntity(null, user.name(), user.taskIds());
        savedUserEntity = userRepository.save(savedUserEntity);

        return new User(
            savedUserEntity.getId(),
            savedUserEntity.getName(),
            savedUserEntity.getTaskIds()
        );
    }

    @GET
    public List<User> getAll() {
        return userRepository.findAll().stream()
            .map(e -> new User(e.getId(), e.getName(), e.getTaskIds()))
            .toList();
    }
}