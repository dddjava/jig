package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.presentation.controller.JigController;
import org.dddjava.jig.presentation.view.html.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JigDocumentHandlers {

    private static final Logger logger = LoggerFactory.getLogger(JigDocumentHandlers.class);

    private final ViewResolver viewResolver;
    private final JigController jigController;
    private final List<JigDocument> jigDocuments;
    private final Path outputDirectory;

    public JigDocumentHandlers(ViewResolver viewResolver,
                               JigController jigController,
                               List<JigDocument> jigDocuments,
                               Path outputDirectory) {
        this.viewResolver = viewResolver;
        this.jigController = jigController;
        this.jigDocuments = jigDocuments;
        this.outputDirectory = outputDirectory;
    }

    public HandleResults handleJigDocuments() {
        long startTime = System.currentTimeMillis();
        logger.info("[JIG] write jig documents: {}", jigDocuments);

        prepareOutputDirectory();

        List<HandleResult> handleResultList = jigDocuments
                .parallelStream()
                .map(jigDocument -> handle(jigDocument, outputDirectory))
                .collect(Collectors.toList());
        writeIndexHtml(outputDirectory, handleResultList);
        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        return new HandleResults(handleResultList);
    }

    private void prepareOutputDirectory() {
        File file = outputDirectory.toFile();
        if (file.exists()) {
            if (file.isDirectory() && file.canWrite()) {
                // ディレクトリかつ書き込み可能なので対応不要
                return;
            }
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " is not Directory. Please review your settings.");
            }
            if (file.isDirectory() && !file.canWrite()) {
                throw new IllegalStateException(file.getAbsolutePath() + " can not writable. Please specify another directory.");
            }
        }

        try {
            Files.createDirectories(outputDirectory);
            logger.info("[JIG] created {}", outputDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    HandleResult handle(JigDocument jigDocument, Path outputDirectory) {
        try {
            long startTime = System.currentTimeMillis();
            Object model = jigController.handle(jigDocument);

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
            JigView jigView = viewResolver.resolve(jigDocument);
            jigView.render(model, jigDocumentWriter);

            long takenTime = System.currentTimeMillis() - startTime;
            logger.info("[{}] completed: {} ms", jigDocument, takenTime);
            return new HandleResult(jigDocument, jigDocumentWriter.outputFilePaths());
        } catch (Exception e) {
            logger.warn("[{}] failed to write document.", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    void writeIndexHtml(Path outputDirectory, List<HandleResult> handleResultList) {
        IndexView indexView = viewResolver.indexView();
        indexView.render(handleResultList, outputDirectory);
        copyAssets(outputDirectory);
    }

    private void copyAssets(Path outputDirectory) {
        Path assetsDirectory = createAssetsDirectory(outputDirectory);
        copyAsset("style.css", assetsDirectory);
        copyAsset("jig.js", assetsDirectory);
        copyAsset("favicon.ico", assetsDirectory);
    }

    private static Path createAssetsDirectory(Path outputDirectory) {
        Path assetsPath = outputDirectory.resolve("assets");
        try {
            Files.createDirectories(assetsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return assetsPath;
    }

    private void copyAsset(String fileName, Path distDirectory) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("templates/assets/" + fileName)) {
            Files.copy(Objects.requireNonNull(is), distDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
