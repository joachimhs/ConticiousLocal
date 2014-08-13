package no.haagensoftware.conticious.local.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import no.haagensoftware.conticious.local.controllers.MainController;
import no.haagensoftware.conticious.local.util.FileUtil;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhsmbp on 12/08/14.
 */
public class MainView extends Application {
    private static final Logger logger = Logger.getLogger(MainView.class.getName());
    private MainController mainController;


    private Appender guiAppender = new AppenderSkeleton() {
        private List<String> messagesToLog = new ArrayList<>();
        @Override
        public boolean requiresLayout() {
            return true;
        }

        @Override
        public void close() {}

        @Override
        protected void append(LoggingEvent event) {

            if (mainController == null) {
                messagesToLog.add(layout.format(event));
            }

            if (mainController != null) {
                for (String message : messagesToLog) {
                    try {
                        mainController.logMessage(message);
                    } catch (Exception e) {
                        //Unable to log. Make sure that app wont fail for loggin errors. Append message to message log
                        messagesToLog.add(message);
                    }
                }
                messagesToLog.clear();

                mainController.logMessage(layout.format(event));
            }

        }
    };
    @Override
    public void start(Stage primaryStage) throws Exception {
        configureLog4J();

        logger.info("Start called... " + System.getProperty("java.home"));
        URL resource = FileUtil.getUrlForResource("MainController.fxml");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            AnchorPane page = (AnchorPane) fxmlLoader.load();

            mainController = (MainController) fxmlLoader.getController();
            mainController.setPrimaryStage(primaryStage);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(page);

            primaryStage.setScene(scene);
            primaryStage.setTitle("LevelDBrowser");
            primaryStage.show();

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    System.exit(0);
                }
            });
            logger.info("Showed Primary Stage");
        } catch (Exception e) {
            logger.fatal("Unable to load main Controller FXML");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MainView view = new MainView();
        launch(args);
    }

    private void configureLog4J() throws IOException {
        Logger root = Logger.getRootLogger();
        if (!root.getAllAppenders().hasMoreElements()) {
            //Log4J is not configured. Set it up correctly!
            root.setLevel(Level.INFO);
            root.addAppender(new ConsoleAppender(new PatternLayout("%-5p %C{1}: %m%n")));
            guiAppender.setLayout(new PatternLayout("%-5p %C{1}: %m%n"));
            root.addAppender(guiAppender);
            //root.addAppender(new FileAppender(new PatternLayout("%-5p [%t]: %m%n"), workspace.getWorkspaceDir() + File.separatorChar + "maps.log", true));
        }

        System.setErr(createLoggingProxy(System.err));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                logger.info(string);
            }
            public void println(final String string) {
                logger.info(string);
            }
        };
    }
}
