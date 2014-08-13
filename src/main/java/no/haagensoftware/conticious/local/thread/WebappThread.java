package no.haagensoftware.conticious.local.thread;

import no.haagensoftware.contentice.main.Main;
import org.apache.log4j.Logger;

/**
 * Created by jhsmbp on 12/08/14.
 */
public class WebappThread extends Thread {
    private static final Logger logger = Logger.getLogger(WebappThread.class.getName());

    private Main main;

    @Override
    public void run() {
        try {
            main = new Main();
            main.bootstrap();
        } catch (Exception e) {
            logger.error("Unable to execute Webapp Thread: " + e.getMessage(), e);
            throw new RuntimeException("Unable to execute Webapp Thread: " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        main.shutdown();
    }
}
