package jig.application.service;

import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.jdeps.JdepsResult;
import jig.model.jdeps.AnalysisCriteria;
import jig.model.thing.Things;
import org.springframework.stereotype.Service;

@Service
public class ThingService {

    public Things toModels(AnalysisCriteria criteria) {
        JdepsExecutor jdepsExecutor = new JdepsExecutor(criteria);
        JdepsResult jdepsResult = jdepsExecutor.execute();

        return jdepsResult.toModels();
    }
}
