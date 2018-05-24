package org.dddjava.jig.presentation.view;

import java.util.Arrays;

public enum JigDocumentHandler {

    ServiceMethodCallHierarchy(JigDocument.ServiceMethodCallHierarchy) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.serviceMethodCallHierarchyController.serviceMethodCallHierarchy();
        }
    },
    PackageDependency(JigDocument.PackageDependency) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.packageDependencyController.packageDependency(jigHandlerContext.packageDepth());
        }
    },
    ApplicationList(JigDocument.ApplicationList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.classListController.applicationList();
        }
    },
    DomainList(JigDocument.DomainList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.classListController.domainList();
        }
    },
    EnumUsage(JigDocument.EnumUsage) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.enumUsageController.enumUsage();
        }
    };

    private final JigDocument jigDocument;

    JigDocumentHandler(JigDocument jigDocument) {
        this.jigDocument = jigDocument;
    }

    public static JigDocumentHandler of(JigDocument jigDocument) {
        return Arrays.stream(values())
                .filter(item -> item.jigDocument == jigDocument)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(jigDocument.name()));
    }

    abstract JigModelAndView<?> handle(JigHandlerContext jigHandlerContext);

    public JigLocalRenderer<?> handleLocal(JigHandlerContext jigHandlerContext) {
        return new JigLocalRenderer<>(this.jigDocument, handle(jigHandlerContext));
    }
}
