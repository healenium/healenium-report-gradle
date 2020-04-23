package com.epam.healenium;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PluginTest {

    private static final String PLUGIN_ID = "com.epam.healenium.hlm-report";

    @Test
    public void greetingTest(){
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(PLUGIN_ID);
        Assertions.assertTrue(project.getPluginManager().hasPlugin(PLUGIN_ID));
        Assertions.assertNotNull(project.getTasks().getByName("initReport"));
    }
}
