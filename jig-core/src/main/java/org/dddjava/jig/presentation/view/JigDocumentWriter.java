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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JigDocumentWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentWriter.class);

    JigDocument jigDocument;
    Path directory;
    List<Path> outputFilePaths = new ArrayList<>();

    public JigDocumentWriter(JigDocument jigDocument, Path directory) {
        this.jigDocument = jigDocument;
        this.directory = directory;
    }

    public void writeDiagram(OutputStreamWriter writer, DiagramFormat diagramFormat, DocumentSuffix documentSuffix) {
        Path outputFilePath = directory.resolve(documentSuffix.withFileNameOf(jigDocument) + "." + diagramFormat.name().toLowerCase(Locale.ENGLISH));
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
            outputFilePaths.add(outputFilePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeXlsx(OutputStreamWriter writer) {
        Path outputFilePath = directory.resolve(jigDocument.fileName() + ".xlsx");
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
            outputFilePaths.add(outputFilePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void skip() {
        LOGGER.info("出力対象がないため {} をスキップしました。", jigDocument);
    }

    public List<Path> outputFilePaths() {
        return outputFilePaths;
    }

    public interface OutputStreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }
}
