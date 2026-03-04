package com.example.user;

import com.example.user.db.MongoOperations;
import org.bson.Document;
import org.pragmatica.http.HttpOperations;
import org.pragmatica.json.JsonMapper;
import org.pragmatica.lang.Promise;
import org.pragmatica.lang.Result;
import org.pragmatica.lang.type.TypeToken;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface UserSlice {
    record CreateUserRequest(String name) {}

    record User(String id, String name, List<String> taskIds) {}

    record Task(String id, String description, String userId) {}

    record UserWithTasks(String id, String name, List<Task> tasks) {}

    record UserTasksUpdateEvent(String id, String description, String userId) {}

    Promise<Result<User>> createUser(CreateUserRequest request);

    Promise<Result<List<User>>> getAllUsers();

    Promise<Result<List<UserWithTasks>>> getUsersWithTasks();

    void updateUserTasks(UserTasksUpdateEvent event);

    static UserSlice userSlice(MongoOperations mongo, HttpOperations http, String taskServiceUrl) {
        var jsonMapper = JsonMapper.defaultJsonMapper();

        return new UserSlice() {
            private static final String COLLECTION = "users";

            @Override
            public Promise<Result<User>> createUser(CreateUserRequest request) {
                var user = new User(generateId(request.name()), request.name(), List.of());
                var doc = new Document("_id", user.id())
                    .append("name", user.name())
                    .append("taskIds", user.taskIds());
                return mongo.save(COLLECTION, doc)
                            .map(_ -> Result.success(user));
            }

            @Override
            public Promise<Result<List<User>>> getAllUsers() {
                return mongo.findAll(COLLECTION, UserSlice::fromDocument)
                            .map(Result::success);
            }

            @Override
            public Promise<Result<List<UserWithTasks>>> getUsersWithTasks() {
                var usersPromise = mongo.findAll(COLLECTION, UserSlice::fromDocument);
                var tasksPromise = fetchAllTasks();

                return usersPromise
                  .flatMap(users ->
                      tasksPromise.map(tasksResult ->
                        tasksResult.map(tasks -> merge(users, tasks)))
                  );
            }

            private List<UserWithTasks> merge(List<User> users, List<Task> tasks) {
                var tasksByUser = tasks.stream()
                        .collect(Collectors.groupingBy(Task::userId));

                return users.stream()
                        .map(u -> new UserWithTasks(
                                u.id(),
                                u.name(),
                                tasksByUser.getOrDefault(u.id(), List.of())
                        ))
                        .toList();
            }

            private Promise<Result<List<Task>>> fetchAllTasks() {
                var request = HttpRequest.newBuilder()
                    .uri(URI.create(taskServiceUrl + "/api/v1/tasks"))
                    .GET()
                    .build();
                return http.sendString(request)
                           .map(response -> response.toResult()
                               .flatMap(body -> jsonMapper.readString(body, new TypeToken<>() {
                               })));
            }

            @Override
            public void updateUserTasks(UserTasksUpdateEvent event) {
                var filter = new Document("_id", event.userId());
                var update = new Document("$push", new Document("taskIds", event.id()));
                mongo.updateOne(COLLECTION, filter, update)
                     .onFailure(cause -> System.err.println("Failed to update user tasks: " + cause.message()));
            }
        };
    }

    private static User fromDocument(Document doc) {
        return new User(
            doc.getString("_id"),
            doc.getString("name"),
            doc.getList("taskIds", String.class)
        );
    }

    private static String generateId(String name) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ID", e);
        }
    }
}
