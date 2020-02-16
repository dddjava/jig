package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

public class CategoryUsageDiagram implements DiagramSourceEditor<CategoryAngles> {

    AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public CategoryUsageDiagram(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotText edit(CategoryAngles categoryAngles) {
        return categoryAngles.toUsageDotText(aliasFinder, jigDocumentContext);
    }
}
