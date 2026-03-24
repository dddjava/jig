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
            task.getDiagramFormat().convention(project.provider(() -> config.getDiagramFormat().name()));
            task.getDiagramTransitiveReduction().convention(project.provider(config::isDiagramTransitiveReduction));
            task.getDotTimeout().convention(project.provider(config::getDotTimeout));

            // 出力ディレクトリ
            task.getOutputDirectory().convention(project.provider(() -> {
                String outputDir = config.getOutputDirectory();
                if (outputDir.isEmpty()) {
                    return project.getLayout().getBuildDirectory().dir("jig").get();
                }
                return project.getLayout().getProjectDirectory().dir(outputDir);
            }));

            // Java プラグインの適用状態
            task.getJavaPluginApplied().convention(project.provider(() -> GradleProject.isJavaProject(project)));

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
