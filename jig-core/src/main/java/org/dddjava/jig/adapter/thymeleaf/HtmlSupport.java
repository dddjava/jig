package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import static java.util.stream.Collectors.joining;

public class HtmlSupport {

    public static String htmlMethodIdText(JigMethodId jigMethodId) {
        var tuple = jigMethodId.tuple();

        var typeText = tuple.declaringTypeId().packageAbbreviationText();
        var parameterText = tuple.parameterTypeIdList().stream()
                .map(TypeId::packageAbbreviationText)
                .collect(joining(", ", "(", ")"));
        return (typeText + '.' + tuple.name() + parameterText).replaceAll("[^a-zA-Z0-9]", "_");
    }
}
