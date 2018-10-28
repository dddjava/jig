package org.dddjava.jig.presentation.view;

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
    DocumentSuffix documentSuffix;
    Path directory;
    boolean jigDebugMode;

    public JigDocumentWriter(JigDocument jigDocument, Path directory, boolean jigDebugMode) {
        this(jigDocument, new DocumentSuffix(""), directory, jigDebugMode);
    }

    private JigDocumentWriter(JigDocument jigDocument, DocumentSuffix documentSuffix, Path directory, boolean jigDebugMode) {
        this.jigDocument = jigDocument;
        this.documentSuffix = documentSuffix;
        this.directory = directory;
        this.jigDebugMode = jigDebugMode;
    }

    public void writeDiagram(OutputStreamWriter writer, DiagramFormat diagramFormat) {
        Path outputFilePath = directory.resolve(documentSuffix.withFileNameOf(jigDocument) + "." + diagramFormat.name().toLowerCase(Locale.ENGLISH));
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
            LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeXlsx(OutputStreamWriter writer) {
        Path outputFilePath = directory.resolve(documentSuffix.withFileNameOf(jigDocument) + ".xlsx");
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
            LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeDebugText(String s) {
        if (!jigDebugMode) return;
        try {
            Path outputFilePath = directory.resolve(documentSuffix.withFileNameOf(jigDocument) + ".txt");
            Files.write(outputFilePath, s.getBytes());
            LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JigDocumentWriter apply(DocumentSuffix documentSuffix) {
        return new JigDocumentWriter(this.jigDocument, documentSuffix, this.directory, this.jigDebugMode);
    }

    public void skip() {
        LOGGER.info("出力対象がないため {} をスキップしました。", jigDocument);
    }

    public interface OutputStreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }
}
