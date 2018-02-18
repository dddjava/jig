package jig;

import jig.analizer.dependency.ModelFormatter;
import jig.analizer.dependency.Models;
import jig.application.service.AnalyzeService;
import jig.domain.model.DiagramSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PackageDependency {

    public static void main(String[] paths) {
        AnalyzeService analyzeService = new AnalyzeService();

        Models models = analyzeService.toModels(
                Arrays.stream(paths).map(Paths::get).collect(Collectors.toList()));

        Path sourceRootPath = Paths.get("./");

        ModelFormatter modelFormatter = analyzeService.modelFormatter(sourceRootPath);

        DiagramSource diagramSource = analyzeService.toDiagramSource(models, modelFormatter);

        System.out.println(diagramSource.value());
    }
}
