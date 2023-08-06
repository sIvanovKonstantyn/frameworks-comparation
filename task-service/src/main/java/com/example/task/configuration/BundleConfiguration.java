package com.example.task.configuration;

import io.dropwizard.ConfiguredBundle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class BundleConfiguration<T> {
    private final ConfiguredBundle<TaskServiceConfiguration>  configuredBundle;
    private final Class clazz;
    @Setter
    private T instance;
}
