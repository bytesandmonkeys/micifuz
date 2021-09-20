package com.micifuz.vets;

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
    final static String VETS_HOST = "localhost";
    final static int VETS_PORT = 8083;
    static String deploymentId;

    @BeforeAll
    static void beforeAll(Vertx vertx, VertxTestContext testContext) {
        Runner.start(vertx, VetsMainVerticle.class.getName())
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
                .port(VETS_PORT)
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body("hello", is("world: Vets"));
    }

    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void should_healthCheck_up(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, VETS_PORT, VETS_HOST, "/health").compose(req -> req.send()
                .onComplete(testContext.succeeding(httpResp -> testContext.verify(() -> {
                    assertThat(httpResp.statusCode(), is(200));
                    testContext.completeNow();
                }))));
    }
}
