package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

/**
 * システムが持つEnumと列挙値の1枚絵
 */
public class CategoryDiagram implements DiagramSourceEditor<CategoryAngles> {

    AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public CategoryDiagram(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DiagramSource edit(CategoryAngles categoryAngles) {
        return categoryAngles.valuesDotText(jigDocumentContext, aliasFinder);
    }
}
