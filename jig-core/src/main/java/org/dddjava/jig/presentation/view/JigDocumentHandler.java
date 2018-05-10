package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.DocumentType;

import java.util.Arrays;

public enum JigDocumentHandler {

    ServiceMethodCallHierarchy(DocumentType.ServiceMethodCallHierarchy) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.serviceMethodCallHierarchyController.serviceMethodCallHierarchy();
        }
    },
    PackageDependency(DocumentType.PackageDependency) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.packageDependencyController.packageDependency(jigHandlerContext.packageDepth());
        }
    },
    ApplicationList(DocumentType.ApplicationList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.classListController.applicationList();
        }
    },
    DomainList(DocumentType.DomainList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.classListController.domainList();
        }
    },
    EnumUsage(DocumentType.EnumUsage) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext) {
            return jigHandlerContext.enumUsageController.enumUsage();
        }
    };

    private final DocumentType documentType;

    JigDocumentHandler(DocumentType documentType) {
        this.documentType = documentType;
    }

    public static JigDocumentHandler of(DocumentType documentType) {
        return Arrays.stream(values())
                .filter(item -> item.documentType == documentType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(documentType.name()));
    }

    abstract JigModelAndView<?> handle(JigHandlerContext jigHandlerContext);

    public JigLocalRenderer<?> handleLocal(JigHandlerContext jigHandlerContext) {
        return new JigLocalRenderer<>(this.documentType, handle(jigHandlerContext));
    }
}
