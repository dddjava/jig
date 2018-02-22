package jig.application.service;

import jig.analizer.javaparser.PackageInfoParser;
import jig.analizer.jdeps.JdepsExecutor;
import jig.analizer.jdeps.JdepsResult;
import jig.analizer.plantuml.PlantUmlModelFormatter;
import jig.analizer.plantuml.PlantUmlModelNameFormatter;
import jig.domain.model.dependency.AnalysisCriteria;
import jig.domain.model.dependency.JapaneseNameRepository;
import jig.domain.model.dependency.ModelFormatter;
import jig.domain.model.dependency.Models;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class AnalyzeService {

    @Value("${target.package:.*.domain.model}")
    private String prefix;

    public Models toModels(AnalysisCriteria criteria) {
        JdepsExecutor jdepsExecutor = new JdepsExecutor(criteria);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        return jdepsResult.toModels();
    }

    public ModelFormatter modelFormatter(Path sourceRootPath) {
        PackageInfoParser packageInfoParser = new PackageInfoParser(sourceRootPath);
        JapaneseNameRepository japaneseNameRepository = packageInfoParser.parse();

        return new PlantUmlModelFormatter(new PlantUmlModelNameFormatter(prefix, japaneseNameRepository));
    }
}
