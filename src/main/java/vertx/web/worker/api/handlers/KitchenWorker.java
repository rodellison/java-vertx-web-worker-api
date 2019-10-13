package vertx.web.worker.api.handlers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import vertx.web.worker.api.Services;

public class KitchenWorker extends AbstractVerticle {
    private String thisContext;

    private static final Logger logger = Logger.getLogger(KitchenWorker.class);

    public void executeLongRunningBlockingOperation() {
        try {
            logger.info("\t\tExecuting long running simulation - Kitchen " + thisContext + " making items requested..");
            Thread.sleep(2000);
        } catch (InterruptedException ie) {

        }

    }

    @Override
    public void start(Promise<Void> startPromise) {
        final EventBus eventBus = vertx.eventBus();
        thisContext = context.toString();
        thisContext = thisContext.substring(thisContext.lastIndexOf("@") + 1);

        eventBus.consumer(Services.MAKEFOOD.toString(), message -> {
            // Do something with Vert.x async, reactive APIs

            String theMessage = message.body().toString();
            JsonObject KitchenOrder = new JsonObject(theMessage);
            String customerOrder = KitchenOrder.getValue("customerorder").toString();

            logger.info("\tKitchen " + thisContext + " received order request for customer order: " + customerOrder);
            executeLongRunningBlockingOperation();
            logger.info("\tKitchen " + thisContext + " finished creating items for customer order: " + customerOrder);

            message.reply(KitchenOrder);

        });

        startPromise.complete();
    }

}

