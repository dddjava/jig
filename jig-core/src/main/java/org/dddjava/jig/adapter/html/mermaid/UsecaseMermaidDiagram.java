package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethodFinder;

public record UsecaseMermaidDiagram(
        JigTypes contextJigTypes,
        MethodRelations simpleMethodRelations
) {

    public String textFor(JigMethod jigMethod) {
        JigMethodFinder jigMethodFinder = methodIdentifier -> contextJigTypes.resolveJigMethod(methodIdentifier);
        return jigMethod.usecaseMermaidText(jigMethodFinder, simpleMethodRelations);
    }
}
