package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

public class ServiceMethodCallDiagram implements DotTextEditor<ServiceAngles> {

    AliasFinder aliasFinder;
    MethodNodeLabelStyle methodNodeLabelStyle;
    JigDocumentContext jigDocumentContext;

    public ServiceMethodCallDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(ServiceAngles serviceAngles) {
        DotText dotText = serviceAngles.methodCallDotText(jigDocumentContext, aliasFinder, methodNodeLabelStyle);
        return new DotTexts(dotText);
    }
}
