package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

public class ServiceMethodCallDiagram implements DiagramSourceEditor<ServiceAngles> {

    AliasFinder aliasFinder;
    MethodNodeLabelStyle methodNodeLabelStyle;
    JigDocumentContext jigDocumentContext;

    public ServiceMethodCallDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DiagramSource edit(ServiceAngles serviceAngles) {
        return serviceAngles.methodCallDotText(jigDocumentContext, aliasFinder, methodNodeLabelStyle);
    }
}
