package vertx.web.worker.api;

import org.apache.log4j.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class);
    private static VerticleLauncher ml;

    public static void main(String[] args) {

        logger.info("Starting Verticles from Main.");
        ml = new VerticleLauncher();
        ml.launchVerticles();

    }


}
