package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.io.*;
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

    public List<Path> outputFilePaths() {
        return writtenDocuments;
    }

    public void writeHtml() {
        String fileName = jigDocument.fileName();
        Path outputFilePath = outputDirectory.resolve(fileName + ".html");
        try (OutputStream outputStream = Files.newOutputStream(outputFilePath);
             InputStream resource = JigDocumentWriter.class.getResourceAsStream("/templates/" + fileName + ".html")) {
            Objects.requireNonNull(resource).transferTo(outputStream);
        } catch (
                IOException e) {
            throw new UncheckedIOException(e);
        }
        writtenDocuments.add(outputFilePath);
    }

    public void writeData(String variableName, String json) {
        String fileName = jigDocument.fileName();
        writeString(variableName, json, fileName + "-data");
    }

    public void writeData(String variableName, String json, String fileName) {
        writeString(variableName, json, fileName);
    }

    private void writeString(String variableName, String value, String fileName) {
        Path outputFilePath = outputDirectory.resolve("data/" + fileName + ".js");
        try (OutputStream outputStream = Files.newOutputStream(outputFilePath);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write("globalThis." + variableName + " = " + value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        writtenDocuments.add(outputFilePath);
    }
}
