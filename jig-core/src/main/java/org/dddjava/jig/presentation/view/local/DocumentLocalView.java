package org.dddjava.jig.presentation.view.local;

import org.dddjava.jig.domain.model.DocumentType;

import java.util.Arrays;

public enum DocumentLocalView {

    ServiceMethodCallHierarchy(DocumentType.ServiceMethodCallHierarchy) {
        @Override
        public LocalView execute(LocalViewContext localViewContext) {
            return localViewContext.serviceMethodCallHierarchyController.serviceMethodCallHierarchy();
        }
    },
    PackageDependency(DocumentType.PackageDependency) {
        @Override
        public LocalView execute(LocalViewContext localViewContext) {
            return localViewContext.packageDependencyController.packageDependency(localViewContext.packageDepth());
        }
    },
    ApplicationList(DocumentType.ApplicationList) {
        @Override
        public LocalView execute(LocalViewContext localViewContext) {
            return localViewContext.classListController.applicationList();
        }
    },
    DomainList(DocumentType.DomainList) {
        @Override
        public LocalView execute(LocalViewContext localViewContext) {
            return localViewContext.classListController.domainList();
        }
    },
    EnumUsage(DocumentType.EnumUsage) {
        @Override
        public LocalView execute(LocalViewContext localViewContext) {
            return localViewContext.enumUsageController.enumUsage();
        }
    };

    private final DocumentType documentType;

    DocumentLocalView(DocumentType documentType) {
        this.documentType = documentType;
    }

    public static DocumentLocalView of(DocumentType documentType) {
        return Arrays.stream(DocumentLocalView.values())
                .filter(documentLocalView -> documentLocalView.documentType == documentType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(documentType.name()));
    }

    public abstract LocalView execute(LocalViewContext localViewContext);
}
