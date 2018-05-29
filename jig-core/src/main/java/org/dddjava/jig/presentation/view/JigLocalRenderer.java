package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.basic.FileWriteFailureException;
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

            JigDocumentLocation jigDocumentLocation = new JigDocumentLocation(jigDocument, outputDirectory);
            modelAndView.render(jigDocumentLocation);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }
}
