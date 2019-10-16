package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.Collections;

public class ServiceMethodCallDiagram implements DotTextEditor<ServiceAngles> {

    final AliasFinder aliasFinder;
    final MethodNodeLabelStyle methodNodeLabelStyle;
    JigDocumentContext jigDocumentContext;

    public ServiceMethodCallDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(ServiceAngles serviceAngles) {
        if (serviceAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }

        DotText dotText = serviceAngles.methodCallDotText(jigDocumentContext, aliasFinder, methodNodeLabelStyle, this);
        return new DotTexts(dotText);
    }
}
