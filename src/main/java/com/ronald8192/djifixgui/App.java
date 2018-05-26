package com.ronald8192.djifixgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application{

    private Logger log = LoggerFactory.getLogger(App.class);
    public static StringProperty appStatus = new SimpleStringProperty("Ready");
    public static StringProperty outLogs = new SimpleStringProperty("");
    private static OSType osType;
    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            osType = OSType.WINDOWS;
        } else if (osName.contains("mac")) {
            osType = OSType.MAC;
        } else if (osName.contains("linux")) {
            osType = OSType.LINUX;
        } else {
            osType = OSType.NOT_SUPPORTED;
        }
    }

    public static OSType getOsType() {
        return osType;
    }

    public static void main(String[] args)  {

        launch(args);
	}

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(e -> log.trace("Exiting..."));

        primaryStage.setTitle("djifix-GUI");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("view/home.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void setStatus(String status) {
        if (status != null) Platform.runLater(() -> appStatus.set(status));
    }
    public static void appendLogs(String log) {
        if (log != null) Platform.runLater(() -> outLogs.set(outLogs.getValue() + log + System.getProperty("line.separator")));
    }
}
