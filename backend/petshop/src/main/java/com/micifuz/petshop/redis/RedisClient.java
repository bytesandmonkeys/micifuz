package com.micifuz.petshop.redis;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.redis.client.Command;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.Request;
import io.vertx.reactivex.redis.client.Response;
import io.vertx.redis.client.RedisOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedisClient {

  private RedisAPI redisApi;
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class.getName());

  private String username, password,host;
  private Integer port;
  // Pding to get then from config.

  public RedisClient() {
  }

  /**
   * init.
   */
  public Completable init() {

    return Redis.createClient(Vertx.currentContext().owner(), new RedisOptions()
            .setConnectionString(String.format("redis://%s:%d", host, port))
            .setNetClientOptions(
                new NetClientOptions().setTcpNoDelay(true).setTcpKeepAlive(true).setSsl(true))
            .setMaxPoolSize(300))
        .send(Request.cmd(Command.AUTH).arg(username).arg(password), r -> {
          LOGGER.info("result : " + r.result());
          LOGGER.info("failed : " + r.failed());
          LOGGER.info("succeeded : " + r.succeeded());
          if (r.failed()) {
            r.cause().printStackTrace();
          }
        })
        .rxConnect()
        .doOnError(err -> {
          err.printStackTrace();
          LOGGER.error(err);
        })
        .map(connection -> {
          connection.exceptionHandler(err -> {
            err.printStackTrace();
            LOGGER.error(err);
            redisApi = null;
            throw new RuntimeException("Redis is broken " + err.getMessage());
          });
          return RedisAPI.api(connection);
        })
        .flatMapCompletable(res -> {
          redisApi = res;
          return Completable.complete();
        })
        .doOnError(err -> {
          LOGGER.error(err.getMessage());
        })
        .onErrorComplete();
  }

  /**
   * get.
   */
  public Maybe<String> get(String key) {
    return getRedisApi()
        .flatMap(res -> res.rxGet(key).timeout(600, TimeUnit.MILLISECONDS))
        .map(Response::toString)
        .onErrorComplete(err -> {
          err.printStackTrace();
          LOGGER.error("Error connecting to redis to get key: " + key);
          return true;
        });
  }

  /**
   * setValue.
   */
  public Completable setValue(String key, String value) {
    return getRedisApi()
        .flatMapCompletable(
            res -> res.rxSet(List.of(key, value)).timeout(5, TimeUnit.SECONDS).ignoreElement())
        .onErrorComplete(err -> {
          err.printStackTrace();
          LOGGER.error("Error connecting to redis to set key " + err);
          return true;
        });
  }

  private Maybe<RedisAPI> getRedisApi() {
    return (redisApi == null) ? Maybe.error(
        () -> new RuntimeException("Could not connect to Redis")) : Maybe.just(
        redisApi);
  }

  /**
   * ping.
   */
  public Maybe<Response> ping() {
    return getRedisApi().flatMap(redis -> redis.rxPing(new ArrayList<>()));
  }

}
