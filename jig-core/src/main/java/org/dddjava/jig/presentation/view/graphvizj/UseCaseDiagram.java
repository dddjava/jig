package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellowsAngle;

public class UseCaseDiagram implements DiagramSourceEditor<UseCaseAndFellowsAngle> {

    @Override
    public DiagramSources edit(UseCaseAndFellowsAngle model) {
        return model.diagramSource();
    }
}
