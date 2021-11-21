package com.micifuz.petshop.router;

import com.micifuz.petshop.ioc.IoC;

import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class Routing {
    private static final String HEALTH_CHECK = "/health";
    private static final String HELLO = "/hello";
    private static final String ROOT = "/";
    private static final String REDIS_KEY = "/redis/:key";
    private static final String REDIS_SET = "/redis/:key/:value";

    public Single<Router> createRouter() {
        Router router = Router.router(Vertx.currentContext().owner());
        router.post().handler(BodyHandler.create());
        router.get(ROOT).handler(IoC.getInstance().getHelloHandler()::execute);
        router.get(HELLO).handler(IoC.getInstance().getHelloHandler()::execute);
        router.get(HEALTH_CHECK).handler(IoC.getInstance().getHealthCheck());
        router.get(REDIS_KEY).handler(IoC.getInstance().getRedisHandler()::execute);
        router.get(REDIS_SET).handler(IoC.getInstance().getRedisHandler()::set);
        return Single.just(router);
    }
}
