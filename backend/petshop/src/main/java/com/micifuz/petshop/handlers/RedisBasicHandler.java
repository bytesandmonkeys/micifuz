package com.micifuz.petshop.handlers;

import com.micifuz.petshop.redis.RedisClient;
import io.reactivex.functions.Consumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.RoutingContext;

public class RedisBasicHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisBasicHandler.class.getName());

  private final RedisClient redisClient;

  public RedisBasicHandler(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  /**
   * execute.
   */
  public void execute(RoutingContext context) {
    String key = context.pathParam("key");
    context.response().putHeader("content-type", "application/json")
        .end(new JsonObject().put("hello", "testbed").encode());
    redisClient.get(key)
        .subscribe(item ->
            context.response().putHeader("content-type", "application/json")
                .end(Json.encodePrettily(item)), fail(context));
  }

  private Consumer<Throwable> fail(RoutingContext context) {
    return err -> {
      LOGGER.error(err.getMessage());
      context.fail(err);
    };
  }

  /**
   * set.
   */
  public void set(RoutingContext context) {
    String key = context.pathParam("key");
    String value = context.pathParam("value");
    context.response().putHeader("content-type", "application/json")
        .end(new JsonObject().put("hello", "testbed").encode());
    redisClient.setValue(key, value)
        .subscribe(() ->
            context.response().putHeader("content-type", "application/json")
                .end("OK"), fail(context));
  }
}
