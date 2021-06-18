package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.JigSourceReadService;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.jigreader.ReadStatus;
import org.dddjava.jig.domain.model.sources.jigreader.ReadStatuses;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class JigExecutor {
    static Logger logger = LoggerFactory.getLogger(JigExecutor.class);

    public static List<HandleResult> execute(Configuration configuration, SourcePaths sourcePaths) {

        JigSourceReadService jigSourceReadService = configuration.implementationService();
        JigDocumentHandlers jigDocumentHandlers = configuration.documentHandlers();

        ReadStatuses status = jigSourceReadService.readSourceFromPaths(sourcePaths);
        if (status.hasError()) {
            for (ReadStatus readStatus : status.listErrors()) {
                logger.error("{}", readStatus.localizedMessage());
            }
            return Collections.emptyList();
        }
        if (status.hasWarning()) {
            for (ReadStatus readStatus : status.listWarning()) {
                logger.warn("{}", readStatus.localizedMessage());
            }
        }

        return jigDocumentHandlers.handleJigDocuments(configuration.jigDocuments(), configuration.outputDirectory());
    }
}
