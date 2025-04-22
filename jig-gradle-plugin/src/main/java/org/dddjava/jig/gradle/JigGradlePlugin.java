package org.dddjava.jig.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

public class JigGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        extensions.create("jig", JigConfig.class);
        TaskContainer tasks = project.getTasks();

        tasks.register("jigReports", JigReportsTask.class).configure(task -> {
            task.setGroup("JIG");
            task.setDescription("Generates JIG documentation for the main source code.");
        });

        tasks.register("verifyJigEnvironment", VerifyJigEnvironmentTask.class).configure(task -> {
            task.setGroup("JIG");
            task.setDescription("Verify JIG environment.");
        });
    }
}
