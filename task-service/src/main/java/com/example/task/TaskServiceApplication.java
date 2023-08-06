package com.example.task;

import com.example.task.configuration.BundleConfigurationFactory;
import com.example.task.configuration.BundleConfiguration;
import com.example.task.configuration.TaskServiceConfiguration;
import com.example.task.configuration.TaskServiceConfiguration.DependencyInjectionBundle;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.List;

public class TaskServiceApplication extends Application<TaskServiceConfiguration> {
    private static final List<BundleConfiguration> bundleConfigurationFactories = BundleConfigurationFactory.create();

    public static void main(String[] args) throws Exception {
        new TaskServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "Task service";
    }

    @Override
    public void initialize(Bootstrap<TaskServiceConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bundleConfigurationFactories.forEach(
                c ->  bootstrap.addBundle(c.getConfiguredBundle())
        );
    }

    @Override
    public void run(TaskServiceConfiguration configuration, Environment environment) {
        BundleConfigurationFactory.instantiate(bundleConfigurationFactories);
        var dependencyInjectionBundle = new DependencyInjectionBundle(bundleConfigurationFactories);
        dependencyInjectionBundle.run(configuration, environment);
    }
}
