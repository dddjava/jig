package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.basic.FileWriteFailureException;
import org.dddjava.jig.domain.model.report.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JigLocalRenderer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigLocalRenderer.class);

    JigDocument jigDocument;
    JigModelAndView<T> modelAndView;

    public JigLocalRenderer(JigDocument jigDocument, JigModelAndView<T> modelAndView) {
        this.jigDocument = jigDocument;
        this.modelAndView = modelAndView;
    }

    public void render(Path outputDirectory) {
        try {
            if (Files.notExists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                LOGGER.info("{} を作成しました。", outputDirectory.toAbsolutePath());
            }

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
            modelAndView.render(jigDocumentWriter);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }
}
