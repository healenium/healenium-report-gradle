package com.epam.healenium.action;

import com.epam.healenium.client.HealingClient;
import com.epam.healenium.extension.ReportExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
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
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class InitReportAction extends DefaultTask {

    public static final String ACTION_NAME = "initReport";

    private final String configFile = "healenium.properties";
    private final Logger logger = Logging.getLogger(InitReportAction.class);

    public InitReportAction() {
    }

    @TaskAction
    public void makeReportRecord() {
        ProjectLayout layout = getProject().getLayout();
        Directory targetDir = layout.getBuildDirectory().dir("resources/main").get();
        Directory sourceDir = layout.getProjectDirectory().dir("src/test/resources");

        ReportExtension extension = getProject().getExtensions().findByType(ReportExtension.class);
        if (extension == null) return;

        findConfig(targetDir, sourceDir).ifPresent(it -> {
            try {
                Properties configProperty = loadConfig(it);
                // normalize
                normalizeProperties(configProperty);

                //build remote
                String serverHost = configProperty.getProperty("serverHost", "localhost");
                Integer serverPort = stringToInteger(configProperty.getProperty("serverPort", "7878"));

                HealingClient client = new HealingClient(serverHost, serverPort);

                // get session key
                String sessionKey = client.initReport();
                if (sessionKey == null || sessionKey.isEmpty()) {
                    logger.warn("Couldn't obtain session key from server!");
                    return;
                }

                configProperty.setProperty("sessionKey", sessionKey);

                // store session key in plugin extension
                extension.getSessionKey().set(sessionKey);
                extension.getServerUrl().set(client.getBaseUrl());

                // append key info
                uploadConfig(it, configProperty);
            } catch (Exception ex) {
                logger.error("Failed to perform init action", ex);
            }
        });

    }

    private Optional<File> findConfig(Directory targetDir, Directory sourceDir) {
        Optional<String> result = Optional.empty();
        try (Stream<Path> walk = Files.walk(Paths.get(targetDir.toString()))) {
            result = walk.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(it -> it.endsWith(configFile))
                    .findFirst();
        } catch (Exception ex) {
            // no logging
        }
        File file = result.map(File::new)
                .orElseGet(() -> {
                    Path fromPath = Paths.get(String.valueOf(sourceDir), configFile);
                    Path toPath = Paths.get(String.valueOf(targetDir), configFile);
                    try {
                        Files.createDirectories(toPath.getParent());
                        if (sourceDir.file(configFile).getAsFile().exists()) {
                            Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            Files.createFile(toPath);
                        }
                        return targetDir.file(configFile).getAsFile();
                    } catch (IOException ex) {
                        logger.error("Failed to create config file", ex);
                        return null;
                    }
                });
        return Optional.ofNullable(file);
    }

    private void normalizeProperties(Properties configProperty) {
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
