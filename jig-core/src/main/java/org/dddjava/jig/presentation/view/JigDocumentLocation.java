package org.dddjava.jig.presentation.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JigDocumentLocation {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentLocation.class);

    JigDocument jigDocument;
    Path directory;

    public JigDocumentLocation(JigDocument jigDocument, Path directory) {
        this.jigDocument = jigDocument;
        this.directory = directory;
    }

    public void writeDocument(JigDocumentWriter writer) {
        Path outputFilePath = directory.resolve(jigDocument.fileName());
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
            LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeDebugText(String s) {
        try {
            Path outputFilePath = directory.resolve(jigDocument.fileName() + ".txt");
            Files.write(outputFilePath, s.getBytes());
            LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public interface JigDocumentWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }
}
