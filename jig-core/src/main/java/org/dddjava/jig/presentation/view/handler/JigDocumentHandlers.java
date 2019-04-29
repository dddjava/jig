package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.presentation.controller.ClassListController;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceDiagramController;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public class JigDocumentHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentHandlers.class);

    Object[] controllers;
    boolean jigDebugMode;

    public JigDocumentHandlers(ServiceDiagramController serviceDiagramController,
                               ClassListController classListController,
                               PackageDependencyController packageDependencyController,
                               EnumUsageController enumUsageController,
                               boolean jigDebugMode) {
        // FIXME @Controllerをスキャンするようにしたい。現状はController追加のたびにここに足す必要がある。
        this.controllers = new Object[]{
                serviceDiagramController,
                classListController,
                packageDependencyController,
                enumUsageController
        };
        this.jigDebugMode = jigDebugMode;
    }

    JigModelAndView<?> resolveHandlerMethod(JigDocument jigDocument, HandlerMethodArgumentResolver argumentResolver) {
        try {
            for (Object controller : controllers) {
                Optional<Method> mayBeHandlerMethod = Arrays.stream(controller.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(DocumentMapping.class))
                        .filter(method -> method.getAnnotation(DocumentMapping.class).value() == jigDocument)
                        .findFirst();
                if (mayBeHandlerMethod.isPresent()) {
                    Method method = mayBeHandlerMethod.get();
                    Object[] args = Arrays.stream(method.getParameterTypes())
                            .map(clz -> argumentResolver.resolve(clz))
                            .toArray();
                    return (JigModelAndView<?>) method.invoke(controller, args);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException();
    }

    public HandleResult handle(JigDocument jigDocument, HandlerMethodArgumentResolver argumentResolver, Path outputDirectory) {
        try {
            JigModelAndView<?> jigModelAndView = resolveHandlerMethod(jigDocument, argumentResolver);

            if (Files.notExists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                LOGGER.info("{} を作成しました。", outputDirectory.toAbsolutePath());
            }

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
            jigModelAndView.render(jigDocumentWriter);

            return new HandleResult(jigDocument, jigDocumentWriter.outputFilePaths());
        } catch (Exception e) {
            LOGGER.warn("{} の出力に失敗しました。", jigDocument, e);
            return new HandleResult(jigDocument, e.getMessage());
        }
    }
}
