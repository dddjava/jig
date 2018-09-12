package org.dddjava.jig.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;

public class JigGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        extensions.create("jig", JigConfig.class);
        TaskContainer tasks = project.getTasks();

        JigReportsTask jigReports = tasks.create("jigReports", JigReportsTask.class);
        jigReports.setGroup("JIG");
        jigReports.setDescription("Generates JIG documentation for the main source code.");

        Task compileJava = tasks.getByName("compileJava");
        if (compileJava == null) {
            throw new IllegalStateException("Java プロジェクトではありません。");
        }
        jigReports.dependsOn(compileJava);

        Task clean = tasks.getByName(LifecycleBasePlugin.CLEAN_TASK_NAME);
        clean.doLast(task -> {
            JigConfig config = task.getProject().getExtensions().findByType(JigConfig.class);
            File outputDir = new File(config.getOutputDirectory());
            clean(outputDir);
        });
    }

    public void clean(File dir) {
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    clean(file);
                    continue;
                }
                file.delete();
            }
        }
        dir.delete();
    }
}
