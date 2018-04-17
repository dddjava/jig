package jig.gradle;

import jig.domain.model.diagram.Diagram;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.jdeps.*;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class JigPackageDiagramTask extends DefaultTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigPackageDiagramTask.class);

    ServiceFactory serviceFactory = new ServiceFactory();

    @TaskAction
    public void apply() throws IOException {

        JigPackageDiagramExtension extension = getProject().getExtensions().findByType(JigPackageDiagramExtension.class);

        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        long startTime = System.currentTimeMillis();

        Path projectPath = getProject().getProjectDir().toPath();
        Path output = Paths.get(extension.getOutputDiagramName());
        ensureExists(output);

        PackageDependencies packageDependencies = serviceFactory.analyzeService().packageDependencies(new ProjectLocation(projectPath));

        PackageDependencies jdepsPackageDependencies = serviceFactory.relationAnalyzer().analyzeRelations(new AnalysisCriteria(
                new SearchPaths(Collections.singletonList(projectPath)),
                new AnalysisClassesPattern(extension.getPackagePattern() + "\\..+"),
                new DependenciesPattern(extension.getPackagePattern() + "\\..+"),
                AnalysisTarget.PACKAGE));

        List<PackageDependency> list = packageDependencies.list();
        List<PackageDependency> jdepsList = jdepsPackageDependencies.list();
        LOGGER.debug("件数       : " + list.size());
        LOGGER.debug("件数(jdeps): " + jdepsList.size());
        jdepsList.stream()
                .filter(relation -> !list.contains(relation))
                .forEach(relation -> LOGGER.debug("jdepsでのみ検出された依存: " + relation.from().value() + " -> " + relation.to().value()));

        PackageDependencies outputRelation = jdepsPackageDependencies
                // class解析で取得できたModelのパッケージで上書きする
                .withAllPackage(packageDependencies.allPackages())
                .applyDepth(new PackageDepth(extension.getDepth()));
        LOGGER.info("関連数: " + outputRelation.list().size());

        Diagram diagram = serviceFactory.diagramService(extension.getOutputOmitPrefix()).generateFrom(outputRelation);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
        LOGGER.info(output.toAbsolutePath() + "を出力しました。");

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
