package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocumentContext;

public class BooleanServiceTraceDiagram implements DotTextEditor<ServiceAngles> {

    AliasFinder aliasFinder;
    MethodNodeLabelStyle methodNodeLabelStyle;
    JigDocumentContext jigDocumentContext;

    public BooleanServiceTraceDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotText edit(ServiceAngles model) {
        return model.returnBooleanTraceDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder);
    }
}
