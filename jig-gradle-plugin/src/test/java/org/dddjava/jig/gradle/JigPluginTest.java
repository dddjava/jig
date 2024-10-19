package org.dddjava.jig.gradle;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class JigPluginTest {

    @Test
    void プラグイン適用でjigReportsタスクが登録される() {
        var project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("org.dddjava.jig-gradle-plugin");

        assertInstanceOf(JigReportsTask.class, project.getTasks().getByName("jigReports"));
    }
}
