package com.example.task.configuration;

import brave.Tracer;
import com.example.task.repositories.entities.TaskEntity;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.kafka.KafkaProducerBundle;
import io.dropwizard.kafka.KafkaProducerFactory;
import org.apache.kafka.clients.producer.Producer;
import org.hibernate.SessionFactory;

import java.util.List;

public class BundleConfigurationFactory {
    private static final ZipkinBundle<TaskServiceConfiguration> zipkinBundle = new ZipkinBundle<>("Task service") {
        @Override
        public ZipkinFactory getZipkinFactory(TaskServiceConfiguration configuration) {
            return configuration.getZipkinFactory();
        }
    };

    private static final HibernateBundle<TaskServiceConfiguration> hibernateBundle = new HibernateBundle<>(TaskEntity.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(TaskServiceConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    private static final KafkaProducerBundle<String, String, TaskServiceConfiguration> kafkaProducerBundle
            = new KafkaProducerBundle<>(List.of("task-updates")) {
        @Override
        public KafkaProducerFactory<String, String> getKafkaProducerFactory(TaskServiceConfiguration configuration) {
            return configuration.getKafkaProducerFactory();
        }
    };
    public static List<BundleConfiguration> create() {
        return List.of(
                new BundleConfiguration(zipkinBundle, Tracer.class, null),
                new BundleConfiguration(hibernateBundle, SessionFactory.class, null),
                new BundleConfiguration(kafkaProducerBundle, Producer.class, null)

        );
    }

    public static void instantiate(List<BundleConfiguration> bundleConfigurationFactories) {
        bundleConfigurationFactories.forEach(
                c -> {
                    if (c.getConfiguredBundle().equals(zipkinBundle)) {
                        c.setInstance(zipkinBundle.getHttpTracing().get().tracing().tracer());
                    } else if (c.getConfiguredBundle().equals(hibernateBundle)) {
                        c.setInstance(hibernateBundle.getSessionFactory());
                    } else if (c.getConfiguredBundle().equals(kafkaProducerBundle)) {
                        c.setInstance(kafkaProducerBundle.getProducer());
                    }
                }
        );
    }
}
