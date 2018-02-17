package jig.analizer;

import jig.analizer.dependency.JapaneseNameRepository;
import jig.analizer.dependency.Models;
import jig.analizer.jdeps.JdepsExecutor;
import jig.analizer.jdeps.JdepsResult;
import jig.analizer.plantuml.PlantUmlModelFormatter;
import jig.analizer.plantuml.PlantUmlModelNameFormatter;

public class PackageDependency {

    public static final String DEFAULT_TARGET_PREFIX = ".*.domain.model";

    public static void main(String[] paths) {
        String targetPattern = DEFAULT_TARGET_PREFIX + "\\.(.*)";

        JdepsExecutor jdepsExecutor = new JdepsExecutor(targetPattern, targetPattern, paths);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        Models models = jdepsResult.toModels();

        JapaneseNameRepository japaneseNameRepository = new JapaneseNameRepository();

        String text = models.format(new PlantUmlModelFormatter(new PlantUmlModelNameFormatter(targetPattern, japaneseNameRepository)));

        System.out.println(text);
    }
}
