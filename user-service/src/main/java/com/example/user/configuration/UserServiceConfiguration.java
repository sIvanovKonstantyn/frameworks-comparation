package com.example.user.configuration;

import brave.Tracing;
import com.example.user.client.TaskServiceClient;
import com.example.user.consumers.UserTaskUpdatesConsumer;
import com.example.user.repositories.UserRepository;
import com.example.user.resources.UserResource;
import com.example.user.services.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ConsoleZipkinFactory;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import feign.Feign;
import feign.gson.GsonDecoder;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.kafka.KafkaConsumerFactory;
import io.dropwizard.setup.Environment;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.List;

public class UserServiceConfiguration extends Configuration {

    @NotNull
    private KafkaConsumerFactory<String, String> kafkaConsumerFactory;

    @NotNull
    public final ZipkinFactory zipkin = new ConsoleZipkinFactory();

    @NotNull
    public String mongoUrl;

    @NotNull
    private String taskServiceUrl;

    @JsonProperty
    public String getMongoUrl() {
        return mongoUrl;
    }

    @JsonProperty
    public void setMongoUrl(String mongoUrl) {
        this.mongoUrl = mongoUrl;
    }

    public List<Class<?>> getSingletons() {
        return List.of(UserResource.class);
    }

    @JsonProperty("consumer")
    public KafkaConsumerFactory<String, String> getKafkaConsumerFactory() {
        return kafkaConsumerFactory;
    }

    @JsonProperty("consumer")
    public void setKafkaConsumerFactory(KafkaConsumerFactory<String, String> kafkaConsumerFactory) {
        this.kafkaConsumerFactory = kafkaConsumerFactory;
    }

    @JsonProperty("taskServiceUrl")
    public void setTaskServiceUrl(String taskServiceUrl) {
        this.taskServiceUrl = taskServiceUrl;
    }


    public static class DependencyInjectionBundle implements ConfiguredBundle<UserServiceConfiguration> {

        private final List<BundleConfiguration> bundleConfigurationFactories;

        public DependencyInjectionBundle(List<BundleConfiguration> bundleConfigurationFactories) {
            this.bundleConfigurationFactories = bundleConfigurationFactories;
        }

        @Override
        public void run(UserServiceConfiguration configuration, Environment environment) {
            //Components scan
            environment
                    .jersey()
                    .packages("com.example.user.resources");

            //DI
            Consumer<String, String> consumer = configuration.getKafkaConsumerFactory()
                    .build(
                            environment.lifecycle(),
                            environment.healthChecks(),
                            Tracing.current(),
                            new NoOpConsumerRebalanceListener()
                    );

            TaskServiceClient taskServiceClient = Feign.builder()
                    .decoder(new GsonDecoder())
                    .target(TaskServiceClient.class, configuration.getTaskServiceUrl());

            UserService userService = new UserService(new UserRepository(
                    configuration.getMongoUrl(),
                    Tracing.currentTracer()),
                    taskServiceClient);

            UserTaskUpdatesConsumer userTaskUpdatesConsumer = new UserTaskUpdatesConsumer(
                    consumer,
                    userService,
                    environment.getObjectMapper(),
                    Tracing.currentTracer()
            );

            environment.lifecycle().manage(userTaskUpdatesConsumer);

            environment
                    .jersey()
                    .register(
                            new AbstractBinder() {
                                @Override
                                protected void configure() {
                                    for (Class<?> singletonClass : configuration.getSingletons()) {
                                        bindAsContract(singletonClass).in(Singleton.class);
                                    }

                                    bundleConfigurationFactories.forEach(
                                            c -> {
                                                if (c.getInstance() != null) {
                                                    bind(c.getInstance()).to(c.getClazz());
                                                    bindAsContract(c.getClazz()).in(Singleton.class);
                                                }
                                            }
                                    );

                                    bind(userService).to(UserService.class);
                                    bindAsContract(UserService.class).in(Singleton.class);
                                }
                            }
                    );

        }

    }
    @JsonProperty("taskServiceUrl")
    private String getTaskServiceUrl() {
        return taskServiceUrl;
    }

    @JsonProperty
    public ZipkinFactory getZipkinFactory() {
        return zipkin;
    }
}
