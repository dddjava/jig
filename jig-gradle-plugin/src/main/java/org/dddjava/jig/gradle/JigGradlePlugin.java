package org.dddjava.jig.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

import java.nio.file.Path;

public class JigGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        JigConfig config = extensions.create("jig", JigConfig.class);
        TaskContainer tasks = project.getTasks();

        tasks.register("jigReports", JigReportsTask.class).configure(task -> {
            task.setGroup("JIG");
            task.setDescription("Generates JIG documentation for the main source code.");

            // JigConfig のプロパティをタスクプロパティにワイヤリング
            task.getModelPattern().convention(project.provider(config::getModelPattern));
            task.getDocumentTypes().convention(project.provider(config::getDocumentTypes));
            task.getDocumentTypesExclude().convention(project.provider(config::getDocumentTypesExclude));

            // 出力ディレクトリ（project をキャプチャしないよう configuration phase で解決）
            String outputDir = config.getOutputDirectory();
            if (outputDir.isEmpty()) {
                task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("jig"));
            } else {
                task.getOutputDirectory().set(project.getLayout().getProjectDirectory().dir(outputDir));
            }

            // Java プラグインの適用状態（configuration phase で解決）
            task.getJavaPluginApplied().convention(GradleProject.isJavaProject(project));

            // ソース/クラスパス（configuration phase で解決し直接設定。
            // Provider でラップすると project をキャプチャしてしまい、Gradle 8 の configuration cache でシリアライズできないため。）
            if (GradleProject.isJavaProject(project)) {
                GradleProject gp = new GradleProject(project);
                task.getClassFiles().from(gp.allClassPaths().stream().map(Path::toFile).toList());
                task.getSourceFiles().from(gp.allSourcePaths().stream().map(Path::toFile).toList());
            }
        });
    }
}
