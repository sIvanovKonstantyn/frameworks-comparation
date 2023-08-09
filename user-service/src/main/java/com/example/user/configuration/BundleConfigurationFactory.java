package com.example.user.configuration;

import brave.Tracer;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;

import java.util.List;

public class BundleConfigurationFactory {
    private static final ZipkinBundle<UserServiceConfiguration> zipkinBundle = new ZipkinBundle<>("User service") {
        @Override
        public ZipkinFactory getZipkinFactory(UserServiceConfiguration configuration) {
            return configuration.getZipkinFactory();
        }
    };

    public static List<BundleConfiguration> create() {
        return List.of(
                new BundleConfiguration(zipkinBundle, Tracer.class, null)
        );
    }

    public static void instantiate(List<BundleConfiguration> bundleConfigurationFactories) {
        bundleConfigurationFactories.forEach(
                c -> {
                    if (c.getConfiguredBundle().equals(zipkinBundle)) {
                        c.setInstance(zipkinBundle.getHttpTracing().get().tracing().tracer());
                    }
                }
        );
    }
}
