package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JigDocumentWriter {
    private static final Logger logger = LoggerFactory.getLogger(JigDocumentWriter.class);

    private final Path outputDirectory;
    private final Locale locale;
    private final String assetVersion;

    public JigDocumentWriter(Path outputDirectory, Locale locale) {
        this(outputDirectory, locale, Long.toString(System.currentTimeMillis()));
    }

    // テストから決定的な値を注入できるようにするためのコンストラクタ。
    // 一意性の検証をSystemの時計精度に委ねないようにする。
    JigDocumentWriter(Path outputDirectory, Locale locale, String assetVersion) {
        this.outputDirectory = outputDirectory;
        this.locale = locale;
        this.assetVersion = assetVersion;
    }

    String assetVersion() {
        return assetVersion;
    }

    public Path outputDirectory() {
        return outputDirectory;
    }

    public void copyAssetsResource(String fileName) {
        copyResourceTo("templates/assets/", fileName, outputDirectory.resolve("assets"));
    }

    public Path writeHtmlAndJs(JigDocument jigDocument) {
        String fileName = jigDocument.fileName();
        copyResourceTo("templates/", fileName + ".html", outputDirectory);
        copyAssetsResource(fileName + ".js");
        return outputDirectory.resolve(fileName + ".html");
    }

    public void writeData(String fileName, String variableName, String json) {
        Path outputFilePath = outputDirectory.resolve("data/" + fileName + ".js");
        try (OutputStream outputStream = Files.newOutputStream(outputFilePath);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write("globalThis." + variableName + " = " + json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void copyResourceTo(String resourceDir, String fileName, Path outputDir) {
        String resourcePath = resourceDir + fileName;
        Path outputPath = outputDir.resolve(fileName);
        try (InputStream is = getResourceAsStream(resourcePath)) {
            Files.createDirectories(outputPath.getParent());
            if (fileName.endsWith(".html")) {
                String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Files.writeString(outputPath, resolvePlaceholders(html), StandardCharsets.UTF_8);
            } else {
                Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.error("リソースのコピーに失敗したため、ドキュメントの一部もしくはすべてが欠落します。" +
                    "通常は起こらない例外なので環境を確認してみてください。(from:{} to:{})", resourcePath, outputPath, e);
        }
    }

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("\\{\\{include:([a-zA-Z0-9_-]+)}}");

    /**
     * テンプレートのプレースホルダー処理
     *
     * テンプレート HTML 中の {@code {{assetVersion}}} プレースホルダを現在のバージョン値で置換する。
     * 各テンプレートはローカルアセット参照に明示的に {@code ?v={{assetVersion}}} を書いておく。
     * {@code {{include:name}}} は {@code templates/partials/name.html} の内容にそのまま置換する。
     * CDN スクリプトタグなど、複数テンプレートに同一内容がコピペされがちな断片を一箇所にまとめるために使う。
     */
    String resolvePlaceholders(String html) {
        String replaced = html.replace("{{lang}}", locale.getLanguage())
                .replace("{{assetVersion}}", assetVersion);
        return resolveIncludes(replaced);
    }

    private String resolveIncludes(String html) {
        Matcher matcher = INCLUDE_PATTERN.matcher(html);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(readPartial(matcher.group(1))));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String readPartial(String name) {
        try (InputStream is = getResourceAsStream("templates/partials/" + name + ".html")) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).stripTrailing();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    public void prepareOutputDirectory() {
        createOutputDirectory(outputDirectory);
        createOutputDirectory(outputDirectory.resolve("assets"));
        createOutputDirectory(outputDirectory.resolve("data"));
    }

    private void createOutputDirectory(Path dir) {
        File file = dir.toFile();
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
            Files.createDirectories(dir);
            logger.info("[JIG] created {}", dir.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
