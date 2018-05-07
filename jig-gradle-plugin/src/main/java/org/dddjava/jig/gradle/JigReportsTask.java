package org.dddjava.jig.gradle;

import org.dddjava.jig.application.usecase.ImportService;
import org.dddjava.jig.domain.model.DocumentType;
import org.dddjava.jig.domain.model.identifier.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceMethodCallHierarchyController;
import org.dddjava.jig.presentation.controller.classlist.ClassListController;
import org.dddjava.jig.presentation.view.LocalView;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;

public class JigReportsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(JigReportsTask.class);

    Dependencies dependencies = new Dependencies();


    @TaskAction
    void outputReports() {

        ExtensionContainer extensions = getProject().getExtensions();
        JigConfig config = extensions.findByType(JigConfig.class);

        List<DocumentType> documentTypes = config.documentTypes();

        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        LocalProject localProject = dependencies.localProject(getProject());
        ImportService importService = dependencies.importService();
        importService.importSources(
                localProject.getSpecificationSources(),
                localProject.getSqlSources(),
                localProject.getTypeNameSources(),
                localProject.getPackageNameSources());

        for (DocumentType documentType : documentTypes) {
            writer(documentType, config).write(Paths.get(config.getOutputDirectory()));
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }


    private LocalView writer(DocumentType documentType, JigConfig config) {
        if (documentType == DocumentType.ServiceMethodCallHierarchy) {
            ServiceMethodCallHierarchyController serviceMethodCallHierarchyController = dependencies.serviceMethodCallHierarchyController(config.getOutputOmitPrefix());
            return serviceMethodCallHierarchyController.serviceMethodCallHierarchy();
        } else if (documentType == DocumentType.PackageDependency) {
            PackageDependencyController packageDependencyController = dependencies.packageDependencyController(config.getOutputOmitPrefix());
            return packageDependencyController.packageDependency(new PackageDepth(config.getDepth()));
        } else if (documentType == DocumentType.ApplicationList) {
            ClassListController classListController = dependencies.classListController(config.getOutputOmitPrefix());
            return classListController.applicationList();
        } else if (documentType == DocumentType.DomainList) {
            ClassListController classListController = dependencies.classListController(config.getOutputOmitPrefix());
            return classListController.domainList();
        } else if (documentType == DocumentType.EnumUsage) {
            EnumUsageController enumUsageController = dependencies.enumUsageController(config.getOutputOmitPrefix());
            return enumUsageController.enumUsage();
        }
        throw new IllegalArgumentException(documentType.toString());
    }
}
