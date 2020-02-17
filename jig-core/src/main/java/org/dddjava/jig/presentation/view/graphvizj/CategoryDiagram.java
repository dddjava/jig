package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;

/**
 * システムが持つEnumと列挙値の1枚絵
 */
public class CategoryDiagram implements DiagramSourceEditor<CategoryAngles> {

    AliasFinder aliasFinder;
    ResourceBundleJigDocumentContext jigDocumentContext;

    public CategoryDiagram(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();
    }

    @Override
    public DiagramSources edit(CategoryAngles categoryAngles) {
        return categoryAngles.valuesDotText(jigDocumentContext, aliasFinder);
    }
}
