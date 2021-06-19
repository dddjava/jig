package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JigDocumentWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentWriter.class);

    JigDocument jigDocument;
    Path directory;
    List<Path> writtenDocuments = new ArrayList<>();

    public JigDocumentWriter(JigDocument jigDocument, Path directory) {
        this.jigDocument = jigDocument;
        this.directory = directory;
    }

    public void writeXlsx(OutputStreamWriter writer) {
        String fileName = jigDocument.fileName() + ".xlsx";
        write(writer, fileName);
    }

    public void writeAs(String extension, Consumer<Writer> consumer) {
        String fileName = jigDocument.fileName() + extension;
        Path outputFilePath = directory.resolve(fileName);
        try (OutputStream out = Files.newOutputStream(outputFilePath);
             OutputStream outputStream = new BufferedOutputStream(out);
             Writer writer = new java.io.OutputStreamWriter(outputStream);
        ) {
            consumer.accept(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        writtenDocuments.add(outputFilePath);
    }

    public void write(OutputStreamWriter writer, String fileName) {
        Path outputFilePath = directory.resolve(fileName);
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        writtenDocuments.add(outputFilePath);
    }

    public void writePath(BiConsumer<Path, List<Path>> biConsumer) {
        biConsumer.accept(directory, writtenDocuments);
    }

    public void markSkip() {
        LOGGER.info("出力対象がないため {} をスキップしました。", jigDocument);
    }

    public List<Path> outputFilePaths() {
        return writtenDocuments;
    }

    public interface OutputStreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }

    public JigDocument jigDocument() {
        return jigDocument;
    }
}
