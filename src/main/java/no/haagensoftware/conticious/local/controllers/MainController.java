package no.haagensoftware.conticious.local.controllers;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import no.haagensoftware.conticious.local.thread.WebappThread;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jhsmbp on 12/08/14.
 */
public class MainController implements Initializable {
    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    private Stage primaryStage;

    private WebappThread webappThread;

    private BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private BooleanProperty cantRun = new SimpleBooleanProperty(true);
    private BooleanProperty isStopped = new SimpleBooleanProperty(true);

    @FXML private TextArea loggingTextArea;
    @FXML private TextField dataDirTextField;
    @FXML private TextField portTextField;
    @FXML private Button startButton;
    @FXML private Button stopButton;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void logMessage(String message) {
        if (loggingTextArea != null && message != null) {
            try {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loggingTextArea.appendText(message);
                    }
                });
            } catch (Exception e) {
                logger.info("Exception while logging. swallowing", e);
            }
        }
    }

    @FXML
    public void chooseDataDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        File sistValgteDir = new File(System.getProperty("user.home"));
        if (sistValgteDir != null && sistValgteDir.exists()) {
            chooser.setInitialDirectory(sistValgteDir);
        }


        File selectedDir = chooser.showDialog(primaryStage);
        if (selectedDir != null && selectedDir.exists() && selectedDir.isDirectory()) {
            dataDirTextField.setText(selectedDir.getAbsolutePath());
            loggingTextArea.appendText("Data directory selected: " + selectedDir.getAbsolutePath() + "\n");
            loggingTextArea.appendText("Downloading documents and admin webapp\n");
        } else {
            logger.info("Unable to open or create Data Directory at: " + selectedDir.getAbsolutePath());
        }

        File zipFile = new File(selectedDir.getAbsolutePath() + File.separatorChar + "conticious_local.zip");
        if (zipFile != null && zipFile.exists() && zipFile.isFile()) {
            logger.info("Conticious is already downloaded. Skipping download and unzip");
        } else {
            downloadAndExtract(selectedDir);
        }

        cantRun.setValue(false);
    }

    private void downloadAndExtract(File selectedDir) {
        URL website = null;
        try {
            website = new URL("http://install.conticious.com/conticious_local.zip");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(selectedDir.getAbsolutePath() + File.separatorChar + "conticious_local.zip");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        unZipIt(selectedDir.getAbsolutePath() + File.separatorChar + "conticious_local.zip", selectedDir.getAbsolutePath());
    }

    /**
     * Unzip it
     * @param zipFile input zip file
     * @param outputFolder zip file output folder
     */
    public void unZipIt(String zipFile, String outputFolder){

        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {

                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }

                zis.closeEntry();
                ze = zis.getNextEntry();
            }

            zis.close();

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void startWebapp() throws Exception {
        System.setProperty("no.conticious.startWithoutConfigProperties", "true");
        System.setProperty("no.haagensoftware.contentice.storage.file.documentsDirectory", dataDirTextField.getText() + File.separatorChar + "documents");
        System.setProperty("no.haagensoftware.contentice.pluginDirectory", dataDirTextField.getText() + File.separatorChar + "plugins");
        System.setProperty("no.haagensoftware.contentice.storage.plugin", "FileSystemStoragePlugin");
        System.setProperty("no.haagensoftware.contentice.port", portTextField.getText());
        System.setProperty("no.haagensoftware.contentice.webappDir", dataDirTextField.getText() + File.separatorChar + "webapps");

        webappThread = new WebappThread();
        webappThread.start();

        isRunning.set(true);

    }

    @FXML
    public void stopWebapp() {
        webappThread.shutdown();
        webappThread = null;

        logger.info("Webapp shutdown...");

        isRunning.set(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                startButton.disableProperty().set(true);
                stopButton.disableProperty().set(true);
                startButton.disableProperty().bindBidirectional(cantRun);
                stopButton.disableProperty().bindBidirectional(isStopped);
            }
        });

        isRunning.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
                isStopped.set(!newVal.booleanValue());
                if (dataDirTextField.getText() != null && dataDirTextField.getText().length() > 3 && newVal.booleanValue()) {
                    cantRun.set(true);
                } else {
                    cantRun.set(false);
                }
            }
        });
    }
}
