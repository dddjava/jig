package org.dddjava.jig.presentation.view;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.implementation.BusinessRuleRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.ArchitectureDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.Categories;
import org.dddjava.jig.domain.model.jigdocument.specification.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.PackageRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigmodel.services.MethodNodeLabelStyle;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.SummaryView;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ViewResolver {

    PackageIdentifierFormatter packageIdentifierFormatter;
    MethodNodeLabelStyle methodNodeLabelStyle;
    JigDiagramFormat diagramFormat;
    AliasService aliasService;

    JigDocumentContext jigDocumentContext;
    DotCommandRunner dotCommandRunner;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, MethodNodeLabelStyle methodNodeLabelStyle, JigDiagramFormat diagramFormat, JigDocumentContext jigDocumentContext, AliasService aliasService) {
        this.jigDocumentContext = jigDocumentContext;
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.diagramFormat = diagramFormat;
        this.aliasService = aliasService;
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
                return new DotView(model -> ((ServiceMethodCallHierarchyDiagram) model).methodCallDotText(jigDocumentContext), diagramFormat, dotCommandRunner);
            case PackageRelationDiagram:
                return dependencyWriter();
            case BusinessRuleRelationDiagram:
                return new DotView(model -> ((BusinessRuleRelationDiagram) model).relationDotText(jigDocumentContext, packageIdentifierFormatter), diagramFormat, dotCommandRunner);
            case OverconcentrationBusinessRuleDiagram:
                return new DotView(model -> ((BusinessRuleRelationDiagram) model).overconcentrationRelationDotText(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CoreBusinessRuleRelationDiagram:
                return new DotView(model -> ((BusinessRuleRelationDiagram) model).coreRelationDotText(jigDocumentContext, packageIdentifierFormatter), diagramFormat, dotCommandRunner);
            case CategoryUsageDiagram:
                return new DotView(model -> ((CategoryUsageDiagram) model).diagramSource(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CategoryDiagram:
                return new DotView(model -> ((Categories) model).valuesDotText(jigDocumentContext), diagramFormat, dotCommandRunner);
            case ArchitectureDiagram:
                return new DotView(model -> ((ArchitectureDiagram) model).dotText(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CompositeUsecaseDiagram:
                return new DotView(model -> ((CompositeUsecaseDiagram) model).diagramSource(jigDocumentContext), diagramFormat, dotCommandRunner);
            case BusinessRuleList:
            case ApplicationList:
                return new ModelReportsPoiView(new ConvertContext(aliasService));
            case ApplicationSummary:
            case DomainSummary:
                return new SummaryView(jigDocumentContext.aliasFinder());
        }

        throw new IllegalArgumentException("View未定義のJigDocumentを出力しようとしています: " + jigDocument);
    }
}

