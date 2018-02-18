package jig.analizer;

import jig.analizer.dependency.JapaneseNameRepository;
import jig.analizer.dependency.Models;
import jig.analizer.javaparser.PackageInfoParser;
import jig.analizer.jdeps.JdepsExecutor;
import jig.analizer.jdeps.JdepsResult;
import jig.analizer.plantuml.PlantUmlModelFormatter;
import jig.analizer.plantuml.PlantUmlModelNameFormatter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PackageDependency {

    public static final String DEFAULT_TARGET_PREFIX = ".*.domain.model";

    public static void main(String[] paths) {
        String targetPattern = DEFAULT_TARGET_PREFIX + "\\.(.*)";

        JdepsExecutor jdepsExecutor = new JdepsExecutor(targetPattern, targetPattern, paths);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        Models models = jdepsResult.toModels();

        Path sourceRootPath = Paths.get("ソースを探すディレクトリ");
        PackageInfoParser packageInfoParser = new PackageInfoParser(sourceRootPath);
        JapaneseNameRepository japaneseNameRepository = packageInfoParser.parse();

        String text = models.format(new PlantUmlModelFormatter(new PlantUmlModelNameFormatter(targetPattern, japaneseNameRepository)));

        System.out.println(text);
    }
}
