package vertx.web.worker.api.handlers;

import vertx.web.worker.api.Services;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BaristaWorker extends AbstractVerticle {
    private String thisContext;

    private static final Logger logger = Logger.getLogger(BaristaWorker.class);

    public void executeLongRunningBlockingOperation() {
        try {
            logger.info("\t\tExecuting long running simulation - Barista " + thisContext + " making items requested..");
            Thread.sleep(2000);
        } catch (InterruptedException ie) {

        }

    }

    @Override
    public void start(Promise<Void> startPromise) {
        final EventBus eventBus = vertx.eventBus();
        thisContext = context.toString();
        thisContext = thisContext.substring(thisContext.lastIndexOf("@") + 1);

        eventBus.consumer(Services.MAKEDRINKS.toString(), message -> {
            // Do something with Vert.x async, reactive APIs

            String theMessage = message.body().toString();
            JsonObject BaristaOrder = new JsonObject(theMessage);
            String customerOrder = BaristaOrder.getValue("customerorder").toString();

            logger.info("\tBarista " + thisContext + " received order request for customer order: " + customerOrder);
            executeLongRunningBlockingOperation();
            logger.info("\tBarista " + thisContext + " finished creating items for customer order: " + customerOrder);
            message.reply(BaristaOrder);

        });


        startPromise.complete();
    }

}

