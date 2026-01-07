package org.dddjava.jig.domain.model.data.types;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * 型パラメタ
 *
 * @param name   パラメタ名。 `T` などが慣習的に使用されるが、慣習。
 * @param bounds 境界型引数
 */
public record JigTypeParameter(String name, List<JigTypeArgument> bounds) {

        public String nameAndBounds() {
            List<JigTypeArgument> meaningfulBounds = bounds.stream()
                    .filter(JigTypeArgument::notObject)
                    .toList();

            if (meaningfulBounds.isEmpty()) {
                return name;
            }

            String boundsString = meaningfulBounds.stream()
                    .map(JigTypeArgument::simpleNameWithGenerics)
                    .collect(joining(" & "));

            return name + " extends " + boundsString;
        }
    }
