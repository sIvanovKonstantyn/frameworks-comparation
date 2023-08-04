package com.example.user.model;

import java.util.List;

public record UserTask(String id, String name, List<String> tasks) {
}
