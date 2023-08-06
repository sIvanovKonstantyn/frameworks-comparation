package com.example.task.configuration;

import com.example.task.repositories.TaskRepository;
import com.example.task.resources.TaskResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoketurner.dropwizard.zipkin.ConsoleZipkinFactory;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.kafka.BasicKafkaProducerFactory;
import io.dropwizard.kafka.KafkaProducerFactory;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class TaskServiceConfiguration extends Configuration {
    @NotEmpty
    private String defaultName = "Stranger";
    @NotNull
    public final ZipkinFactory zipkin = new ConsoleZipkinFactory();

    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @NotNull
    private KafkaProducerFactory<String, String> kafkaProducerFactory = new BasicKafkaProducerFactory<>();

    public List<Class<?>> getSingletons() {
        return List.of(TaskRepository.class, TaskResource.class);
    }

    public static class DependencyInjectionBundle implements ConfiguredBundle<TaskServiceConfiguration> {

        private final List<BundleConfiguration> bundleConfigurationFactories;

        public DependencyInjectionBundle(List<BundleConfiguration> bundleConfigurationFactories) {
            this.bundleConfigurationFactories = bundleConfigurationFactories;
        }

        @Override
        public void run(TaskServiceConfiguration configuration, Environment environment) {
            //Components scan
            environment
                    .jersey()
                    .packages("com.example.task.resources");

            //DI
            environment
                    .jersey()
                    .register(
                            new AbstractBinder() {
                                @Override
                                protected void configure() {
                                    for (Class<?> singletonClass : configuration.getSingletons()) {
                                        bindAsContract(singletonClass).in(Singleton.class);
                                    }

                                    bind(new ObjectMapper()).to(ObjectMapper.class);
                                    bindAsContract(ObjectMapper.class).in(Singleton.class);

                                    bundleConfigurationFactories.forEach(
                                            c -> {
                                                if (c.getInstance() != null) {
                                                    bind(c.getInstance()).to(c.getClazz());
                                                    bindAsContract(c.getClazz()).in(Singleton.class);
                                                }
                                            }
                                    );
                                }
                            }
                    );
        }

    }
    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    @JsonProperty
    public ZipkinFactory getZipkinFactory() {
        return zipkin;
    }

    @JsonProperty("producer")
    public KafkaProducerFactory<String, String> getKafkaProducerFactory() {
        return kafkaProducerFactory;
    }

    @JsonProperty("producer")
    public void setKafkaProducerFactory(KafkaProducerFactory<String, String> kafkaProducerFactory) {
        this.kafkaProducerFactory = kafkaProducerFactory;
    }
}
