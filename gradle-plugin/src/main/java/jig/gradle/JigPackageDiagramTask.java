package jig.gradle;

import jig.domain.basic.FileWriteFailureException;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.infrastructure.LocalProject;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JigPackageDiagramTask extends DefaultTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigPackageDiagramTask.class);

    ServiceFactory serviceFactory = new ServiceFactory();

    @TaskAction
    public void apply() throws IOException {
        ExtensionContainer extensions = getProject().getExtensions();
        JigPackageDiagramExtension extension = extensions.findByType(JigPackageDiagramExtension.class);

        long startTime = System.currentTimeMillis();

        Path output = Paths.get(extension.getOutputDiagramName());
        ensureExists(output);

        LocalProject localProject = serviceFactory.localProject(getProject());
        serviceFactory.importService(getProject()).importSources(
                localProject.getSpecificationSources(),
                localProject.getSqlSources(),
                localProject.getTypeNameSources(), localProject.getPackageNameSources());

        PackageDependencies packageDependencies = serviceFactory.dependencyService().packageDependencies(new PackageDepth(extension.getDepth()));
        LOGGER.info("関連数: " + packageDependencies.list().size());

        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            serviceFactory.diagramService(extension.getOutputOmitPrefix()).write(packageDependencies, outputStream);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    private void ensureExists(Path output) {
        try {
            Files.createDirectories(output.getParent());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
