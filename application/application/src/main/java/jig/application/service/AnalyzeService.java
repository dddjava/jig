package jig.application.service;

import jig.analizer.dependency.JapaneseNameRepository;
import jig.analizer.dependency.ModelFormatter;
import jig.analizer.dependency.Models;
import jig.analizer.javaparser.PackageInfoParser;
import jig.analizer.jdeps.JdepsExecutor;
import jig.analizer.jdeps.JdepsResult;
import jig.analizer.plantuml.PlantUmlModelFormatter;
import jig.analizer.plantuml.PlantUmlModelNameFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class AnalyzeService {

    private final String targetPattern;

    public AnalyzeService(@Value("${target.package:.*.domain.model}") String packagePattern) {
        this.targetPattern = packagePattern + "\\.(.*)";
    }

    public Models toModels(List<Path> searchPaths) {
        String[] paths = searchPaths.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .toArray(String[]::new);
        JdepsExecutor jdepsExecutor = new JdepsExecutor(targetPattern, targetPattern, paths);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        return jdepsResult.toModels();
    }

    public ModelFormatter modelFormatter(Path sourceRootPath) {
        PackageInfoParser packageInfoParser = new PackageInfoParser(sourceRootPath);
        JapaneseNameRepository japaneseNameRepository = packageInfoParser.parse();
        return new PlantUmlModelFormatter(new PlantUmlModelNameFormatter(targetPattern, japaneseNameRepository));
    }
}
