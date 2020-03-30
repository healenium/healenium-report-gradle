package com.epam.healenium.action;

import com.epam.healenium.client.HealingClient;
import com.epam.healenium.extension.ReportExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.util.PropertiesUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class InitReportAction extends DefaultTask {

    private Logger logger = Logging.getLogger(InitReportAction.class);

    public static final String ACTION_NAME = "initReport";

    private final Directory targetDir;
    private final String configFile = "application.conf";

    public InitReportAction() {
        targetDir = getProject().getLayout().getBuildDirectory().dir("resources/main").get();
    }

    @TaskAction
    public void makeReportRecord() {
        try (Stream<Path> walk = Files.walk(Paths.get(targetDir.toString()))) {
            Optional<String> result = walk.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(it -> it.endsWith(configFile))
                    .findFirst();

            File file = result.map(File::new).orElseGet(() -> targetDir.file(configFile).getAsFile());
            Properties configProperty = loadConfig(file);

            // normalize
            for (String name : configProperty.stringPropertyNames()) {
                String value = configProperty.getProperty(name);
                if (value == null || value.isEmpty()) {
                    configProperty.remove(name);
                    continue;
                }
                if (name.equals(";")) {
                    configProperty.remove(name);
                    configProperty.setProperty(name.concat(value.substring(0, value.indexOf("=")).replaceAll("\\s+", "")), value.substring(value.indexOf("=") + 1).trim());
                }
            }

            //build remote
            String serverHost = configProperty.getProperty("serverHost", null);
            Integer serverPort = stringToInteger(configProperty.getProperty("serverPort", null));
            if (serverHost == null || serverHost.isEmpty() || serverPort == null) {
                logger.warn("Properties 'serverHost' and 'serverPort' are required for report generation!");
                return;
            }

            // get session key
            HealingClient client = new HealingClient(serverHost, serverPort);
            String sessionKey = client.initReport();
            if (sessionKey == null || sessionKey.isEmpty()) {
                logger.warn("Couldn't obtain session key from server!");
                return;
            } else {
                configProperty.setProperty("sessionKey", sessionKey);
            }

            // store session key in plugin extension
            getProject().getExtensions().findByType(ReportExtension.class).getSessionKey().set(sessionKey);
            getProject().getExtensions().findByType(ReportExtension.class).getServerUrl().set(client.getBaseUrl());

            // append key info
            uploadConfig(file, configProperty);

        } catch (Exception ex) {
            logger.error("Fail to walk though resources", ex);
        }
    }

    /**
     * @param file
     * @return
     */
    private Properties loadConfig(File file) {
        Properties configProperty = new Properties();
        if (file.length() > 0) {
            try {
                FileInputStream fileIn = new FileInputStream(file);
                configProperty.load(fileIn);
                fileIn.close();
            } catch (IOException ex) {
                logger.error("Fail to read config file", ex);
            }
        }
        return configProperty;
    }

    /**
     * @param file
     * @param properties
     */
    private void uploadConfig(File file, Properties properties) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            PropertiesUtils.store(properties, fileOut, null, StandardCharsets.ISO_8859_1, "\n");
            fileOut.close();
        } catch (IOException ex) {
            logger.error("Failed to append data", ex);
        }
    }

    public Integer stringToInteger(String inputString) {
        return (inputString == null || inputString.isEmpty()) ? null : Integer.parseInt(inputString);
    }
}
