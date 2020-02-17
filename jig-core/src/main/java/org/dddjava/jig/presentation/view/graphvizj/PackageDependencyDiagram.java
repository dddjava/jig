package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageNetwork;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class PackageDependencyDiagram implements DiagramSourceEditor<PackageNetwork> {

    static final Logger logger = LoggerFactory.getLogger(PackageDependencyDiagram.class);

    PackageIdentifierFormatter formatter;
    AliasFinder aliasFinder;
    ResourceBundleJigDocumentContext jigDocumentContext;

    public PackageDependencyDiagram(PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        this.formatter = formatter;
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();
    }

    @Override
    public DiagramSources edit(PackageNetwork packageNetwork) {
        List<PackageDepth> depths = packageNetwork.maxDepth().surfaceList();

        List<DiagramSource> diagramSources = depths.stream()
                .map(packageNetwork::applyDepth)
                .map(packageNetwork1 -> packageNetwork1.dependencyDotText(jigDocumentContext, formatter, aliasFinder))
                .filter(diagramSource -> !diagramSource.noValue())
                .collect(toList());
        return DiagramSource.createDiagramSource(diagramSources);
    }
}
