package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.presentation.view.JigDocumentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class PackageDependencyDiagram implements DotTextEditor<PackageNetwork> {

    static final Logger logger = LoggerFactory.getLogger(PackageDependencyDiagram.class);

    final PackageIdentifierFormatter formatter;
    final AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public PackageDependencyDiagram(PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        this.formatter = formatter;
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(PackageNetwork packageNetwork) {
        List<PackageDepth> depths = packageNetwork.maxDepth().surfaceList();

        List<DotText> dotTexts = depths.stream()
                .map(packageNetwork::applyDepth)
                .filter(PackageNetwork::available)
                .map(packageNetwork1 -> packageNetwork1.toDotText(jigDocumentContext, formatter, aliasFinder))
                .collect(toList());
        return new DotTexts(dotTexts);
    }

}
