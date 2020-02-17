package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSources;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.MethodNodeLabelStyle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;

public class BooleanServiceTraceDiagram implements DiagramSourceEditor<ServiceAngles> {

    AliasFinder aliasFinder;
    MethodNodeLabelStyle methodNodeLabelStyle;
    ResourceBundleJigDocumentContext jigDocumentContext;

    public BooleanServiceTraceDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = ResourceBundleJigDocumentContext.getInstance();
    }

    @Override
    public DiagramSources edit(ServiceAngles model) {
        return model.returnBooleanTraceDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder);
    }
}
