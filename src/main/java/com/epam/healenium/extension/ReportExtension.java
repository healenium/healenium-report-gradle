package com.epam.healenium.extension;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class ReportExtension {

    private final Property<String> serverUrl;
    private final Property<String> sessionKey;

    public ReportExtension(Project project) {
        serverUrl = project.getObjects().property(String.class);
        sessionKey = project.getObjects().property(String.class);
    }

    public Property<String> getServerUrl() {
        return serverUrl;
    }

    public Property<String> getSessionKey() {
        return sessionKey;
    }

}
