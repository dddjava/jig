package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.JigSourceReadService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzeStatus;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzeStatuses;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class JigExecutor {

    public static List<HandleResult> execute(List<JigDocument> jigDocuments, JigSourceReadService jigSourceReadService, JigDocumentHandlers jigDocumentHandlers, SourcePaths sourcePaths, Path outputDirectory, Logger logger) {
        ResourceBundle jigMessages = Utf8ResourceBundle.messageBundle();
        AnalyzedImplementation implementations = jigSourceReadService.readSourceFromPaths(sourcePaths);

        AnalyzeStatuses status = implementations.status();
        if (status.hasError()) {
            logger.warn(jigMessages.getString("failure"));
            for (AnalyzeStatus analyzeStatus : status.listErrors()) {
                logger.warn(jigMessages.getString("failure.details"), jigMessages.getString(analyzeStatus.messageKey));
            }
            return Collections.emptyList();
        }
        if (status.hasWarning()) {
            logger.warn(jigMessages.getString("implementation.warning"));
            for (AnalyzeStatus analyzeStatus : status.listWarning()) {
                logger.warn(jigMessages.getString("implementation.warning.details"), jigMessages.getString(analyzeStatus.messageKey));
            }
        }

        return jigDocumentHandlers.handleJigDocuments(jigDocuments, outputDirectory);
    }
}
