package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.JigDocumentContext;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.MethodNodeLabelStyle;
import org.dddjava.jig.domain.model.jigpresentation.diagram.*;
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.dddjava.jig.presentation.view.graphvizj.DiagramSourceEditor;
import org.dddjava.jig.presentation.view.graphvizj.GraphvizjView;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ViewResolver {

    AliasFinder aliasFinder;
    PackageIdentifierFormatter packageIdentifierFormatter;
    MethodNodeLabelStyle methodNodeLabelStyle;
    DiagramFormat diagramFormat;

    JigDocumentContext jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();

    public ViewResolver(AliasFinder aliasFinder, PackageIdentifierFormatter packageIdentifierFormatter, MethodNodeLabelStyle methodNodeLabelStyle, DiagramFormat diagramFormat) {
        this.aliasFinder = aliasFinder;
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.diagramFormat = diagramFormat;
    }

    public JigView<PackageRelationDiagram> dependencyWriter() {
        return newGraphvizjView(model -> {
            List<PackageDepth> depths = model.maxDepth().surfaceList();

            List<DiagramSource> diagramSources = depths.stream()
                    .map(model::applyDepth)
                    .map(packageNetwork1 -> packageNetwork1.dependencyDotText(jigDocumentContext, packageIdentifierFormatter, aliasFinder))
                    .filter(diagramSource -> !diagramSource.noValue())
                    .collect(toList());
            return DiagramSource.createDiagramSource(diagramSources);
        });
    }

    public JigView<ServiceMethodCallHierarchyDiagram> serviceMethodCallHierarchy() {
        return newGraphvizjView(model ->
                model.methodCallDotText(jigDocumentContext, aliasFinder));
    }

    public JigView<CategoryUsageDiagram> enumUsage() {
        return newGraphvizjView(model ->
                model.diagramSource(aliasFinder, jigDocumentContext));
    }

    public JigView<CompositeUsecaseDiagram> compositeUsecaseDiagram() {
        return newGraphvizjView(model ->
                model.diagramSource(jigDocumentContext, aliasFinder));
    }

    private <T> JigView<T> newGraphvizjView(DiagramSourceEditor<T> diagram) {
        return new GraphvizjView<>(diagram, diagramFormat);
    }

    public JigView<BusinessRuleRelationDiagram> businessRuleRelationWriter() {
        return newGraphvizjView(model ->
                model.relationDotText(jigDocumentContext, packageIdentifierFormatter, aliasFinder));
    }

    public JigView<CategoryDiagram> categories() {
        return newGraphvizjView(model ->
                model.valuesDotText(jigDocumentContext, aliasFinder));
    }

    public JigView<ArchitectureDiagram> architecture() {
        return newGraphvizjView(model -> model.dotText(ResourceBundleJigDocumentContext.getInstance()));
    }
}
