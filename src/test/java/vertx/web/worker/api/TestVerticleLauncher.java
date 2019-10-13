package vertx.web.worker.api;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.*;
import vertx.web.worker.api.handlers.BaristaWorker;
import vertx.web.worker.api.handlers.FrontLine;
import vertx.web.worker.api.handlers.KitchenWorker;
import vertx.web.worker.api.handlers.LineWorker;

import java.io.IOException;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestVerticleLauncher {

    private final Logger logger = Logger.getLogger(TestVerticleLauncher.class);
    private static VerticleLauncher ml;

    @BeforeClass
    public static void setUp() throws IOException {

        //This call establishes service front door, and starts vertx
        ml = new VerticleLauncher();
        ml.launchVerticles();
        try
        {
            //Just waiting one second for verticles to get up, before running tests
            Thread.sleep(1000);

        } catch (InterruptedException ie)
        {

        }
    }

    @Test
    @Order(1)
    public void testAllVerticlesStarted() throws Throwable {

        List<Boolean> testVerticles = ml.verticleResult;
        Boolean finalResult = true;
        for (Boolean result: testVerticles)
        {
            if (result == false)
            {
                finalResult = false;
            }
        }
        logger.info("Test all verticles started.");
        Assert.assertTrue(finalResult);

    }

    @AfterClass
    public static void tearDown() throws IOException {

        ml.vertx.undeploy(FrontLine.class.getName());
        ml.vertx.undeploy(BaristaWorker.class.getName());
        ml.vertx.undeploy(KitchenWorker.class.getName());
        ml.vertx.undeploy(LineWorker.class.getName());

        ml = null;

    }

}
