package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.jigdocument.JigDocument;

public enum DiagramView {

    ServiceMethodCallHierarchyDiagram(JigDocument.ServiceMethodCallHierarchyDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.serviceMethodCallHierarchy();
        }
    },
    PackageRelationDiagram(JigDocument.PackageRelationDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.dependencyWriter();
        }
    },
    BusinessRuleRelationDiagram(JigDocument.BusinessRuleRelationDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.businessRuleRelationWriter();
        }
    },
    CategoryUsageDiagram(JigDocument.CategoryUsageDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.enumUsage();
        }
    },
    CategoryDiagram(JigDocument.CategoryDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.categories();
        }
    },
    ArchitectureDiagram(JigDocument.ArchitectureDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.architecture();
        }
    },
    CompositeUsecaseDiagram(JigDocument.CompositeUsecaseDiagram) {
        @Override
        JigView<?> create(ViewResolver viewResolver) {
            return viewResolver.compositeUsecaseDiagram();
        }
    };

    final JigDocument jigDocument;

    DiagramView(JigDocument jigDocument) {
        this.jigDocument = jigDocument;
    }

    public static DiagramView of(JigDocument jigDocument) {
        for (DiagramView value : values()) {
            if (value.jigDocument == jigDocument) {
                return value;
            }
        }

        throw new IllegalArgumentException("Diagram以外が指定された？: " + jigDocument);
    }

    abstract JigView<?> create(ViewResolver viewResolver);

    public JigModelAndView<?> createModelAndView(Object model, ViewResolver viewResolver) {
        JigView<?> jigView = create(viewResolver);
        @SuppressWarnings("unchecked")
        JigModelAndView<?> jigModelAndView = new JigModelAndView(model, jigView);

        return jigModelAndView;
    }
}
