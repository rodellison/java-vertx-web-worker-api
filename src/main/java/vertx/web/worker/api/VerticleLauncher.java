package vertx.web.worker.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.apache.log4j.Logger;
import vertx.web.worker.api.handlers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class VerticleLauncher {

    private static Logger logger = Logger.getLogger(VerticleLauncher.class);
    public static Vertx vertx;
    public static List<Boolean> verticleResult = new ArrayList<>();

    // Convenience method so you can run it in your IDE
    public void launchVerticles() {

        VertxOptions vertxOptions = new VertxOptions()
                .setBlockedThreadCheckInterval(5000);
        vertx = Vertx.vertx(vertxOptions);

        final int instanceCount = Runtime.getRuntime().availableProcessors();
        logger.info("Starting Verticles and setting instances to: " + instanceCount);

        DeploymentOptions standardDeploymentOptions = new DeploymentOptions()
                .setInstances(instanceCount);

        DeploymentOptions workerDeploymentOptions = new DeploymentOptions()
                .setWorkerPoolName("data-processing-pool")
                .setWorkerPoolSize(instanceCount)
                .setInstances(instanceCount)
                .setWorker(true);

        CompletableFuture.allOf(

                //     deploy("just testing fail", standardDeploymentOptions),
                deploy(FrontLine.class.getName(), standardDeploymentOptions),
                deploy(BaristaWorker.class.getName(), workerDeploymentOptions),
                deploy(KitchenWorker.class.getName(), workerDeploymentOptions),
                deploy(LineWorker.class.getName(), workerDeploymentOptions)

        ).whenComplete((res, err) -> {
            logger.info("Deploy all verticles complete.");
            for (Boolean result : verticleResult) {
                if (result == false) {
                    logger.info("One or more verticles did NOT deploy successfully.");
                    //take action if necessary
                }
            }
        });
    }

    public CompletableFuture<Boolean> deploy(String name, DeploymentOptions opts) {
        CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

        vertx.deployVerticle(name, opts, res -> {
            if (res.failed()) {
                logger.error("Failed to deploy verticle: " + name);
                verticleResult.add(false);
                cf.complete(false);
            } else {
                logger.info("Deployed verticle: " + name);
                verticleResult.add(true);
                cf.complete(true);
            }
        });
        return cf;
    }

}
