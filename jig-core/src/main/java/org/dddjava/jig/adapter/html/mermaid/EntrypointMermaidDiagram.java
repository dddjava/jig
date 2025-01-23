package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.data.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;

public record EntrypointMermaidDiagram(Entrypoint entrypoint, JigTypes contextJigTypes) {

    public String textFor(JigType jigType) {
        return entrypoint().list().stream()
                .filter(entrypointGroup -> entrypointGroup.jigType() == jigType)
                .findAny()
                .map(entrypointGroup -> entrypointGroup.mermaid(entrypoint().methodRelations(), contextJigTypes))
                .orElse("");
    }
}
