package org.dddjava.jig.domain.model.documents.summaries;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;

import java.util.List;
import java.util.function.Predicate;

public record Summaries(
        JigTypes allJigTypes,
        MethodRelations allMethodRelations,
        Predicate<JigType> typeFilter,
        Predicate<JigMethod> methodFilter
) {
    Summaries(JigTypes allJigTypes, MethodRelations methodRelations) {
        this(allJigTypes, methodRelations, e -> true, e -> true);
    }

    public List<JigType> listJigTypes() {
        return allJigTypes().listMatches(typeFilter);
    }
}
