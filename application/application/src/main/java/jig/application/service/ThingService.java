package jig.application.service;

import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.jdeps.JdepsResult;
import jig.infrastructure.plantuml.PlantUmlNameFormatter;
import jig.infrastructure.plantuml.PlantUmlThingFormatter;
import jig.model.jdeps.AnalysisCriteria;
import jig.model.tag.JapaneseNameDictionary;
import jig.model.thing.ThingFormatter;
import jig.model.thing.Things;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ThingService {

    @Value("${target.package:.*.domain.model}")
    private String prefix;


    public Things toModels(AnalysisCriteria criteria) {
        JdepsExecutor jdepsExecutor = new JdepsExecutor(criteria);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        return jdepsResult.toModels();
    }

    public ThingFormatter modelFormatter(JapaneseNameDictionary japaneseNameDictionary) {
        return new PlantUmlThingFormatter(new PlantUmlNameFormatter(prefix, japaneseNameDictionary));
    }
}
