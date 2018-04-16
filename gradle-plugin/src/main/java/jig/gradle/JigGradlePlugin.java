package jig.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JigGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("jigList", JigListExtension.class);
        project.getTasks().create("jigList", JigListTask.class);
    }

}
