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
import org.dddjava.jig.presentation.view.graphviz.DiagramSourceEditor;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.HtmlListView;
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

    public JigView<PackageRelationDiagram> dependencyWriter() {
        return newGraphvizjView(model -> {
            List<PackageDepth> depths = model.maxDepth().surfaceList();

            List<DiagramSource> diagramSources = depths.stream()
                    .map(model::applyDepth)
                    .map(packageNetwork1 -> packageNetwork1.dependencyDotText(jigDocumentContext, packageIdentifierFormatter))
                    .filter(diagramSource -> !diagramSource.noValue())
                    .collect(toList());
            return DiagramSource.createDiagramSource(diagramSources);
        });
    }

    public JigView<ServiceMethodCallHierarchyDiagram> serviceMethodCallHierarchy() {
        return newGraphvizjView(model ->
                model.methodCallDotText(jigDocumentContext));
    }

    public JigView<CategoryUsageDiagram> enumUsage() {
        return newGraphvizjView(model ->
                model.diagramSource(jigDocumentContext));
    }

    public JigView<CompositeUsecaseDiagram> compositeUsecaseDiagram() {
        return newGraphvizjView(model ->
                model.diagramSource(jigDocumentContext));
    }

    private <T> JigView<T> newGraphvizjView(DiagramSourceEditor<T> diagram) {
        return new DotView<>(diagram, diagramFormat, dotCommandRunner);
    }

    public JigView<BusinessRuleRelationDiagram> businessRuleRelationWriter() {
        return newGraphvizjView(model ->
                model.relationDotText(jigDocumentContext, packageIdentifierFormatter));
    }

    public JigView<Categories> categories() {
        return newGraphvizjView(model ->
                model.valuesDotText(jigDocumentContext));
    }

    public JigView<ArchitectureDiagram> architecture() {
        return newGraphvizjView(model -> model.dotText(jigDocumentContext));
    }

    public JigView<?> resolve(JigDocument jigDocument) {
        switch (jigDocument) {
            case ServiceMethodCallHierarchyDiagram:
                return serviceMethodCallHierarchy();
            case PackageRelationDiagram:
                return dependencyWriter();
            case BusinessRuleRelationDiagram:
                return businessRuleRelationWriter();
            case OverconcentrationBusinessRuleDiagram:
                return new DotView<BusinessRuleRelationDiagram>(
                        model -> model.overconcentrationRelationDotText(jigDocumentContext), diagramFormat, dotCommandRunner);
            case CoreBusinessRuleRelationDiagram:
                return new DotView<BusinessRuleRelationDiagram>(
                        model -> model.coreRelationDotText(jigDocumentContext, packageIdentifierFormatter), diagramFormat, dotCommandRunner);
            case CategoryUsageDiagram:
                return enumUsage();
            case CategoryDiagram:
                return categories();
            case ArchitectureDiagram:
                return architecture();
            case CompositeUsecaseDiagram:
                return compositeUsecaseDiagram();
            case BusinessRuleList:
            case ApplicationList:
                return new ModelReportsPoiView(new ConvertContext(aliasService));
            case DomainSummary:
                return new HtmlListView(jigDocumentContext.aliasFinder());
        }

        throw new IllegalArgumentException("View未定義のJigDocumentを出力しようとしています: " + jigDocument);
    }
}

