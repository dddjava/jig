package jig.application.service;

import jig.analizer.javaparser.PackageInfoParser;
import jig.analizer.jdeps.JdepsExecutor;
import jig.analizer.jdeps.JdepsResult;
import jig.analizer.plantuml.PlantUmlThingFormatter;
import jig.analizer.plantuml.PlantUmlNameFormatter;
import jig.domain.model.jdeps.AnalysisCriteria;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.domain.model.thing.ThingFormatter;
import jig.domain.model.thing.Things;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class AnalyzeService {

    @Value("${target.package:.*.domain.model}")
    private String prefix;

    public Things toModels(AnalysisCriteria criteria) {
        JdepsExecutor jdepsExecutor = new JdepsExecutor(criteria);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        return jdepsResult.toModels();
    }

    public ThingFormatter modelFormatter(Path sourceRootPath) {
        PackageInfoParser packageInfoParser = new PackageInfoParser(sourceRootPath);
        JapaneseNameDictionary japaneseNameDictionary = packageInfoParser.parse();

        return new PlantUmlThingFormatter(new PlantUmlNameFormatter(prefix, japaneseNameDictionary));
    }
}
