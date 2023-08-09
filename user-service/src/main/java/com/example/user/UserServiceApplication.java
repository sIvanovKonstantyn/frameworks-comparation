package com.example.user;

import com.example.user.configuration.BundleConfiguration;
import com.example.user.configuration.BundleConfigurationFactory;
import com.example.user.configuration.UserServiceConfiguration;
import com.example.user.configuration.UserServiceConfiguration.DependencyInjectionBundle;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.List;

public class UserServiceApplication extends Application<UserServiceConfiguration> {
    private static final List<BundleConfiguration> bundleConfigurationFactories = BundleConfigurationFactory.create();

    public static void main(String[] args) throws Exception {
        new UserServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "User service";
    }

    @Override
    public void initialize(Bootstrap<UserServiceConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bundleConfigurationFactories.forEach(
                c -> bootstrap.addBundle(c.getConfiguredBundle())
        );
    }

    @Override
    public void run(UserServiceConfiguration configuration, Environment environment) {
        BundleConfigurationFactory.instantiate(bundleConfigurationFactories);
        var dependencyInjectionBundle = new DependencyInjectionBundle(bundleConfigurationFactories);
        dependencyInjectionBundle.run(configuration, environment);
    }
}
