package com.example.task.controller;

import com.example.task.model.Task;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskController {

    // Get all tasks
    @GET
    public List<Task> getAll() {
        return List.of();
    }

    // Create a new task
    @POST
    public Response save(Task task) {
        return Response.created(null).entity(task).build();
    }
}
