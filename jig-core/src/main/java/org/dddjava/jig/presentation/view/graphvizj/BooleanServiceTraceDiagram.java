package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.Collections;

public class BooleanServiceTraceDiagram implements DotTextEditor<ServiceAngles> {

    private final AliasFinder aliasFinder;
    private final MethodNodeLabelStyle methodNodeLabelStyle;
    JigDocumentContext jigDocumentContext;

    public BooleanServiceTraceDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(ServiceAngles model) {
        ServiceAngles booleanServiceAngles = model.filterReturnsBoolean();

        if (booleanServiceAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }
        DotText dotText = booleanServiceAngles.getDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder);
        return new DotTexts(dotText);
    }
}
