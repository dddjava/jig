package jig;

import org.dddjava.jig.gradle.JigReportsTask;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * jig-gradle-pluginのUnitTest
 *
 * POJOからProjectBuilderを使用しての範囲をUnitTestとする。
 */
public class JigPluginUnitTest {

    /**
     * プラグイン適用はProjectBuilderで検証する。
     */
    @Test
    void プラグイン適用でjigReportsタスクが登録される() {
        var project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("org.dddjava.jig-gradle-plugin");

        assertInstanceOf(JigReportsTask.class, project.getTasks().getByName("jigReports"));
    }
}
