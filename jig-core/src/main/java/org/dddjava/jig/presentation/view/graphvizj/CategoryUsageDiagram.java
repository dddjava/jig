package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;

public class CategoryUsageDiagram implements DiagramSourceEditor<CategoryAngles> {

    AliasFinder aliasFinder;
    ResourceBundleJigDocumentContext jigDocumentContext;

    public CategoryUsageDiagram(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();
    }

    @Override
    public DiagramSource edit(CategoryAngles categoryAngles) {
        return categoryAngles.toUsageDotText(aliasFinder, jigDocumentContext);
    }
}
