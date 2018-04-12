package jig.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

class JigGradlePluginTest {

    @Test
    void test() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.github.irof.Jig");

        Set<Task> tasks = project.getTasksByName("jigList", true);

        assertTrue(tasks.size() == 1);
        Task task = tasks.iterator().next();

        List<Action<? super Task>> actions = task.getActions();
        Action<? super Task> action = actions.iterator().next();
        action.execute(task);
    }



}