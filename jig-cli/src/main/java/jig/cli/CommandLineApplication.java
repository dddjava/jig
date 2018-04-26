package jig.cli;

import jig.application.usecase.ImportService;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.infrastructure.LocalProject;
import jig.presentation.controller.ClassListController;
import jig.presentation.controller.PackageDependencyController;
import jig.presentation.controller.ServiceMethodCallHierarchyController;
import jig.presentation.view.LocalView;
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

@SpringBootApplication(scanBasePackages = "jig")
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
        } else if (documentType == DocumentType.ClassList) {
            return classListController.classList();
        }
        throw new IllegalArgumentException(documentType.toString());
    }

}
