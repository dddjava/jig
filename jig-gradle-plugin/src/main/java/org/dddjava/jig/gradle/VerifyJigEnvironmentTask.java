package org.dddjava.jig.gradle;

import org.dddjava.jig.infrastructure.view.graphviz.dot.DotCommandRunner;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

public class VerifyJigEnvironmentTask extends DefaultTask {

    @TaskAction
    void verify() {
        try {
            DotCommandRunner dotCommandRunner = new DotCommandRunner();
            dotCommandRunner.verify();
        } catch (RuntimeException e) {
            Logger logger = getLogger();
            logger.warn("-- JIG ERROR -----------------------------------------------");
            logger.warn("+ 実行可能なGraphvizが見つけられませんでした。");
            logger.warn("+ dotにPATHが通っているか確認してください。");
            logger.warn("+ JIGは図の出力にGraphvizを使用しています。");
            logger.warn("+ ");
            logger.warn("+ Graphvizは以下から入手できます。");
            logger.warn("+     https://www.graphviz.org/");
            logger.warn("------------------------------------------------------------");

            throw e;
        }
    }
}
