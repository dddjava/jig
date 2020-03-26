package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellows;

public class UseCaseDiagram implements DiagramSourceEditor<UseCaseAndFellows> {

    @Override
    public DiagramSources edit(UseCaseAndFellows model) {
        return model.diagramSource();
    }
}
