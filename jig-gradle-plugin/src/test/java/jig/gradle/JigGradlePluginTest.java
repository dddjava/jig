package jig.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JigGradlePluginTest {

    @Test
    void test() {
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply("org.dddjava.jig");

        Task jigList = project.getTasks().findByName("jigList");
        Task jigPackageDiagram = project.getTasks().findByName("jigPackageDiagram");
        assertThat(jigList).isInstanceOf(JigListTask.class);
        assertThat(jigPackageDiagram).isInstanceOf(JigPackageDiagramTask.class);
    }
}
