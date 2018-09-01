package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.basic.FileWriteFailureException;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.presentation.controller.ClassListController;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceDiagramController;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        this.controllers = new Object[]{
                serviceDiagramController,
                classListController,
                packageDependencyController,
                enumUsageController
        };
        this.jigDebugMode = jigDebugMode;
    }

    JigModelAndView<?> resolveHandlerMethod(JigDocument jigDocument, ProjectData projectData) {
        try {
            for (Object controller : controllers) {
                Optional<Method> mayBeHandlerMethod = Arrays.stream(controller.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(DocumentMapping.class))
                        .filter(method -> method.getAnnotation(DocumentMapping.class).value() == jigDocument)
                        .findFirst();
                if (mayBeHandlerMethod.isPresent()) {
                    return (JigModelAndView<?>) mayBeHandlerMethod.get().invoke(controller, projectData);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException();
    }

    public void handle(JigDocument jigDocument, ProjectData projectData, Path outputDirectory) {
        try {
            JigModelAndView<?> jigModelAndView = resolveHandlerMethod(jigDocument, projectData);

            if (Files.notExists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                LOGGER.info("{} を作成しました。", outputDirectory.toAbsolutePath());
            }

            JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory, jigDebugMode);
            jigModelAndView.render(jigDocumentWriter);
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }
}
