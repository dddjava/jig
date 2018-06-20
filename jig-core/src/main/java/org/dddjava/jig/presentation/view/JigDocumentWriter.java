package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.report.JigDocument;
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class JigDocumentWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentWriter.class);

    JigDocument jigDocument;
    Path directory;

    public JigDocumentWriter(JigDocument jigDocument, Path directory) {
        this.jigDocument = jigDocument;
        this.directory = directory;
    }

    public void writeDiagram(OutputStreamWriter writer, DiagramFormat diagramFormat) {
        Path outputFilePath = directory.resolve(jigDocument.fileName() + "." + diagramFormat.name().toLowerCase(Locale.ENGLISH));
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
            LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeXlsx(OutputStreamWriter writer) {
        Path outputFilePath = directory.resolve(jigDocument.fileName() + ".xlsx");
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

    public interface OutputStreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }
}
