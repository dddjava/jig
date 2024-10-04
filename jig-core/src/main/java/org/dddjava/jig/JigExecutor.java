package org.dddjava.jig;

import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;

import java.util.List;

public class JigExecutor {

    public static List<HandleResult> execute(Configuration configuration, SourcePaths sourcePaths) {
        JigSourceReader jigSourceReader = configuration.sourceReader();
        JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();

        return jigSourceReader.readSource(sourcePaths)
                .map(jigSource -> jigDocumentGenerator.generate(jigSource))
                .orElseGet(List::of);
    }
}
