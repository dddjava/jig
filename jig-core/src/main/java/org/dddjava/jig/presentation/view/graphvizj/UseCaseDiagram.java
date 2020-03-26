package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.JigDocumentContext;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellowsAngle;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;

public class UseCaseDiagram implements DiagramSourceEditor<UseCaseAndFellowsAngle> {

    AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public UseCaseDiagram(AliasFinder aliasFinder) {
        this.jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();
        this.aliasFinder = aliasFinder;
    }

    @Override
    public DiagramSources edit(UseCaseAndFellowsAngle model) {
        return model.diagramSource(jigDocumentContext, aliasFinder);
    }
}
