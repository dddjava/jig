package org.dddjava.jig.domain.model.data.types;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * 型パラメタ
 * @param name パラメタ名。 `T` などが慣習的に使用されるが、慣習。
 * @param bounds
 */
public record JigTypeParameter(String name, List<JigTypeArgument> bounds) {
    public JigTypeParameter(String name) {
        this(name, List.of());
    }

    public String nameAndBounds() {
        List<JigTypeArgument> outputBounds = this.bounds.stream()
                .filter(jigTypeArgument -> jigTypeArgument.notObject())
                .toList();
        return outputBounds.isEmpty()
                ? name()
                : name() + outputBounds.stream()
                .map(jigTypeArgument -> jigTypeArgument.simpleNameWithGenerics())
                .collect(joining(" & ", " extends ", ""));
    }
}
