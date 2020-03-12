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

        TaskProvider<InitReportAction> initReportTask = buildInitTask(taskContainer, extension);
        TaskProvider<BuildReportAction> buildReportTask = buildCompleteTask(taskContainer, extension);

        project.getTasksByName("test", false).forEach(it-> {
            it.setDependsOn(Collections.singleton(initReportTask));
            it.finalizedBy(buildReportTask);
        });
    }

    /**
     *
     * @param taskContainer
     * @param extension
     * @return
     */
    private TaskProvider<InitReportAction> buildInitTask(TaskContainer taskContainer, ReportExtension extension){

        TaskProvider<InitReportAction> task = taskContainer.register(
                InitReportAction.ACTION_NAME,
                InitReportAction.class,
                initAction -> initAction.getServerUrl().set(extension.getServerUrl())
        );

        task.configure(action-> action.onlyIf(it-> {
            String url = extension.getServerUrl().getOrElse("");
            boolean isEnabled = !url.isEmpty();
            if(isEnabled){
                logger.info("Use remote server url: {} ", url);
            } else {
                logger.warn("No server url specified!");
            }
            return isEnabled;
        }));

        return task;
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
            String url = extension.getServerUrl().getOrElse("");
            String key = extension.getSessionKey().getOrElse("");
            return !url.isEmpty() && !key.isEmpty();
        }));

        return task;
    }
}
