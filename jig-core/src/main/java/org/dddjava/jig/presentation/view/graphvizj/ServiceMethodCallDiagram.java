package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

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
        DotText dotText = serviceAngles.methodCallDotText(jigDocumentContext, aliasFinder, methodNodeLabelStyle, this);
        return new DotTexts(dotText);
    }
}
