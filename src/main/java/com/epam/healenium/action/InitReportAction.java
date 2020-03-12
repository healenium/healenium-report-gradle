package com.epam.healenium.action;

import com.epam.healenium.ReportPlugin;
import com.epam.healenium.client.HealingClient;
import com.epam.healenium.extension.ReportExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.util.PropertiesUtils;

public class InitReportAction extends DefaultTask {

    private Logger logger = Logging.getLogger(InitReportAction.class);

    public static final String ACTION_NAME = "initReport";

    private final Property<String> serverUrl;
    private final Directory targetDir;
    private final String configFile = "application.conf";

    public InitReportAction() {
        serverUrl = getProject().getObjects().property(String.class);
        targetDir = getProject().getLayout().getBuildDirectory().dir("resources/main").get();
    }

    @Input
    public Property<String> getServerUrl() {
        return serverUrl;
    }

    @TaskAction
    public void makeReportRecord() {
        HealingClient client = new HealingClient(serverUrl.get());
        try (Stream<Path> walk = Files.walk(Paths.get(targetDir.toString()))){
            Optional<String> result = walk.filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(it-> it.endsWith(configFile))
                .findFirst();

            Properties configProperty = new Properties();
            File file = result.map(File::new).orElseGet(() -> targetDir.file(configFile).getAsFile());
            if(file.length() > 0 ){
                try{
                    FileInputStream fileIn = new FileInputStream(file);
                    configProperty.load(fileIn);
                    fileIn.close();
                } catch (IOException ex){
                    logger.error("Fail to read config file", ex);
                }
            }
            // get session key
            String sessionKey = client.initReport();
            if(sessionKey==null || sessionKey.isEmpty()){
                logger.warn("Couldn't obtain session key from server!");
                return;
            }
            // store session key in plugin extension
            getProject().getExtensions().findByType(ReportExtension.class).getSessionKey().set(sessionKey);

            URL url = new URL(serverUrl.get());
            configProperty.setProperty("sessionKey", sessionKey);
            configProperty.setProperty("serverHost", url.getHost());
            configProperty.setProperty("serverPort", String.valueOf(url.getPort()));
            // append info
            try {
                FileOutputStream fileOut = new FileOutputStream(file);
                PropertiesUtils.store(configProperty, fileOut, null, StandardCharsets.ISO_8859_1, "\n");
                fileOut.close();
            } catch (IOException ex){
                logger.error("Failed to append data", ex);
            }
        } catch (Exception ex){
            logger.error("Fail to walk though resources", ex);
        }
    }

}
