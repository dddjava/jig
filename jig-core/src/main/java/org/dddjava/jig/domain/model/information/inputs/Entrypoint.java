package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record Entrypoint(List<EntrypointGroup> list, MethodRelations methodRelations) {

    public static Entrypoint from(JigTypes jigTypes, MethodRelations methodRelations) {
        return new Entrypoint(jigTypes.list().stream()
                .map(jigType -> EntrypointGroup.from(jigType))
                .filter(entrypointGroup -> entrypointGroup.hasEntrypoint())
                .toList(),
                methodRelations);
    }

    public Map<String, String> mermaidMap(JigTypes jigTypes) {
        var map = new HashMap<String, String>();

        for (EntrypointGroup entrypointGroup : list()) {
            var jigType = entrypointGroup.jigType();
            var mermaidText = entrypointGroup.mermaid(methodRelations, jigTypes);
            map.put(jigType.fqn(), mermaidText);
        }

        return map;
    }

    public List<EntrypointMethod> listRequestHandlerMethods() {
        return requetHandlerMethodStream().toList();
    }

    private Stream<EntrypointMethod> requetHandlerMethodStream() {
        return list.stream()
                .filter(entrypointGroup -> entrypointGroup.isRequestHandler())
                .flatMap(entrypointGroup -> entrypointGroup.entrypointMethod().stream());
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<EntrypointMethod> collectEntrypointMethodOf(CallerMethods callerMethods) {
        return requetHandlerMethodStream()
                .filter(entrypointMethod -> entrypointMethod.anyMatch(callerMethods))
                .toList();
    }

    public List<TypeIdentifier> listTypeIdentifiers() {
        return list.stream()
                .map(entrypointGroup -> entrypointGroup.jigType().identifier())
                .toList();
    }

}
