package com.micifuz.petshop.ioc;

import com.micifuz.petshop.handlers.HealthChecks;
import com.micifuz.petshop.handlers.HelloHandler;
import com.micifuz.petshop.handlers.RedisBasicHandler;
import com.micifuz.petshop.redis.RedisClient;
import com.micifuz.petshop.router.Routing;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;

public class IoC {

    private final Routing routing;
    private final HelloHandler helloHandler;
    private final HealthCheckHandler healthCheckHandler;
    private final RedisClient redisClient;
    private final RedisBasicHandler redisHandler;
    private static IoC instance = null;

    public static synchronized IoC getInstance() {
        if (instance == null) {
            instance = new IoC();
        }
        return instance;
    }

    private IoC() {
        routing = new Routing();
        Vertx vertx = Vertx.currentContext().owner();
        HealthChecks healthCheckHealthChecks = new HealthChecks(vertx);
        healthCheckHandler = healthCheckHealthChecks.getHealthCheckHandler();
        helloHandler = new HelloHandler();
        redisClient = new RedisClient();
        redisHandler = new RedisBasicHandler(redisClient);
    }

    public Routing getRouting() {
        return routing;
    }

    public HelloHandler getHelloHandler() {
        return helloHandler;
    }

    public HealthCheckHandler getHealthCheck() {
        return healthCheckHandler;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public RedisBasicHandler getRedisHandler() {
        return redisHandler;
    }
}
