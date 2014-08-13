package no.haagensoftware.conticious.local.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;

public class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

    public static URL getUrlForResource(String resourceName) {

        URL resource = null;

        try {
            resource = FileUtil.class.getResource(resourceName);
            logger.info(resource.getPath());
        }
        catch (Exception e) {
            logger.info("fant ikke " + resourceName + ", prøver /" + resourceName);
            resource =  FileUtil.class.getResource("/" + resourceName);
            logger.info(resource.getPath());
        }

        logger.info("Got Resource: " + resource.getFile());

        return resource;
    }

    public static File getFileForResource(String resourceName) {

        File resource = null;
        String javaHome = System.getProperty("java.home");
        logger.info("sensemaps.embedded: " + System.getProperty("sensemaps.embedded"));
        if (System.getProperty("sensemaps.embedded") != null && javaHome.contains("Contents")) {
            int contentsIndex = javaHome.indexOf("Contents");

            resource = new File(javaHome.substring(0, contentsIndex) + "Contents/Java/" + resourceName);
            logger.info("resourceFile: " + resource.getAbsolutePath());
        } else if (System.getProperty("sensemaps.embedded") != null && javaHome.contains("runtime")) {
            int contentsIndex = javaHome.indexOf("runtime");

            resource = new File(javaHome.substring(0, contentsIndex) + "app" + File.separatorChar + resourceName);
            logger.info("resourceFile: " + resource.getAbsolutePath());
        } else {
            resource = new File(getUrlForResource(resourceName).getPath());
        }

        logger.info("Got Resource: " + resource.getAbsolutePath());

        return resource;
    }
}
