package jig.gradle;

import jig.domain.model.identifier.namespace.PackageDepth;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class JigPackageDiagramTask extends DefaultTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigPackageDiagramTask.class);

    @TaskAction
    public void apply() {
        ExtensionContainer extensions = getProject().getExtensions();
        JigPackageDiagramExtension extension = extensions.findByType(JigPackageDiagramExtension.class);

        ServiceFactory serviceFactory = JigImportTask.getServiceFactory(getProject());

        serviceFactory.packageDependencyController(extension.getOutputOmitPrefix())
                .packageDependency(new PackageDepth(extension.getDepth()))
                .write(Paths.get(extension.getOutputDirectory()));
    }
}
