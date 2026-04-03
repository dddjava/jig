package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JigDocumentWriter {

    JigDocument jigDocument;
    Path outputDirectory;
    List<Path> writtenDocuments = new ArrayList<>();

    public JigDocumentWriter(JigDocument jigDocument, Path outputDirectory) {
        this.jigDocument = jigDocument;
        this.outputDirectory = outputDirectory;
    }

    public void write(OutputStreamWriter writer, String fileName) {
        Path outputFilePath = outputDirectory.resolve(fileName);
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
            writer.writeTo(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        writtenDocuments.add(outputFilePath);
    }

    public List<Path> outputFilePaths() {
        return writtenDocuments;
    }

    public interface OutputStreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }

    public void writeHtmlTemplate() {
        String fileName = jigDocument.fileName();
        write(
                outputStream -> {
                    try (var resource = JigDocumentWriter.class.getResourceAsStream("/templates/" + fileName + ".html")) {
                        Objects.requireNonNull(resource).transferTo(outputStream);
                    }
                },
                fileName + ".html"
        );
    }

    public void writeJsData(String variableName, String json) {
        String fileName = jigDocument.fileName();
        write(
                outputStream -> {
                    try (var writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        writer.write("globalThis." + variableName + " = " + json);
                    }
                },
                "data/" + fileName + "-data.js"
        );
    }

    public void writeJsDataAs(String variableName, String json, String fileName) {
        write(
                outputStream -> {
                    try (var writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        writer.write("globalThis." + variableName + " = " + json);
                    }
                },
                "data/" + fileName + ".js"
        );
    }

    public JigDocument jigDocument() {
        return jigDocument;
    }
}
