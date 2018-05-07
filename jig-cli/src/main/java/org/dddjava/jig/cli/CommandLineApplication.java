package org.dddjava.jig.cli;

import org.dddjava.jig.application.usecase.ImportService;
import org.dddjava.jig.domain.model.DocumentType;
import org.dddjava.jig.domain.model.identifier.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceMethodCallHierarchyController;
import org.dddjava.jig.presentation.controller.classlist.ClassListController;
import org.dddjava.jig.presentation.view.LocalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication(scanBasePackages = "org.dddjava.jig")
public class CommandLineApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApplication.class, args);
    }

    @Value("${documentType:}")
    String documentTypeText;
    @Value("${outputDirectory}")
    String outputDirectory;
    @Value("${depth:-1}")
    int depth;

    @Autowired
    ImportService importService;
    @Autowired
    LocalProject localProject;
    @Autowired
    ServiceMethodCallHierarchyController serviceMethodCallHierarchyController;
    @Autowired
    ClassListController classListController;
    @Autowired
    PackageDependencyController packageDependencyController;
    @Autowired
    EnumUsageController enumUsageController;

    @Override
    public void run(String... args) throws IOException {
        List<DocumentType> documentTypes =
                documentTypeText.isEmpty()
                        ? Arrays.asList(DocumentType.values())
                        : DocumentType.resolve(documentTypeText);


        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        importService.importSources(
                localProject.getSpecificationSources(),
                localProject.getSqlSources(),
                localProject.getTypeNameSources(),
                localProject.getPackageNameSources());

        for (DocumentType documentType : documentTypes) {
            writer(documentType).write(Paths.get(outputDirectory));
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }

    private LocalView writer(DocumentType documentType) {
        if (documentType == DocumentType.ServiceMethodCallHierarchy) {
            return serviceMethodCallHierarchyController.serviceMethodCallHierarchy();
        } else if (documentType == DocumentType.PackageDependency) {
            return packageDependencyController.packageDependency(new PackageDepth(this.depth));
        } else if (documentType == DocumentType.ApplicationList) {
            return classListController.applicationList();
        } else if (documentType == DocumentType.DomainList) {
            return classListController.domainList();
        } else if (documentType == DocumentType.EnumUsage) {
            return enumUsageController.enumUsage();
        }
        throw new IllegalArgumentException(documentType.toString());
    }

}
