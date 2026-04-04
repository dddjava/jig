package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        String resourcePath = "templates/" + fileName + ".html";
        Path outputFilePath = outputDirectory.resolve(fileName + ".html");
        try (OutputStream outputStream = Files.newOutputStream(outputFilePath);
             InputStream resource = getResourceAsStream(resourcePath)) {
            resource.transferTo(outputStream);
            writtenDocuments.add(outputFilePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static InputStream getResourceAsStream(String absolutePath) {
        var clz = JigDocumentWriter.class;
        // Classから探す
        var resource = clz.getResourceAsStream("/" + absolutePath);
        if (resource != null) {
            return resource;
        }

        // クラスパスで探す
        var classLoader = clz.getClassLoader();
        var classLoaderResource = classLoader.getResourceAsStream(absolutePath);
        if (classLoaderResource != null) {
            return classLoaderResource;
        }

        // コンテキストクラスローダーで探す
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != contextClassLoader) {
            var contextClassLoaderResource = contextClassLoader.getResourceAsStream(absolutePath);
            if (contextClassLoaderResource != null) {
                return contextClassLoaderResource;
            }
        }

        // 見つからなければ例外
        throw new IllegalStateException(absolutePath + " not found." +
                " This may be because the resource is not in the classpath or the module is not configured to allow resource access.");
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
