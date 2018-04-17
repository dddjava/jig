package jig.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class JigGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("jigList", JigListExtension.class);
        TaskContainer tasks = project.getTasks();
        tasks.create("jigList", JigListTask.class);
        tasks.create("jigPackageDiagram", JigPackageDiagramTask.class);
    }

}
