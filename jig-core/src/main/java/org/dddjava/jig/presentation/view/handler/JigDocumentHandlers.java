package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.html.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class JigDocumentHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentHandlers.class);

    ViewResolver viewResolver;
    Object[] handlers;

    public JigDocumentHandlers(ViewResolver viewResolver,
                               BusinessRuleListController businessRuleListController,
                               ApplicationListController applicationListController,
                               DiagramController diagramController) {
        this.viewResolver = viewResolver;
        // FIXME @Controllerをスキャンするようにしたい。現状はController追加のたびにここに足す必要がある。
        this.handlers = new Object[]{
                businessRuleListController,
                applicationListController,
                diagramController
        };
    }

    Object invokeHandlerMethod(JigDocument jigDocument) {
        try {
            for (Object handler : handlers) {
                Optional<Method> mayBeHandlerMethod = Arrays.stream(handler.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(DocumentMapping.class))
                        .filter(method -> method.getAnnotation(DocumentMapping.class).value() == jigDocument)
                        .findFirst();
                if (mayBeHandlerMethod.isPresent()) {
                    Method method = mayBeHandlerMethod.get();
                    return method.invoke(handler);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException("cannot find handler method for " + jigDocument);
    }

    HandleResult handle(JigDocument jigDocument, Path outputDirectory) {
        try {
            Object model = invokeHandlerMethod(jigDocument);

            if (Files.notExists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                LOGGER.info("{} を作成しました。", outputDirectory.toAbsolutePath());
            }

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
            JigView jigView = viewResolver.resolve(jigDocument);
            jigView.render(model, jigDocumentWriter);

            return new HandleResult(jigDocument, jigDocumentWriter.outputFilePaths());
        } catch (Exception e) {
            LOGGER.warn("{} の出力に失敗しました。", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }

    public List<HandleResult> handleJigDocuments(List<JigDocument> jigDocuments, Path outputDirectory) {
        List<HandleResult> handleResultList = new ArrayList<>();
        for (JigDocument jigDocument : jigDocuments) {
            HandleResult result = handle(jigDocument, outputDirectory);
            handleResultList.add(result);
        }
        writeIndexHtml(outputDirectory, handleResultList);
        return handleResultList;
    }

    void writeIndexHtml(Path outputDirectory, List<HandleResult> handleResultList) {
        IndexView indexView = viewResolver.indexView();
        indexView.render(handleResultList, outputDirectory);
        copyStaticResourcesForHtml(null, outputDirectory);
    }

    private void copyStaticResourcesForHtml(JigDocument jigDocument, Path outputDirectory) {
        Path assetsPath = outputDirectory.resolve("assets");
        try {
            Files.createDirectories(assetsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        copyFile("style.css", "templates/assets/", assetsPath);
        copyFile("marked.min.js", "templates/assets/", assetsPath);
        copyFile("jig.js", "templates/assets/", assetsPath);
        copyFile("favicon.ico", "templates/assets/", assetsPath);
    }

    private void copyFile(String fileName, String sourceDirectory, Path distDirectory) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(sourceDirectory + fileName)) {
            Files.copy(Objects.requireNonNull(is), distDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
