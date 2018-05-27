package com.ronald8192.djifixgui;

import com.ronald8192.djifixgui.viewController.HomeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class DjifixRunner {
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String jarPWD = "";

    {
        try {
            jarPWD = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + "/";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private File sourceVideo;
    private HomeController fromController;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    public DjifixRunner(File sourceVideo, HomeController fromController) {
        this.sourceVideo = sourceVideo;
        this.fromController = fromController;
    }

    public List<Runnable> shutdownNow() {
        return this.executor.shutdownNow();
    }

    public void setSourceVideo(File sourceVideo) {
        this.sourceVideo = sourceVideo;
    }

    private Future prepareProcess(String ...commands) {
        return executor.submit(() -> {
            log.info("Run command: " + String.join(" ", commands));
            ProcessResult processResult = new ProcessResult();
            try {
                Process p = (new ProcessBuilder(commands)).start();
                p.waitFor();

                BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                processResult = new ProcessResult(outReader, errReader);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            log.debug("Command ends: " + String.join(" ", commands));
            return processResult;
        });
    }

    public void repair() {
        App.setStatus("Checking binary files...");
        File binDjifix = new File(jarPWD + "bin/djifix" + App.getOsType().getBinSuffix());
        File binFfmpeg = new File(jarPWD + "bin/ffmpeg" + App.getOsType().getBinSuffix());
        log.trace(binDjifix.getAbsolutePath());

        if(binDjifix.exists() && binFfmpeg.exists()){
            try {
                this.prepareProcess("chmod", "+x", binDjifix.getAbsolutePath()).get();
                this.prepareProcess("chmod", "+x", binFfmpeg.getAbsolutePath()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            App.setStatus("Error, see logs below");
            App.appendLogs("Binary file 'djifix" + App.getOsType().getBinSuffix() + "' and/or 'ffmpeg" + App.getOsType().getBinSuffix() + "' not found.");
            App.appendLogs("They should be exist in the 'bin' folder.");
            fromController.enableStartFix(true);
        }

        fromController.enableStartFix(false);
        fromController.enableSelectVideo(false);
        App.setStatus("repairing...");
        (new Thread(() -> {
            try {
                ProcessResult prDjifix = (ProcessResult) this.prepareProcess(
                        binDjifix.getAbsolutePath(),
                        this.sourceVideo.getAbsolutePath()
                ).get();
                (new Thread(() -> App.appendLogs(prDjifix.getStdout() + prDjifix.getStderr()))).start();

                String filename = this.sourceVideo.getName().split("[.]")[0];
                File[] files = (new File(this.sourceVideo.getParent())).listFiles((dir, name) -> name.contains(filename + "-repaired.") && !name.toLowerCase().contains(".mp4"));
                if(files != null && files.length > 0) {
                    log.trace("*-repaired.* files: " + Arrays.stream(files).map(File::getName).reduce((a, b) -> a + ", " + b).get());
                    File repairedFile = files[0];

                    App.setStatus("converting to mp4...");
                    File targetMP4 = new File(this.sourceVideo.getParent() + "/" + filename + "-repaired.mp4");
                    if(targetMP4.exists()){
                        if(!targetMP4.delete()){
                            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                            targetMP4 = new File(this.sourceVideo.getParent() + "/" + filename + "-repaired"+ timeStamp +".mp4");
                        }
                    }
                    ProcessResult prffmpeg = (ProcessResult) this.prepareProcess(
                            binFfmpeg.getAbsolutePath(),
                            "-framerate",
                            "30",
                            "-i",
                            repairedFile.getAbsolutePath(),
                            "-c",
                            "copy",
                            targetMP4.getAbsolutePath()
                    ).get();
                    (new Thread(() -> App.appendLogs(prffmpeg.getStdout() + prffmpeg.getStderr()))).start();
                    repairedFile.delete();

                    App.setStatus("repair done.");
                    fromController.enableStartFix(true);
                    fromController.enableSelectVideo(true);
                } else {
                    App.setStatus("Failed: repaired file not exist.");
                    fromController.enableSelectVideo(true);
                }
            } catch (InterruptedException | ExecutionException e) {
                fromController.enableStartFix(true);
                fromController.enableSelectVideo(true);
                e.printStackTrace();
                App.appendLogs("oops: " + e.getMessage());
            }
        })).start();

    }

    protected class ProcessResult {
        private String stdout = "";
        private String stderr = "";

        ProcessResult() { }

        ProcessResult(BufferedReader outReader, BufferedReader errReader) throws IOException {
            String line;
            StringBuilder commandOut = new StringBuilder();
            StringBuilder commandErr = new StringBuilder();
            while ((line = outReader.readLine()) != null) {
                commandOut.append(line);
                commandOut.append(System.getProperty("line.separator"));
            }
            while ((line = errReader.readLine()) != null) {
                commandErr.append(line);
                commandErr.append(System.getProperty("line.separator"));
            }
            this.stdout = commandOut.toString();
            this.stderr = commandErr.toString();
        }

        String getStdout() {
            return stdout;
        }

        String getStderr() {
            return stderr;
        }
    }
}
