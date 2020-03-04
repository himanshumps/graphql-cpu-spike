package com.test.graphql;

import com.test.graphql.verticle.ServerVerticle;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Application {
    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        Vertx vertx = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(20).setWorker(false);
        vertx.deployVerticle(ServerVerticle::new, deploymentOptions);
        MeterRegistry registry = new SimpleMeterRegistry();
        new ProcessorMetrics().bindTo(registry);
        java.util.Timer timer = new Timer();
        long delay = TimeUnit.SECONDS.toMillis(1);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log.info("{}", "system.cpu.count=" + registry.find("system.cpu.count").gauge().value() +
//                        " system.load.average.1m=" + registry.find("system.load.average.1m").gauge().value() +
                        " system.cpu.usage=" + registry.find("system.cpu.usage").gauge().value() +
                        " process.cpu.usage=" + registry.find("process.cpu.usage").gauge().value());
            }
        }, 0, delay);
    }
}
