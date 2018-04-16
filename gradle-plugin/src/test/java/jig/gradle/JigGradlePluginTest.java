package jig.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JigGradlePluginTest {

    @Test
    void test() {
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply("com.github.irof.Jig");
        Task task = project.getTasks().findByName("jigList");

        assertThat(task).isInstanceOf(JigListTask.class);
    }



}