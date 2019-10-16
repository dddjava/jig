package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;

import java.util.Collections;

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
        if (model.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }

        DotText dotText = model.getDotText(aliasFinder);
        return new DotTexts(dotText);
    }
}
