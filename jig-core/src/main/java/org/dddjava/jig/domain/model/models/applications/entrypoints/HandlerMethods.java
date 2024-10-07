package org.dddjava.jig.domain.model.models.applications.entrypoints;

import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * ハンドラ一覧
 */
public class HandlerMethods {
    private final Entrypoint entrypoint;

    public HandlerMethods(Entrypoint entrypoint) {
        this.entrypoint = entrypoint;
    }

    public static HandlerMethods from(Entrypoint entrypoint) {
        return new HandlerMethods(entrypoint);
    }

    public List<EntrypointMethod> list() {
        return entrypoint.listRequestHandlerMethods().stream()
                .sorted(Comparator.comparing(requestEntrypointMethod -> requestEntrypointMethod.method().declaration().asFullNameText()))
                .collect(toList());
    }

    public boolean empty() {
        return entrypoint.isEmpty();
    }

    public List<EntrypointMethod> filter(CallerMethods callerMethods) {
        return entrypoint.collectEntrypointMethodOf(callerMethods);
    }
}
