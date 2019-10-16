package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.Collections;

/**
 * システムが持つEnumと列挙値の1枚絵
 */
public class CategoryDiagram implements DotTextEditor<CategoryAngles> {

    private final AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public CategoryDiagram(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(CategoryAngles categoryAngles) {
        if (categoryAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }
        return new DotTexts(categoryAngles.dotText(jigDocumentContext, aliasFinder));
    }
}
