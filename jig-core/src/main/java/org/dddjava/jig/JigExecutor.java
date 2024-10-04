package org.dddjava.jig;

import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigSourceReadService;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.jigreader.ReadStatus;
import org.dddjava.jig.domain.model.sources.jigreader.ReadStatuses;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JigExecutor {
    static Logger logger = LoggerFactory.getLogger(JigExecutor.class);

    public static List<HandleResult> execute(Configuration configuration, SourcePaths sourcePaths) {
        JigSourceReadService jigSourceReadService = configuration.sourceReader();

        ReadStatuses status = jigSourceReadService.readSourceFromPaths(sourcePaths);
        if (status.hasError()) {
            for (ReadStatus readStatus : status.listErrors()) {
                logger.error("{}", readStatus.localizedMessage());
            }
            return List.of();
        }
        if (status.hasWarning()) {
            for (ReadStatus readStatus : status.listWarning()) {
                logger.warn("{}", readStatus.localizedMessage());
            }
        }

        JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();
        return jigDocumentGenerator.handleJigDocuments();
    }

}
