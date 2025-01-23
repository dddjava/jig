package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethodFinder;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;

public record UsecaseMermaidDiagram(
        JigTypes contextJigTypes,
        MethodRelations simpleMethodRelations
) {

    public String textFor(JigMethod jigMethod) {
        JigMethodFinder jigMethodFinder = methodIdentifier -> contextJigTypes.resolveJigMethod(methodIdentifier);
        return jigMethod.usecaseMermaidText(jigMethodFinder, simpleMethodRelations);
    }
}
