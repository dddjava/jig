package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.AliasFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageNetworks;
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

    public JigView<PackageNetworks> dependencyWriter(AliasFinder aliasFinder) {
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

    public JigView<BusinessRuleNetwork> businessRuleNetworkWriter(AliasFinder aliasFinder) {
        return newGraphvizjView(new BusinessRuleNetworkDiagram(packageIdentifierFormatter, aliasFinder));
    }

    public JigView<CategoryAngles> categories(AliasFinder aliasFinder) {
        return newGraphvizjView(new CategoryDiagram(aliasFinder));
    }

    public JigView<AllPackageIdentifiers> packageTreeWriter(AliasFinder aliasFinder) {
        return newGraphvizjView(new PackageTreeDiagram(packageIdentifierFormatter, aliasFinder));
    }
}
