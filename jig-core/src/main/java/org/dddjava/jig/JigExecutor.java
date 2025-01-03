package org.dddjava.jig;

import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JigExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JigExecutor.class);

    public static List<HandleResult> execute(Configuration configuration, SourcePaths sourcePaths) {
        long startTime = System.currentTimeMillis();

        JigSourceReader jigSourceReader = configuration.sourceReader();
        JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();

        jigDocumentGenerator.prepareOutputDirectory();
        var results = jigSourceReader.readPathSource(sourcePaths)
                .map(jigDocumentGenerator::generateDocuments)
                .orElseGet(List::of);

        jigDocumentGenerator.generateIndex(results);
        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);

        return results;
    }
}
