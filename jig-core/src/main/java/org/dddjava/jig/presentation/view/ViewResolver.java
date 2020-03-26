package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.jigmodel.applications.services.MethodNodeLabelStyle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.jigpresentation.architectures.ArchitectureAngle;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellows;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellowsAngle;
import org.dddjava.jig.presentation.view.graphvizj.*;

public class ViewResolver {

    AliasFinder aliasFinder;
    PackageIdentifierFormatter packageIdentifierFormatter;
    MethodNodeLabelStyle methodNodeLabelStyle;
    DiagramFormat diagramFormat;

    public ViewResolver(AliasFinder aliasFinder, PackageIdentifierFormatter packageIdentifierFormatter, MethodNodeLabelStyle methodNodeLabelStyle, DiagramFormat diagramFormat) {
        this.aliasFinder = aliasFinder;
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.diagramFormat = diagramFormat;
    }

    public JigView<PackageNetwork> dependencyWriter() {
        return newGraphvizjView(new PackageDependencyDiagram(packageIdentifierFormatter, aliasFinder));
    }

    public JigView<ServiceAngles> serviceMethodCallHierarchy() {
        return newGraphvizjView(new ServiceMethodCallDiagram(aliasFinder, methodNodeLabelStyle));
    }

    public JigView<CategoryAngles> enumUsage() {
        return newGraphvizjView(new CategoryUsageDiagram(aliasFinder));
    }

    public JigView<UseCaseAndFellowsAngle> useCase() {
        return newGraphvizjView(new UseCaseDiagram());
    }

    private <T> JigView<T> newGraphvizjView(DiagramSourceEditor<T> diagram) {
        return new GraphvizjView<>(diagram, diagramFormat);
    }

    public JigView<ServiceAngles> booleanServiceTrace() {
        return newGraphvizjView(new BooleanServiceTraceDiagram(aliasFinder, methodNodeLabelStyle));
    }

    public JigView<BusinessRuleNetwork> businessRuleRelationWriter() {
        return newGraphvizjView(new BusinessRuleRelationDiagram(packageIdentifierFormatter, aliasFinder));
    }

    public JigView<CategoryAngles> categories() {
        return newGraphvizjView(new CategoryDiagram(aliasFinder));
    }

    public JigView<ArchitectureAngle> architecture() {
        return newGraphvizjView(model -> model.dotText(ResourceBundleJigDocumentContext.getInstance()));
    }
}
