package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.implementation.*;
import org.dddjava.jig.domain.model.jigdocument.specification.ArchitectureDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.Categories;
import org.dddjava.jig.domain.model.jigdocument.specification.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.PackageRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.package_.PackageDepth;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifierFormatter;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.SummaryView;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ViewResolver {

    PackageIdentifierFormatter packageIdentifierFormatter;
    JigDiagramFormat diagramFormat;

    JigDocumentContext jigDocumentContext;
    DotCommandRunner dotCommandRunner;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, JigDiagramFormat diagramFormat, JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.diagramFormat = diagramFormat;
        this.dotCommandRunner = new DotCommandRunner();
    }

    private JigView dependencyWriter() {
        return new DotView(e -> {
            PackageRelationDiagram model = (PackageRelationDiagram) e;
            List<PackageDepth> depths = model.maxDepth().surfaceList();

            List<DiagramSource> diagramSources = depths.stream()
                    .map(model::applyDepth)
                    .map(packageNetwork1 -> packageNetwork1.dependencyDotText(jigDocumentContext, packageIdentifierFormatter))
                    .filter(diagramSource -> !diagramSource.noValue())
                    .collect(toList());
            return DiagramSource.createDiagramSource(diagramSources);
        }, diagramFormat, dotCommandRunner);
    }

    public JigView resolve(JigDocument jigDocument) {
        switch (jigDocument) {
            case ServiceMethodCallHierarchyDiagram:
                return new DotView(model -> ((ServiceMethodCallHierarchyDiagram) model).sources(jigDocumentContext), diagramFormat, dotCommandRunner);
            case PackageRelationDiagram:
                return dependencyWriter();
            case BusinessRuleRelationDiagram:
                return new DotView(model -> ((BusinessRuleRelationDiagram) model).sources(jigDocumentContext, packageIdentifierFormatter), diagramFormat, dotCommandRunner);
            case OverconcentrationBusinessRuleDiagram:
                return new DotView(model -> ((ConcentrateDomainDiagram) model).sources(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CoreBusinessRuleRelationDiagram:
                return new DotView(model -> ((CoreDomainDiagram) model).sources(jigDocumentContext, packageIdentifierFormatter), diagramFormat, dotCommandRunner);
            case CategoryUsageDiagram:
                return new DotView(model -> ((CategoryUsageDiagram) model).sources(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CategoryDiagram:
                return new DotView(model -> ((Categories) model).sources(jigDocumentContext), diagramFormat, dotCommandRunner);
            case ArchitectureDiagram:
                return new DotView(model -> ((ArchitectureDiagram) model).sources(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CompositeUsecaseDiagram:
                return new DotView(model -> ((CompositeUsecaseDiagram) model).sources(jigDocumentContext), diagramFormat, dotCommandRunner);
            case BusinessRuleList:
            case ApplicationList:
                return new ModelReportsPoiView(jigDocumentContext);
            case ApplicationSummary:
            case DomainSummary:
                return new SummaryView(jigDocumentContext);
        }

        throw new IllegalArgumentException("View未定義のJigDocumentを出力しようとしています: " + jigDocument);
    }
}

