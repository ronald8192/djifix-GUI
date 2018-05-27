package com.ronald8192.djifixgui.viewController;

import com.ronald8192.djifixgui.App;
import com.ronald8192.djifixgui.DjifixRunner;
import com.ronald8192.djifixgui.OSType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @FXML AnchorPane stageAnchorPane;

    @FXML public Button btnSelectVideo;
    @FXML public Button btnStartFix;
    @FXML public Label lblStatusText;
    @FXML public TextArea txtLogsOut;

    private FileChooser sourceVideoChooser = new FileChooser();

    private DjifixRunner djifixRunner;
    private File sourceVideo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblStatusText.textProperty().bind(App.appStatus);
        txtLogsOut.textProperty().bind(App.outLogs);
        if(App.getOsType() == OSType.NOT_SUPPORTED) {
            enableSelectVideo(false);
            lblStatusText.setText("uh-oh!");
            txtLogsOut.appendText("Not support OS: " + System.getProperty("os.name"));
        }
        enableStartFix(false);
        sourceVideoChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video files (*.mp4)", "*.mp4", "*.MP4", "*.Mp4", "*.mP4"));
//        sourceVideoChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video files (*.MP4)", "*.MP4"));
    }

    public void enableSelectVideo(boolean bool) {
        this.btnSelectVideo.setDisable(!bool);
    }
    public void enableStartFix(boolean bool) {
        this.btnStartFix.setDisable(!bool);
    }

    public void selectVideoClick() {
        File sourceVideo = this.sourceVideoChooser.showOpenDialog(stageAnchorPane.getScene().getWindow());
        if (sourceVideo != null) {
            this.sourceVideo = sourceVideo;
            App.setStatus("File selected: " + this.sourceVideo.getAbsolutePath());
            btnStartFix.setDisable(false);
        }
    }

    public void startFixClick() {
        if(djifixRunner == null) {
            djifixRunner = new DjifixRunner(this.sourceVideo, this);
        } else {
            djifixRunner.setSourceVideo(this.sourceVideo);
        }
        djifixRunner.repair();
    }

    public void hrefClick(ActionEvent actionEvent) {
        String url = actionEvent.getTarget().toString().split("'")[1];
        (new Thread(()-> {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        })).start();
    }

    public void shutdownNow() {
        djifixRunner.shutdownNow();
    }
}
