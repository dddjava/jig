package jig.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class JigGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("jigList", JigListExtension.class);
        JigListTask jigList = project.getTasks().create("jigList", JigListTask.class);
        jigList.dependsOn(project.getTasks().findByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME));
    }

}
