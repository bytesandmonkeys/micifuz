package com.micifuz.petshop;

import com.micifuz.commons.Runner;
import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(VertxExtension.class)
public class SimpleTest {
    final static String PETSHOP_HOST = "localhost";
    final static int PETSHOP_PORT = 8081;
    static String deploymentId;

    @BeforeAll
    static void beforeAll(Vertx vertx, VertxTestContext testContext) {
        Runner.start(vertx, PetShopMainVerticle.class.getName())
              .onFailure(Throwable::printStackTrace)
              .onComplete(res -> {
                  deploymentId = res.result();
                  System.out.println(deploymentId);
                  testContext.completeNow();
              });
    }

    @AfterAll
    static void afterAll(Vertx vertx) {
        vertx.undeploy(deploymentId);
    }

    @Test
    void should_simplyWork() {
        RestAssured.given()
                   .port(PETSHOP_PORT)
                   .when().get("/hello")
                   .then()
                   .statusCode(200)
                   .body("hello", is("world: petShop"));
    }

    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void should_healthCheck_up(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, PETSHOP_PORT, PETSHOP_HOST, "/health").compose(req -> req.send()
                                                                                                .onComplete(testContext.succeeding(httpResp -> testContext.verify(() -> {
                                                                                                    assertThat(httpResp.statusCode(), is(200));
                                                                                                    testContext.completeNow();
                                                                                                }))));
    }
}
