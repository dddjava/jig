package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.DiagramView;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

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

    JigModelAndView<?> invokeHandlerMethod(JigDocument jigDocument, HandlerMethodArgumentResolver argumentResolver) {
        try {
            for (Object handler : handlers) {
                Optional<Method> mayBeHandlerMethod = Arrays.stream(handler.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(DocumentMapping.class))
                        .filter(method -> method.getAnnotation(DocumentMapping.class).value() == jigDocument)
                        .findFirst();
                if (mayBeHandlerMethod.isPresent()) {
                    Method method = mayBeHandlerMethod.get();
                    Object[] args = Arrays.stream(method.getParameterTypes())
                            .map(clz -> argumentResolver.resolve(clz))
                            .toArray();
                    Object result = method.invoke(handler, args);

                    if (result instanceof JigModelAndView) {
                        return (JigModelAndView<?>) result;
                    }

                    DiagramView diagramView = DiagramView.of(jigDocument);
                    return diagramView.createModelAndView(result, viewResolver);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException();
    }

    public HandleResult handle(JigDocument jigDocument, HandlerMethodArgumentResolver argumentResolver, Path outputDirectory) {
        try {
            JigModelAndView<?> jigModelAndView = invokeHandlerMethod(jigDocument, argumentResolver);

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
