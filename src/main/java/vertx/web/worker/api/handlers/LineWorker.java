package vertx.web.worker.api.handlers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import vertx.web.worker.api.Services;

public class LineWorker extends AbstractVerticle {
    private String thisContext;

    private static final Logger logger = Logger.getLogger(LineWorker.class);

    public void executeLongRunningBlockingOperation() {
        try {
            logger.info("\t\tExecuting long running simulation - LineWorker " + thisContext + " packaging items requested..");
            Thread.sleep(2000);
        } catch (InterruptedException ie) {

        }

    }

    @Override
    public void start(Promise<Void> startPromise) {
        final EventBus eventBus = vertx.eventBus();
        thisContext = context.toString();
        thisContext = thisContext.substring(thisContext.lastIndexOf("@") + 1);

        eventBus.consumer(Services.PACKAGEORDER.toString(), message -> {
            // Do something with Vert.x async, reactive APIs

            String theMessage = message.body().toString();
            JsonObject LineWorkerOrder = new JsonObject(theMessage);
            String customerOrder = LineWorkerOrder.getValue("customerorder").toString();

            logger.info("\tLineWorker " + thisContext + " received items for customer order: " + customerOrder);
            executeLongRunningBlockingOperation();
            logger.info("\tLineWorker " + thisContext + " finished creating packaging items for customer order: " + customerOrder);

            eventBus.send(Services.FINISHORDER.toString(), LineWorkerOrder.encode());

        });

        startPromise.complete();
    }

}

