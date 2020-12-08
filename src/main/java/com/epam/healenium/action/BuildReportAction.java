package com.epam.healenium.action;

import com.epam.healenium.client.HealingClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.net.MalformedURLException;
import java.net.URL;

public class BuildReportAction extends DefaultTask {

    private final Logger logger = Logging.getLogger(BuildReportAction.class);

    public static final String ACTION_NAME = "buildReport";

    private final Property<String> serverUrl;
    private final Property<String> sessionKey;

    public BuildReportAction() {
        serverUrl = getProject().getObjects().property(String.class);
        sessionKey = getProject().getObjects().property(String.class);
    }

    @Input
    public Property<String> getServerUrl() {
        return serverUrl;
    }

    @Input
    public Property<String> getSessionKey() {
        return sessionKey;
    }

    @TaskAction
    public void makeReportRecord() throws MalformedURLException {
        HealingClient client = new HealingClient(serverUrl.get());
        String reportUrl = client.buildReport(sessionKey.get());
        if (reportUrl != null && reportUrl.length() > 0) {
            logger.warn("Report available at {}", new URL(new URL(client.getBaseUrl()), reportUrl));
        }
    }

}
