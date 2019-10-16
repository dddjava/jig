package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.package_.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.graphvizj.*;

public class ViewResolver {

    private final PackageIdentifierFormatter packageIdentifierFormatter;
    private final MethodNodeLabelStyle methodNodeLabelStyle;
    private final DiagramFormat diagramFormat;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, MethodNodeLabelStyle methodNodeLabelStyle, DiagramFormat diagramFormat) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.diagramFormat = diagramFormat;
    }

    public JigView<PackageNetwork> dependencyWriter(AliasFinder aliasFinder) {
        return newGraphvizjView(new PackageDependencyDiagram(packageIdentifierFormatter, aliasFinder));
    }

    public JigView<ServiceAngles> serviceMethodCallHierarchy(AliasFinder aliasFinder) {
        return newGraphvizjView(new ServiceMethodCallDiagram(aliasFinder, methodNodeLabelStyle));
    }

    public JigView<CategoryAngles> enumUsage(AliasFinder aliasFinder) {
        return newGraphvizjView(new CategoryUsageDiagram(aliasFinder));
    }

    private <T> JigView<T> newGraphvizjView(DotTextEditor<T> diagram) {
        return new GraphvizjView<>(diagram, diagramFormat);
    }

    public JigView<ServiceAngles> booleanServiceTrace(AliasFinder aliasFinder) {
        return newGraphvizjView(new BooleanServiceTraceDiagram(aliasFinder, methodNodeLabelStyle));
    }

    public JigView<BusinessRuleNetwork> businessRuleRelationWriter(AliasFinder aliasFinder) {
        return newGraphvizjView(new BusinessRuleRelationDiagram(packageIdentifierFormatter, aliasFinder));
    }

    public JigView<CategoryAngles> categories(AliasFinder aliasFinder) {
        return newGraphvizjView(new CategoryDiagram(aliasFinder));
    }

    public JigView<AllPackageIdentifiers> packageTreeWriter(AliasFinder aliasFinder) {
        return newGraphvizjView(new PackageTreeDiagram(packageIdentifierFormatter, aliasFinder));
    }
}
