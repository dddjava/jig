package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.report.JigDocument;
import org.dddjava.jig.presentation.controller.*;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JigHandlerContext {

    PackageDepth packageDepth;

    Object[] controllers;

    public JigHandlerContext(ServiceMethodCallHierarchyController serviceMethodCallHierarchyController,
                             ClassListController classListController,
                             PackageDependencyController packageDependencyController,
                             EnumUsageController enumUsageController,
                             BooleanServiceTraceController booleanServiceTraceController,
                             @Value("${depth:-1}") int packageDepth) {
        this.packageDepth = new PackageDepth(packageDepth);

        this.controllers = new Object[]{
                serviceMethodCallHierarchyController,
                classListController,
                packageDependencyController,
                enumUsageController,
                booleanServiceTraceController
        };
    }

    public PackageDepth packageDepth() {
        return packageDepth;
    }

    public JigModelAndView<?> resolveHandlerMethod(JigDocument jigDocument, ProjectData projectData) {
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
}
