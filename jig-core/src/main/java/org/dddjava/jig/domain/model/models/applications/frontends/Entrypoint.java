package org.dddjava.jig.domain.model.models.applications.frontends;

import org.dddjava.jig.domain.model.models.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;

import java.util.*;

public record Entrypoint(List<EntrypointGroup> list, ServiceMethods serviceMethods) {

    public Entrypoint(JigTypes jigTypes, ServiceMethods serviceMethods) {
        this(jigTypes.list().stream()
                        .map(jigType -> EntrypointGroup.from(jigType))
                        .filter(entrypointGroup -> entrypointGroup.hasEntrypoint())
                        .toList(),
                serviceMethods);
    }

    public Map<String, String> mermaidMap() {
        var map = new HashMap<String, String>();

        for (EntrypointGroup entrypointGroup : list()) {
            var jigType = entrypointGroup.jigType();
            var mermaidText = entrypointGroup.mermaid(serviceMethods);
            map.put(jigType.fqn(), mermaidText);
        }

        return map;
    }
}
