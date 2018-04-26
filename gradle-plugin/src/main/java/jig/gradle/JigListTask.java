package jig.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class JigListTask extends DefaultTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigListTask.class);

    @TaskAction
    public void apply() {
        ExtensionContainer extensions = getProject().getExtensions();
        JigListExtension extension = extensions.findByType(JigListExtension.class);

        ServiceFactory serviceFactory = JigImportTask.getServiceFactory(getProject());

        serviceFactory.classListController(extension.getOutputOmitPrefix())
                .classList()
                .write(Paths.get(extension.getOutputDirectory()));
    }

}
