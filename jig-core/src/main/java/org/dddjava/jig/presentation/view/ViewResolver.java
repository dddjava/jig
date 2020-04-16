package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.JigDocumentContext;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.jigmodel.applications.services.MethodNodeLabelStyle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.jigpresentation.architectures.ArchitectureAngle;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryUsages;
import org.dddjava.jig.domain.model.jigpresentation.servicecall.ServiceMethodCallHierarchy;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellowsAngle;
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

    public JigView<PackageNetwork> dependencyWriter() {
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

    public JigView<ServiceMethodCallHierarchy> serviceMethodCallHierarchy() {
        return newGraphvizjView(model ->
                model.methodCallDotText(jigDocumentContext, aliasFinder));
    }

    public JigView<CategoryUsages> enumUsage() {
        return newGraphvizjView(model ->
                model.diagramSource(aliasFinder, jigDocumentContext));
    }

    public JigView<UseCaseAndFellowsAngle> useCase() {
        return newGraphvizjView(model ->
                model.diagramSource(jigDocumentContext, aliasFinder));
    }

    private <T> JigView<T> newGraphvizjView(DiagramSourceEditor<T> diagram) {
        return new GraphvizjView<>(diagram, diagramFormat);
    }

    public JigView<ServiceAngles> booleanServiceTrace() {
        return newGraphvizjView(model ->
                model.returnBooleanTraceDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder));
    }

    public JigView<BusinessRuleNetwork> businessRuleRelationWriter() {
        return newGraphvizjView(model ->
                model.relationDotText(jigDocumentContext, packageIdentifierFormatter, aliasFinder));
    }

    public JigView<CategoryAngles> categories() {
        return newGraphvizjView(model ->
                model.valuesDotText(jigDocumentContext, aliasFinder));
    }

    public JigView<ArchitectureAngle> architecture() {
        return newGraphvizjView(model -> model.dotText(ResourceBundleJigDocumentContext.getInstance()));
    }
}
