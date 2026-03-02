package com.example.task.event;

import org.pragmatica.lang.Result;

public interface EventPublisher {
    Result<Void> publish(Event event);
}
