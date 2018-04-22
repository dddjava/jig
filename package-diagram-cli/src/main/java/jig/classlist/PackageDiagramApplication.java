package jig.classlist;

import jig.application.service.DependencyService;
import jig.application.usecase.ImportLocalProjectService;
import jig.domain.basic.FileWriteFailureException;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDiagramApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDiagramApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PackageDiagramApplication.class, args);
    }

    @Value("${output.diagram.name}")
    String outputDiagramName;

    @Value("${depth}")
    int depth;

    @Autowired
    ImportLocalProjectService importLocalProjectService;
    @Autowired
    DependencyService dependencyService;

    @Autowired
    PackageDependencyWriter writer;

    @Override
    public void run(String... args) {
        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        importLocalProjectService.importProject();

        LOGGER.info("パッケージ依存情報を取得します(設定深度: {})", this.depth);
        PackageDependencies packageDependencies = dependencyService.packageDependencies()
                .applyDepth(new PackageDepth(this.depth));

        LOGGER.info("出力する関連数: {}", packageDependencies.number().asText());

        Path path = Paths.get(outputDiagramName);
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            writer.write(packageDependencies, outputStream);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}

