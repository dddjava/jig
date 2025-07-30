package org.dddjava.jig.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class VerifyJigEnvironmentTask extends DefaultTask {

    @TaskAction
    @Deprecated
    void verify() {
        getLogger().warn("verifyタスクは廃止になります。");
    }
}
