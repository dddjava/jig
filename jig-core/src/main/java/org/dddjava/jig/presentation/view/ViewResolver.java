package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.networks.packages.PackageNetworks;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.graphvizj.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ViewResolver {

    private final PackageIdentifierFormatter packageIdentifierFormatter;
    private final MethodNodeLabelStyle methodNodeLabelStyle;
    private final DiagramFormat diagramFormat;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, @Value("${methodNodeLabelStyle:SIMPLE}") String methodNodeLabelStyle, @Value("${diagram.format:SVG}") String diagramFormat) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.methodNodeLabelStyle = MethodNodeLabelStyle.valueOf(methodNodeLabelStyle.toUpperCase(Locale.ENGLISH));
        this.diagramFormat = DiagramFormat.valueOf(diagramFormat.toUpperCase(Locale.ENGLISH));
    }

    public JigView<PackageNetworks> dependencyWriter(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new PackageDependencyDiagram(packageIdentifierFormatter, japaneseNameFinder));
    }

    public JigView<ServiceAngles> serviceMethodCallHierarchy(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new ServiceMethodCallDiagram(japaneseNameFinder, methodNodeLabelStyle));
    }

    public JigView<CategoryAngles> enumUsage(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new EnumUsageDiagram(japaneseNameFinder));
    }

    private <T> JigView<T> newGraphvizjView(DotTextEditor<T> diagram) {
        return new GraphvizjView<T>(diagram, diagramFormat);
    }

    public JigView<ServiceAngles> booleanServiceTrace(JapaneseNameFinder japaneseNameFinder) {
        return newGraphvizjView(new BooleanServiceTraceDiagram(japaneseNameFinder, methodNodeLabelStyle));
    }
}
