package com.example.task.resources;

import com.example.task.model.Task;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TaskResource {

    // Get all tasks
    @GET
    public List<Task> getAll() {
        return List.of(new Task(1L, "", ""));
    }

    // Create a new task
    @POST
    public Response save(Task task) {

        return Response
                .created(null)
                .entity(new Task(1L, "", ""))
                .build();
    }
}