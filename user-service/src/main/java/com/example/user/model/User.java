package com.example.user.model;

import java.util.List;

public record User(String id, String name, List<Long> taskIds) {
}
