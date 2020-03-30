package com.epam.healenium;

import com.epam.healenium.action.BuildReportAction;
import com.epam.healenium.action.InitReportAction;
import com.epam.healenium.extension.ReportExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.Collections;

public class ReportPlugin implements Plugin<Project> {

    private Logger logger = Logging.getLogger(ReportPlugin.class);

    @Override
    public void apply(Project project) {

        ReportExtension extension = project.getExtensions().create("options", ReportExtension.class, project);
        TaskContainer taskContainer = project.getTasks();

        TaskProvider<InitReportAction> initReportTask = buildInitTask(taskContainer);
        TaskProvider<BuildReportAction> buildReportTask = buildCompleteTask(taskContainer, extension);

        project.getTasksByName("test", false).forEach(it-> {
            it.setDependsOn(Collections.singleton(initReportTask));
            it.finalizedBy(buildReportTask);
        });
    }

    /**
     *
     * @param taskContainer
     * @return
     */
    private TaskProvider<InitReportAction> buildInitTask(TaskContainer taskContainer){
        return taskContainer.register(InitReportAction.ACTION_NAME, InitReportAction.class);
    }

    /**
     *
     * @param taskContainer
     * @param extension
     * @return
     */
    private TaskProvider<BuildReportAction> buildCompleteTask(TaskContainer taskContainer, ReportExtension extension){
        TaskProvider<BuildReportAction> task = taskContainer.register(
                BuildReportAction.ACTION_NAME,
                BuildReportAction.class,
                buildAction -> {
                    buildAction.getServerUrl().set(extension.getServerUrl());
                    buildAction.getSessionKey().set(extension.getSessionKey());
                }
        );
        task.configure(action-> action.onlyIf(it-> {
            boolean isDisabled = extension.getSessionKey().getOrElse("").isEmpty();
            if(isDisabled){
                logger.warn("No session key was obtained!");
            }
            return !isDisabled;
        }));
        return task;
    }
}
