package jig.cli;

import jig.analizer.dependency.ModelFormatter;
import jig.analizer.dependency.Models;
import jig.application.service.AnalyzeService;
import jig.application.service.DiagramService;
import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackages = "jig")
public class PackageDependencyCliApplication implements CommandLineRunner {

    public static void main(String[] args) {
        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        SpringApplication.run(PackageDependencyCliApplication.class, args);
    }

    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    DiagramService diagramService;

    @Override
    public void run(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("usage: cli.jar <options> <jar or classes directories...>");
            System.out.println("  -source  ソースコードの含まれているディレクトリを指定します。");
            System.out.println("           デフォルトは ./src です。");
            System.out.println("  -output  出力ファイル名を指定します。");
            System.out.println("           デフォルトは ./diagram.png です。");
            return;
        }

        List<Path> searchPaths = new ArrayList<>();
        Path sourceRoot = Paths.get("./src");
        Path output = Paths.get("./diagram.png");

        ParameterType parameterType = ParameterType.NONE;
        for (String arg : args) {
            if (arg.equals("-source")) {
                parameterType = ParameterType.SOURCE;
                continue;
            }
            if (arg.equals("-output")) {
                parameterType = ParameterType.OUTPUT;
                continue;
            }

            switch (parameterType) {
                case NONE:
                    Path jarPath = Paths.get(arg);
                    if (Files.notExists(jarPath)) {
                        throw new IllegalArgumentException("存在するパスを指定してください");
                    }
                    searchPaths.add(jarPath);
                    continue;
                case SOURCE:
                    sourceRoot = Paths.get(arg);
                    if (!Files.isDirectory(sourceRoot)) {
                        throw new IllegalArgumentException("-sourceはディレクトリを指定してください");
                    }
                    parameterType = ParameterType.NONE;
                    continue;
                case OUTPUT:
                    output = Paths.get(arg);
                    parameterType = ParameterType.NONE;
                    continue;
            }

            System.err.println("ignore:" + arg);
        }

        if (searchPaths.isEmpty()) {
            throw new IllegalArgumentException("検索対象パスを一つ以上指定してください");
        }

        Models models = analyzeService.toModels(searchPaths);
        ModelFormatter modelFormatter = analyzeService.modelFormatter(sourceRoot);
        DiagramSource diagramSource = diagramService.toDiagramSource(models, modelFormatter);
        DiagramIdentifier identifier = diagramService.request(diagramSource);
        diagramService.generate(identifier);
        Diagram diagram = diagramService.get(identifier);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output))) {
            outputStream.write(diagram.getBytes());
        }
    }
}

enum ParameterType {
    SOURCE,
    OUTPUT,
    NONE
}
