package com.example.task;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("")
public class TaskServiceApplication extends Application {

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        SeBootstrap.Configuration.Builder configBuilder = SeBootstrap.Configuration.builder();
        configBuilder.property(SeBootstrap.Configuration.PROTOCOL, "HTTP")
            .property(SeBootstrap.Configuration.HOST, "localhost")
            .property(SeBootstrap.Configuration.PORT, 8080);

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(TaskServiceApplication.class.getPackageName());

        SeBootstrap.start(resourceConfig, configBuilder.build())
            .thenAccept(instance -> System.out.println("Service started in(ms): " + (System.currentTimeMillis() - start)));

        Thread.currentThread()
            .join();
    }
}