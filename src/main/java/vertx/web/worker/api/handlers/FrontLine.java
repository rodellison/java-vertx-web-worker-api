package vertx.web.worker.api.handlers;

import vertx.web.worker.api.Services;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.log4j.Logger;
import vertx.web.worker.api.VerticleLauncher;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class FrontLine extends AbstractVerticle {

    private static Logger logger = Logger.getLogger(VerticleLauncher.class);
    private String thisContext;

    @Override
    public void start(Promise<Void> startPromise) {
        final EventBus eventBus = vertx.eventBus();
        thisContext = context.toString();
        thisContext = thisContext.substring(thisContext.lastIndexOf("@") + 1);

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/health").handler(this::healthCheck);
        router.post("/submitorder").handler(this::submitOrder);

        vertx.createHttpServer().requestHandler(router).listen(8080);

        eventBus.consumer(Services.FINISHORDER.toString(), message -> {
            // Do something with Vert.x async, reactive APIs
            String theMessage = message.body().toString();
            JsonObject FrontLineOrder = new JsonObject(theMessage);
            String customerOrder = FrontLineOrder.getValue("customerorder").toString();

            logger.info("FrontLine resource " + thisContext + " provides completed and packaged order for : " + customerOrder);

        });

        logger.info("FrontLine resource " + thisContext + " ready to take orders..");
        startPromise.complete();

    }

    private void healthCheck(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-type", "text/html");
        response.setStatusCode(200);
        response.end("running...");

    }

    private String createOrder() {
        // create instance of Random class
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        return "Order-" + rand.nextInt(1000);

    }

    private CompletableFuture<Boolean> submitDrinkOrder(JsonObject theDrinkOrder) {
        CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

        logger.info("Submitting Drink order");
        vertx.eventBus().request(Services.MAKEDRINKS.toString(), theDrinkOrder.encode(), rs -> {
            if (rs.succeeded()) {
                cf.complete(true);
            } else {
                cf.complete(false);
            }
        });
        return cf;
    }

    private CompletableFuture<Boolean> submitFoodOrder(JsonObject theFoodOrder) {
        CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

            logger.info("Submitting Food order");
            vertx.eventBus().request(Services.MAKEFOOD.toString(), theFoodOrder.encode(), rs -> {
                if (rs.succeeded()) {
                    cf.complete(true);
                } else {
                    cf.complete(false);
                }
            });
        return cf;
    }


    private void submitOrder(RoutingContext routingContext) {

        /*  This will be a POST request using simple JSON
        {
            "Food":
            {
                "1" : "Breakfast Sandwich"
            },
           "Drinks":
            {
                "1" : "Medium Coffee",
                "2" : "Small Frapuchino"
            }
        }
         */
        final EventBus eventBus = vertx.eventBus();

        String customerOrder = createOrder();
        JsonObject theOrder = routingContext.getBodyAsJson();
        theOrder.put("customerorder", customerOrder);
        JsonObject theFoodOrder = theOrder.getJsonObject("Food");
        theFoodOrder.put("customerorder", customerOrder);
        JsonObject theDrinkOrder = theOrder.getJsonObject("Drinks");
        theDrinkOrder.put("customerorder", customerOrder);

        logger.info("FrontLine resource " + thisContext + " took customer order, created order number: " + customerOrder);

        CompletableFuture<Void> completeTotalOrder =  CompletableFuture.allOf(

                submitDrinkOrder(theDrinkOrder),
                submitFoodOrder(theFoodOrder)

        ).whenComplete((response, error) -> {
            eventBus.send(Services.PACKAGEORDER.toString(), theOrder);
        });


        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-type", "text/html");
        response.setStatusCode(200);
        response.end("Your order is: " + customerOrder);

    }

}
