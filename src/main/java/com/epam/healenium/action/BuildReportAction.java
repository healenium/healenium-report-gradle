package com.epam.healenium.action;

import com.epam.healenium.client.HealingClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class BuildReportAction extends DefaultTask {

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
    public void makeReportRecord() {
        System.out.println("Building report");
        HealingClient client = new HealingClient(serverUrl.get());
        String reportUrl = client.buildReport(sessionKey.get());
        getLogger().log(LogLevel.INFO, "Report available at {}", reportUrl);
        System.out.println("Report available at " + reportUrl);
    }

}
