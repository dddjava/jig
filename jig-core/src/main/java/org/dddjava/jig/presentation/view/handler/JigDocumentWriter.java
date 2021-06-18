package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
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
import java.util.function.BiConsumer;

public class JigDocumentWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentWriter.class);

    JigDocument jigDocument;
    Path directory;
    List<Path> documentPaths = new ArrayList<>();

    public JigDocumentWriter(JigDocument jigDocument, Path directory) {
        this.jigDocument = jigDocument;
        this.directory = directory;
    }

    public void writeHtml(OutputStreamWriter writer) {
        String fileName = jigDocument.fileName() + ".html";
        write(writer, fileName);
    }

    public void writeXlsx(OutputStreamWriter writer) {
        String fileName = jigDocument.fileName() + ".xlsx";
        write(writer, fileName);
    }

    public void write(OutputStreamWriter writer, String fileName) {
        Path outputFilePath = directory.resolve(fileName);
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        documentPaths.add(outputFilePath);
    }

    public void writePath(BiConsumer<Path, List<Path>> biConsumer) {
        biConsumer.accept(directory, documentPaths);
    }

    public void markSkip() {
        LOGGER.info("出力対象がないため {} をスキップしました。", jigDocument);
    }

    public List<Path> outputFilePaths() {
        return documentPaths;
    }

    public interface OutputStreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }

    public JigDocument jigDocument() {
        return jigDocument;
    }
}
