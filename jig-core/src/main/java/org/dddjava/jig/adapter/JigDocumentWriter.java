package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class JigDocumentWriter {
    private static final Logger logger = LoggerFactory.getLogger(JigDocumentWriter.class);

    private static final String ASSET_VERSION = Long.toString(System.currentTimeMillis());
    private static final Pattern LOCAL_ASSET_REFERENCE = Pattern.compile("(href|src)=\"(\\./(?:assets|data)/[^\"?]+)\"");

    static String assetVersion() {
        return ASSET_VERSION;
    }

    public static void copyAssetsResource(String fileName, Path outputDirectory) {
        copyResourceTo("templates/assets/", fileName, outputDirectory.resolve("assets"));
    }

    public static Path writeHtmlAndJs(JigDocument jigDocument, Path outputDirectory) {
        String fileName = jigDocument.fileName();
        return writeHtmlAndJs(fileName, outputDirectory);
    }

    private static Path writeHtmlAndJs(String fileName, Path outputDirectory) {
        copyResourceTo("templates/", fileName + ".html", outputDirectory);
        copyAssetsResource(fileName + ".js", outputDirectory);
        return outputDirectory.resolve(fileName + ".html");
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

    public static void copyResourceTo(String resourceDir, String fileName, Path outputDir) {
        String resourcePath = resourceDir + fileName;
        Path outputPath = outputDir.resolve(fileName);
        try (InputStream is = getResourceAsStream(resourcePath)) {
            Files.createDirectories(outputPath.getParent());
            if (fileName.endsWith(".html")) {
                String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Files.writeString(outputPath, applyAssetVersion(html), StandardCharsets.UTF_8);
            } else {
                Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.error("リソースのコピーに失敗したため、ドキュメントの一部もしくはすべてが欠落します。" +
                    "通常は起こらない例外なので環境を確認してみてください。(from:{} to:{})", resourcePath, outputPath, e);
        }
    }

    static String applyAssetVersion(String html) {
        return LOCAL_ASSET_REFERENCE.matcher(html).replaceAll("$1=\"$2?v=" + ASSET_VERSION + "\"");
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

    public static void prepareOutputDirectory(Path outputDirectory) {
        createOutputDirectory(outputDirectory);
        createOutputDirectory(outputDirectory.resolve("assets"));
        createOutputDirectory(outputDirectory.resolve("data"));
    }

    private static void createOutputDirectory(Path outputDirectory) {
        File file = outputDirectory.toFile();
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " is not Directory. Please review your settings.");
            }
            if (!file.canWrite()) {
                throw new IllegalStateException(file.getAbsolutePath() + " can not writable. Please specify another directory.");
            }
            return;
        }

        try {
            Files.createDirectories(outputDirectory);
            logger.info("[JIG] created {}", outputDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
