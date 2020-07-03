package org.dddjava.jig.presentation.view;

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
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.dddjava.jig.presentation.view.graphvizj.DiagramSourceEditor;
import org.dddjava.jig.presentation.view.graphvizj.GraphvizjView;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ViewResolver {

    PackageIdentifierFormatter packageIdentifierFormatter;
    MethodNodeLabelStyle methodNodeLabelStyle;
    DiagramFormat diagramFormat;

    JigDocumentContext jigDocumentContext;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, MethodNodeLabelStyle methodNodeLabelStyle, DiagramFormat diagramFormat, JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.diagramFormat = diagramFormat;
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
        return new GraphvizjView<>(diagram, diagramFormat);
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
}
