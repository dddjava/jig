package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JigDocumentWriter {
    private static final Logger logger = LoggerFactory.getLogger(JigDocumentWriter.class);

    public static Path writeHtml(JigDocument jigDocument, Path outputDirectory) {
        String fileName = jigDocument.fileName();
        Path outputFilePath = outputDirectory.resolve(fileName + ".html");
        copyResourceTo("templates/" + fileName + ".html", outputFilePath);
        return outputFilePath;
    }

    public static void writeData(Path outputDirectory, String fileName, String variableName, String json) {
        Path outputFilePath = outputDirectory.resolve("data/" + fileName + ".js");
        try (OutputStream outputStream = Files.newOutputStream(outputFilePath);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write("globalThis." + variableName + " = " + json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyResourceTo(String resourcePath, Path outputPath) {
        try (InputStream is = getResourceAsStream(resourcePath)) {
            Files.createDirectories(outputPath.getParent());
            Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("リソースのコピーに失敗したため、ドキュメントの一部もしくはすべてが欠落します。" +
                    "通常は起こらない例外なので環境を確認してみてください。(from:{} to:{})", resourcePath, outputPath, e);
        }
    }

    public static InputStream getResourceAsStream(String absolutePath) {
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
}
