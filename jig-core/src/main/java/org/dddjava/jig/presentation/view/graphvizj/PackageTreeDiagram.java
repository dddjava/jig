package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;

/**
 * パッケージツリー
 */
public class PackageTreeDiagram implements DotTextEditor<AllPackageIdentifiers> {

    AliasFinder aliasFinder;

    public PackageTreeDiagram(PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
    }

    @Override
    public DotTexts edit(AllPackageIdentifiers model) {
        return new DotTexts(model.treeDotText(aliasFinder));
    }
}
