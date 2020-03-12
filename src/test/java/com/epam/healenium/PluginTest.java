package com.epam.healenium;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class PluginTest {

    private static final String PLUGIN_ID = "com.epam.healenium.hlm-report";

    @Test
    public void greetingTest(){
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(PLUGIN_ID);
        assertTrue(project.getPluginManager().hasPlugin(PLUGIN_ID));
        assertNotNull(project.getTasks().getByName("initReport"));
    }
}
