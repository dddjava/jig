package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.report.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;

import java.util.Arrays;

public enum JigDocumentHandler {

    ServiceMethodCallHierarchy(JigDocument.ServiceMethodCallHierarchy) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.serviceMethodCallHierarchyController.serviceMethodCallHierarchy(projectData);
        }
    },
    PackageDependency(JigDocument.PackageDependency) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.packageDependencyController.packageDependency(jigHandlerContext.packageDepth(), projectData);
        }
    },
    ApplicationList(JigDocument.ApplicationList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.classListController.applicationList(projectData);
        }
    },
    DomainList(JigDocument.DomainList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.classListController.domainList(projectData);
        }
    },
    BranchList(JigDocument.BranchList) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.classListController.branchList(projectData);
        }
    },
    EnumUsage(JigDocument.EnumUsage) {
        @Override
        public JigModelAndView handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.enumUsageController.enumUsage(projectData);
        }
    },
    BooleanService(JigDocument.BooleanService) {
        @Override
        JigModelAndView<?> handle(JigHandlerContext jigHandlerContext, ProjectData projectData) {
            return jigHandlerContext.booleanServiceTraceController.diagram(projectData);
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

    abstract JigModelAndView<?> handle(JigHandlerContext jigHandlerContext, ProjectData projectData);

    public JigLocalRenderer<?> handleLocal(JigHandlerContext jigHandlerContext, ProjectData projectData) {
        return new JigLocalRenderer<>(this.jigDocument, handle(jigHandlerContext, projectData));
    }
}
