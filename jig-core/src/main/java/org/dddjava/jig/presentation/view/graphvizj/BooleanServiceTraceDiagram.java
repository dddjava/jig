package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

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

        DotText dotText = booleanServiceAngles.booleanServiceTraceDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder);
        return new DotTexts(dotText);
    }
}
