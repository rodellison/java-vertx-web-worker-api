package vertx.web.worker.api;

import io.vertx.core.VertxOptions;
import org.junit.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import vertx.web.worker.api.handlers.BaristaWorker;
import vertx.web.worker.api.handlers.KitchenWorker;
import vertx.web.worker.api.handlers.LineWorker;


/*
 * Example of an asynchronous JUnit test for a Verticle.
 */
@RunWith(VertxUnitRunner.class)
public class TestWorkers {

    private static Vertx vertx;
    private static final Logger logger = Logger.getLogger(TestWorkers.class);
    private static DeploymentOptions dopt = new DeploymentOptions();

    @BeforeClass
    public static void before() throws IOException {

        dopt = new DeploymentOptions()
                .setWorkerPoolSize(1)
                .setWorker(true)
                .setInstances(1);
        VertxOptions vertxOptions = new VertxOptions()
                .setBlockedThreadCheckInterval(5000);

        vertx = Vertx.vertx(vertxOptions);

        vertx.deployVerticle(BaristaWorker.class.getName(), dopt);
        vertx.deployVerticle(LineWorker.class.getName(), dopt);
        vertx.deployVerticle(KitchenWorker.class.getName(), dopt);

    }

    @Test
    public void canMakeDrinks() {


        JsonObject theDrinkOrder = new JsonObject();
        theDrinkOrder.put("customerorder", "customer-123");
        logger.info("Test Barista Worker submit order");

        CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

        CompletableFuture.runAsync(() -> {
            vertx.eventBus().request(Services.MAKEDRINKS.toString(), theDrinkOrder.encode(), rs -> {
                if (rs.succeeded()) {
                    cf.complete(true);
                } else {
                    cf.complete(false);
                }
            });
        });

        try {
            Boolean madeDrink = cf.get();
            Assert.assertTrue(madeDrink);

        } catch (InterruptedException | ExecutionException  ioe) {

        }
    }

    @Test
    public void canMakeFood() {

        JsonObject theFoodOrder = new JsonObject();
        theFoodOrder.put("customerorder", "customer-123");
        logger.info("Test Kitchen Worker");

        CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

        CompletableFuture.runAsync(() -> {
            vertx.eventBus().request(Services.MAKEFOOD.toString(), theFoodOrder.encode(), rs -> {
                if (rs.succeeded()) {
                    cf.complete(true);
                } else {
                    cf.complete(false);
                }
            });
        });

        try {
            Boolean madeFood = cf.get();
            Assert.assertTrue(madeFood);

        } catch (InterruptedException | ExecutionException  ioe) {

        }
    }

    @Test
    public void canPackageOrder() {

        JsonObject theOrderToPackage = new JsonObject();
        theOrderToPackage.put("customerorder", "customer-123");
        logger.info("Test Line Worker");

        CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

        vertx.eventBus().send(Services.PACKAGEORDER.toString(), theOrderToPackage.encode());
        CompletableFuture.runAsync(() -> {

            vertx.eventBus().consumer(Services.FINISHORDER.toString(), message -> {
                // Do something with Vert.x async, reactive APIs
                cf.complete(true);
              });
          });

        try {
            Boolean orderPackaged = cf.get();
            Assert.assertTrue(orderPackaged);

        } catch (InterruptedException | ExecutionException  ioe) {

        }

    }

    @AfterClass
    public static void undeploy() throws IOException {

        vertx.undeploy(BaristaWorker.class.getName());
        vertx.undeploy(KitchenWorker.class.getName());
        vertx.undeploy(LineWorker.class.getName());
        logger.info("Worker Verticles undeployed, test complete");

    }

}
