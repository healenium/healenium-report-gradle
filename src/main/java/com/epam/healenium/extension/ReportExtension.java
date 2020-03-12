package com.epam.healenium.extension;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class ReportExtension {

    private Property<String> serverUrl;
    private Property<String> sessionKey;

    public ReportExtension(Project project) {
        serverUrl = project.getObjects().property(String.class);
        sessionKey = project.getObjects().property(String.class);
    }

    public Property<String> getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(Object url) {
        this.serverUrl = (Property<String>) url;
    }

    public Property<String> getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(Object sessionKey) {
        this.sessionKey = (Property<String>)sessionKey;
    }
}
